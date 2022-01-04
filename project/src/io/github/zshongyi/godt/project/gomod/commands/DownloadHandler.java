package io.github.zshongyi.godt.project.gomod.commands;

public class DownloadHandler extends GoModCommandsHandler {

	@Override
	public String[] getModCommandParameters() {
		return new String[] { "download" };
	}

}
