package org.martus.mspa.server;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.martus.common.Version;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.mspa.network.NetworkInterfaceXmlRpcConstants;
import org.martus.mspa.network.ServerSideHandler;


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(File dir) throws Exception
	{	
		mspaHandler = new ServerSideHandler(this);								
		initalizeFileDatabase(dir);		
	}	
	
	private void initalizeFileDatabase(File dir)
	{
		serverDirectory = dir;				
		String keyPairFile = getConfigDirectory()+KEYPAIR_FILE;

		security = loadMartusKeypair(keyPairFile);
		martusDatabaseToUse = new ServerFileDatabase(getPacketDirectory(), security);

		try
		{
			martusDatabaseToUse.initialize();
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map File");
			System.exit(7);
		}
		catch(FileDatabase.MissingAccountMapSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map Signature File");
			System.exit(7);
		}
		catch(FileVerificationException e)
		{
			e.printStackTrace();
			System.out.println("Account Map did not verify against signature file");
			System.exit(7);
		}		
	}	
	
	public MartusCrypto loadMartusKeypair(String keyPairFileName)
	{
		return MartusServerUtilities.loadKeyPair(keyPairFileName, true);		
	}
	
	public File getPacketDirectory()
	{
		return new File(getServerDirectory(), "packets");
	}
	
	public File getServerDirectory()
	{
		return serverDirectory;
	}		
	
	public File getConfigDirectory()
	{
		return new File(getServerDirectory(),ADMIN_STARTUP_CONFIG_DIRECTORY);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getConfigDirectory(), MAGICWORDS_FILENAME);
	}		
	
	public void createMSPAXmlRpcServerOnPort(int port) throws Exception
	{				
		MartusSecureWebServer.security = getSecurity();
		MartusXmlRpcServer.createSSLXmlRpcServer(getMSPAHandler(),serverObjectName, port, getMainIpAddress());
	}
	
	public ServerSideHandler getMSPAHandler()
	{
		return mspaHandler;
	}	
	
	public InetAddress getMainIpAddress() throws UnknownHostException
	{
		return InetAddress.getByName(ipAddress);
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public ServerFileDatabase getDatabase()
	{		
		return martusDatabaseToUse;
	}
	
	boolean isAuthorizeClient(String myAccountId)
	{
		return authorizedClients.contains(myAccountId);
	}	
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}
	
	public static File getDefaultDataDirectory()
	{
		return new File(MSPAServer.getDefaultDataDirectoryPath());
	}	
	
	public static String getDefaultDataDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
			dataDirectory = WINDOW_ENVIRONMENT;
		else
			dataDirectory = System.getProperty("user.home")+UNIX_ENVIRONMENT;
		return dataDirectory;
	}
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}

	public int getPortToUse()
	{
		return (portToUse <= 0)? DEFAULT_PORT:portToUse;
	}

	public void setIPAddress(String ipAddr)
	{
		ipAddress = ipAddr;
	}

	public String getIpAddress()
	{	
		return (ipAddress == null)? DEFAULT_HOST:ipAddress;
	}
	
	public static void main(String[] args)
	{
		System.out.println("MSPAServer");
		try
		{					
			System.out.println("Setting up socket connection for listener ...");
			
			MSPAServer server = new MSPAServer(MSPAServer.getDefaultDataDirectory());
			server.setIPAddress(DEFAULT_HOST);
			server.createMSPAXmlRpcServerOnPort(server.getPortToUse());
																				
			System.out.println("Waiting for connection...");
		
		}
		catch(Exception e) 
		{
			System.out.println("UnknownHost Exception" + e);
			System.exit(1);			
		}
	}	
		
	ServerSideHandler mspaHandler;
	String ipAddress;
	int portToUse;
	Vector authorizedClients;
	ServerFileDatabase martusDatabaseToUse;	
	MartusCrypto security;
		
	private File serverDirectory;	
	private final static String ADMIN_STARTUP_CONFIG_DIRECTORY = "deleteOnStartup";
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_ENVIRONMENT = "/var/MartusServer/";
	
	private final static int DEFAULT_PORT = 443;
	private final static String DEFAULT_HOST = "localHost";
	
}
