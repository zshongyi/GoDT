/**
 * 
 */
package io.github.zshongyi.godt.project.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import io.github.zshongyi.godt.common.preference.GoEnvPreferenceConstants;
import io.github.zshongyi.godt.common.preference.GoEnvPreferencePlugin;
import io.github.zshongyi.godt.common.tooling.GoToolChain;
import io.github.zshongyi.godt.project.builder.GodtNature;
import io.github.zshongyi.godt.project.ui.icons.Icons;

/**
 * @author zshongyi
 *
 */
public class GoModProjectWizard extends BasicNewProjectResourceWizard {

	@Override
	public void addPages() {
		super.addPages();
		IWizardPage mainPage = this.getPage("basicNewProjectPage");
		mainPage.setTitle("Create a Go Mod Project");
		mainPage.setImageDescriptor(ImageDescriptor.createFromFile(Icons.class, Icons.GO_PROJECT_LARGE));
	}

	@Override
	public boolean performFinish() {
		boolean result = super.performFinish();

		
		final String goBinary = GoEnvPreferencePlugin.getPlugin().getPreferenceStore()
				.getString(GoEnvPreferenceConstants.GO_BINARY_PATH);
		if (goBinary.isBlank()) {
			MessageDialog.openError(null, GoEnvPreferenceConstants.TTL_CANTFINDGO, GoEnvPreferenceConstants.MSG_PLSINSTALLGO);
			return result &= false;
		}

		IProject newProject = getNewProject();

		result &= GoToolChain.exec(newProject, new String[] { "mod", "init", newProject.getName() });
		try {
			IProjectDescription projectDescription = newProject.getDescription();
			String[] prevNatures = projectDescription.getNatureIds();
			String[] newNatures = new String[prevNatures.length + 1];
			System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
			newNatures[prevNatures.length] = GodtNature.NATURE_ID;
			projectDescription.setNatureIds(newNatures);
			newProject.setDescription(projectDescription, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		return result;
	}

}
