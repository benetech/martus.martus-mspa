
package org.martus.mspa.client.core;

import java.util.Vector;

import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.network.*;

public class MSPAClient 
{				
	public MSPAClient(String ipAddr, int port)
	{
		ipToUse = ipAddr;
		portToUse = port;
		handler = createXmlRpcNetworkInterfaceHandler();		
	}
	
	public ClientSideXmlRpcHandler getClientSideXmlRpcHandler()
	{
		return handler;
	}
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}			
				
	private ClientSideXmlRpcHandler createXmlRpcNetworkInterfaceHandler()
	{		
		try 
		{			
			return new ClientSideXmlRpcHandler(ipToUse, portToUse);
		} 
		catch (SSLSocketSetupException e) 
		{
			e.printStackTrace();			
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return null;
	}	
	
	
	
	
	private Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws Exception 
	{
		return handler.getAccountIds(myAccountId, parameters, signature);
	}
	
	private void ping() 
	{
		try
		{
			Vector result = handler.ping();
			System.out.println(result.get(0));
			Vector msg = (Vector) result.get(1);
			System.out.println(msg.get(0));
					
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}	
	
	public static void main (String [] args) 
	{						
		String testIP = "localHost";
		int testPort= 443;
		
		System.out.println("Start MSPA Client side .xmk RPC Handler ...");	
		MSPAClient client = new MSPAClient(testIP, testPort);		
		client.ping();
	}
	
	ClientSideXmlRpcHandler handler;
	String ipToUse;
	int portToUse;		
}
