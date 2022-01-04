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
		return exec(project, commands, false);
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

		String goBinary = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);

		commands.add(0, goBinary);
		ProcessBuilder builder = new ProcessBuilder(commands);
		GoToolChainConsole.stdout(String.format(">>>>> %s <<<<<", StringUtils.join(commands, " ")));

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
		GoToolChainConsole.stdout(String.format("[terminate] >>>>> %s <<<<<", StringUtils.join(commands, " ")));
		return true;
	}

}
