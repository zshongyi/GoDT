/**
 * 
 */
package io.github.zshongyi.godt.editor.lsp4e.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.WorkspaceFolder;
import org.eclipse.lsp4j.jsonrpc.Launcher.Builder;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.eclipse.lsp4j.services.LanguageServer;

public class GoPatchLauncherBuilder extends Builder<LanguageServer> {
	@Override
	protected MessageConsumer wrapMessageConsumer(MessageConsumer consumer) {
		return super.wrapMessageConsumer((Message message) -> {

			if (message instanceof RequestMessage && ((RequestMessage) message).getMethod().equals("initialize")) {
				InitializeParams initParams = (InitializeParams) ((RequestMessage) message).getParams();
				for (WorkspaceFolder workspaceFolder : initParams.getWorkspaceFolders()) {
					Pattern pattern = Pattern.compile("^file:/(?!/)");
					Matcher matcher = pattern.matcher(workspaceFolder.getUri());
					String newUri = matcher.replaceAll("file://");
					workspaceFolder.setUri(newUri);
				}
			}
			// FIXME: ...lsp4j.jsonrpc.ResponseErrorException: start pos is not valid
			consumer.consume(message);
		});
	}
}
