package io.github.zshongyi.godt.editor.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String openDeclarationLabel;
	public static String openReturnTypeLabel;
	public static String enableGodtDeclarationLabel;
	public static String goplsPathLabel;
	public static String addGoPathBeforeRunGopls;

	static {
		NLS.initializeMessages("io.github.zshongyi.godt.editor.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
