package org.martus.mspa.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.channels.FileChannel;
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


public class MSPAServer implements NetworkInterfaceXmlRpcConstants
{
		
	public MSPAServer(File dir) throws Exception
	{				
		serverDirectory = dir;	
		authorizedClients = new Vector();
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
		getConfigDirectory().mkdirs();
		
		initAccountConfigFiles(new File(getConfigDirectory(), MAGICWORDS_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), UPLOADSOK_FILENAME));		
		initAccountConfigFiles(new File(getConfigDirectory(), CLIENTS_NOT_TO_AMPLIFY_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), UPLOADSOK_FILENAME));
		initAccountConfigFiles(new File(getConfigDirectory(), BANNEDCLIENTS_FILENAME));
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
	
	public File getConfigDirectory()
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
				copyFile(new File(sourceDirectory, file), new File(destDirectory, file));
			}	
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			logger.log("(Update Mirror Server) Problem when try to update/copy files: "+ e.toString());
		}	
	}
	
	public boolean sendCmdToStartServer(String cmdType)
	{
		boolean result = true;
		if (cmdType.equals(NetworkInterfaceConstants.START_SERVER))
		{			
			result = copyAllManageFilesToMartusDeleteOnStart();
			//send cmd to root helper...
		}	
		
		return result;
	}		
	
	void deleteAllFilesFromMirrorDirectory(File[] files)
	{
		for (int i=0;i<files.length;i++)
			files[i].delete();
	}
	
	boolean copyAllManageFilesToMartusDeleteOnStart()
	{
		File file = getMagicWordsFile();
		try
		{	
			copyFile(file, getMartusServerMagicWordFile());
			
			file = getBannedFile();
			copyFile(file, getMartusServerBannedFile());
			
			file = getClientsNotToAmplifiyFile();
			copyFile(file, getMartusServerClientsNotToAmplifiyFile());
			
			file = getAllowUploadFile();
			copyFile(file, getMartusServerAllowUploadFile());
		}
		catch (Exception ieo)
		{	
			logger.log(file.getPath()+" not found."+ ieo.toString());
			return false;		
		}
		
		return true;
		
	}
	
	void copyFile(File in, File out) throws Exception 
	{
		 FileChannel sourceChannel = new FileInputStream(in).getChannel();
		 FileChannel destinationChannel = new FileOutputStream(out).getChannel();
		 sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		
		 sourceChannel.close();
		 destinationChannel.close();
	}
	
	public void updateMagicWords(Vector words)
	{				
		try
		{			
			File backUpFile = new File(getMagicWordsFile().getPath() + ".bak");			
			copyFile(getMagicWordsFile(), backUpFile);
			magicWords.writeMagicWords(getMagicWordsFile(), words);
			magicWords.loadMagicWords(getMagicWordsFile());							
		}
		catch (Exception ieo)
		{	
			logger.log("MagicWord.txt file not found."+ ieo.toString());		
		}	
	}	
	
	public File getMartusMagicWordsFile()
	{
		return new File(getMartusConfigDirectory(), MAGICWORDS_FILENAME);
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
			copyFile(file, backUpFile);
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
		return authorizedClients.contains(myAccountId);
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

	public void addAuthorizedClients(String authorizedClientId)
	{
		if (!isAuthorizedClients(authorizedClientId))
			authorizedClients.add(authorizedClientId);
	}

	public boolean isAuthorizedClients(String authorizedClientId)
	{
		return authorizedClients.contains(authorizedClientId);
	}

	public void setListenersIpAddress(String ipAddr)
	{
		ipAddress = ipAddr;
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
	Vector authorizedClients;
	ServerFileDatabase martusDatabaseToUse;	
	MartusCrypto security;
	LoggerInterface logger;
	MagicWords magicWords;
	Vector clientsBanned;
	Vector clientsAllowedUpload;
	Vector clientNotSendToAmplifier;
		
	private File serverDirectory;
		
	private final static String ADMIN_MSPA_CONFIG_DIRECTORY = "accountConfig";
	private final static String ADMIN_MARTUS_CONFIG_DIRECTORY = "deleteOnStartup";
	private final static String MAGICWORDS_FILENAME = "magicwords.txt";
	private static final String BANNEDCLIENTS_FILENAME = "banned.txt";
	private static final String UPLOADSOK_FILENAME = "uploadsok.txt";
	private static final String HIDDEN_PACKETS_FILENAME = "isHidden.txt";
	private static final String CLIENTS_NOT_TO_AMPLIFY_FILENAME = "clientsNotToAmplify.txt";

	private final static String KEYPAIR_FILE ="\\keypair.dat"; 
	private final static String WINDOW_MARTUS_ENVIRONMENT = "C:/MartusServer/";
	private final static String UNIX_MARTUS_ENVIRONMENT = "/var/MartusServer/";
	private final static String WINDOW_MSPA_ENVIRONMENT = "C:/MSPAServer/";
	private final static String UNIX_MSPA_ENVIRONMENT = "/var/MSPAServer/";
	
	private final static int DEFAULT_PORT = 443;
	
}
