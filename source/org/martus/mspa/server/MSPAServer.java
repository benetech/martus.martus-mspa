package org.martus.mspa.server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWords;
import org.martus.common.MartusUtilities;
import org.martus.common.Version;
import org.martus.common.MartusUtilities.FileVerificationException;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.FileDatabase;
import org.martus.common.database.ServerFileDatabase;
import org.martus.common.network.MartusSecureWebServer;
import org.martus.common.network.MartusXmlRpcServer;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.client.core.AccountAdminOptions;
import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.mspa.network.NetworkInterfaceXmlRpcConstants;
import org.martus.mspa.network.ServerSideHandler;
import org.martus.mspa.network.roothelper.FileTransfer;
import org.martus.mspa.network.roothelper.MessageType;
import org.martus.mspa.network.roothelper.Messenger;
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
		
		rootConnector = new RootHelperConnector("localhost");
	}	
	
	private void initializedEnvironmentDirectory()
	{
		getServerWhoWeCallDirectory().mkdirs();
		getServerWhoCallUsDirectory().mkdirs();
		getAmplifyWhoCallUsDirectory().mkdirs();
		getAvailableMirrorServerDirectory().mkdirs();
		getConfigDirectory().mkdirs();
		
		initAccountConfigFiles(new File(getConfigDirectory(), MAGICWORDS_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), UPLOADSOK_FILENAME));		
		initAccountConfigFiles(new File(getConfigDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), UPLOADSOK_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), BANNEDCLIENTS_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), HIDDEN_PACKETS_FILENAME));
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
			logger.log(targetFile.getPath()+" error when created."+e.toString());
		}		
	}
	
	private void loadConfigurationFiles()
	{						
		clientsBanned = MartusUtilities.loadBannedClients(getBannedFile());
		clientsAllowedUpload = MartusUtilities.loadCanUploadFile(getAllowUploadFile());		
		clientNotSendToAmplifier = MartusUtilities.loadClientsNotAmplified(getClientsNotToAmplifiyFile());
				
		hiddenBulletins = new HiddenBulletins(getDatabase(),security, getLogger(), getHiddenPacketsFile());
	}
	
	private void initalizeFileDatabase(File dir)
	{					
		security = loadMartusKeypair(getMSPAServerKeyPairFile());
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
	
	public String getMSPAServerKeyPairFile()
	{
		return getConfigDirectory()+KEYPAIR_FILE;
	}
	
	public File getBannedFile()
	{
		return new File(getConfigDirectory(), BANNEDCLIENTS_FILENAME);
	}
	
	public File getAllowUploadFile()
	{
		return new File(getConfigDirectory(), UPLOADSOK_FILENAME);
	}
	
	public File getClientsNotToAmplifiyFile()
	{
		return new File(getConfigDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME);
	}
	
	public File getMagicWordsFile()
	{
		return new File(getConfigDirectory(), MAGICWORDS_FILENAME);		
	}
	
	public File getMartusServerMagicWordFile()
	{
		return new File(getMartusConfigDirectory(),MAGICWORDS_FILENAME);
	}	
	
	public File getMartusServerBannedFile()
	{
		return new File(getMartusConfigDirectory(), BANNEDCLIENTS_FILENAME);
	}
	
	public File getMartusServerAllowUploadFile()
	{
		return new File(getMartusConfigDirectory(), UPLOADSOK_FILENAME);
	}
	
	public File getMartusServerClientsNotToAmplifiyFile()
	{
		return new File(getMartusConfigDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME);
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
		return new File(getConfigDirectory(), HIDDEN_PACKETS_FILENAME);
	}
	
	public File getMartusHiddenPacketsFile()
	{
		return new File(getMartusConfigDirectory(), HIDDEN_PACKETS_FILENAME);
	}
	
	public static File getServerWhoWeCallDirectory()
	{
		return new File(getAppDirectoryPath(),"ServersWhoWeCall");
	}
	
	public static File getServerWhoCallUsDirectory()
	{
		return new File(getAppDirectoryPath(),"ServersWhoCallUs");
	}
	
	public static File getAmplifyWhoCallUsDirectory()
	{
		return new File(getAppDirectoryPath(),"AmplifyWhoCallUs");
	}
	
	public static File getAvailableMirrorServerDirectory()
	{
		return new File(getAppDirectoryPath(),"AvailableMirrorServers");
	}
	
	public File getMartusMagicWordsFile()
	{
		return new File(getMartusConfigDirectory(), MAGICWORDS_FILENAME);
	}
	
	public File getMartusComplianceFile()
	{
		return new File(getMartusConfigDirectory(),COMPLIANCE_FILE );
	}	
	
	public File getMSPAComplianceFile()
	{
		return new File(getConfigDirectory(),COMPLIANCE_FILE );
	}	
	
	public static File getConfigDirectory()
	{
		return new File(getAppDirectoryPath(),ADMIN_MSPA_CONFIG_DIRECTORY);
	}
	
	public File getMartusConfigDirectory()
	{
		return new File(getServerDirectory(),ADMIN_MARTUS_CONFIG_DIRECTORY);
	}
	
	public MagicWords getMagicWordsInfo()
	{
		return magicWords;
	}
	
	public Vector getComplianceFile(String accountId)
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return results;	
	}

	public void updateComplianceFile(String accountId, String compliantsMsg)
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
				rootConnector.getMessenger().copyFilesTo(accountId, transfers);	
			}
		}
		catch (RemoteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	public boolean hideBulletins(String accountId, Vector localIds)
	{
		hiddenBulletins.hideBulletins(accountId, localIds);
				
		try
		{	
			File backUpFile = new File(getHiddenPacketsFile() + ".bak");			
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
			logger.log("Unable to read/write isHidden.txt."+ ieo.toString());		
		}			
		return true;
	}
	
	public Vector getListOfHiddenBulletins(String accountId)
	{
		return hiddenBulletins.getListOfHiddenBulletins(accountId);
	}
	
	public void updateManagingMirrorServerInfo(Vector mirrorInfo, int mirrorType)
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
			logger.log("(Update Mirror Server) Problem when try to update/copy files: "+ e.toString());
		}	
	}
	
	public boolean sendCmdToStartServer(String cmdType, String cmd)
	{
		boolean result = true;
		if (cmdType.equals(NetworkInterfaceConstants.START_SERVER))
		{						
			try
			{
				result = copyAllManageFilesToMartusDeleteOnStart();
				//send cmd to root helper...
				rootConnector.getMessenger().sendCommand("", MessageType.START_SERVER, cmd);
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
	
	boolean copyAllManageFilesToMartusDeleteOnStart() throws RemoteException
	{
		Messenger messenger = rootConnector.getMessenger();
		Vector listOfFiles = new Vector();
	
		listOfFiles.add(getFileTransfer(getMagicWordsFile(),getMartusServerMagicWordFile()));
		listOfFiles.add(getFileTransfer(getBannedFile(), getMartusServerBannedFile()));
		listOfFiles.add(getFileTransfer(getClientsNotToAmplifiyFile(), getMartusServerClientsNotToAmplifiyFile()));
		listOfFiles.add(getFileTransfer(getAllowUploadFile(), getMartusServerAllowUploadFile()));
		listOfFiles.add(getFileTransfer(getHiddenPacketsFile(), getMartusHiddenPacketsFile()));
					
		messenger.copyFilesTo("", listOfFiles);
		
		return true;
		
	}
	
	public void updateMagicWords(Vector words)
	{				
		try
		{			
			File backUpFile = new File(getMagicWordsFile().getPath() + ".bak");			
			FileTransfer.copyFile(getMagicWordsFile(), backUpFile);
			magicWords.writeMagicWords(getMagicWordsFile(), words);
			magicWords.loadMagicWords(getMagicWordsFile());							
		}
		catch (Exception ieo)
		{	
			logger.log("MagicWord.txt file not found."+ ieo.toString());		
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
	
	public void updateAccountInfo(String manageAccountId, Vector accountInfo)
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
			File backUpFile = new File(file.getPath() + ".bak");			
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

	public void addAuthorizedMartusAccounts(String authorizedClientId)
	{
		if (!isAuthorizedMartusAccounts(authorizedClientId))
			authorizedMartusAccounts.add(authorizedClientId);
	}
	
	public void addAuthorizedMSPAClients(String authorizedClientId)
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

	public void updateMartusServerArguments(Vector props)
	{
		File propertyFile = new File(getConfigDirectory(), MARTUS_ARGUMENTS_PROPERTY_FILE);
		LoadMartusServerArguments args = new LoadMartusServerArguments();
		args.convertFromVector(props);
		args.writePropertyFile(propertyFile.getPath());
	}
	
	public static LoadMartusServerArguments getMartusServerArguments()
	{
		File propertyFile = new File(getConfigDirectory(), MARTUS_ARGUMENTS_PROPERTY_FILE);
		LoadMartusServerArguments property = null;

		if (!propertyFile.exists())
			property = loadDefaultMartusServerArguments(propertyFile.getPath());
		else
			property = new LoadMartusServerArguments( propertyFile.getPath());
						
		return property;
	}	

	public static LoadMartusServerArguments loadDefaultMartusServerArguments(String propertyFile)
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
	
	public void processCommandLine(String[] args)
	{	
		String listenersIpTag = "--listener-ip=";	
		String portToListenTag = "--port=";
		
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
		}
		System.out.println("");
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
			System.out.println("Waiting for connection...");
		
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
		
	private final static String ADMIN_MSPA_CONFIG_DIRECTORY = "accountConfig";
	private final static String ADMIN_MARTUS_CONFIG_DIRECTORY = "deleteOnStartup";
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";
	private static final String BANNEDCLIENTS_FILENAME = "banned.txt";
	private static final String UPLOADSOK_FILENAME = "uploadsok.txt";
	private static final String HIDDEN_PACKETS_FILENAME = "isHidden.txt";
	private static final String CLIENTS_NOT_TO_AMPLIFY_FILENAME = "clientsNotToAmplify.txt";
	private static final String COMPLIANCE_FILE =  "compliance.txt";
	private static final String MARTUS_ARGUMENTS_PROPERTY_FILE = "ServerArguments.props";

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_MARTUS_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_MARTUS_ENVIRONMENT = "/var/MartusServer/";
	private final static String WINDOW_MSPA_ENVIRONMENT = "C:/MSPAServer/";
	private final static String UNIX_MSPA_ENVIRONMENT = "/var/MSPAServer/";
	
	private final static int DEFAULT_PORT = 443;
	
}
