
package org.martus.mspa.network;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.martus.common.ContactInfo;
import org.martus.common.bulletin.BulletinConstants;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.mspa.network.roothelper.Status;
import org.martus.mspa.server.LoadMartusServerArguments;
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
				server.addAuthorizedMartusAccounts(accountString);	
			}
	
			public Vector getAccounts()
			{
				return accounts;
			}
			
			Vector accounts;
		}
				
		Vector result = new Vector();				
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			result.add(NetworkInterfaceConstants.NOT_AUTHORIZED);
			server.log("Client : "+NetworkInterfaceConstants.NOT_AUTHORIZED+" \npublic code: "+ myAccountId);				
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
		
		File contactFile=null;		
		try		
		{
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);							
				return results;
			}

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
			Vector contactInfo = ContactInfo.loadFromFile(contactFile);
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
	
	public Vector getAccountManageInfo(String myAccountId, String manageAccountId)
	{	
		Vector results = new Vector();	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
					
		Vector acccountAdminInfo = server.getAccountAdminInfo(manageAccountId);
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(acccountAdminInfo);		

		return results;					
	}
	
	public Vector updateAccountManageInfo(String myAccountId, String manageAccountId, Vector accountInfo)
	{			
		Vector results = new Vector();
		try
		{
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
			
			server.updateAccountInfo(manageAccountId, accountInfo);								
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
	
	public Vector sendCommandToServer(String myAccountId, String cmdType, String cmd) throws IOException
	{
		Vector results = new Vector();		
		
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
		
		Status status = new Status();
		try
		{				
			if (cmdType.equals(NetworkInterfaceConstants.START_SERVER))
				status = server.getMessenger().startServer("");
			else if (cmdType.equals(NetworkInterfaceConstants.STOP_SERVER))
				status = server.getMessenger().stopServer("");
			else if (cmdType.equals(NetworkInterfaceConstants.READ_WRITE))
				status = server.getMessenger().setReadWrite("");
			else if (cmdType.equals(NetworkInterfaceConstants.READ_ONLY))
				status = server.getMessenger().setReadOnly("");
			else
			{
				results.add(NetworkInterfaceConstants.UNKNOWN_COMMAND);
				return results;
			}													
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
						
								
		if (status.isSuccess())
		{	
			results.add(NetworkInterfaceConstants.OK);
			results.add(status.getStdOutMsg());
		}
		else
		{	
			results.add(NetworkInterfaceConstants.EXEC_ERROR);		
			results.add(status.getAllMessages());
		}	
				
		return results;	
	}
	
	public Vector removeHiddenBulletins(String myAccountId, String manageAccountId, Vector localIds)
	{			
		Vector results = new Vector();
		try
		{	
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
											
			boolean result = server.hideBulletins(manageAccountId, localIds);
			if (result)	
				results.add(NetworkInterfaceConstants.OK);
			else
				results.add(NetworkInterfaceConstants.NOT_FOUND);
	
			return results;
		}
		
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}							
	}	
	
	public Vector recoverHiddenBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException
	{
		Vector results = new Vector();
		try
		{	
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
											
			boolean result = server.recoverHiddenBulletins(manageAccountId, localIds);
			if (result)	
				results.add(NetworkInterfaceConstants.OK);
			else
				results.add(NetworkInterfaceConstants.NOT_FOUND);
	
			return results;
		}
		
		catch (Exception e1)
		{
			e1.printStackTrace();
			results.add(NetworkInterfaceConstants.SERVER_ERROR);
			return results;
		}			
	}

	
	public Vector getListOfHiddenBulletinIds(String myAccountId, String manageAccountId) 
	{		
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}			
											
		results.add(NetworkInterfaceConstants.OK);
		results.add(server.getListOfHiddenBulletins(manageAccountId));		

		return results;		
	}
	
	public Vector getListOfBulletinIds(String myAccountId)
	{			

		class Collector implements Database.PacketVisitor
		{
			public void visit(DatabaseKey key)
			{
				try
				{					
					Vector info = new Vector();						
					String localId = key.getLocalId().trim();	
					if (BulletinHeaderPacket.isValidLocalId(localId))
					{
						if (server.containHiddenBulletin(key.getUniversalId()))
							return;
							
						info.add(key.getLocalId().trim());							
						if (key.isDraft())
							info.add(BulletinConstants.STATUSDRAFT);
						else if (key.isSealed())
							info.add(BulletinConstants.STATUSSEALED);				
	
						infos.add(info);
					}
				}
				catch (Exception e)
				{		
					server.log("ListBulletins: Problem when visited record for account."+ e.toString());
				}
			}
			
			Vector infos = new Vector();
		}
		
		Vector results = new Vector();	

		Collector collector = new Collector();		
		server.getDatabase().visitAllRecordsForAccount(collector, myAccountId);		
											
		results.add(NetworkInterfaceConstants.OK);
		results.add(collector.infos);		

		return results;					
	}
	
	public Vector getServerCompliance(String myAccountId)
	{
		Vector results = new Vector();	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
					
		Vector compliances = server.getComplianceFile(myAccountId);
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(compliances);		

		return results;		
	}
	
	public Vector updateServerCompliance(String myAccountId, String compliantsMsg)
	{		
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		server.updateComplianceFile(myAccountId, compliantsMsg);
		results.add(NetworkInterfaceConstants.OK);
		
		return results;
	}
	
	public Vector getInactiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
						
		Vector magicWords = server.getMagicWordsInfo().getInactiveMagicWordsWithNoSign();
		
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;					
	}

	public Vector getActiveMagicWords(String myAccountId)
	{	
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
						
		Vector magicWords = server.getMagicWordsInfo().getActiveMagicWords();
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);

		return results;					
	}
	
	public Vector getAllMagicWords(String myAccountId)
	{			
		Vector results = new Vector();
		
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}		

		Vector magicWords = server.getMagicWordsInfo().getAllMagicWords();			
		results.add(NetworkInterfaceConstants.OK);
		results.add(magicWords);		

		return results;		
	}								
	
	public Vector updateMagicWords(String myAccountId, Vector magicWords)
	{	
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			Vector results = new Vector();
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
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
	
	public Vector getListOfAvailableServers(String myAccountId)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		File availableDir = MSPAServer.getAvailableMirrorServerDirectory();	
		List list = Arrays.asList(availableDir.list());		
					
		results.add(NetworkInterfaceConstants.OK);
		results.add(new Vector(list));		

		return results;		
	}	
	
	public Vector getListOfAssignedServers(String myAccountId, int mirrorType)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
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
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
			
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
			if (!server.isAuthorizedMSPAClients(myAccountId))
			{
				results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
				return results;
			}
			
			if (mirrorInfo.size() > 0)
			{	
				String ip = (String) mirrorInfo.get(0);
				String publicCode = (String) mirrorInfo.get(1);				
				String fileName = (String) mirrorInfo.get(2);
				
				String port = String.valueOf(server.getPortToUse());
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
	
	public Vector getMartusServerArguments(String myAccountId)
	{			
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
				
		LoadMartusServerArguments arguments = MSPAServer.getMartusServerArguments();	
		results.add(NetworkInterfaceConstants.OK);
		results.add(arguments.convertToVector());		
		return results;		
	}	
	
	public Vector updateMartusServerArguments(String myAccountId, Vector args)
	{
		Vector results = new Vector();
		if (!server.isAuthorizedMSPAClients(myAccountId))
		{
			results.add(NetworkInterfaceConstants.NOT_AUTHORIZED);				
			return results;
		}
		
		try
		{
			server.updateMartusServerArguments(args);					
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
			
	MSPAServer server;
}
