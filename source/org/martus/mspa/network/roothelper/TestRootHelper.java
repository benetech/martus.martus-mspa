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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TestRootHelper
{

	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}
	
	private static void startRootHelper()
	{	
		try 
		{
		  MessengerImpl localObject = new MessengerImpl();
		  Naming.rebind("rmi:///RootHelper", localObject);							  
		  System.out.println("MessengerImpl object has been bound");

		} 
		catch(RemoteException re) 
		{
		  System.out.println("RemoteException: " + re);
		} 
		catch(MalformedURLException mfe) 
		{
		  System.out.println("MalformedURLException: "+ mfe);
		}
	}	
	
//	private static void setupEnviornment()
//	{		
//		try
//		{
//			proc = Runtime.getRuntime().exec("cmd.exe /C start rmiregistry");
//			proc.waitFor();
//			startRootHelper(); 
//		}
//		catch (IOException e1)
//		{
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		catch (InterruptedException e) 
//		{ 
//			System.out.println("InterruptedException raised: "+e.getMessage()); 
//		} 
//				
//	}

	public static Test suite ( )
	{
		TestSuite suite= new TestSuite("All RootHelper Tests");
//		setupEnviornment();		
		startRootHelper(); 
		suite.addTest(new TestSuite(TestRootHelperConnecter.class));

		return suite;
	}	
	
//	static Process proc = null;
}
