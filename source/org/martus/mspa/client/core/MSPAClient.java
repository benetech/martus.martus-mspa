
package org.martus.mspa.client.core;

import java.io.File;
import java.util.Vector;

import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.network.ClientSideXmlRpcHandler;
import org.martus.util.Base64.InvalidBase64Exception;

public class MSPAClient 
{				
	public MSPAClient(UiBasicLocalization local, String ipAddr, int port, String serverToConnect) throws Exception
	{
		ipToUse = ipAddr;
		portToUse = port;
		serverPublicCode = serverToConnect;
		
		security = new MartusSecurity();		
		handler = createXmlRpcNetworkInterfaceHandler();	
		setServerPublicCode(serverPublicCode);
			
	}
	
	public ClientSideXmlRpcHandler getClientSideXmlRpcHandler()
	{
		return handler;
	}
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}		
	
	private void setServerPublicCode(String key)
	{	

		String serverPublicCode = MartusCrypto.removeNonDigits(key);
		handler.getSimpleX509TrustManager().setExpectedPublicCode(serverPublicCode);	
	}
				
	private ClientSideXmlRpcHandler createXmlRpcNetworkInterfaceHandler()
	{		
		try 
		{					
			ClientSideXmlRpcHandler handler =  new ClientSideXmlRpcHandler(ipToUse, portToUse);
			return handler;			
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
	
	public File getUiStateFile()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), "UiState.dat");
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public File getKeypairFile()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), KEYPAIR_FILE);
	}
	
	public void signIn(String userName, char[] userPassPhrase) throws Exception
	{
		try
		{				
			getSecurity().readKeyPair(getKeypairFile(), getCombinedPassPhrase(userName, userPassPhrase));		
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
	public char[] getCombinedPassPhrase(String userName, char[] userPassPhrase)
	{
		char[] combined = new char[userName.length() + userPassPhrase.length + 1];
		System.arraycopy(userPassPhrase,0,combined,0,userPassPhrase.length);
		combined[userPassPhrase.length] = ':';
		System.arraycopy(userName.toCharArray(),0,combined,userPassPhrase.length+1,userName.length());
		
		return(combined);
	}
	
	public String getCurrentServerPublicCode()
	{
		return serverPublicCode;
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
	
	public String getPublicCode(String accountId)
	{
		String publicCode = null;
		try
		{																		
			publicCode = MartusSecurity.getFormattedPublicCode(accountId);
		}
		catch (InvalidBase64Exception e)
		{						
			e.printStackTrace();					
		}
		
		return publicCode;
	}			
	
	ClientSideXmlRpcHandler handler;
	String ipToUse;
	int portToUse;
	String serverPublicCode;
	UiBasicLocalization localization;
	MartusCrypto security;
	File keyPairFile;	
	
	final static int DEFAULT_PORT = 443;
	final static String DEFAULT_HOST = "localHost";	
	
	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
}
