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
	
	public Status startServer(String accountKey) throws RemoteException
	{
		return callScript(START_SERVER);
	}
	
	public Status stopServer(String accountKey) throws RemoteException
	{	
		return callScript(STOP_SERVER);
	}
	
	public Status getStatus(String accountKey, int statusType) throws RemoteException
	{		
		return callScript(statusType);
	}
	
	public Status setReadOnly(String accountKey) throws RemoteException
	{	
		return callScript(READONLY);
	}
	
	public Status setReadWrite(String accountKey) throws RemoteException
	{			
		return callScript(READ_WRITE);
	}
	
	private Status callScript(int scriptType)
	{
		Status status = new Status();
		switch (scriptType)
		{
			case START_SERVER:
			case STOP_SERVER:
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
		

	public Status getAdminFile(String key, String fileFrom, String fileTo) throws RemoteException 
	{
		Status status = new Status();		
								
		try
		{
			FileTransfer.copyFile(new File(fileFrom), new File(fileTo));			
			status.setStatus(Status.SUCCESS);		
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(fileFrom+" not found: ");				
		}
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+fileFrom+")file.\n"+e.toString());				
			e.printStackTrace();			
		}		
				
		return status;
	}	
	
	public static final String CONNET_MSG = "Connected: Start remote message ...";
}
