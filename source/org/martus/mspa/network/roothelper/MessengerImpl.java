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
import java.util.Vector;

import org.martus.common.Version;


public class MessengerImpl extends UnicastRemoteObject implements Messenger, MessageType 
{
	public MessengerImpl() throws RemoteException 
	{
		super();
		
	}
	
	public Status sendCommand(String accountKey, int msgType, String cmd) throws RemoteException 
	{
		try
		{
			switch (msgType)
			{
				case START_SERVER:
					return executeCommand(cmd);
				case STOP_SERVER:
					return stopMartusServer(cmd);
				case HIDE_BULLETIN:
					break;	
			}		
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return new Status(Status.FAILED);
	}
	
	public Status copyFilesTo(String accountKey, Vector transfers) throws RemoteException 
	{
		Status status = new Status();
		String from=null;		
		try
		{		
			for (int i=0;i<transfers.size();++i)
			{
				FileTransfer fileTransfer = (FileTransfer) transfers.get(i);
				from = fileTransfer.getFromFileName();
				String to = fileTransfer.getToFileName();			
				FileTransfer.copyFile(new File(from), new File(to));
				status.setStatus(Status.SUCCESS);								
			}
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(from+" not found: "+nothingToWorryAbout.getMessage());							
		}
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+ from+ ")file.\n"+e.toString());				
			e.printStackTrace();		
		}		
	
		return status;		
	}
	
	public Status copyFilesFrom(String accountKey, Vector transfers) throws RemoteException 
	{
		String from=null;
		Status status = new Status();
		
		try
		{		
			for (int i=0;i<transfers.size();++i)
			{
				FileTransfer fileTransfer = (FileTransfer) transfers.get(i);			
				from = fileTransfer.getFromFileName();
				String to = fileTransfer.getToFileName();			
				FileTransfer.copyFile(new File(from), new File(to));
				status.setStatus(Status.SUCCESS);
			}									
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(from+" not found: "+ nothingToWorryAbout.toString());			
		}		
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+ from+ ")file.\n"+e.toString());				
			e.printStackTrace();		
		}	
		
		return status;
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
	
	private Status stopMartusServer(String command) throws IOException
	{
		//Need to get process id
		return executeCommand(command);
	}
	
		
	private Status executeCommand (String externCommand) throws IOException 
	{ 
		Status status = new Status();		
		String command = externCommand;	
		if(Version.isRunningUnderWindows())
			command = "cmd.exe /C "+externCommand;
		
		Process proc = Runtime.getRuntime().exec(command);  
		try 
		{
			int exitVal = proc.waitFor();
			if (exitVal !=0)
			{
				status.setStatus(Status.FAILED);			
				status.setErrorMsg("Process return code: "+ exitVal);		
			}	 
			else
				status.setStatus(Status.SUCCESS);
		} 
		catch (InterruptedException e) 
		{ 
			status.setStatus(Status.FAILED);
			String errorMsg = "InterruptedException raised: "+e.getMessage();			
			status.setErrorMsg(errorMsg);		
			System.out.println(errorMsg);			
		} 
		
		return status;
	} 	


	public String getInitMsg() throws RemoteException 
	{
	  return(CONNET_MSG);
	}
	
	public static final String CONNET_MSG = "Connected: Start remote message ...";
}
