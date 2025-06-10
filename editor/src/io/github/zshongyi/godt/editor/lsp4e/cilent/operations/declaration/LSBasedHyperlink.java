/*******************************************************************************
 * Copyright (c) 2016, 2020 Red Hat Inc. and others.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Mickael Istria (Red Hat Inc.) - initial implementation
 *  Michał Niewrzał (Rogue Wave Software Inc.) - hyperlink range detection
 *  Lucas Bullen (Red Hat Inc.) - [Bug 517428] Requests sent before initialization
 *  Martin Lippert (Pivotal Inc.) - [Bug 561270] labels include more details now
 *******************************************************************************/
package io.github.zshongyi.godt.editor.lsp4e.cilent.operations.declaration;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.LanguageServerPlugin;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.LocationLink;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.intro.config.IIntroURL;
import org.eclipse.ui.intro.config.IntroURLFactory;

import io.github.zshongyi.godt.editor.ui.Messages;

/**
 * Modify according to source file
 * org.eclipse.lsp4e.operations.declaration.LSBasedHyperlink
 * 
 * @author zshongyi
 *
 */
public class LSBasedHyperlink implements IHyperlink {

	private final String labelPrefix;
	private final Either<Location, LocationLink> location;
	private final IRegion highlightRegion;

	public LSBasedHyperlink(String labelPrefix, Either<Location, LocationLink> location, IRegion highlightRegion) {
		this.labelPrefix = labelPrefix;
		this.location = location;
		this.highlightRegion = highlightRegion;
	}

	public LSBasedHyperlink(Either<Location, LocationLink> location, IRegion linkRegion) {
		this(Messages.openDeclarationLabel, location, linkRegion);
	}

	public LSBasedHyperlink(String labelPrefix, Location location, IRegion linkRegion) {
		this(labelPrefix, Either.forLeft(location), linkRegion);
	}

	public LSBasedHyperlink(Location location, IRegion linkRegion) {
		this(Messages.openDeclarationLabel, location, linkRegion);
	}

	public LSBasedHyperlink(String labelPrefix, LocationLink locationLink, IRegion linkRegion) {
		this(labelPrefix, Either.forRight(locationLink), linkRegion);
	}

	public LSBasedHyperlink(LocationLink locationLink, IRegion linkRegion) {
		this(Messages.openDeclarationLabel, locationLink, linkRegion);
	}

	@Override
	public IRegion getHyperlinkRegion() {
		return this.highlightRegion;
	}

	@Override
	public String getTypeLabel() {
		return getLabel();
	}

	@Override
	public String getHyperlinkText() {
		return getLabel();
	}

	/**
	 *
	 * @return
	 * @noreference test only
	 */
	public Either<Location, LocationLink> getLocation() {
		return location;
	}

	@Override
	public void open() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activatePage = window == null ? null : window.getActivePage();

		if (location.isLeft()) {
			LSPEclipseUtils.openInEditor(location.getLeft(), activatePage);
		} else {
			LSPEclipseUtils.openInEditor(location.getRight(), activatePage);
		}
	}

	private String getLabel() {
		if (this.location != null) {
			String uri = this.location.isLeft() ? this.location.getLeft().getUri()
					: this.location.getRight().getTargetUri();
			if (uri != null) {
				if (uri.startsWith(LSPEclipseUtils.FILE_URI) && uri.length() > LSPEclipseUtils.FILE_URI.length()) {
					return getFileBasedLabel(uri);
				} else if (uri.startsWith(LSPEclipseUtils.INTRO_URL)) {
					return getIntroUrlBasedLabel(uri);
				} else if (uri.startsWith("http")) {
					return getHttpBasedLabel(uri);
				}
			}
		}

		return labelPrefix;
	}

	private String getIntroUrlBasedLabel(String uri) {
		try {
			IIntroURL introUrl = IntroURLFactory.createIntroURL(uri);
			if (introUrl != null) {
				String label = introUrl.getParameter("label"); //$NON-NLS-1$
				if (label != null) {
					return labelPrefix + " - " + label; //$NON-NLS-1$
				}
			}
		} catch (Exception e) {
			LanguageServerPlugin.logError(e.getMessage(), e);
		}

		return labelPrefix;
	}

	private String getHttpBasedLabel(String uri) {
		return labelPrefix + " - " + uri; //$NON-NLS-1$
	}

	private String getFileBasedLabel(String uri) {
		return labelPrefix + " - " + uri.substring(LSPEclipseUtils.FILE_URI.length()); //$NON-NLS-1$
	}

}
