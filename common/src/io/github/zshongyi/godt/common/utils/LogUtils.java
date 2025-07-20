package io.github.zshongyi.godt.common.utils;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class LogUtils {

	private static ILog log;
	private static final String BUNDLE_SYMBOLIC_NAME = "io.github.zshongyi.godt.common";

	private static ILog getLog() {
		if (log == null) {
			synchronized (LogUtils.class) {
				Bundle bundle = Platform.getBundle(BUNDLE_SYMBOLIC_NAME);
				log = Platform.getLog(bundle);
			}
		}
		return log;
	}

	public static void logError(String message, Throwable exception) {
		Status status = new Status(IStatus.ERROR, BUNDLE_SYMBOLIC_NAME, message, exception);
		getLog().log(status);
	}
}
