package org.martus.mspa.network;

import java.io.IOException;
import java.util.Vector;

import org.martus.common.network.MartusXmlrpcClient;


public class ClientSideXmlRpcHandler 
       extends MartusXmlrpcClient 
       implements NetworkInterface, NetworkInterfaceXmlRpcConstants
{
	
	public ClientSideXmlRpcHandler(String serverIpAddr, int portToUse) throws Exception
	{
		super(serverIpAddr, portToUse);		
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
	
	public Vector getInactiveMagicWords(String myAccountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);		
		return (Vector)callServer(cmdGetInActiveMagicWords, params);
	}	
	
	public Vector getAllMagicWords(String myAccountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetAllMagicWords, params);
	}	
	
	public Vector getActiveMagicWords(String myAccountId) throws IOException
	{
		
		Vector params = new Vector();
		params.add(myAccountId);	
		return (Vector)callServer(cmdGetActiveMagicWords, params);
	}				
	
	public Vector updateMagicWords(String myAccountId, Vector magicWords) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(magicWords);
		return (Vector)callServer(cmdUpdateMagicWords, params);
	}	
	
	public Vector addAvailableMirrorServer(String myAccountId, Vector mirrorInfo) throws IOException
	{	
		Vector params = new Vector();		
		params.add(myAccountId);		
		params.add(mirrorInfo);
		
		return (Vector)callServer(cmdAddAvailableMirrorServer, params);
	}	
		
	public Object callServer(String method, Vector params) throws IOException
	{		
		return callserver(serverObjectName, method, params);
	}	
}
