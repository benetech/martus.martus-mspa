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
import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Vector;

import org.martus.common.test.TestCaseEnhanced;
import org.martus.mspa.server.MSPAServer;
import org.martus.util.UnicodeWriter;


public class TestRootHelperConnecter extends TestCaseEnhanced
{
	public TestRootHelperConnecter(String name)
	{
		super(name);
		setup();
	}	
	
	private void setup()
	{
		String martusAppDir = MSPAServer.getMartusDefaultDataDirectoryPath();
		martusDeleteOnStartDir = new File(martusAppDir, "deleteOnStartup");
		
		try 
		{		
		  register = new RootHelperConnector("localhost");	
		  				 		 
		} 
		catch(RemoteException re) 
		{
		  System.out.println("RemoteException: " + re);
		} 
		catch(NotBoundException nbe) 
		{
		  System.out.println("NotBoundException: " + nbe);
		} 
		catch(MalformedURLException mfe) 
		{
		  System.out.println("MalformedURLException: "+ mfe);
		}			
	}	
	
	public void testGetInitMessage()
	{
		try
		{
			messenger = register.getMessenger();
			assertEquals("Got init message", MessengerImpl.CONNET_MSG,register.getMessenger().getInitMsg());			
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testGetAdminFile()
	{
		try
		{
			File magicFile = new File(martusDeleteOnStartDir.getPath(), "magicwords.txt");			
			tempFile = createTempFileFromName("$$$MartusTestAdminFile");
			messenger = register.getMessenger();
			
			Status status = messenger.getAdminFile("", magicFile.getPath(), tempFile.getPath());
			assertEquals("Status should be success.", Status.SUCCESS, status.getStatus());
			
			Vector entries = FileTransfer.readDataFromFile(tempFile);
			assertTrue("Should contain some magicwords", entries.size() >0);
			tempFile.delete();		
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void testCopyFilesTo()
	{
		try
		{
			File toFile = createTempFileFromName("$$$MartusCopyToFile");
			tempFile = createTempFileFromName("$$$MartusTestAdminFile");

			UnicodeWriter writer = new UnicodeWriter(tempFile);
			writer.writeln("Test 1");
			writer.writeln("Test 2");	
			writer.close();		
			messenger = register.getMessenger();
			FileTransfer transfer = new FileTransfer(tempFile.getPath(), toFile.getPath());
			Vector transfers = new Vector();
			transfers.add(transfer);

			Status status = messenger.copyFilesTo("", transfers);
			assertEquals("Status should be success.", Status.SUCCESS, status.getStatus());
			
			Vector entries = FileTransfer.readDataFromFile(toFile);
			assertTrue("entries size should be 2", entries.size()==2);
			assertEquals("Test 1", (String) entries.get(0));

			toFile.delete();
			tempFile.delete(); 
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
	
	public void tearDown() throws Exception
	{				
		super.tearDown();
	}
	
	RootHelperConnector register;
	Messenger messenger;
	File tempFile;	
	File martusDeleteOnStartDir;
}
