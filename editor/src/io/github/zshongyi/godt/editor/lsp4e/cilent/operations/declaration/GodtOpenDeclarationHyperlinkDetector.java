package io.github.zshongyi.godt.editor.lsp4e.cilent.operations.declaration;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4e.LanguageServers;
import org.eclipse.lsp4e.internal.Pair;
import org.eclipse.lsp4e.operations.declaration.LSBasedHyperlink;
import org.eclipse.lsp4e.ui.Messages;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

//import io.github.zshongyi.godt.editor.ui.Messages;

/**
 * Modify according to source file
 * org.eclipse.lsp4e.operations.declaration.OpenDeclarationHyperlinkDetector
 * 
 * @author zshongyi
 *
 */
public class GodtOpenDeclarationHyperlinkDetector extends AbstractHyperlinkDetector {

	@Override
	public IHyperlink @Nullable [] detectHyperlinks(ITextViewer textViewer, IRegion region,
			boolean canShowMultipleHyperlinks) {
		final IDocument document = textViewer.getDocument();
		if (document == null) {
			return null;
		}
		TextDocumentPositionParams params;
		try {
			params = LSPEclipseUtils.toTextDocumentPosistionParams(region.getOffset(), document);
		} catch (BadLocationException e) {
			LanguageServerPlugin.logError(e);
			return null;
		}
		final var allLinks = new LinkedHashMap<Either<Location, LocationLink>, LSBasedHyperlink>();
		try {
			var definitions = LanguageServers.forDocument(document)
					.withCapability(ServerCapabilities::getDefinitionProvider)
					.collectAll(ls -> ls.getTextDocumentService().definition(LSPEclipseUtils.toDefinitionParams(params))
							.thenApply(l -> Pair.of(Messages.definitionHyperlinkLabel, l)));
			var declarations = LanguageServers.forDocument(document)
					.withCapability(ServerCapabilities::getDeclarationProvider).collectAll(
							ls -> ls.getTextDocumentService().declaration(LSPEclipseUtils.toDeclarationParams(params))
									.thenApply(l -> Pair.of(Messages.declarationHyperlinkLabel, l)));
			var typeDefinitions = LanguageServers.forDocument(document)
					.withCapability(ServerCapabilities::getTypeDefinitionProvider)
					.collectAll(ls -> ls.getTextDocumentService()
							.typeDefinition(LSPEclipseUtils.toTypeDefinitionParams(params))
							.thenApply(l -> Pair.of(Messages.typeDefinitionHyperlinkLabel, l)));
			var implementations = LanguageServers.forDocument(document)
					.withCapability(ServerCapabilities::getImplementationProvider)
					.collectAll(ls -> ls.getTextDocumentService()
							.implementation(LSPEclipseUtils.toImplementationParams(params))
							.thenApply(l -> Pair.of(Messages.implementationHyperlinkLabel, l)));
			LanguageServers.addAll(definitions, typeDefinitions)
					// .addAll(LanguageServers.addAll(LanguageServers.addAll(definitions,
					// declarations), typeDefinitions), implementations)
					.get(1500, TimeUnit.MILLISECONDS).stream()
					.flatMap(
							locations -> toHyperlinks(document, region, locations.first(), locations.second()).stream())
					.forEach(link -> allLinks.putIfAbsent(link.getLocation(), link));
		} catch (ExecutionException e) {
			LanguageServerPlugin.logError(e);
		} catch (InterruptedException e) {
			LanguageServerPlugin.logError(e);
			Thread.currentThread().interrupt();
		} catch (TimeoutException e) {
			LanguageServerPlugin.logWarning("Could not detect hyperlinks due to timeout after 800 milliseconds", e); //$NON-NLS-1$
		}
		if (allLinks.isEmpty()) {
			return null;
		}
		return allLinks.values().toArray(IHyperlink[]::new);

	}

	/**
	 * Returns a list of {@link LSBasedHyperlink} using the given LSP locations
	 *
	 * @param document     the document
	 * @param linkRegion   the region
	 * @param locationType the location type
	 * @param locations    the LSP locations
	 */
	private static Collection<LSBasedHyperlink> toHyperlinks(IDocument document, IRegion region, String locationType,
			@NonNullByDefault({}) Either<List<? extends Location>, List<? extends LocationLink>> locations) {
		if (locations == null) {
			return Collections.emptyList();
		}
		return locations.map(//
				l -> l.stream().filter(Objects::nonNull)
						.map(location -> new LSBasedHyperlink(location, findWord(document, region), locationType))
						.toList(),
				r -> r.stream().filter(Objects::nonNull).map(locationLink -> new LSBasedHyperlink(locationLink,
						getSelectedRegion(document, region, locationLink), locationType)).toList());
	}

	/**
	 * Returns the selection region, or if that fails , fallback to
	 * {@link #findWord(IDocument, IRegion)}
	 */
	private static IRegion getSelectedRegion(IDocument document, IRegion region, LocationLink locationLink) {
		Range originSelectionRange = locationLink.getOriginSelectionRange();
		if (originSelectionRange != null) {
			try {
				int offset = LSPEclipseUtils.toOffset(originSelectionRange.getStart(), document);
				int endOffset = LSPEclipseUtils.toOffset(originSelectionRange.getEnd(), document);
				return new Region(offset, endOffset - offset);
			} catch (BadLocationException e) {
				LanguageServerPlugin.logError(e.getMessage(), e);
			}
		}
		return findWord(document, region);
	}

	/**
	 * Fallback for missing range value (which can be used to highlight hyperlink)
	 * in LSP 'definition' response.
	 */
	private static IRegion findWord(IDocument document, IRegion region) {
		int start = -2;
		int end = -1;
		int offset = region.getOffset();

		try {

			int pos = offset;
			char c;

			while (pos >= 0 && pos < document.getLength()) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c)) {
					break;
				}
				--pos;
			}

			start = pos;

			pos = offset;
			int length = document.getLength();

			while (pos < length) {
				c = document.getChar(pos);
				if (!Character.isUnicodeIdentifierPart(c))
					break;
				++pos;
			}

			end = pos;

		} catch (BadLocationException x) {
			LanguageServerPlugin.logWarning(x.getMessage(), x);
		}

		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}

		return region;
	}

}
