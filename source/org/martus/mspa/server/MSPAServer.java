package org.martus.mspa.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.database.Database;
import org.martus.common.database.MockServerDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.mspa.network.NetworkInterfaceXmlRpcConstants;
import org.martus.mspa.network.ServerSideHandler;


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(Database db) 
	{	
		mspaHandler = new ServerSideHandler(this);
		database = db;
	}	
	
	public void createMSPAXmlRpcServerOnPort(int port) throws Exception
	{	
		MartusSecureWebServer.security = MockMartusSecurity.createClient();
		MartusXmlRpcServer.createSSLXmlRpcServer(getMSPAHandler(),serverObjectName, port, getMainIpAddress());
	}
	
	public ServerSideHandler getMSPAHandler()
	{
		return mspaHandler;
	}
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}
	
	public InetAddress getMainIpAddress() throws UnknownHostException
	{
		return InetAddress.getByName(ipAddress);
	}
	
	public MartusCrypto getSecurity()
	{
		return MartusSecureWebServer.security;
	}
	
	public Database getDatabase()
	{		
		return database;
	}
	
	boolean isAuthorizeClient(String myAccountId)
	{
		return authorizedClients.contains(myAccountId);
	}
	
	public void setIPAddress(String ipAddr)
	{
		ipAddress = ipAddr;
	}
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}	
	
	public static void main(String[] args)
	{
		System.out.println("MSPAServer");
		try
		{	
			int port = 443;	
			MSPAServer server = new MSPAServer(new MockServerDatabase());
			server.setPortToUse(port);
			server.setIPAddress("localHost");
				
			System.out.println("Setting up socket connection for listener ...");			
			server.createMSPAXmlRpcServerOnPort(port);
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
	Database database;	
}
