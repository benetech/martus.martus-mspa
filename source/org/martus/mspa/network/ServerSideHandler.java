
package org.martus.mspa.network;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.server.MSPAServer;

public class ServerSideHandler implements NetworkInterface
{
	public ServerSideHandler(MSPAServer serverToUse)
	{
		server = serverToUse;			
	}
	
	public Vector ping()
	{
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		result.add(data);
			
		return result;	
	}

	public Vector getAccountIds(String myAccountId, Vector parameters, String signature)
	{
		class AccountVisitor implements Database.AccountVisitor
		{
			AccountVisitor()
			{
				accounts = new Vector();
			}
	
			public void visit(String accountString)
			{																					
				accounts.add(accountString);
			}
	
			public Vector getAccounts()
			{
				return accounts;
			}
			
			Vector accounts;
		}
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);				
			return result;
		}
					
		AccountVisitor visitor = new AccountVisitor();
		server.getDatabase().visitAllAccounts(visitor);
		result.add(NetworkInterfaceConstants.OK);
	
		result.add(visitor.getAccounts());		
		return result;
	}
	
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId)
	{	
		Vector results = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			results.add(NetworkInterfaceConstants.SIG_ERROR);				
			return results;
		}
		
		File contactFile=null;		
		try
		{
			contactFile = server.getDatabase().getContactInfoFile(accountId);		
			if(!contactFile.exists())
			{

				results.add(NetworkInterfaceConstants.NOT_FOUND);
				return results;
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			results.add(NetworkInterfaceConstants.NOT_FOUND);
			return results;
		}

		try
		{
			Vector contactInfo = MartusServerUtilities.getContactInfo(contactFile);
			if(!server.getSecurity().verifySignatureOfVectorOfStrings(contactInfo, accountId))
			{						
				results.add(NetworkInterfaceConstants.SIG_ERROR);
				return results;
			}
			results.add(NetworkInterfaceConstants.OK);
			results.add(contactInfo);		

			return results;
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}											
	}
	
	public Vector getInactiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();				
		Vector magicWords = server.getMagicWordsInfo().getInactiveMagicWords();
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;					
	}

	public Vector getActiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();				
		Vector magicWords = server.getMagicWordsInfo().getActiveMagicWords();
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);

		return results;					
	}
	
	public Vector getAllMagicWords(String myAccountId)
	{			
		Vector results = new Vector();		

		Vector magicWords = server.getMagicWordsInfo().getAllMagicWords();			
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;		
	}								
	
	public Vector updateMagicWords(String myAccountId, Vector magicWords)
	{			
		return writeMagicWords(magicWords);						
	}								
	
	private Vector writeMagicWords(Vector magicWords)
	{
		Vector results = new Vector();
		try
		{
			server.updateMagicWords(magicWords);								
			results.add(NetworkInterfaceConstants.OK);		
			return results;
		}

		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}				
	}
	
	public Vector getListOfAvailableAccounts(String myAccountId)
	{			
		Vector results = new Vector();		
		File availableDir = MSPAServer.getAvailableMirrorServerDirectory();	
		List list = Arrays.asList(availableDir.list());		
					
		results.add(NetworkInterfaceConstants.OK);
		results.add(new Vector(list));		

		return results;		
	}	
	
	public Vector getListOfAssignedAccounts(String myAccountId, int mirrorType)
	{			
		Vector results = new Vector();		
		File mirrorDir = MSPAServer.getMirrorDirectory(mirrorType);	
		List list = Arrays.asList(mirrorDir.list());		
					
		results.add(NetworkInterfaceConstants.OK);
		results.add(new Vector(list));		

		return results;		
	}	
	
	public Vector updateManagingMirrorServers(String myAccountId, Vector mirrorInfo, int mirrorType)
	{
		Vector results = new Vector();
		try
		{
			server.updateManagingMirrorServerInfo(mirrorInfo, mirrorType);								
			results.add(NetworkInterfaceConstants.OK);		
			return results;
		}

		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}		
	}
	
	public Vector addAvailableMirrorServer(String myAccountId, Vector mirrorInfo)
	{
		Vector results = new Vector();
		try
		{						
			if (mirrorInfo.size() > 0)
			{	
				String ip = (String) mirrorInfo.get(0);
				String publicCode = (String) mirrorInfo.get(1);
				String port = (String) mirrorInfo.get(2);
				String fileName = (String) mirrorInfo.get(3);
				
				File outputFileName = new File(MSPAServer.getAvailableMirrorServerDirectory(), fileName.trim());
				
				RetrievePublicKey retrievePubKey = new RetrievePublicKey(ip, port, publicCode, outputFileName.getPath());
				 
				if (retrievePubKey.isSuccess())
					results.add(NetworkInterfaceConstants.OK);
				else
					results.add(NetworkInterfaceConstants.NO_SERVER);
			}										
		}
		catch (MartusSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Error signing request");
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}		
		return results;		
	}
	
	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return verifier.verifySignatureOfVectorOfStrings(parameters, myAccountId, signature);
	}	
			
	MSPAServer server;
}
