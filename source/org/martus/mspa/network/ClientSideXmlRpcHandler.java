package org.martus.mspa.network;

import java.io.IOException;
import java.util.Vector;

import org.martus.common.crypto.MockMartusSecurity;
import org.martus.common.network.MartusXmlrpcClient;
import org.martus.common.network.SimpleX509TrustManager;


public class ClientSideXmlRpcHandler 
       extends MartusXmlrpcClient 
       implements NetworkInterface, NetworkInterfaceXmlRpcConstants
{
	
	public ClientSideXmlRpcHandler(String serverIpAddr, int portToUse) throws Exception
	{
		super(serverIpAddr, portToUse);
		SimpleX509TrustManager tm = getSimpleX509TrustManager();
		
		tm.setExpectedPublicKey(getServerPublicKey());
	}
	
	String getServerPublicKey() throws Exception 
	{
		return MockMartusSecurity.createClient().getPublicKeyString();
	}	
	 
	 
	 
	public Vector ping() throws Exception
	{
		return (Vector)callServer(cmdPing, new Vector());
	}
	
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		return (Vector)callServer(cmdGetAccountIds, params);
	}	
	
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);
		params.add(parameters);
		params.add(signature);
		params.add(accountId);
		return (Vector)callServer(cmdGetContactInfo, params);
	}	
		
	public Object callServer(String method, Vector params) throws IOException
	{		
		return callserver(serverObjectName, method, params);
	}	
}
