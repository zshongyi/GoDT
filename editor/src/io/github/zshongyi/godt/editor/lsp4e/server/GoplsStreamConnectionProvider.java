/**
 * 
 */
package io.github.zshongyi.godt.editor.lsp4e.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.lsp4e.server.ProcessStreamConnectionProvider;
import org.eclipse.lsp4e.server.StreamConnectionProvider;
import org.eclipse.swt.widgets.Display;

import io.github.zshongyi.godt.editor.preference.GoplsPreferenceConstants;
import io.github.zshongyi.godt.editor.preference.GoplsPreferencePage;
import io.github.zshongyi.godt.editor.preference.GoplsPreferencePlugin;

/**
 * @author zshongyi
 *
 */
public class GoplsStreamConnectionProvider extends ProcessStreamConnectionProvider implements StreamConnectionProvider {

	/**
	 * 
	 */
	public GoplsStreamConnectionProvider() {
		String goplsBinary = GoplsPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoplsPreferenceConstants.GOPLS_PATH);
		if (goplsBinary.isEmpty()) {
			goplsBinary = GoplsPreferencePage.getBinaryName();
		}

		List<String> commands = new ArrayList<>();
		commands.add(goplsBinary);
		commands.add("-vv");
		setCommands(commands);

	}

	@Override
	public void start() {
		Display.getDefault().syncExec(() -> {
			try {
				super.start();
			} catch (IOException e) {
				MessageDialog.openError(null, "Can't start gopls",
						"Please download it with command:\n" + "go install golang.org/x/tools/gopls@latest");
				e.printStackTrace();
			}
		});
	}

}
