/**
 * 
 */
package io.github.zshongyi.godt.common.tooling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import io.github.zshongyi.godt.common.preference.GoEnvPreferenceConstants;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePlugin;
import io.github.zshongyi.godt.common.ui.consoles.GoToolChainConsole;
import io.github.zshongyi.godt.common.utils.LogUtils;

/**
 * @author zshongyi
 *
 */
public class GoToolChain {

	private static final ReentrantLock runningLock = new ReentrantLock(); // 执行状态锁
	private static final ReentrantLock preemptiveLock = new ReentrantLock(); // 抢占锁
	private static final BlockingQueue<GoCommandJob> jobQueue = new ArrayBlockingQueue<>(10);

	private static final ExecutorService outputRedirectExecutor = Executors.newCachedThreadPool();

	private static class GoCommandJob extends Job {
		private final IProject project;
		private final List<String> commands;

		private Process process;
		private boolean resultStatus = false;

		public GoCommandJob(IProject project, List<String> commands) {
			super("Go Command: " + String.join(" ", commands));
			this.project = project;
			this.commands = commands;
			this.setUser(true);
		}

		@Override
		protected void canceling() {
			if (process != null && process.isAlive()) {
				process.destroyForcibly();
			}
			super.canceling();
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			GoToolChainConsole.bindProject(project);

			commands.add(0, getGoBinary());
			ProcessBuilder builder = new ProcessBuilder(commands);
			builder.directory(project.getLocation().toFile());
			String commandString = StringUtils.join(commands, " ");

			monitor.beginTask("Waitting For: " + commandString, IProgressMonitor.UNKNOWN);
			runningLock.lock();
			try {

				GoToolChainConsole.stdout(String.format("[started] >>>>> %s <<<<<", commandString));
				monitor.beginTask(commandString, 10);

				process = builder.start();

				outputRedirectExecutor.submit(() -> {
					try (InputStreamReader reader = new InputStreamReader(process.getInputStream());
							BufferedReader bufferedReader = new BufferedReader(reader)) {
						String line;
						while ((line = bufferedReader.readLine()) != null && !monitor.isCanceled()) {
							GoToolChainConsole.stdout(line);
						}
					} catch (IOException e) {
						LogUtils.logError("Error reading process stdout output: ", e);
					}
				});

				outputRedirectExecutor.submit(() -> {
					try (InputStreamReader reader = new InputStreamReader(process.getErrorStream());
							BufferedReader bufferedReader = new BufferedReader(reader)) {
						String line;
						while ((line = bufferedReader.readLine()) != null && !monitor.isCanceled()) {
							GoToolChainConsole.stderr(line);
						}
					} catch (IOException e) {
						LogUtils.logError("Error reading process stderr output: ", e);
					}
				});

				// 等待进程结束或被取消
				while (!monitor.isCanceled() && process.isAlive()) {
					if (!process.waitFor(500, TimeUnit.MILLISECONDS)) {
						continue;
					}
					break;
				}

				if (monitor.isCanceled()) {
					GoToolChainConsole
							.stdout(String.format("[cancelled] >>>>> %s <<<<<", StringUtils.join(commands, " ")));
					return Status.CANCEL_STATUS;
				}

				GoToolChainConsole.stdout(String.format("[successed] >>>>> %s <<<<<", StringUtils.join(commands, " ")));
				resultStatus = true;
				return Status.OK_STATUS;

			} catch (IOException | InterruptedException e) {
				GoToolChainConsole.stderr(e.getMessage());
				LogUtils.logError("Execute Go Command" + commandString + "Failed:", e);
				return new Status(IStatus.ERROR, "GoToolChain", 2, e.getMessage(), null);
			} finally {
				runningLock.unlock();
				monitor.done();
			}
		}

		public boolean getResultStatus() {
			return resultStatus;
		}
	}

	public static String getGoBinary() {
		String goBinary = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		if (goBinary.isBlank()) {
			goBinary = System.getProperty("os.name").toLowerCase().startsWith("win") ? "go.exe" : "go";
		}
		return goBinary;
	}

	public static void asyncExecute(IProject project, String[] commands) {
		asyncExecute(project, new ArrayList<>(Arrays.asList(commands)));
	}

	public static void asyncExecute(IProject project, List<String> commands) {
		GoCommandJob job = new GoCommandJob(project, commands);

		if (!jobQueue.offer(job)) {
			Shell activeShell = Display.getDefault().getActiveShell();
			MessageDialog.openError(activeShell, "Error", "Exec Go Tool Job Failed:" + job.getName());
			return;
		}
		if (!runningLock.isLocked()) {
			processNextJob();
		}
	}

	public static boolean syncExecute(IProject project, String[] commands) {
		return syncExecute(project, new ArrayList<>(Arrays.asList(commands)));
	}

	public static boolean syncExecute(IProject project, List<String> commands) {
		GoCommandJob job = new GoCommandJob(project, commands);
		if (preemptiveLock.tryLock()) {

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(Display.getDefault().getActiveShell());
			try {
				dialog.run(true, true, monitor -> {
					job.schedule();
					job.join();
				});
			} catch (InvocationTargetException | InterruptedException e) {
				e.printStackTrace();
			}
			preemptiveLock.unlock();
		} else {
			Shell activeShell = Display.getDefault().getActiveShell();
			MessageDialog.openError(activeShell, "Error", "Go Tool Job Failed: Too Busy");
		}

		new Thread(GoToolChain::processNextJob).start();
		return job.getResultStatus();
	}

	private static void processNextJob() {
		try {
			while (!jobQueue.isEmpty() && !preemptiveLock.isLocked()) {
				GoCommandJob job = jobQueue.poll();
				if (job != null) {
					job.schedule();
					try {
						job.join();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt(); // 恢复中断状态
					}
				}
				if (jobQueue.isEmpty()) {
					break;
				}
			}
		} finally {

		}
	}
}
