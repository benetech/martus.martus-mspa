package org.martus.mspa.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWords;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.crypto.MartusCrypto.MartusSignatureException;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.packet.UniversalId;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.client.core.AccountAdminOptions;
import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.mspa.network.NetworkInterfaceXmlRpcConstants;
import org.martus.mspa.network.RetrievePublicKey;
import org.martus.mspa.network.ServerSideHandler;
import org.martus.mspa.network.roothelper.FileTransfer;
import org.martus.mspa.network.roothelper.Messenger;
import org.martus.mspa.network.roothelper.RootHelperConnector;
import org.martus.util.UnicodeWriter;


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(File dir) 
	{				
		serverDirectory = dir;	
		authorizedMartusAccounts = new Vector();
		authorizeMSPAClients = new Vector();
		logger = new LoggerToConsole();	
		mspaHandler = new ServerSideHandler(this);								
		initalizeFileDatabase();
	}
	
	public void initConfig()
	{		
		initializedEnvironmentDirectory();				
		loadConfigurationFiles();	
	}
	
	private void setMagicWords() throws Exception
	{
		magicWords = new MagicWords(logger);
		magicWords.loadMagicWords(getMagicWordsFile());
	}			
	
	private void initializedEnvironmentDirectory()
	{			
		try
		{
			log("Initialize environments ...");
			getMessenger().setReadWrite(security.getPublicKeyString());
			getServerWhoWeCallDirectory().mkdirs();
			getMirrorServerWhoCallUsDirectory().mkdirs();
			getMirrorServerWhoWeCallDirectory().mkdirs();
			getAmpsWhoCallUsDirectory().mkdirs();
			getAvailableMirrorServerDirectory().mkdirs();
			getMartusServerDataDirectory().mkdirs();
				
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), MAGICWORDS_FILENAME));
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), UPLOADSOK_FILENAME));		
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME));
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), UPLOADSOK_FILENAME));
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), BANNEDCLIENTS_FILENAME));
			initAccountConfigFiles(new File(getMartusServerDataDirectory(), HIDDEN_PACKETS_FILENAME));
			
			getMessenger().setReadOnly(security.getPublicKeyString());
			log("Completed setting up server environments...\n");	
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			log(" Error when initialized configuration files."+e.toString());
		}		
	}
	
	private void initAccountConfigFiles(File targetFile) throws IOException
	{			
		targetFile.createNewFile();
		log(targetFile.getPath()+" (ready)" );				
	}
	
	private void loadConfigurationFiles()
	{						
		clientsBanned = MartusUtilities.loadBannedClients(getBannedFile());
		clientsAllowedUpload = MartusUtilities.loadCanUploadFile(getAllowUploadFile());		
		clientNotSendToAmplifier = MartusUtilities.loadClientsNotAmplified(getClientsNotToAmplifiyFile());
				
		hiddenBulletins = new HiddenBulletins(getDatabase(),security, getLogger(), getHiddenPacketsFile());
		loadAuthorizedClients();
	}

	private void loadAuthorizedClients() 
	{
		if (!getAuthorizedClientsDir().exists())
		{			
			System.out.println("Warning: missing authorizedclients information.");
			return;
		}	
		
		File[] authorizedDir = getAuthorizedClientsDir().listFiles();	
		log("Load authorized clients now.");
		for (int i=0; i<authorizedDir.length;i++)
		{	
			File authorizedFile = authorizedDir[i];
			if(!authorizedFile.isDirectory())
			{				
				try
				{
					Vector publicInfo = MartusUtilities.importServerPublicKeyFromFile(authorizedFile, security);
					String serverPublicKey = (String)publicInfo.get(0);
					authorizeMSPAClients.add(serverPublicKey);
					log("Client "+i+" "+serverPublicKey);
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (InvalidPublicKeyFileException e)
				{
					// TODO Auto-generated catch block
//					e.printStackTrace();
					log("Error when load "+authorizedFile.getName());
				}
				catch (PublicInformationInvalidException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}			
	}	
	
	private void initalizeFileDatabase()
	{					
		security = loadMSPAKeypair(getMSAPKeypairFileName());
		martusDatabaseToUse = new ServerFileDatabase(getPacketDirectory(), security);		

		try
		{
			martusDatabaseToUse.initialize();
		}
		catch(FileDatabase.MissingAccountMapException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map File");
			System.exit(7);
		}
		catch(FileDatabase.MissingAccountMapSignatureException e)
		{
			e.printStackTrace();
			System.out.println("Missing Account Map Signature File");
			System.exit(7);
		}
		catch(FileVerificationException e)
		{
			e.printStackTrace();
			System.out.println("Account Map did not verify against signature file");
			System.exit(7);
		}		
	}	
	
	public MartusCrypto loadMSPAKeypair(String keyPairFileName)
	{
		return MartusServerUtilities.loadKeyPair(keyPairFileName, true);		
	}	
	
	public static File getMSPADeleteOnStartup()
	{
		return new File(getAppDirectoryPath(),DELETE_ON_STARTUP);
	}
	
	public static File getAuthorizedClientsFile()
	{
		return new File(getAppDirectoryPath(), MSPA_CLIENT_AUTHORIZED_FILES);
	}	

	public static File getAuthorizedClientsDir()
	{
		return new File(getMSPADeleteOnStartup(),MSPA_CLIENT_AUTHORIZED_DIR );
	}
	
	public File getMSPAServerKeyPairFile()
	{
		return new File(getMSPADeleteOnStartup().getPath(), KEYPAIR_FILE);
	}
	
	public String getMSAPKeypairFileName()
	{
		return getMSPAServerKeyPairFile().getPath();
	}
	
	public File getBannedFile()
	{
		return new File(getMartusServerDataDirectory(), BANNEDCLIENTS_FILENAME);
	}
	
	public File getAllowUploadFile()
	{
		return new File(getMartusServerDataDirectory(), UPLOADSOK_FILENAME);
	}
	
	public File getClientsNotToAmplifiyFile()
	{
		return new File(getMartusServerDataDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getMartusServerDataDirectory(), MAGICWORDS_FILENAME);		
	}	
	
	public File getPacketDirectory()
	{
		return new File(getServerDirectory(), "packets");
	}
	
	public File getServerDirectory()
	{
		return serverDirectory;
	}		
	
	private File getHiddenPacketsFile()
	{
		return new File(getMartusServerDataDirectory(), HIDDEN_PACKETS_FILENAME);
	}	
	
	public static File getServerWhoWeCallDirectory()
	{
		return new File(getMartusServerDataDirectory(),"serversWhoWeCall");
	}
	
	public static File getMirrorServerWhoCallUsDirectory()
	{
		return new File(getMartusServerDataDirectory(),"mirrorsWhoCallUs");
	}
	
	public static File getMirrorServerWhoWeCallDirectory()
	{
		return new File(getMartusServerDataDirectory(),"mirrorsWhoWeUs");
	}
	
	public static File getAmpsWhoCallUsDirectory()
	{
		return new File(getMartusServerDataDirectory(),"ampsWhoCallUs");
	}
	
	public static File getAvailableMirrorServerDirectory()
	{
		return new File(getMartusServerDataDirectory(),"AvailableMirrorServers");
	}		
	
	public File getMartusServerDataComplianceFile()
	{
		return new File(getMartusServerDataDirectory(),COMPLIANCE_FILE );
	}	
	
	public static File getMartusServerDataDirectory()
	{
		return new File(getAppDirectoryPath(),MARTUS_SERVER_DATA);
	}
	
	public static File getMartusServerDataBackupDirectory()
	{
		return new File(getMartusServerDataDirectory(),MARTUSSERVER_BACKUP_DIRECTORY);
	}	
	
	public MagicWords getMagicWordsInfo()
	{
		return magicWords;
	}
	
	public synchronized Vector getComplianceFile(String accountId)
	{
		Vector results = new Vector();
		File complianceFile = getMartusServerDataComplianceFile();	
		try
		{
			if (complianceFile.createNewFile())				
				return new Vector();
			results = FileTransfer.readDataFromFile(complianceFile);
		}
		catch (RemoteException e)
		{
			e.printStackTrace();
			log(" Error when try to get a compliance file."+e.toString());
		}
		catch (IOException e)
		{
			e.printStackTrace();
			log(" Error when try to get a compliance file."+e.toString());
		}
		
		return results;	
	}

	public synchronized void updateComplianceFile(String accountId, String compliantsMsg)
	{		
		File complianceFile = getMartusServerDataComplianceFile();		
		try
		{
		
			logAction("Update compliance file", compliantsMsg);			
			getMessenger().setReadWrite(security.getPublicKeyString());
			backupFile(complianceFile);
						
			UnicodeWriter writer = new UnicodeWriter(complianceFile);
			writer.writeln(compliantsMsg);
			writer.close();
			
			getMessenger().setReadOnly(security.getPublicKeyString());	
		}
		catch (RemoteException e)
		{			
			e.printStackTrace();
			log(" Error when try to update a compliance file."+e.toString());
		}
		catch (IOException e)
		{			
			e.printStackTrace();
			log(" Error when try to update a compliance file."+e.toString());
		}	
	}
	
	private void writeHiddenBulletinToFile()
	{
		try
		{		
			getMessenger().setReadWrite(security.getPublicKeyString());			
			backupFile(getHiddenPacketsFile());
						
			UnicodeWriter writer = new UnicodeWriter(getHiddenPacketsFile());							
			for (int aId = 0; aId < authorizedMartusAccounts.size();++aId)
			{	
				String currentAccountId = (String) authorizedMartusAccounts.get(aId);
				writer.writeln(currentAccountId);	 	
				hiddenBulletins.writeLineOfHiddenBulletinsToFile(currentAccountId, writer);
			}
			writer.close();
			getMessenger().setReadOnly(security.getPublicKeyString());	
		}
		catch (Exception ieo)
		{	
			log("Unable to read/write isHidden.txt."+ ieo.toString());		
		}			
	}
	
	public boolean containHiddenBulletin(UniversalId uid)
	{
		return hiddenBulletins.containHiddenUids(uid);
	}
	
	public synchronized boolean hideBulletins(String accountId, Vector localIds)
	{	
		hiddenBulletins.hideBulletins(accountId, localIds);
		logActions("Hide Bulletins", localIds);			
		writeHiddenBulletinToFile();		

		return true;
	}
	
	public synchronized boolean recoverHiddenBulletins(String accountId, Vector localIds)
	{	
		if (!hiddenBulletins.recoverHiddenBulletins(accountId, localIds))
			return false;

		logActions("Recover Bulletins", localIds);				
		writeHiddenBulletinToFile();	
		return true;
	}
	
	public Vector getListOfHiddenBulletins(String accountId)
	{
		return hiddenBulletins.getListOfHiddenBulletins(accountId);
	}
	
	public synchronized boolean addAvailableServer(Vector mirrorInfo)
	{	
		if (mirrorInfo.size() > 0)
			return false;

		boolean success=true;
		try 
		{				
			String ip = (String) mirrorInfo.get(0);
			String publicCode = (String) mirrorInfo.get(1);				
			String fileName = (String) mirrorInfo.get(2);			
			String port = String.valueOf(getPortToUse());

			logActions("Add New Server<dir>"+ fileName, mirrorInfo);					 
			getMessenger().setReadWrite(security.getPublicKeyString());					
			
			File outputFileName = new File(getAvailableMirrorServerDirectory(), fileName.trim());
			RetrievePublicKey retrievePubKey = new RetrievePublicKey(ip, port, publicCode, outputFileName.getPath());				 
			getMessenger().setReadOnly(security.getPublicKeyString());
			
			success = retrievePubKey.isSuccess();
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			log("(Add New Server) Problem when try to create file: "+ e.toString());
		}
		catch (MartusSignatureException e)
		{
			e.printStackTrace();
			log("MartusSignatureException: when tried to generate a publickey"+e.getMessage());
			return false;
		}	

		return success;
	}	
	
	public synchronized void updateAssignedServerInfo(Vector mirrorInfo, int mirrorType)
	{
		File sourceDirectory = MSPAServer.getAvailableMirrorServerDirectory();
		File destDirectory = MSPAServer.getMirrorDirectory(mirrorType);		
		
		try 
		{				
			logActions("Update Other Server configuration <dir>"+destDirectory.getName(), mirrorInfo);					 
			getMessenger().setReadWrite(security.getPublicKeyString());					
			deleteAllFilesFromMirrorDirectory(destDirectory.listFiles());
			
			for (int i =0; i<mirrorInfo.size();i++)
			{
				String file = (String) mirrorInfo.get(i);
				FileTransfer.copyFile(new File(sourceDirectory, file), new File(destDirectory, file));
			}	
			getMessenger().setReadOnly(security.getPublicKeyString());
		}
		catch (IOException e) 
		{
			e.printStackTrace();
			log("(Update Mirror Server) Problem when try to update/copy files: "+ e.toString());
		}	
	}	
	
	void deleteAllFilesFromMirrorDirectory(File[] files)
	{
		for (int i=0;i<files.length;i++)
			files[i].delete();
	}
	
	FileTransfer getFileTransfer(File from, File to)
	{
		return new FileTransfer(from.getPath(), to.getPath());		
	}	
	
	public synchronized void updateMagicWords(Vector words)
	{				
		try
		{					
			logActions("Update MagicWords", words);				
			getMessenger().setReadWrite(security.getPublicKeyString());
								
			backupFile(getMagicWordsFile());
			magicWords.writeMagicWords(getMagicWordsFile(), words);
			magicWords.loadMagicWords(getMagicWordsFile());
			
			getMessenger().setReadOnly(security.getPublicKeyString());							
		}
		catch (IOException ieo)
		{	
			log("MagicWord.txt file not found."+ ieo.toString());		
		}	
	}	
	
	public Vector getAccountAdminInfo(String manageAccountId)
	{		
		AccountAdminOptions options = new AccountAdminOptions();
		options.setCanSendOption(!isAccountNotSendToAmplifier(manageAccountId));
		options.setBannedOption(isAccountBanned(manageAccountId));
		options.setCanUploadOption(isAccountAllowedUpload(manageAccountId));
		
		return options.getOptions();
	}
	
	public synchronized void updateAccountInfo(String manageAccountId, Vector accountInfo)
	{			
		AccountAdminOptions options = new AccountAdminOptions();
		options.setOptions(accountInfo);

		updateBannedAccount(options.isBannedSelected(), manageAccountId);
		updateAccountAllowedUpload(options.canUploadSelected(), manageAccountId);
		updateAccountSendToAmplifier(options.canSendToAmplifySelected(), manageAccountId);
	
		updateAccountConfigFiles();								
	}	
	
	private void updateAccountConfigFiles()
	{
		writeListToFile(getBannedFile(), clientsBanned);
		writeListToFile(getAllowUploadFile(), clientsAllowedUpload);
		writeListToFile(getClientsNotToAmplifiyFile(), clientNotSendToAmplifier);
	}
	
	private void writeListToFile(File file, Vector list)
	{
		try
		{

			getMessenger().setReadWrite(security.getPublicKeyString());			
		
			backupFile(file);
			MartusUtilities.writeListToFile(file, list);
			
			getMessenger().setReadOnly(security.getPublicKeyString());			
		}
		catch (Exception ieo)
		{	
			log(file.getPath()+" file not found."+ ieo.toString());		
		} 
	}	
	
	private void updateBannedAccount(boolean isSelected, String accountId)
	{			
		if (isSelected)
		{							
			addBannedAccount(accountId);
			logAction("Add banned account ", accountId);				
		}
		else
		{				
			clientsBanned.remove(accountId);
			logAction("Remove banned account ", accountId);				
		}
	}
	
	private void updateAccountAllowedUpload(boolean isSelected, String accountId)
	{
		if (isSelected)	
		{						
			addAccountAllowedUpload(accountId);
			logAction("Add allowed upload account ", accountId);			
		}
		else
		{	
			clientsAllowedUpload.remove(accountId);
			logAction("Remove allowed upload account ", accountId);
		}	
	}
	
	private void updateAccountSendToAmplifier(boolean isSelected, String accountId)
	{
		if (isSelected)
		{					
			clientNotSendToAmplifier.remove(accountId);
			logAction("Remove <mirrorsWhoWeCall> from directory", accountId);				
		}	
		else			
		{	
			addAccountNotSendToAmplifier(accountId);
			logAction("Add <mirrorsWhoWeCall> to directory", accountId);
		}
	}
	
	private void addBannedAccount(String clientId)
	{
		if (!isAccountBanned(clientId))
			clientsBanned.add(clientId);
	}
	
	private void addAccountAllowedUpload(String clientId)
	{
		if (!isAccountAllowedUpload(clientId))
			clientsAllowedUpload.add(clientId);
	}
	
	private void addAccountNotSendToAmplifier(String clientId)
	{
		if (!isAccountNotSendToAmplifier(clientId))
			clientNotSendToAmplifier.add(clientId);
	}
	
	public boolean isAccountBanned(String clientId)
	{
		return clientsBanned.contains(clientId);
	}	
	
	public boolean isAccountAllowedUpload(String clientId)
	{
		return clientsAllowedUpload.contains(clientId);
	}
	
	public boolean isAccountNotSendToAmplifier(String clientId)
	{
		return clientNotSendToAmplifier.contains(clientId);
	}	
	
	public void createMSPAXmlRpcServerOnPort(int port) throws Exception
	{				
		MartusSecureWebServer.security = getSecurity();
		MartusXmlRpcServer.createSSLXmlRpcServer(getMSPAHandler(),serverObjectName, port, getMainIpAddress());
	}
	
	public ServerSideHandler getMSPAHandler()
	{
		return mspaHandler;
	}
	
	private void backupFile(File from) throws IOException
	{			
		Date today = new Date();		

		String file = from.getName()+"."+getBackupFileExtension(today);
		File backupFile = new File(getMartusServerDataBackupDirectory(), file);
		if (!backupFile.exists())			
		{
			deletePreviousBackupFile(from.getName());	
			FileTransfer.copyFile(from, backupFile);
		}
	}
	
	private String getBackupFileExtension(Date today)
	{
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(today);
		int year = calendar.get(GregorianCalendar.YEAR);
		int month = calendar.get(GregorianCalendar.MONTH);
		int day = calendar.get(GregorianCalendar.DAY_OF_MONTH);
		
		return Integer.toString(year)+Integer.toString(month+1)+Integer.toString(day);
	}	
	
	private void deletePreviousBackupFile(String targetFileName)
	{		
		File backupDir = getMartusServerDataBackupDirectory();
		File[] files = backupDir.listFiles();
		for (int i=0; i<files.length;++i)
		{			
			String filename = files[i].getName();
			if (filename.startsWith(targetFileName))
				files[i].delete();
		}					
	}
	
	private void logActions(String action, Vector data)
	{
		String actionMsg = "["+action+"]: "; 
		StringBuffer recordMsg = new StringBuffer();
		recordMsg.append(actionMsg).append("\n");
		for (int i=0;i<data.size();++i)
		{
			recordMsg.append("("+i+")").append((String)data.get(i)).append("\n");
		}
		log(recordMsg.toString());		
	}
	
	private void logAction(String action, String msg)
	{
		String actionMsg = "["+action+"]: "; 
		StringBuffer recordMsg = new StringBuffer();
		recordMsg.append(actionMsg).append(msg);
		log(recordMsg.toString());		
	}
	
	public LoggerInterface getLogger()
	{
		return logger;
	}	
	
	public InetAddress getMainIpAddress() throws UnknownHostException
	{
		return InetAddress.getByName(ipAddress);
	}
	
	public MartusCrypto getSecurity()
	{
		return security;
	}
	
	public ServerFileDatabase getDatabase()
	{		
		return martusDatabaseToUse;
	}
	
	boolean isAuthorizedMSPAClient(String myAccountId)
	{
		return authorizedMartusAccounts.contains(myAccountId);
	}
	
	public String ping()
	{
		return "" + NetworkInterfaceConstants.VERSION;
	}
	
	public static File getMartusDefaultDataDirectory()
	{
		return new File(MSPAServer.getMartusDefaultDataDirectoryPath());
	}	
	
	public static String getMartusDefaultDataDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
			dataDirectory = WINDOW_MARTUS_ENVIRONMENT;
		else
			dataDirectory = System.getProperty("user.home")+UNIX_MARTUS_ENVIRONMENT;
		return dataDirectory;
	}
	
	
	public static File getAppDirectoryPath()
	{
		String appDirectory = null;
		if(Version.isRunningUnderWindows())
			appDirectory = WINDOW_MSPA_ENVIRONMENT;
		else
			appDirectory = System.getProperty("user.home")+UNIX_MSPA_ENVIRONMENT;
		return new File(appDirectory);
	}	
	
	public void setPortToUse(int port)
	{
		portToUse = port;
	}

	public int getPortToUse()
	{
		return (portToUse <= 0)? DEFAULT_PORT:portToUse;
	}
	
	public void setRootHelperPortToUse(int port)
	{
		rootHelperPortToUse = port;
	}

	public int getRootHelperPortToUse()
	{
		return (rootHelperPortToUse <= 0)? ROOTHELPER_DEFAULT_PORT:rootHelperPortToUse;
	}

	public synchronized void addAuthorizedMartusAccounts(String authorizedClientId)
	{
		if (!isAuthorizedMartusAccounts(authorizedClientId))
			authorizedMartusAccounts.add(authorizedClientId);
	}
	
	public synchronized void addAuthorizedMSPAClients(String authorizedClientId)
	{
		if (!isAuthorizedMSPAClients(authorizedClientId))
			authorizeMSPAClients.add(authorizedClientId);
	}
	
	public boolean isAuthorizedMSPAClients(String authorizedClientId)
	{
		return authorizeMSPAClients.contains(authorizedClientId);
	}

	public boolean isAuthorizedMartusAccounts(String authorizedClientId)
	{
		return authorizedMartusAccounts.contains(authorizedClientId);
	}
	
	public Vector getAuthorizedMartusAccounts()
	{
		return authorizedMartusAccounts;
	}

	public void setListenersIpAddress(String ipAddr) 
	{
		ipAddress = ipAddr;
	}
	
	public synchronized void log(String message)
	{
		getLogger().log(message);
	}

	public synchronized void updateMartusServerArguments(Vector props)
	{
		File propertyFile = new File(getMSPADeleteOnStartup(), MARTUS_ARGUMENTS_PROPERTY_FILE);
		LoadMartusServerArguments args = new LoadMartusServerArguments();
		args.convertFromVector(props);
		args.writePropertyFile(propertyFile.getPath());
	}
	
	public synchronized static LoadMartusServerArguments getMartusServerArguments()
	{
		File propertyFile = new File(getMSPADeleteOnStartup(), MARTUS_ARGUMENTS_PROPERTY_FILE);
		LoadMartusServerArguments property = null;

		try
		{
			if (propertyFile.createNewFile())
			{				
				property = loadDefaultMartusServerArguments(propertyFile.getPath());			
			}
			else
				property = new LoadMartusServerArguments( propertyFile.getPath());
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
						
		return property;
	}	

	public synchronized static LoadMartusServerArguments loadDefaultMartusServerArguments(String propertyFile)
	{
		LoadMartusServerArguments property = new LoadMartusServerArguments(propertyFile);

		property.setProperty(LoadMartusServerArguments.LISTENER_IP,"");
		property.setProperty(LoadMartusServerArguments.PASSWORD,"no");
		property.setProperty(LoadMartusServerArguments.AMPLIFIER_IP,"");
		property.setProperty(LoadMartusServerArguments.AMPLIFIER_INDEXING_MINUTES,"5");
		property.setProperty(LoadMartusServerArguments.AMPLIFIER,"no");
		property.setProperty(LoadMartusServerArguments.CLIENT_LISTENER,"no");
		property.setProperty(LoadMartusServerArguments.MIRROR_LISTENER,"no");
		property.setProperty(LoadMartusServerArguments.AMPLIFIER_LISTENER,"no");	

		property.writePropertyFile(propertyFile);
		
		return property;
	}
	
	public boolean deleteStartupFiles()
	{
		if(!isSecureMode())
			return true;
			
		Vector deleteList = new Vector();	
		File[] startupFiles = getMSPADeleteOnStartup().listFiles();
		for (int i=0; i<startupFiles.length;++i)			
			deleteList.add(startupFiles[i]);
				
		MartusUtilities.deleteAllFiles(deleteList);
		
		File[] remainingStartupFiles = getMSPADeleteOnStartup().listFiles();
		if(remainingStartupFiles.length != 0)
		{
			log("Files still exist in the folder: " + getMSPADeleteOnStartup().getAbsolutePath());
			return false;
		}
		return true;
	}

	public void enterSecureMode()
	{
		secureMode = true;
	}

	public boolean isSecureMode()
	{
		return secureMode;
	}
	
	private void processCommandLine(String[] args) 
	{	
		String listenersIpTag = "--listener-ip=";	
		String portToListenTag = "--port=";
		String secureModeTag = "--secure";
		String rootPortTag = "--roothelper-port=";		
		
		System.out.println("");
		for(int arg = 0; arg < args.length; ++arg)
		{
			String argument = args[arg];
			
			if(argument.startsWith(listenersIpTag))
			{	
				String ip = argument.substring(listenersIpTag.length());
				setListenersIpAddress(ip);
				System.out.println("Listener IP to use: "+ ip);
			}
				
			if(argument.startsWith(portToListenTag))
			{	
				String portToUse = argument.substring(portToListenTag.length());
				setPortToUse(Integer.parseInt(portToUse));	
				System.out.println("Port to use for clients: "+ getPortToUse());
			}
			
			if(argument.startsWith(rootPortTag))
			{	
				String portToUse = argument.substring(rootPortTag.length());
				setRootHelperPortToUse(Integer.parseInt(portToUse));	
				System.out.println("Port to use for connect to RootHelper: "+ getRootHelperPortToUse());
			}
						
			if(argument.equals(secureModeTag))
				enterSecureMode();			
				
		}
		
		if (isSecureMode())
			System.out.println("Running in SECURE mode");
		else
			System.out.println("Running in INSECURE mode");
			
		System.out.println("");
	}

	private void setRootHelperConnector() throws UnknownHostException, MalformedURLException, RemoteException, NotBoundException
	{		
		rootConnector = new RootHelperConnector(getRootHelperPortToUse());	
		log(rootConnector.getMessenger().getInitMsg());
	}
	
	public Messenger getMessenger()
	{
		return rootConnector.getMessenger();
	}
	
	public static File getMirrorDirectory(int type)
	{		
		if (type == ManagingMirrorServerConstants.SERVERS_WHOSE_DATA_WE_BACKUP)
			return getServerWhoWeCallDirectory();
		else if (type == ManagingMirrorServerConstants.SERVERS_WHO_BACKUP_OUR_DATA)
			return getMirrorServerWhoCallUsDirectory();
		else if (type == ManagingMirrorServerConstants.SERVERS_WHO_AMPLIFY_OUR_DATA)
			return getAmpsWhoCallUsDirectory();	

		return getMirrorServerWhoWeCallDirectory();		
	}	
	
	public static void main(String[] args)
	{
		System.out.println("MSPA Server");
		try
		{					
			System.out.println("Setting up socket connection for listener ...");
			
			MSPAServer server = new MSPAServer(MSPAServer.getMartusDefaultDataDirectory());
			server.processCommandLine(args);
			server.setMagicWords();			
			server.createMSPAXmlRpcServerOnPort(server.getPortToUse());	
			server.setRootHelperConnector();
			server.initConfig();																			
			System.out.println("\nWaiting for connection...");
						
			if(!server.deleteStartupFiles())
				System.exit(5);		
		
		}
		catch(Exception e) 
		{
			System.out.println("Exception: " + e);
			System.exit(1);			
		}
	}	
		
	ServerSideHandler mspaHandler;
	String ipAddress;
	int portToUse;
	Vector authorizedMartusAccounts;
	Vector authorizeMSPAClients;
	ServerFileDatabase martusDatabaseToUse;	
	MartusCrypto security;
	LoggerInterface logger;
	MagicWords magicWords;
	Vector clientsBanned;
	Vector clientsAllowedUpload;
	Vector clientNotSendToAmplifier;
	HiddenBulletins hiddenBulletins;
	RootHelperConnector rootConnector;	
		
	private File serverDirectory;
	private boolean secureMode;
	private int rootHelperPortToUse;	
		
	private final static String DELETE_ON_STARTUP = "deleteOnStartup";	
	private final static String MARTUSSERVER_BACKUP_DIRECTORY = "Backups";
	private final static String MARTUS_SERVER_DATA = "MartusServerData";	
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";
	private static final String BANNEDCLIENTS_FILENAME = "banned.txt";
	private static final String UPLOADSOK_FILENAME = "uploadsok.txt";
	private static final String HIDDEN_PACKETS_FILENAME = "isHidden.txt";
	private static final String CLIENTS_NOT_TO_AMPLIFY_FILENAME = "clientsNotToAmplify.txt";
	private static final String COMPLIANCE_FILE =  "compliance.txt";
	private static final String MARTUS_ARGUMENTS_PROPERTY_FILE = "serverarguments.props";
	private static final String MSPA_CLIENT_AUTHORIZED_DIR = "authorizedClients"; 
	private static final String MSPA_CLIENT_AUTHORIZED_FILES = "authorizedClients.txt"; 

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_MARTUS_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_MARTUS_ENVIRONMENT = "/var/MartusServer/";
	private final static String WINDOW_MSPA_ENVIRONMENT = "C:/MSPAServer/";
	private final static String UNIX_MSPA_ENVIRONMENT = "/var/MSPAServer/";
	
	private final static int DEFAULT_PORT = 984;
	private final static int ROOTHELPER_DEFAULT_PORT=983;
	
}
