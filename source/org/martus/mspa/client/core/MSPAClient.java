
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
	
	public Vector displayAccounst()
	{
		Vector results=null;	
		try
		{			
			Vector parameters = new Vector();							
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
	
	public Vector getContactInfo(String accountId)
	{
		Vector results=null;	
		try
		{			
			Vector parameters = new Vector();			
			String signature = security.createSignatureOfVectorOfStrings(parameters);		
			results = handler.getContactInfo(security.getPublicKeyString(), parameters, signature, accountId);
			
			if (results != null && !results.isEmpty())
			{
				Vector info = (Vector) results.get(1);
				if (!info.isEmpty())
					return info;
			}	 
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return results;
	}
	
	public Vector getMagicWords()
	{
		Vector results=null;	
		try
		{			
			Vector parameters = new Vector();			
			String signature = security.createSignatureOfVectorOfStrings(parameters);		
			results = handler.getMagicWords(security.getPublicKeyString(), parameters, signature);
			
			if (results != null && !results.isEmpty())
			{
				Vector info = (Vector) results.get(1);
				if (!info.isEmpty())
					return info;
			}	 
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return results;
	}	
	
	public void updateMagicWords(Vector magicWords)
	{	
		try
		{			
			Vector parameters = new Vector();			
			String signature = security.createSignatureOfVectorOfStrings(parameters);		
			handler.updateMagicWords(security.getPublicKeyString(), parameters, signature, magicWords);			
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}	
	
	ClientSideXmlRpcHandler handler;
	String ipToUse;
	int portToUse;
	MartusCrypto security;	
	
	final static int DEFAULT_PORT = 443;
	final static String DEFAULT_HOST = "localHost";	
}
