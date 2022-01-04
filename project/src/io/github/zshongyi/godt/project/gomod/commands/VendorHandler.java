package io.github.zshongyi.godt.project.gomod.commands;

public class VendorHandler extends GoModCommandsHandler {

	@Override
	public String[] getModCommandParameters() {
		return new String[] { "vendor" };
	}

}
