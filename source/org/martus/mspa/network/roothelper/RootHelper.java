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
import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.martus.common.LoggerInterface;
import org.martus.common.Version;


public class RootHelper
{
	public RootHelper(String[] args)
	{	
		processCommandLine(args);		
		try 
		{		
			getRegistry(portToUse);				
			MessengerImpl localObject = new MessengerImpl();
			String hostname = "//"+hostToBind+":"+portToUse+"/RootHelper";
			Naming.bind(hostname, localObject);			
			System.out.println("Port to use for clients: "+ portToUse);							  
			System.out.println("MessengerImpl object has been bound in the registry");
		} 
		catch(RemoteException re) 
		{
		   	logger.log("RemoteExecption: "+re.getMessage());
		}
		catch (AlreadyBoundException e)
		{
			e.printStackTrace();
			logger.log("Port already bound: "+ e.getMessage());
		}
		catch (MalformedURLException e)
		{			
			e.printStackTrace();
			logger.log("URL Exception: "+ e.getMessage());
		} 
}

	public static Registry getRegistry( int port ) throws RemoteException 
	{
	  	try 
		{
	     	return LocateRegistry.createRegistry(port );
	  	} 
		catch (Exception noRegistry) 
		{
	     	return LocateRegistry.getRegistry( port );
	  	}
	}
	
	public static File getAuthorizedClientsFile()
	{
		return new File(getRootHelperDirectory().getPath(), AUTHORIZED_CLIENTS_FILE);
	}	
	
	public static File getRootHelperDirectory()
	{
		String appDirectory = null;
		if(Version.isRunningUnderWindows())
			appDirectory = WINDOW_ENVIRONMENT;
		else
			appDirectory = System.getProperty("user.home")+UNIX_ENVIRONMENT;
		return new File(appDirectory);
	}	
	
	private void setPortToUse(int port)
	{
		portToUse = port;
	}		
	
	private void processCommandLine(String[] args) 
	{			
		String portToListenTag = "--port=";	
		
		System.out.println("");
		for(int arg = 0; arg < args.length; ++arg)
		{					
			String argument = args[arg];				
								
			if(argument.startsWith(portToListenTag))
			{	
				String portToUse = argument.substring(portToListenTag.length());
				setPortToUse(Integer.parseInt(portToUse));	
			}			
		}
		System.out.println("");
	}
		
	public static void main(String[] args) 
	{	
		System.out.println("Initialize RootHelper environment");	
		new RootHelper(args);
	}	
		
	private int portToUse = DEFAULT_PORT;	
	private String hostToBind=DEFAULT_HOSTNAME_TO_BIND;
	private LoggerInterface logger;
	
	private final static int DEFAULT_PORT = 983;
	private final static String DEFAULT_HOSTNAME_TO_BIND = "127.0.0.1";
	private final static String UNIX_ENVIRONMENT = "/var/MSPARootHelper/";
	private final static String WINDOW_ENVIRONMENT = "C:/MSPARootHelper/";
	private final static String AUTHORIZED_CLIENTS_FILE = "authorizedClient.txt";
}
