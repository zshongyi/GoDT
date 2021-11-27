/**
 * 
 */
package io.github.zshongyi.godt.editor.preference;

import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * @author zshongyi
 *
 */
public class GoplsPreferencePlugin extends AbstractUIPlugin {

	private static GoplsPreferencePlugin plugin;

	public static GoplsPreferencePlugin getPlugin() {
		if (plugin == null) {
			plugin = new GoplsPreferencePlugin();
		}
		return plugin;
	}

}
