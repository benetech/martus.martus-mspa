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
package org.martus.mspa.network.roothelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;


public class MessengerImpl extends UnicastRemoteObject implements Messenger, MessageType 
{
	public MessengerImpl() throws RemoteException 
	{
		super();
		
	}
	
	public ServerStatus startServer(String accountKey) throws RemoteException
	{
		return callScript(SERVER_START);
	}
	
	public ServerStatus stopServer(String accountKey) throws RemoteException
	{	
		return callScript(SERVER_STOP);
	}
	
	public ServerStatus getStatus(String accountKey, int statusType) throws RemoteException
	{		
		return callScript(statusType);
	}
	
	public ServerStatus setReadOnly(String accountKey) throws RemoteException
	{	
		return callScript(READONLY);
	}
	
	public ServerStatus setReadWrite(String accountKey) throws RemoteException
	{			
		return callScript(READ_WRITE);
	}
	
	private ServerStatus callScript(int scriptType)
	{
		ServerStatus status = new ServerStatus();
		switch (scriptType)
		{
			case SERVER_START:
			case SERVER_STOP:
			case READONLY:
			case READ_WRITE:
			case STATUS:
				break;	
		}		
		
		return status;
	}
	
	public String getInitMsg() throws RemoteException 
	{
	  return(CONNET_MSG);
	}
		

	public ServerStatus getAdminFile(String key, String fileFrom, String fileTo) throws RemoteException 
	{
		ServerStatus status = new ServerStatus();		
								
		try
		{
			FileTransfer.copyFile(new File(fileFrom), new File(fileTo));			
			status.setStatus(ServerStatus.SUCCESS);		
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(ServerStatus.FAILED);			
			status.setErrorMsg(fileFrom+" not found: ");				
		}
		catch (IOException e)
		{
			status.setStatus(ServerStatus.FAILED);			
			status.setErrorMsg("Error loading ("+fileFrom+")file.\n"+e.toString());				
			e.printStackTrace();			
		}		
				
		return status;
	}	
	
	public static final String CONNET_MSG = "Connected: Start remote message ...";
}
