package io.github.zshongyi.godt.project.builder;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import io.github.zshongyi.godt.common.tooling.GoToolChain;

public class GodtBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "io.github.zshongyi.godt.project.GodtBuilder";

	private static final String MARKER_TYPE = "io.github.zshongyi.godt.project.GodtProblem";

	@Override
	protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
		case FULL_BUILD:
			fullBuild(monitor);
			break;
		case CLEAN_BUILD:
			clearBuild(monitor);
			break;
		default:
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		String[] command = new String[] { "clean", "-i", "-x", "-cache", "-testcache" };
		GoToolChain.asyncExecute(getProject(), command);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	private void clearBuild(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		String[] command = new String[] { "build", "-v", "-x", };
		GoToolChain.asyncExecute(getProject(), command);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		String[] command = new String[] { "build", "-v", "-a", "-x", };
		GoToolChain.asyncExecute(getProject(), command);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
		String[] command = new String[] { "build", "-v", "-x", };
		GoToolChain.asyncExecute(getProject(), command);
		getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
	}

}
