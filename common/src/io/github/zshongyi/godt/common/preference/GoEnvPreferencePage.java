/**
 * 
 */
package io.github.zshongyi.godt.common.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.zshongyi.godt.common.auxiliary.Utils;
import io.github.zshongyi.godt.common.ui.ErrorOnValidateBinary;
import io.github.zshongyi.godt.common.ui.FFEditor;
import io.github.zshongyi.godt.common.ui.Messages;

/**
 * @author zshongyi
 *
 */
public class GoEnvPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static class GoFFEditor extends FFEditor {
		private GoFFEditor(String name, String labelText, boolean enforceAbsolute, Composite parent, FieldEditorPreferencePage fepp) {
			super(name, labelText, enforceAbsolute, parent, fepp, GoEnvPreferenceConstants.VERSION_PATTERN, 
					new ErrorOnValidateBinary("File error", "Not valid go executable."));
		}
	}

	/**
	 * 
	 */
	public GoEnvPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		this.setPreferenceStore(GoEnvPreferencePlugin.getPlugin().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {

		FileFieldEditor goPath = new GoFFEditor(GoEnvPreferenceConstants.GO_BINARY_PATH, 
				Messages.goPathLabel, true, getFieldEditorParent(), this);

		goPath.setFileExtensions(new String[] { Utils.getOsBinaryName("go") });
		this.addField(goPath);

		getGoBinaryPath();
	}

	public static String getGoBinaryPath() {
		String goBinaryPath = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		Utils.DEBUG("GOBIN 1:" + goBinaryPath);
		// if some path was set in preference page and passed validation return that
		if (Utils.checkBinary(goBinaryPath)) return goBinaryPath;

		goBinaryPath = Utils.getOsBinaryName("go");
		goBinaryPath = Utils.findBinary(goBinaryPath);
		Utils.DEBUG("GOBIN 2:" + goBinaryPath);
		if (goBinaryPath != null) {
			if (!Utils.validateBinary(goBinaryPath, GoEnvPreferenceConstants.VERSION_PATTERN, null)) {
				goBinaryPath = null;
			} else {
				GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
					.setDefault(GoEnvPreferenceConstants.GO_BINARY_PATH, goBinaryPath);
				GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
					.setValue(GoEnvPreferenceConstants.GO_BINARY_PATH, goBinaryPath);
			}
		}

		return goBinaryPath;
	}
}
