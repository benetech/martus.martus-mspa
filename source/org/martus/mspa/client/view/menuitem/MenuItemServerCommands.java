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
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.common.network.NetworkInterfaceConstants;


public class MenuItemServerCommands extends AbstractAction
{
	public MenuItemServerCommands(UiMainWindow mainWindow, String type)
	{
		super(type);	
		parent = mainWindow;
		menuType = type;		
	}

	public void actionPerformed(ActionEvent arg0) 
	{
		if (menuType.equals(UiMainWindow.START_MARTUS_SERVER))
		{	
			String msg = "Starting Server ...";
			parent.setStatusText(msg);
			JOptionPane.showMessageDialog(parent, msg, "Start Server", JOptionPane.INFORMATION_MESSAGE);
			Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.START_SERVER,"");			
			handleResults(results, "Start");			
		}
		
		if (menuType.equals(UiMainWindow.STOP_MARTUS_SERVER))
		{	
			int answer = JOptionPane.showConfirmDialog(parent, 
				"This command will stop the server, preventing any users from accessing it until it is started again.\n\n Are you sure you want to do this?",
				"Stop Server", JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION) 
			{
				parent.setStatusText("Starting Server ...");
				Vector results = parent.getMSPAApp().sendCommandToServer(NetworkInterfaceConstants.STOP_SERVER,"");
				handleResults(results, "Stop");						
			} 	
		}	
	}
	
	private void handleResults(Vector results, String type)
	{
		String status = (String) results.get(0);
		if (status.equals(NetworkInterfaceConstants.EXEC_ERROR))
		{	
			parent.setStatusText(type+" Martus Server status: failed");
			JOptionPane.showMessageDialog(parent, results.get(1), status, JOptionPane.ERROR_MESSAGE);
			parent.setStatusText("");			
		}
		else
			parent.setStatusText(type+" Martus Server status: "+status);
	}
	
	UiMainWindow parent;
	String menuType;	
}
