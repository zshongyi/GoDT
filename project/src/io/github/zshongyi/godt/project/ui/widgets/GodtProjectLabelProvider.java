/**
 * 
 */
package io.github.zshongyi.godt.project.ui.widgets;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import io.github.zshongyi.godt.project.ui.icons.Icons;

/**
 * @author zshongyi
 *
 */
public class GodtProjectLabelProvider implements ILabelProvider {

	private static final Image projectIcon = ImageDescriptor.createFromFile(Icons.class, Icons.GO_PROJECT_SMALL)
			.createImage();

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		return projectIcon;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IProject) {
			return ((IProject) element).getName();
		}
		return null;
	}

}
