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

import org.martus.common.MagicWords;
import org.martus.common.test.TestCaseEnhanced;
import org.martus.util.UnicodeWriter;


public class TestRootHelperConnecter extends TestCaseEnhanced
{
	public TestRootHelperConnecter(String name)
	{
		super(name);
		setup();
	}
	
	private void setupTestData()
	{	
		try
		{
			tempFile = createTempFileFromName("$$$MartusTestFileMagicWords");
			UnicodeWriter writer = new UnicodeWriter(tempFile);
			writer.writeln(MAGICWORD1+ MagicWords.FIELD_DELIMITER + GROUPNAME1+ MagicWords.FIELD_DELIMITER +CREATIONDATE);
			writer.writeln(MAGICWORD2+ MagicWords.FIELD_DELIMITER + GROUPNAME1+ MagicWords.FIELD_DELIMITER +CREATIONDATE);	
			writer.close();		
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setup()
	{
		try 
		{		
		  register = new RootHelperConnector("localhost");
		  messenger = register.getMessenger();			
		  System.out.println(register.getMessenger().getMessage());
		  
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
	
	public void testGetAdminFile()
	{
		setupTestData();		

		try
		{	
			Status status = messenger.getAdminFile("", tempFile.getPath());
			assertEquals(Status.SUCCESS, status.getStatus());
			
			Vector list = status.getListOfFileTransfer();
			for (int i=0;i<list.size();i++)
			{
				FilesTransfer transfer = (FilesTransfer) list.get(i);
				Vector entries = transfer.getLineOfEntries();
				
				if (entries.size() <=0) break;
				
				assertTrue("should be two entries?", entries.size() == 2);	
				assertEquals("Test 1	Group 1	2/12/2004", (String) entries.get(0));
				assertEquals("Test 2	Group 1	2/12/2004", (String) entries.get(1));
			}	
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		tempFile.delete();
	}	
	
	public void tearDown() throws Exception
	{		
		tempFile.delete();
		super.tearDown();
	}
	
	RootHelperConnector register;
	Messenger messenger;
	File tempFile;
	
	MagicWords magicWords;
	
	private static final String MAGICWORD1 = "Test 1";
	private static final String MAGICWORD2 = "Test 2";
	private static final String GROUPNAME1 = "Group 1";	
	private static final String CREATIONDATE = "2/12/2004";
}
