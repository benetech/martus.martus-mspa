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
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class MessengerImpl extends UnicastRemoteObject implements Messenger, MessageType 
{
	public MessengerImpl() throws RemoteException 
	{
		super();
	}
	
	public Status callSystemCommand(int msgType, String cmd) throws RemoteException 
	{		
		try
		{
			switch (msgType)
			{
				case START_SERVER:
				case STOP_SERVER:
					return executeCommand(cmd);
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
	
	public Status filesTransferTo(Vector transfers) throws RemoteException 
	{
		Status status = new Status();
		String fileName=null;
		try
		{		
			for (int i=0;i<transfers.size();++i)
			{
				FilesTransfer fileTransfer = (FilesTransfer) transfers.get(i);
				fileName = fileTransfer.getFileName();
				writeDataToFile(new File(fileName), fileTransfer.getLineOfEntries());			
			}
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(fileName+" not found: "+nothingToWorryAbout.getMessage());							
		}
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+ fileName+ ")file.\n"+e.toString());				
			e.printStackTrace();		
		}		
	
		return status;		
	}
	
	public Status filesTransferFrom(Vector transfers) throws RemoteException 
	{
		String fileName=null;
		Vector results = new Vector();
		Status status = new Status();
		
		try
		{		
			for (int i=0;i<transfers.size();++i)
			{
				fileName = (String) transfers.get(i);
				Vector entries = readDataFromFile(new File(fileName));
				if (entries.size() >0)
					results.add(new FilesTransfer(fileName, entries));			
			}
			
			if (results.size() > 0)
			{				
				status.setListOfFileTransfer(results);
				return status;
			}	
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(fileName+" not found: "+ nothingToWorryAbout.toString());			
		}
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+ fileName+ ")file.\n"+e.toString());			
			e.printStackTrace();			
		}		
		
		return status;
	}

	public Status getAdminFile(String key, String adminFile) throws RemoteException 
	{
		Status status = new Status();
		Vector results = new Vector();
				
		try
		{
			Vector entries = readDataFromFile(new File(adminFile));
			if (entries.size() >0)
				results.add(new FilesTransfer(adminFile, entries));		
			
			if (results.size() > 0)
			{				
				status.setListOfFileTransfer(results);
				return status;
			}	
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg(adminFile+" not found: ");				
		}
		catch (IOException e)
		{
			status.setStatus(Status.FAILED);			
			status.setErrorMsg("Error loading ("+adminFile+")file.\n"+e.toString());				
			e.printStackTrace();			
		}		
				
		return status;
	}
	
		
	private static Status executeCommand (String externCommand) throws IOException 
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
	

	private void writeDataToFile(File fileName, Vector lineEntries) throws IOException
	{		
		UnicodeWriter writer = new UnicodeWriter(fileName);
		for (int i=0;i<lineEntries.size();++i)
		{
			writer.writeln((String)lineEntries.get(i));
		}								
		writer.close();			
	}

	private Vector readDataFromFile(File adminFile) throws IOException
	{
		Vector list = new Vector();
		UnicodeReader reader = new UnicodeReader(adminFile);
		String line = null;
		while( (line = reader.readLine()) != null)
		{
			if(line.trim().length() == 0)
				System.out.println("Warning: Found blank line in " + adminFile.getPath());
			else
				list.add(line);					
		}
		
		reader.close();
		
		return list;
	}






	public String getMessage() throws RemoteException 
	{
	  return("Here is a remote message.");
	}
	
}
