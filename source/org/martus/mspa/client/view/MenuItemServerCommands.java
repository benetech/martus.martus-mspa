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
package org.martus.mspa.client.view;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.network.NetworkInterfaceConstants;


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
			parent.setStatusText("Send Start command to MSPA server ...");
			String result = parent.getMSPAApp().sendCmdToServer(NetworkInterfaceConstants.START_SERVER,"");
			parent.setStatusText("Start Martus Server status: "+result);
		}
		
		if (menuType.equals(UiMainWindow.STOP_MARTUS_SERVER))
		{	
			parent.setStatusText("Send stop command to MSPA server ...");
			String result = parent.getMSPAApp().sendCmdToServer(NetworkInterfaceConstants.STOP_SERVER,"");
			parent.setStatusText("Stop Martus Server status: "+result);
		}	
	}
	
	UiMainWindow parent;
	String menuType;	
}
