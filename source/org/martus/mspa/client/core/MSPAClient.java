
package org.martus.mspa.client.core;

import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.network.ClientSideXmlRpcHandler;

public class MSPAClient 
{				
	public MSPAClient(String ipAddr, int port) throws Exception
	{
		ipToUse = ipAddr;
		portToUse = port;
		security = MockMartusSecurity.createClient();	
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
	
	public Vector displayAccounst()
	{
		Vector results=null;	
		try
		{			
			Vector parameters = new Vector();	
			security = MockMartusSecurity.createClient();					
			String signature = security.createSignatureOfVectorOfStrings(parameters);	
			results = getAccountIds(security.getPublicKeyString(), parameters, signature);
			
			if (results != null && !results.isEmpty())
			{
				Vector accounts = (Vector) results.get(1);
				if (!accounts.isEmpty())
					return accounts;
			}	 
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return results;
	}
	
	public static void main (String [] args) 
	{								
		try
		{	
			System.out.println("Start MSPA Client side .xmk RPC Handler ...");	
			MSPAClient client = new MSPAClient(DEFAULT_HOST, DEFAULT_PORT);		
			client.ping();
			Vector list = client.displayAccounst();
			for (int i=0; i<list.size();++i)
			{					
				String publicCode = (String)list.get(i);	
				System.out.println(publicCode);				 	
			}	
		}
		catch(Exception e) 
		{
			System.out.println("UnknownHost Exception" + e);
			System.exit(1);			
		}	
	}
	
	ClientSideXmlRpcHandler handler;
	String ipToUse;
	int portToUse;
	MartusCrypto security;	
	
	final static int DEFAULT_PORT = 443;
	final static String DEFAULT_HOST = "localHost";	
}
