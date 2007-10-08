/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

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
package org.martus.mspa.roothelper;

import org.martus.common.MartusLogger;


public class RootHelperConnector
{
	public RootHelperConnector(int portToUse) 
	{
		MartusLogger.log("Root Helper not implemented yet");
//		try
//		{	
//			Registry registry = LocateRegistry.getRegistry( portToUse );							
//			String hostToBind = "//"+DEFAULT_HOSTNAME_TO_BIND+":"+portToUse+"/RootHelper";
//			System.out.println("Lookup RootHelper connector from registry: "+ hostToBind);			
//			messenger = (Messenger)registry.lookup(hostToBind);
//			System.out.println("\n");
//		}
//		catch (RemoteException e)
//		{	
//			System.out.println("Lookup status: failled ...");		
//			e.printStackTrace();			
//		}
//		catch (NotBoundException e)
//		{
//			System.out.println("Lookup status: failled ...");	
//			e.printStackTrace();
//		}			
	}	
	
//	public Messenger getMessenger()
//	{
//		return messenger;
//	}		
//		
//	Messenger messenger;
//	private final static String DEFAULT_HOSTNAME_TO_BIND = "127.0.0.1";
}
