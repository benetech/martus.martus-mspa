package org.martus.mspa.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
import org.martus.mspa.network.ServerSideHandler;
import org.martus.mspa.network.roothelper.FileTransfer;
import org.martus.mspa.network.roothelper.RootHelperConnector;
import org.martus.util.UnicodeWriter;


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(File dir) throws Exception
	{				
		serverDirectory = dir;	
		authorizedMartusAccounts = new Vector();
		authorizeMSPAClients = new Vector();
		mspaHandler = new ServerSideHandler(this);								
		initalizeFileDatabase(dir);
		initializedEnvironmentDirectory();
				
		logger = new LoggerToConsole();	
		magicWords = new MagicWords(logger);
		magicWords.loadMagicWords(getMagicWordsFile());	
		loadConfigurationFiles();	
	}	
	
	private void initializedEnvironmentDirectory()
	{
		getServerWhoWeCallDirectory().mkdirs();
		getServerWhoCallUsDirectory().mkdirs();
		getAmplifyWhoCallUsDirectory().mkdirs();
		getAvailableMirrorServerDirectory().mkdirs();
		getDeleteOnStartupDirectory().mkdirs();
		
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), MAGICWORDS_FILENAME));
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), UPLOADSOK_FILENAME));		
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME));
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), UPLOADSOK_FILENAME));
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), BANNEDCLIENTS_FILENAME));
		initAccountConfigFiles(new File(getDeleteOnStartupDirectory(), HIDDEN_PACKETS_FILENAME));
	}
	
	private void initAccountConfigFiles(File targetFile)
	{
		try
		{
			targetFile.createNewFile();
		}
		catch (IOException e)
		{
			e.printStackTrace();			
			log(targetFile.getPath()+" error when created."+e.toString());
		}		
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
		File[] authorizedDir = getAuthorizedClientsDir().listFiles();		
		log("Load authorized clients now.");
		for (int i=0; i<authorizedDir.length;i++)
		{	
			File authorizedFile = authorizedDir[i];
			if(!authorizedFile.isDirectory())
			{
				Vector publicInfo;
				try
				{
					publicInfo = MartusUtilities.importServerPublicKeyFromFile(authorizedFile, security);
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
	
	private void initalizeFileDatabase(File dir)
	{					
		security = loadMartusKeypair(getMSAPKeypairFileName());
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
	
	public MartusCrypto loadMartusKeypair(String keyPairFileName)
	{
		return MartusServerUtilities.loadKeyPair(keyPairFileName, true);		
	}
	
	public static File getAdminDirectory()
	{
		return new File(getAppDirectoryPath(),MSPA_ADMIN_DIR);
	}
	
	public static File getAdminDeleteOnStartup()
	{
		return new File(getAdminDirectory(),DELETE_ON_STARTUP_DIRECTORY);
	}
	
	public static File getAuthorizedClientsFile()
	{
		return new File(getAdminDirectory(), MSPA_CLIENT_AUTHORIZED_FILES);
	}

	public static File getAuthorizedClientsDir()
	{
		return new File(getAdminDirectory(),MSPA_CLIENT_AUTHORIZED_DIR );
	}
	
	public File getMSPAServerKeyPairFile()
	{
		File adminDeleteOnStartup = new File(getAdminDirectory().getPath(), DELETE_ON_STARTUP_DIRECTORY);
		File keypair = new File(adminDeleteOnStartup.getPath(), KEYPAIR_FILE);
		return keypair;
	}
	
	public String getMSAPKeypairFileName()
	{
		return getMSPAServerKeyPairFile().getPath();
	}
	
	public File getBannedFile()
	{
		return new File(getDeleteOnStartupDirectory(), BANNEDCLIENTS_FILENAME);
	}
	
	public File getAllowUploadFile()
	{
		return new File(getDeleteOnStartupDirectory(), UPLOADSOK_FILENAME);
	}
	
	public File getClientsNotToAmplifiyFile()
	{
		return new File(getDeleteOnStartupDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getDeleteOnStartupDirectory(), MAGICWORDS_FILENAME);		
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
		return new File(getDeleteOnStartupDirectory(), HIDDEN_PACKETS_FILENAME);
	}	
	
	public static File getServerWhoWeCallDirectory()
	{
		return new File(getDeleteOnStartupDirectory(),"serversWhoWeCall");
	}
	
	public static File getServerWhoCallUsDirectory()
	{
		return new File(getDeleteOnStartupDirectory(),"serversWhoCallUs");
	}
	
	public static File getAmplifyWhoCallUsDirectory()
	{
		return new File(getDeleteOnStartupDirectory(),"amplifyWhoCallUs");
	}
	
	public static File getAvailableMirrorServerDirectory()
	{
		return new File(getDeleteOnStartupDirectory(),"availableMirrorServers");
	}	
	
	public File getMartusComplianceFile()
	{
		return new File(getMartusConfigDirectory(),COMPLIANCE_FILE );
	}	
	
	public File getMSPAComplianceFile()
	{
		return new File(getDeleteOnStartupDirectory(),COMPLIANCE_FILE );
	}	
	
	public static File getDeleteOnStartupDirectory()
	{
		return new File(getAppDirectoryPath(),DELETE_ON_STARTUP_DIRECTORY);
	}
	
	public static File getDeleteOnStartupBackupDirectory()
	{
		return new File(getAppDirectoryPath(),DELETEONSTARTUP_BACKUP_DIRECTORY);
	}
	
	public File getMartusConfigDirectory()
	{
		return new File(getServerDirectory(),DELETE_ON_STARTUP_DIRECTORY);
	}
	
	public MagicWords getMagicWordsInfo()
	{
		return magicWords;
	}
	
	public synchronized Vector getComplianceFile(String accountId)
	{
		Vector results = new Vector();
		File complianceFile = getMSPAComplianceFile();
		File martusComplianceFile = getMartusComplianceFile();

		try
		{
			if (!complianceFile.exists())
			{
				rootConnector.getMessenger().getAdminFile(accountId, 
					martusComplianceFile.getPath(),complianceFile.getPath());
			}
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
			logger.log(" Error when try to get a compliance file."+e.toString());
		}
		
		return results;	
	}

	public synchronized void updateComplianceFile(String accountId, String compliantsMsg)
	{		
		File complianceFile = getMSPAComplianceFile();
		File martusComplianceFile = getMartusComplianceFile();
		try
		{
			UnicodeWriter writer = new UnicodeWriter(complianceFile);
			writer.writeln(compliantsMsg);
			writer.close();

			if (complianceFile.exists())
			{
				FileTransfer transfer = new FileTransfer(complianceFile.getPath(),martusComplianceFile.getPath() );
				Vector transfers = new Vector();	
				transfers.add(transfer);			
			}
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
			File backUpFile = new File(getDeleteOnStartupBackupDirectory().getPath(), HIDDEN_PACKETS_FILENAME);				
			FileTransfer.copyFile(getHiddenPacketsFile(), backUpFile);
						
			UnicodeWriter writer = new UnicodeWriter(getHiddenPacketsFile());							
			for (int aId = 0; aId < authorizedMartusAccounts.size();++aId)
			{	
				String currentAccountId = (String) authorizedMartusAccounts.get(aId);
				writer.writeln(currentAccountId);	 	
				hiddenBulletins.writeLineOfHiddenBulletinsToFile(currentAccountId, writer);
			}
			writer.close();	
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
		writeHiddenBulletinToFile();		

		return true;
	}
	
	public synchronized boolean recoverHiddenBulletins(String accountId, Vector localIds)
	{	
		if (!hiddenBulletins.recoverHiddenBulletins(accountId, localIds))
			return false;
			
		writeHiddenBulletinToFile();	
		return true;
	}
	
	public Vector getListOfHiddenBulletins(String accountId)
	{
		return hiddenBulletins.getListOfHiddenBulletins(accountId);
	}
	
	public synchronized void updateManagingMirrorServerInfo(Vector mirrorInfo, int mirrorType)
	{
		File sourceDirectory = MSPAServer.getAvailableMirrorServerDirectory();
		File destDirectory = MSPAServer.getMirrorDirectory(mirrorType);	
		deleteAllFilesFromMirrorDirectory(destDirectory.listFiles());
		
		try 
		{			 
			for (int i =0; i<mirrorInfo.size();i++)
			{
				String file = (String) mirrorInfo.get(i);
				FileTransfer.copyFile(new File(sourceDirectory, file), new File(destDirectory, file));
			}	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			log("(Update Mirror Server) Problem when try to update/copy files: "+ e.toString());
		}	
	}
	
	public boolean sendCmdToStartServer(String cmdType, String cmd)
	{
		boolean result = true;
		if (cmdType.equals(NetworkInterfaceConstants.START_SERVER))
		{						
			try
			{				
				//send cmd to root helper...
				rootConnector.getMessenger().startServer("");
			}
			catch (RemoteException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		
		return result;
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
			File backUpFile = new File(getDeleteOnStartupBackupDirectory(),getMagicWordsFile().getName() );			
			FileTransfer.copyFile(getMagicWordsFile(), backUpFile);
			magicWords.writeMagicWords(getMagicWordsFile(), words);
			magicWords.loadMagicWords(getMagicWordsFile());							
		}
		catch (Exception ieo)
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
			File backUpFile = new File(getDeleteOnStartupBackupDirectory(), file.getName());			
			FileTransfer.copyFile(file, backUpFile);
			MartusUtilities.writeListToFile(file, list);
		}
		catch (Exception ieo)
		{	
			logger.log(file.getPath()+" file not found."+ ieo.toString());		
		}
	}	
	
	private void updateBannedAccount(boolean isSelected, String accountId)
	{			
		if (isSelected)						
			addBannedAccount(accountId);
		else			
			clientsBanned.remove(accountId);
	}
	
	private void updateAccountAllowedUpload(boolean isSelected, String accountId)
	{
		if (isSelected)						
			addAccountAllowedUpload(accountId);
		else
			clientsAllowedUpload.remove(accountId);
	}
	
	private void updateAccountSendToAmplifier(boolean isSelected, String accountId)
	{
		if (isSelected)		
			clientNotSendToAmplifier.remove(accountId);	
		else			
			addAccountNotSendToAmplifier(accountId);
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
		File propertyFile = new File(getDeleteOnStartupDirectory(), MARTUS_ARGUMENTS_PROPERTY_FILE);
		LoadMartusServerArguments args = new LoadMartusServerArguments();
		args.convertFromVector(props);
		args.writePropertyFile(propertyFile.getPath());
	}
	
	public synchronized static LoadMartusServerArguments getMartusServerArguments()
	{
		File propertyFile = new File(getDeleteOnStartupDirectory(), MARTUS_ARGUMENTS_PROPERTY_FILE);
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
		File[] startupFiles = getAdminDeleteOnStartup().listFiles();
		for (int i=0; i<startupFiles.length;++i)			
			deleteList.add(startupFiles[i]);
				
		MartusUtilities.deleteAllFiles(deleteList);
		
		File[] remainingStartupFiles = getAdminDeleteOnStartup().listFiles();
		if(remainingStartupFiles.length != 0)
		{
			log("Files still exist in the folder: " + getAdminDeleteOnStartup().getAbsolutePath());
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

			System.out.println("");
			if(argument.equals(secureModeTag))
			{
				System.out.println("Running in SECURE mode");
				enterSecureMode();
			}
			else
				System.out.println("***RUNNING IN INSECURE MODE***");
		}
		System.out.println("");
	}

	private void setRootHelperConnector() throws UnknownHostException, MalformedURLException, RemoteException, NotBoundException
	{
		InetAddress iNetAddr = InetAddress.getByName(ipAddress);
		rootConnector = new RootHelperConnector(iNetAddr.getHostName());	
	}
	
	public static File getMirrorDirectory(int type)
	{		
		if (type == ManagingMirrorServerConstants.SERVER_WHO_WE_CALL)
			return getServerWhoWeCallDirectory();
		
		if (type == ManagingMirrorServerConstants.WHO_CALLS_US)
			return getServerWhoCallUsDirectory();
			
		return getAmplifyWhoCallUsDirectory();		
	}	
	
	public static void main(String[] args)
	{
		System.out.println("MSPA Server");
		try
		{					
			System.out.println("Setting up socket connection for listener ...");
			
			MSPAServer server = new MSPAServer(MSPAServer.getMartusDefaultDataDirectory());
			server.processCommandLine(args);			
			server.createMSPAXmlRpcServerOnPort(server.getPortToUse());	
			server.setRootHelperConnector();																			
			System.out.println("Waiting for connection...");
			
			
			if(!server.deleteStartupFiles())
				System.exit(5);		
		
		}
		catch(Exception e) 
		{
			System.out.println("UnknownHost Exception" + e);
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
		
	private final static String DELETEONSTARTUP_BACKUP_DIRECTORY = "deleteOnStartupBackup";
	private final static String DELETE_ON_STARTUP_DIRECTORY = "deleteOnStartup";
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";
	private static final String BANNEDCLIENTS_FILENAME = "banned.txt";
	private static final String UPLOADSOK_FILENAME = "uploadsok.txt";
	private static final String HIDDEN_PACKETS_FILENAME = "isHidden.txt";
	private static final String CLIENTS_NOT_TO_AMPLIFY_FILENAME = "clientsNotToAmplify.txt";
	private static final String COMPLIANCE_FILE =  "compliance.txt";
	private static final String MARTUS_ARGUMENTS_PROPERTY_FILE = "ServerArguments.props";
	private static final String MSPA_ADMIN_DIR = "admin";
	private static final String MSPA_CLIENT_AUTHORIZED_DIR = "authorizedClients"; 
	private static final String MSPA_CLIENT_AUTHORIZED_FILES = "authorizedClients.txt"; 

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_MARTUS_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_MARTUS_ENVIRONMENT = "/var/MartusServer/";
	private final static String WINDOW_MSPA_ENVIRONMENT = "C:/MSPAListener/";
	private final static String UNIX_MSPA_ENVIRONMENT = "/var/MSPAListener/";
	
	private final static int DEFAULT_PORT = 443;
	
}
