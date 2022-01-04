package io.github.zshongyi.godt.project.gomod.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import io.github.zshongyi.godt.common.tooling.GoToolChain;

public abstract class GoModCommandsHandler extends AbstractHandler {

	public abstract String[] getModCommandParameters();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);

		if (selection instanceof IStructuredSelection) {
			File selectedFile = (File) ((IStructuredSelection) selection).getFirstElement();
			IProject project = selectedFile.getProject();
			GoToolChain.exec(project, getGoParameters(), true);
		}

		return null;

	}

	private String[] getGoParameters() {
		String[] modParams = getModCommandParameters();
		String[] c = new String[1 + modParams.length];
		c[0] = "mod";
		System.arraycopy(modParams, 0, c, 1, modParams.length);
		return c;
	}

}
