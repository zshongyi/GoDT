/**
 * 
 */
package io.github.zshongyi.godt.common.preference;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author zshongyi
 *
 */
public class GoEnvPreferencePlugin extends AbstractUIPlugin {

	private static GoEnvPreferencePlugin plugin;

	public static GoEnvPreferencePlugin getPlugin() {
		if (plugin == null) {
			plugin = new GoEnvPreferencePlugin();
		}
		return plugin;
	}

}
