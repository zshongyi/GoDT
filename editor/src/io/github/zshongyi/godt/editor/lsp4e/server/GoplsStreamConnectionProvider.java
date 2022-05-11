/**
 * 
 */
package io.github.zshongyi.godt.editor.lsp4e.server;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.swt.widgets.Display;

import io.github.zshongyi.godt.common.auxiliary.Utils;
import io.github.zshongyi.godt.common.preference.GoEnvPreferenceConstants;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePage;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePlugin;
import io.github.zshongyi.godt.editor.preference.GoplsPreferencePage;

/**
 * @author zshongyi
 *
 */
public class GoplsStreamConnectionProvider extends ProcessStreamConnectionProvider {

	String title;
	String msg;

	static boolean installGoplsV1(final String goBinary) {
		return Utils.exec(goBinary, 
			null, 
			new ArrayList<>(Arrays.asList(new String[] { "install", "golang.org/x/tools/gopls@latest" })));
	}
	// if installGoplsV1() fail try this deprecated way of installation 
	// GO111MODULE=on go get golang.org/x/tools/gopls@latest
	static boolean installGoplsV2(final String goBinary) {
		return Utils.exec(goBinary, 
			new ArrayList<>(Arrays.asList(new String[] { "GO111MODULE=on" })),
			new ArrayList<>(Arrays.asList(new String[] { "get", "golang.org/x/tools/gopls@latest" }))
			);
	}
	
	public GoplsStreamConnectionProvider() {
		String goplsBinary = null;

		title = GoEnvPreferenceConstants.TTL_CANTFINDGO;
		msg = GoEnvPreferenceConstants.MSG_PLSINSTALLGO;
		final String goBinary = GoEnvPreferencePage.getGoBinaryPath();
		if (goBinary == null) {
			return;
		}
		
		goplsBinary = GoplsPreferencePage.getGoplsBinaryPath();
		if (goplsBinary == null) {
			title = "Error during gopls installation";
			msg = "Assistance with go code editing can't be provided.\n\n" + Utils.formated_exec_err;
			if (installGoplsV1(goBinary) || installGoplsV2(goBinary)) {
				title = "Error after successfull install of gopls";
				msg = "Can't find gopls!\n"
						+ "it should be in $GOPATH/bin, but isn't.\n\n"
						+ "Please point to valid gopls in:\n"
						+ "Window->Preferences->GoDT->Gopls\n"
						+ "After that >>restart<< Eclipse.";
				goplsBinary = GoplsPreferencePage.getGoplsBinaryPath();
			}
		}

		if (goplsBinary != null) {
			List<String> commands = new ArrayList<>();
			commands.add(goplsBinary);
			commands.add("-vv");
			setCommands(commands);
		}
	}

	// if go isn't in PATH add it at head
	@Override
	protected ProcessBuilder createProcessBuilder() {
		ProcessBuilder processBuilder = super.createProcessBuilder();
		Map<String, String> env = processBuilder.environment();
		String goBinaryPath = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		String goPath = new File(goBinaryPath).getParent();
		boolean existsInPath = Stream.of(System.getenv("PATH").split(File.pathSeparator))
				.map(Paths::get).anyMatch(path -> path.toString().equals(goPath));
		if (!existsInPath) {
			env.put("PATH", String.format("%s%s%s", goPath, File.pathSeparator, env.get("PATH")));
		}
		return processBuilder;
	}

	@Override
	public void start() {
		Display.getDefault().syncExec(() -> {
			try {
				if (!Utils.startLS_failed) super.start();
			} catch (IOException e) {
				Utils.startLS_failed = true;
				MessageDialog.openError(null, title, msg);
				e.printStackTrace();
			}
		});
	}

	@Override
	public String toString() {
		return "GoDT Language Server: " + super.toString();
	}
}
