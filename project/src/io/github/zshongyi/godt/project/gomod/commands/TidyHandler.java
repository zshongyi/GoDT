/**
 * 
 */
package io.github.zshongyi.godt.project.gomod.commands;

/**
 * @author zshongyi
 *
 */
public class TidyHandler extends GoModCommandsHandler {

	@Override
	public String[] getModCommandParameters() {
		return new String[] { "tidy", "-v" };
	}

}
