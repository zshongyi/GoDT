package io.github.zshongyi.godt.common.preference;
/**
 * 
 */


import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.internal.dialogs.EmptyPreferencePage;

/**
 * @author zshongyi
 *
 */
@SuppressWarnings("restriction")
public class CategoryPreferencePage extends EmptyPreferencePage implements IWorkbenchPreferencePage {

	/**
	 * 
	 */
	public CategoryPreferencePage() {
		this.setDescription("Expand the tree to edit preferences for GoDT.");
	}

}
