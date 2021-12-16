/**
 * 
 */
package io.github.zshongyi.godt.common.preference;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import io.github.zshongyi.godt.common.ui.Messages;

/**
 * @author zshongyi
 *
 */
public class GoEnvPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

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

		FileFieldEditor goPath = new FileFieldEditor(GoEnvPreferenceConstants.GO_BINARY_PATH, Messages.goPathLabel,
				true, getFieldEditorParent()) {

			@Override
			protected void fireValueChanged(String property, Object oldValue, Object newValue) {
				super.fireValueChanged(property, oldValue, newValue);
				String filePath = newValue.toString();
				if (!filePath.equals("")) {
					ProcessBuilder processBuilder = new ProcessBuilder();
					processBuilder.command(filePath, "version");
					try {
						Process process = processBuilder.start();
						BufferedReader bufferedReader = new BufferedReader(
								new InputStreamReader(process.getInputStream()));
						String firstLine = bufferedReader.readLine();
						Pattern pattern = Pattern.compile("^go version go(\\d+\\.){2}\\d+ .*");
						Matcher matcher = pattern.matcher(firstLine);
						if (matcher.find()) {
							setValid(true);
						} else {
							MessageDialog.openError(null, "Illegal File", "Not an available go binary file.");
							setValid(false);
						}
					} catch (IOException e) {
						MessageDialog.openError(null, "Illegal File", e.getMessage());
						e.printStackTrace();
						setValid(false);
					}
				} else {
					setValid(true);
				}
				refreshValidState();
			}
		};

		goPath.setFileExtensions(new String[] { getBinaryName() });
		this.addField(goPath);

	}

	public static String getBinaryName() {
		String goplsBinary = "go";
		String os = System.getProperty("os.name");
		if (os.toLowerCase().startsWith("win")) {
			goplsBinary = "go.exe";
		}
		return goplsBinary;
	}

}
