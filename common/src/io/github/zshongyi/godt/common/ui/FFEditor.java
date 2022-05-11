package io.github.zshongyi.godt.common.ui;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;

import io.github.zshongyi.godt.common.auxiliary.Utils;

/**
 * @author svujic
 *
 */
public class FFEditor extends FileFieldEditor {
	private String pattern;
	private ErrorOnValidateBinary eovb;
	private FieldEditorPreferencePage fepp;

	public FFEditor(String name, String labelText, boolean enforceAbsolute, Composite parent, 
			FieldEditorPreferencePage fepp, String pattern, ErrorOnValidateBinary eovb) {
		super(name, labelText, enforceAbsolute, parent);
		this.fepp = fepp;
		this.pattern = pattern;
		this.eovb = eovb;
	}

	@Override
	protected void fireValueChanged(String property, Object oldValue, Object newValue) {
		// we act as newValue is wrong by default
		fepp.setValid(false);
		refreshValidState();
		
		if (!(newValue instanceof String)) {
			return;
		}

		// filePath is pointing to user chosen binary
		String filePath = newValue.toString();
		if (!Utils.validateBinary(filePath, pattern, eovb)) {
			return;
		}

		fepp.setValid(true);
		refreshValidState();
		super.fireValueChanged(property, oldValue, newValue);
	}

}