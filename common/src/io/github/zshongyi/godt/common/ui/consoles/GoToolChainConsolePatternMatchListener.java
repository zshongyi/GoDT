/**
 * 
 */
package io.github.zshongyi.godt.common.ui.consoles;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListener;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author zshongyi
 *
 */
public class GoToolChainConsolePatternMatchListener implements IPatternMatchListener {

	private static final String MATCH_REGEX_PATTERN = "\\..*\\.go:\\d+:\\d+: .*: .*";
	private TextConsole console;

	@Override
	public void connect(TextConsole console) {
		this.console = console;
	}

	@Override
	public void disconnect() {
		this.console = null;
	}

	@Override
	public void matchFound(PatternMatchEvent event) {
		try {
			int separatorIndex;
			int length;
			String fileReferenceText = this.console.getDocument().get(event.getOffset(), event.getLength());
			length = separatorIndex = fileReferenceText.indexOf(":");

			String absoluteFilePath = fileReferenceText.substring(0, separatorIndex);

			fileReferenceText = fileReferenceText.substring(separatorIndex + 1);
			separatorIndex = fileReferenceText.indexOf(":");
			length += separatorIndex + 1;
			int lineNumber = Integer.parseInt(fileReferenceText.substring(0, separatorIndex));

			IProject project = (IProject) console.getAttribute(GoToolChainConsole.ATTRIBUTE_PROJECT);

			File targetFile;
			if (project == null) {
				targetFile = new File(absoluteFilePath);
			} else {
				targetFile = new File(project.getLocation().toFile(), absoluteFilePath);
			}

			IHyperlink hyperlink = makeHyperlink(targetFile, lineNumber);
			this.console.addHyperlink(hyperlink, event.getOffset(), length);
		} catch (NumberFormatException | BadLocationException exception) {
			exception.printStackTrace();
		}

	}

	private static IHyperlink makeHyperlink(File targetFile, int lineNumber) {
		return new IHyperlink() {

			@Override
			public void linkEntered() {

			}

			@Override
			public void linkExited() {

			}

			@Override
			public void linkActivated() {
				try {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					IEditorPart editorPart = IDE.openEditorOnFileStore(page, EFS.getStore(targetFile.toURI()));
					goToLine(editorPart, lineNumber);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
	}

	private static void goToLine(IEditorPart editorPart, int lineNumber) {
		if (editorPart instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument(textEditor.getEditorInput());

			if (document != null) {
				try {
					IRegion region = document.getLineInformation(lineNumber - 1);
					textEditor.selectAndReveal(region.getOffset(), region.getLength());
				} catch (BadLocationException exception) {
					exception.printStackTrace();
				}
			}
		}
	}

	@Override
	public String getPattern() {
		return MATCH_REGEX_PATTERN;
	}

	@Override
	public int getCompilerFlags() {
		return 0;
	}

	@Override
	public String getLineQualifier() {
		return null;
	}

}
