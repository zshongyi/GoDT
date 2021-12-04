package io.github.zshongyi.godt.common.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String openDeclarationLabel;
	public static String openReturnTypeLabel;
	public static String enableGodtDeclarationLabel;
	public static String goPathLabel;

	static {
		NLS.initializeMessages("io.github.zshongyi.godt.common.ui.messages", Messages.class); //$NON-NLS-1$
	}
}
