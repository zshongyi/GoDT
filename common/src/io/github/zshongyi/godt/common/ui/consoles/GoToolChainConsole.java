/**
 * 
 */
package io.github.zshongyi.godt.common.ui.consoles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

/**
 * @author zshongyi
 *
 */
public class GoToolChainConsole {

	private GoToolChainConsole() {

	}

	private static final String GO_TOOL_CHAIN_CONSOLE = "Go Tool Chain";
	protected static final String ATTRIBUTE_PROJECT = "project";

	private static MessageConsoleStream consoleStdout;
	private static MessageConsoleStream consoleStderr;

	private static MessageConsole findConsole() {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager consoleManager = plugin.getConsoleManager();
		IConsole[] existingConsoles = consoleManager.getConsoles();
		for (int i = 0; i < existingConsoles.length; i++)
			if (GO_TOOL_CHAIN_CONSOLE.equals(existingConsoles[i].getName()))
				return (MessageConsole) existingConsoles[i];
		// no console found, so create a new one
		MessageConsole console = new MessageConsole(GO_TOOL_CHAIN_CONSOLE, null);
		consoleManager.addConsoles(new IConsole[] { console });
		console.setConsoleAutoScrollLock(true);
		console.addPatternMatchListener(new GoToolChainConsolePatternMatchListener());
		getConsoleStdout(); // 有效避免在线程中执行导致的线程阻塞
		getConsoleStdErr(); // 同上
		return console;
	}

	private static MessageConsoleStream getConsoleStdout() {
		if (consoleStdout == null || consoleStdout.isClosed()) {
			Display.getDefault().syncExec(() -> {
				consoleStdout = findConsole().newMessageStream();
				consoleStdout.setColor(new Color(0, 0, 0));
			});
		}
		return consoleStdout;
	}

	private static MessageConsoleStream getConsoleStdErr() {
		if (consoleStderr == null || consoleStderr.isClosed()) {
			Display.getDefault().syncExec(() -> {
				consoleStderr = findConsole().newMessageStream();
				consoleStderr.setColor(new Color(255, 0, 0));
				consoleStderr.setFontStyle(SWT.BOLD);
			});
		}
		return consoleStderr;
	}

	private static void output(MessageConsoleStream messageConsoleStream, String content, boolean endline) {
		if (endline) {
			messageConsoleStream.println(content);
		} else {
			messageConsoleStream.print(content);
		}
	}

	private static void output(MessageConsoleStream messageConsoleStream, InputStream inputStream) {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String str = null;
		while (messageConsoleStream != null) {
			try {
				str = bufferedReader.readLine();
				if (str != null)
					messageConsoleStream.println(str);
				else
					break;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void clearConsole() {
		findConsole().clearConsole();
	}

	public static void stdout(String content) {
		stdout(content, true);
	}

	public static void stdout(String content, boolean endline) {
		output(getConsoleStdout(), content, endline);
	}

	public static void stdout(InputStream inputStream) {
		output(getConsoleStdout(), inputStream);
	}

	public static void stderr(String content) {
		stderr(content, true);
	}

	public static void stderr(String content, boolean endline) {
		output(getConsoleStdErr(), content, endline);
	}

	public static void stderr(InputStream inputStream) {
		output(getConsoleStdErr(), inputStream);
	}

	public static void bindProject(IProject project) {
		findConsole().setAttribute(ATTRIBUTE_PROJECT, project);
	}

}
