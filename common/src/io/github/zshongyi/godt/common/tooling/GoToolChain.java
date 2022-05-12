/**
 * 
 */
package io.github.zshongyi.godt.common.tooling;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import io.github.zshongyi.godt.common.auxiliary.Utils;
import io.github.zshongyi.godt.common.preference.GoEnvPreferenceConstants;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePlugin;
import io.github.zshongyi.godt.common.ui.consoles.GoToolChainConsole;

/**
 * @author zshongyi
 *
 */
public class GoToolChain {

	private GoToolChain() {

	}

	public static boolean exec(IProject project, String[] commands) {
//		return exec(project, commands, false);
		return exec(project, commands, true);
	}

	public static boolean exec(IProject project, List<String> commands) {
		return exec(project, commands, false);
	}

	public static boolean exec(IProject project, String[] commands, boolean clearConsole) {
		return exec(project, new ArrayList<>(Arrays.asList(commands)), clearConsole);
	}

	public static synchronized boolean exec(IProject project, List<String> commands, boolean clearConsole) {
		GoToolChainConsole.bindProject(project);

		if (clearConsole) {
			GoToolChainConsole.clearConsole();
		}

		final String goBinary = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
			.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		if (!Utils.checkBinary(goBinary)) {
			GoToolChainConsole.stderr(String.format("[invalid] %s %s", goBinary, StringUtils.join(commands, " ")));
			Display.getDefault().syncExec(() -> {
				final String title = "Error running go!";
				final String msg = goBinary + " is no longer valid executable!\n\n"
						+ "Please go to:\n"
						+ "Window->Preferences->GoDT->Go Env\n"
						+ "and point to valid executable";
				MessageDialog.openError(null, title, msg);
			});
			return false;
		}

		commands.add(0, goBinary);
		GoToolChainConsole.stdout(String.format("[exec] %s", StringUtils.join(commands, " ")));

		ProcessBuilder builder = new ProcessBuilder(commands);

		builder.directory(project.getLocation().toFile());
		try {
			Process process = builder.start();
			try (InputStream processOut = process.getInputStream()) {
				try (InputStream processErr = process.getErrorStream()) {
					Thread stdoutThread = new Thread(() -> {
						GoToolChainConsole.stdout(processOut);
					});
					Thread stderrThread = new Thread(() -> {
						GoToolChainConsole.stderr(processErr);
					});
					stdoutThread.start();
					stderrThread.start();
					process.waitFor();
					stdoutThread.join();
					stderrThread.join();
				}
			}
		} catch (IOException | InterruptedException e) {
			GoToolChainConsole.stderr(e.getMessage());
			e.printStackTrace();
			return false;
		}
		GoToolChainConsole.stdout(String.format("[terminate] %s", StringUtils.join(commands, " ")));
		return true;
	}

}
