package io.github.zshongyi.godt.common.auxiliary;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import io.github.zshongyi.godt.common.ui.ErrorOnValidateBinary;

/**
 * @author svujic
 *
 */
public class Utils {
	private static final boolean DEBUG = System.getenv("GODTDEBUG") == null ? false : true;

	// holds error of last command executed via exec()
	public static String formated_exec_err;
	public static String exec_err;

	// holds first line of STDOUT from last command executed via validateBinary()
	// example
	// command: go version
	// STDOUT: go version go1.17.8 linux/amd64
	public static String version;
	public static boolean startLS_failed = false;
	// TODO in exec implement support for avoiding repetitive calls when go binary isn't valid  
	public static boolean validGo = true; 

	private Utils() {}
	
	public static void DEBUG(final String str) {
		if (DEBUG) {
			System.out.println(str);
		}
	}

	public static boolean checkBinary(final String binaryPath) {
		if (binaryPath != null && !binaryPath.isBlank()) {
			File f = new File(binaryPath);
			if (f.isFile() && f.canExecute()) {
				return true;
			}
		}
		return false;
	}

	// this validates output of 'some_command version' against pattern. Relevant at least for go and gopls
	// ErrorOnValidateBinary supplies methods for messaging alerts to a user
	public static boolean validateBinary(final String binaryPath, final String pattern, ErrorOnValidateBinary eovb) {
		if (!checkBinary(binaryPath)) return false; 

		version = "";
		boolean res = false;
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(binaryPath, "version");
		try {
			Process process = processBuilder.start();
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			final String line = bufferedReader.readLine();
			final Matcher matcher = Pattern.compile(pattern).
					matcher(line);
			if (matcher.find()) {
				res = true;
				version = line;
			} else {
				if (eovb != null) {
					eovb.noMatch();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			if (eovb != null) {
				eovb.ioException(e.getMessage());
			}
		}
		
		return res;
	}

	// closeQuietly is un-deprecated as of 08/Oct/2020; https://issues.apache.org/jira/browse/IO-504
	@SuppressWarnings("deprecation")
	public static String findBinary(String binaryName) {
		String res = null;
		
        // try to find binaryName somewhere in env. PATH
		String[] command = new String[] { "/bin/bash", "-c", "which " + binaryName }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (Utils.osIsWindows()) {
            command = new String[] { "cmd", "/c", "where " + binaryName }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        BufferedReader reader = null;
        try {
            Process p = Runtime.getRuntime().exec(command);
            reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            res = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(reader);
        }

        return res;
	}

	public static boolean osIsWindows() {
		return System.getProperty("os.name").toLowerCase().startsWith("win");
	}

	public static String getOsBinaryName(final String binaryname) {
		return binaryname + ((osIsWindows()) ? ".exe" : "");
	}

	public static boolean exec(final String binaryName, List<String> envs, List<String> commands) {
		exec_err = null;
		formated_exec_err = "";
		ProcessBuilder builder = new ProcessBuilder(commands);

		if (envs != null) {
			Map<String, String> env = builder.environment();
			envs.forEach((e) -> {
	                    final String[] keyval = e.split("=", -1);
	                    env.put(keyval[0], keyval[1]);
	                }
	        );
		}

		commands.add(0, binaryName);
		DEBUG(String.format("exec: %s", StringUtils.join(commands, " ")));

		boolean res;
		BufferedReader reader;
		try {
			Process process = builder.start();
			try (InputStream processOut = process.getInputStream()) {
				try (InputStreamReader processErr = new InputStreamReader(process.getErrorStream())) {
					reader = new BufferedReader(processErr);
					exec_err = reader.readLine();
					process.waitFor();
					res = process.exitValue() == 0;
				}
			}
		} catch (IOException | InterruptedException e) {
			res = false;
			exec_err = e.getMessage();
			DEBUG("exec ERROR: " + exec_err);
			e.printStackTrace();
		}
		DEBUG(String.format("terminate: %s", StringUtils.join(commands, " ")));
		if (exec_err != null && !exec_err.isBlank()) {
			formated_exec_err = "Executing command:\n" + StringUtils.join(commands, " ") + "\n\nOutput:\n" + exec_err;
		}
		return res;
	}
}
