/**
 * 
 */
package io.github.zshongyi.godt.editor.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.zshongyi.godt.editor.ui.Messages;
import io.github.zshongyi.godt.common.ui.ErrorOnValidateBinary;
import io.github.zshongyi.godt.common.ui.FFEditor;
import io.github.zshongyi.godt.common.auxiliary.Utils;

/**
 * @author zshongyi
 *
 */
public class GoplsPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static class GoplsFFEditor extends FFEditor {
		private GoplsFFEditor(String name, String labelText, boolean enforceAbsolute, Composite parent, FieldEditorPreferencePage fepp) {
			super(name, labelText, enforceAbsolute, parent, fepp, GoplsPreferenceConstants.VERSION_PATTERN, 
					new ErrorOnValidateBinary("File error", "Not valid gopls executable."));
		}
	}

	/**
	 * 
	 */
	public GoplsPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
		this.setPreferenceStore(GoplsPreferencePlugin.getPlugin().getPreferenceStore());
	}

	@Override
	public void init(IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void createFieldEditors() {

		FileFieldEditor goplsPath = new GoplsFFEditor(GoplsPreferenceConstants.GOPLS_PATH, Messages.goplsPathLabel, 
				true, getFieldEditorParent(), this);

		goplsPath.setFileExtensions(new String[] { Utils.getOsBinaryName("gopls") });
		this.addField(goplsPath);

		BooleanFieldEditor enableGodtDeclaration = new BooleanFieldEditor(GoplsPreferenceConstants.GODT_DECLARATION,
				Messages.enableGodtDeclarationLabel, getFieldEditorParent());
		this.addField(enableGodtDeclaration);

// TODO try to understand this code below. is it safe to remove or is it here to stay in some other form
/*
		BooleanFieldEditor addGoPath = new BooleanFieldEditor(GoplsPreferenceConstants.GODT_ADDGOPATH,
				Messages.addGoPathBeforeRunGopls, getFieldEditorParent()) {
			@Override
			protected void fireStateChanged(String property, boolean oldValue, boolean newValue) {
				super.fireStateChanged(property, oldValue, newValue);
				for (LanguageServer languageServer : LanguageServiceAccessor.getActiveLanguageServers(
						capabilities -> LSPEclipseUtils.hasCapability(capabilities.getWorkspaceSymbolProvider()))) {
					// FIXME: not work
					if (languageServer instanceof GoplsStreamConnectionProvider) {
						languageServer.shutdown();
					}
				}
			}
		};
		this.addField(addGoPath);
*/

		getGoplsBinaryPath();
	}

	public static String getGoplsBinaryPath() {
		String goplsBinaryPath = GoplsPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoplsPreferenceConstants.GOPLS_PATH);
		Utils.DEBUG("GOPLS 1:" + goplsBinaryPath);
		// if some path was set in preference page and passed validation return that
		if (Utils.checkBinary(goplsBinaryPath)) return goplsBinaryPath;

		goplsBinaryPath = Utils.getOsBinaryName("gopls");
		goplsBinaryPath = Utils.findBinary(goplsBinaryPath);
		Utils.DEBUG("GOPLS 2:" + goplsBinaryPath);
		if (goplsBinaryPath != null) {
			if (!Utils.validateBinary(goplsBinaryPath, GoplsPreferenceConstants.VERSION_PATTERN, null)) {
				goplsBinaryPath = null;
			} else {
				GoplsPreferencePlugin.getPlugin().getPreferenceStore()
					.setDefault(GoplsPreferenceConstants.GOPLS_PATH, goplsBinaryPath);
				GoplsPreferencePlugin.getPlugin().getPreferenceStore()
					.setValue(GoplsPreferenceConstants.GOPLS_PATH, goplsBinaryPath);
			}
		}

		return goplsBinaryPath;
	}
}
