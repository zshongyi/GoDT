/**
 * 
 */
package io.github.zshongyi.godt.editor.lsp4e.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.swt.widgets.Display;

import io.github.zshongyi.godt.common.preference.GoEnvPreferenceConstants;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePlugin;
import io.github.zshongyi.godt.editor.preference.GoplsPreferenceConstants;
import io.github.zshongyi.godt.editor.preference.GoplsPreferencePage;
import io.github.zshongyi.godt.editor.preference.GoplsPreferencePlugin;

/**
 * @author zshongyi
 *
 */
public class GoplsStreamConnectionProvider extends ProcessStreamConnectionProvider {

	/**
	 * 
	 */
	public GoplsStreamConnectionProvider() {
		String goplsBinary = GoplsPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoplsPreferenceConstants.GOPLS_PATH);
		if (goplsBinary.isEmpty()) {
			goplsBinary = GoplsPreferencePage.getBinaryName();
		}

		List<String> commands = new ArrayList<>();
		commands.add(goplsBinary);
		commands.add("-vv");
		setCommands(commands);

	}

	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder processBuilder = super.createProcessBuilder();
		Map<String, String> env = processBuilder.environment();
		Boolean addGoPath = GoplsPreferencePlugin.getPlugin().getPreferenceStore()
				.getBoolean(GoplsPreferenceConstants.GODT_ADDGOPATH);
		String goBinaryPath = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		if (Boolean.TRUE.equals(!addGoPath || goBinaryPath == null) || goBinaryPath.isBlank()) {
			return processBuilder;
		}
		String goPath = new File(goBinaryPath).getParent();
		for (String path : env.get("PATH").split(File.pathSeparator)) {
			if (goPath.equals(path)) {
				return processBuilder;
			}
		}
		env.put("PATH", String.format("%s%s%s", goPath, File.pathSeparator, env.get("PATH")));
		return processBuilder;
	}

	@Override
	public void start() {
		Display.getDefault().syncExec(() -> {
			try {
				super.start();
			} catch (IOException e) {
				MessageDialog.openError(null, "Can't start gopls",
						"Please download it with command:\n" + "go install golang.org/x/tools/gopls@latest");
				e.printStackTrace();
			}
		});
	}

}
