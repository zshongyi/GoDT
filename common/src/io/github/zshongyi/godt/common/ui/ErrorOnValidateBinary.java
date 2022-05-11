package io.github.zshongyi.godt.common.ui;

import org.eclipse.jface.dialogs.MessageDialog;

import io.github.zshongyi.godt.common.auxiliary.Utils;

/**
 * @author svujic
 *
 */
public class ErrorOnValidateBinary {
	String title;
	String msg;
	
	public ErrorOnValidateBinary(final String title, final String msg) {
		this.title = title;
		this.msg = msg;
	}

	public static void errMsgDialog(final String title, final String msg) {
		Utils.DEBUG("UI MessageDialog.openError: " + title + "\n" + msg);

		MessageDialog.openError(null, title, msg);
	}
	
	public void noMatch() {
		errMsgDialog(title, msg);
	}

	public void ioException(final String msg) {
		errMsgDialog(title, this.msg + "\n\n" + msg);
	}
}