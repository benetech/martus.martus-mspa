
package org.martus.mspa.client.core;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import org.martus.common.MartusUtilities;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusSecurity;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.network.ClientSideXmlRpcHandler;
import org.martus.util.Base64.InvalidBase64Exception;

public class MSPAClient 
{				
	public MSPAClient(UiBasicLocalization local) throws Exception
	{		
		security = new MartusSecurity();	
		loadServerToCall();	
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
			return handler =  new ClientSideXmlRpcHandler(ipToUse, portToUse);			
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
	
	private void loadServerToCall() throws
			IOException, 
			MartusUtilities.InvalidPublicKeyFileException, 
			MartusUtilities.PublicInformationInvalidException, 
			SSLSocketSetupException, InvalidBase64Exception
	{
		portToUse = 443;					
		File[] toCallFiles = getServerToCallDirectory().listFiles();
		if(toCallFiles != null)
		{
			for (int i = 0; i < toCallFiles.length; i++)
			{
				File toCallFile = toCallFiles[i];		
				if(!toCallFile.isDirectory())
				{
					ipToUse = MartusUtilities.extractIpFromFileName(toCallFile.getName());				
					Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(toCallFile, security);
					String serverPublicKey = (String)publicInfo.get(0);	
					if (serverPublicKey != null)
					{				
						String nonFormatPublicCode = MartusCrypto.computePublicCode(serverPublicKey);
						serverPublicCode = MartusCrypto.formatPublicCode(nonFormatPublicCode);	
					}
				}
			}
		}	
	}
	
	private File getServerToCallDirectory()
	{
		return new File(UiMainWindow.getDefaultDirectoryPath(), SERVER_WHO_WE_CALL_DIRIRECTORY);
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
	
	public String getCurrentServerIp()
	{
		return ipToUse;
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
	
	public Vector getMagicWordsFromMartus()
	{
		Vector results=null;	
		try
		{			
			Vector parameters = new Vector();			
			String signature = security.createSignatureOfVectorOfStrings(parameters);		
			results = handler.getMagicWordsFromMartus(security.getPublicKeyString(), parameters, signature);
			
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
			results = handler.getMagicWords(security.getPublicKeyString());
			
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
	
	public void updateMagicWordsToMartus(Vector magicWords)
	{	
		try
		{			
			Vector parameters = new Vector();			
			String signature = security.createSignatureOfVectorOfStrings(parameters);		
			handler.updateMagicWordsToMartus(security.getPublicKeyString(), parameters, signature, magicWords);			
		}		
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}				
	}	
	
	public void updateMagicWords(Vector magicWords)
	{	
		try
		{			
			handler.updateMagicWords(security.getPublicKeyString(), magicWords);			
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
	private static final String SERVER_WHO_WE_CALL_DIRIRECTORY = "serverToCall";
}
