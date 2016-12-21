package org.sprite.splitter;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;

public class ManualDialog extends Dialog {

	/**
	 * Create the dialog.
	 * @param parentShell
	 */
	public ManualDialog(Shell parentShell) {
		super(parentShell);
	}

	/**
	 * Create contents of the dialog.
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = (GridLayout) container.getLayout();
		gridLayout.numColumns = 5;
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		Label lblSpriteSplitter = new Label(container, SWT.NONE);
		lblSpriteSplitter.setText("Sprite Splitter");
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		new Label(container, SWT.NONE);
		
		StyledText styledText = new StyledText(container, SWT.BORDER | SWT.READ_ONLY);
		styledText.setToolTipText("Usage:\n1. Right click and use menu \"Load File\" to choose an image file.\n2. Right click and use menu \"Generate BoundingBox\"\n3. Adjust the bounding boxes. Click to select a box, then use ←↑↓→ to move, with SHIFT and ALT to modify the size. Backspace and Delete to remove selected box. Double click a box to split, drag boxes to merge.\n4. Right click and use menu \"Save plist\". The saved file is in the same folder  of the image file, with same name only difference on the extension \"plist\".\n5. Then you can import the plist and image file to Cocos creator directly.\nHope you enjoy it.");
		styledText.setEditable(false);
		styledText.setText("SpriteSplitter is to generate plist files for given image.\nUsage:\n1. Right click and use menu \"Load File\" to choose an image file.\n2. Right click and use menu \"Generate BoundingBox\"\n3. Adjust the bounding boxes. Click to select a box, then use ←↑↓→ to move, with SHIFT and ALT to modify the size. Backspace and Delete to remove selected box. Double click a box to split, drag boxes to merge.\n4. Right click and use menu \"Save plist\". The saved file is in the same folder  of the image file, with same name only difference on the extension \"plist\".\n5. Then you can import the plist and image file to Cocos creator directly.\nHope you enjoy it.\n\nAuthor: leafsax@gmail.com");
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(762, 331);
	}

}
