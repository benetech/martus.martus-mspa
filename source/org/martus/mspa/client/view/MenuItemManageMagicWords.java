
package org.martus.mspa.client.view;

import java.awt.event.ActionEvent;
import java.util.Vector;

import javax.swing.AbstractAction;

import org.martus.mspa.main.UiMainWindow;


public class MenuItemManageMagicWords extends AbstractAction
{
	public MenuItemManageMagicWords(UiMainWindow mainWindow, String label)
	{
		super(label);
		parent = mainWindow;
	}
	
	public void actionPerformed(ActionEvent arg0) 
	{
		Vector magicWords = parent.getMSPAApp().getMagicWords();
		MagicWordsDlg magicWordsDlg = new MagicWordsDlg(parent, magicWords);
		magicWordsDlg.show();
	}
	
	UiMainWindow parent;				
}
