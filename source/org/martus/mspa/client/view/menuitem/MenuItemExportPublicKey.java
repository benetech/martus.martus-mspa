/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.mspa.client.view.menuitem;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.martus.common.MartusUtilities;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.mspa.main.UiMainWindow;
import org.martus.util.Base64.InvalidBase64Exception;


public class MenuItemExportPublicKey extends AbstractAction
{
	public MenuItemExportPublicKey(UiMainWindow mainWindow, String label, File dir)
	{
		super(label);	
		parent = mainWindow;
		currentDir = dir;
	}

	public void actionPerformed(ActionEvent arg0) 
	{								
		File keypair = parent.getMSPAApp().getKeypairFile();
		if (!keypair.exists())
		{
			JOptionPane.showMessageDialog(parent, keypair.getParent()+"Keypair not found.",
				 "MSPA Error Message", JOptionPane.ERROR_MESSAGE);
		}	
		
		MartusCrypto security = parent.getMSPAApp().getSecurity();
		File outputFile = new File(currentDir, "publicKey.txt");
		try
		{
			// would just overwritten the existing file.
			MartusUtilities.exportServerPublicKey(security, outputFile);			
			if (outputFile.exists())				
				JOptionPane.showMessageDialog(parent, "Public key has been exported at "+outputFile.getPath(),
				 "", JOptionPane.INFORMATION_MESSAGE);
			else
				JOptionPane.showMessageDialog(parent, "Public key has been exported at "+outputFile.getPath(),
				 "Export Error", JOptionPane.ERROR_MESSAGE);
		}
		catch (MartusSignatureException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidBase64Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	UiMainWindow parent;
	File currentDir;	
}
