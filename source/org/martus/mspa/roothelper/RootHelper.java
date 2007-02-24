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

import java.io.BufferedReader;
import java.io.File;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.TimerTask;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.util.UnicodeReader;


public class RootHelper
{
	public RootHelper(String[] args, final String passphrase)
	{			
		logger = new LoggerToConsole();	
		processCommandLine(args);		
		try 
		{		
			Registry registry = getRegistry(portToUse);				
			MessengerImpl localObject = new MessengerImpl(passphrase);
			String hostname = "//"+hostToBind+":"+portToUse+"/RootHelper";
			
			registry.bind(hostname, localObject);			
			System.out.println("Port to use for clients: "+ portToUse);							  
			System.out.println("MessengerImpl object has been bound in the registry");
			
			startBackgroundTimers();	
		} 
		catch(RemoteException re) 
		{
		   	log("RemoteExecption: "+re.getMessage());
		}
		catch (AlreadyBoundException e)
		{
			e.printStackTrace();
			log("Port already bound: "+ e.getMessage());
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
			System.out.println("Create a new Registry with port: "+port+" is not available: "+noRegistry.getMessage());
			System.out.println("Will return an default registry with port: "+port);
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
			appDirectory = UNIX_ENVIRONMENT;
		return new File(appDirectory);
	}	
	
	private void setPortToUse(int port)
	{
		portToUse = port;
	}
	
	public synchronized void log(String message)
	{
		logger.logNotice(message);
	}

	
	public File getTriggerDirectory()
	{
		return new File(getRootHelperDirectory(), ADMINTRIGGERDIRECTORY);
	}
	
	public File getShutdownFile()
	{
		return new File(getTriggerDirectory(), SHUTDOWN_FILENAME);
	}
	
	public boolean isShutdownRequested()
	{
		boolean exitFile = getShutdownFile().exists();
		if(exitFile && !loggedShutdownRequested)
		{
			loggedShutdownRequested = true;
			log("Exit file found, attempting to shutdown.");
		}
		return(exitFile);
	}
	
	protected void startBackgroundTimers()
	{
		MartusUtilities.startTimer(new ShutdownRequestMonitor(), shutdownRequestIntervalMillis);
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
				String portToListen = argument.substring(portToListenTag.length());
				setPortToUse(Integer.parseInt(portToListen));	
			}			
		}
		System.out.println("");
	}
		
	public static void main(String[] args) 
	{	
		System.out.println("Initialize RootHelper environment");
		String passphrase = null;
		try
		{
			System.out.print("Enter passphrase: ");
			System.out.flush();
			BufferedReader reader = new BufferedReader(new UnicodeReader(System.in));		
			passphrase = reader.readLine();
		}
		catch(Exception e)
		{
			System.out.println("RootHelper: " + e);
			System.exit(3);
		}
		
		new RootHelper(args, passphrase);
	}	
	
	class ShutdownRequestMonitor extends TimerTask
	{
		public void run()
		{
			if( isShutdownRequested())
			{
				log("Shutdown request acknowledged, preparing to shutdown.");										
				getShutdownFile().delete();
				log("RootHelper has exited.");
				try
				{
					System.exit(0);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
		
	private int portToUse = DEFAULT_PORT;	
	private String hostToBind=DEFAULT_HOSTNAME_TO_BIND;
	private LoggerInterface logger;
	private boolean loggedShutdownRequested;
	
	private final static int DEFAULT_PORT = 983;
	private final static String DEFAULT_HOSTNAME_TO_BIND = "127.0.0.1";
	private final static String UNIX_ENVIRONMENT = "/var/RootHelper/";
	private final static String WINDOW_ENVIRONMENT = "C:/RootHelper/";
	private final static String AUTHORIZED_CLIENTS_FILE = "authorizedClient.txt";
	private static final String SHUTDOWN_FILENAME = "exit";
	private static final String ADMINTRIGGERDIRECTORY = "adminTriggers";
	private static final long shutdownRequestIntervalMillis = 1000;
}
