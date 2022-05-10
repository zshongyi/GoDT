package io.github.zshongyi.godt.common.auxiliary;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import io.github.zshongyi.godt.common.preference.GoEnvPreferencePage;

public class Activator extends AbstractUIPlugin {

	private static Activator plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		GoEnvPreferencePage.getGoBinaryPath();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getInstance() {
		return plugin;
	}

}
