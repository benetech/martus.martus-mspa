package org.martus.mspa.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.martus.common.Version;
import org.martus.common.MartusUtilities.InvalidPublicKeyFileException;
import org.martus.common.MartusUtilities.PublicInformationInvalidException;
import org.martus.common.clientside.CurrentUiState;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiBasicSigninDlg;
import org.martus.common.network.MartusXmlrpcClient.SSLSocketSetupException;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.client.view.AccountDetailPanel;
import org.martus.mspa.client.view.AccountsTree;
import org.martus.mspa.client.view.ServerConnectionDlg;
import org.martus.mspa.client.view.menuitem.MenuItemAboutHelp;
import org.martus.mspa.client.view.menuitem.MenuItemExitApplication;
import org.martus.mspa.client.view.menuitem.MenuItemExportPublicKey;
import org.martus.mspa.client.view.menuitem.MenuItemManageMagicWords;
import org.martus.mspa.client.view.menuitem.MenuItemManagingMirrorServers;
import org.martus.mspa.client.view.menuitem.MenuItemMartusServerArgumentsConfig;
import org.martus.mspa.client.view.menuitem.MenuItemServerCommands;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;
import org.martus.util.Base64.InvalidBase64Exception;

public class UiMainWindow extends JFrame
{
	public UiMainWindow()
	{		
		super("Martus Server Policy Administrator (MSPA)");	
		
		try
		{			
			localization  = new UiBasicLocalization(getDefaultDirectoryPath(), EnglishStrings.strings);	
			localization.currentLanguageCode = Localization.ENGLISH;			
			mspaApp = new MSPAClient(localization);		
			initalizeUiState();
		}
		catch(Exception e)
		{
			initializationErrorDlg(e.getMessage());
		}
		
		currentActiveFrame = this;						
	}
	
	public UiBasicLocalization getLocalization()
	{
		return localization;
	}
		
	public boolean run()	
	{
		int result = signIn(UiBasicSigninDlg.INITIAL); 
		int loginTimes=0;
		while (result != UiBasicSigninDlg.SIGN_IN)
		{	
			if(result == UiBasicSigninDlg.CANCEL)
				return false;
				
			if (loginTimes > 2)	
			{
				String msg = "Login Failed. Exit application.";
				initializationErrorDlg(msg);	
				return false;
			}	
			
			if (result != UiBasicSigninDlg.SIGN_IN)
			{
				String msg = "User Name and Passphrase not match.";
				initializationErrorDlg(msg);					
			}
			result = signIn(UiBasicSigninDlg.INITIAL);
			++loginTimes; 
		}
		
		if (!whichServerToCall())
		{
			JOptionPane.showMessageDialog(this, "MSPA Client would not be able to continue due to missing connect server ip and public code.", "MSPA error message", JOptionPane.ERROR_MESSAGE);	
			return false;
		}	
		
		setSize(840, 650);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
	
	
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
	
		createTabbedPaneRight();
		Vector accounts = mspaApp.displayAccounts();			
		accountTree = new AccountsTree(accounts, this);

		JPanel leftPanel = createServerInfoPanel(mspaApp.getCurrentServerIp(), mspaApp.getCurrentServerPublicCode());								
		m_sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel,tabPane);
		m_sp.setContinuousLayout(false);
		m_sp.setDividerLocation(260);
		m_sp.setDividerSize(5);		
		m_sp.setOneTouchExpandable(true);
		
		mainPanel.add(m_sp, BorderLayout.CENTER);
		mainPanel.add(createStatusInfo(), BorderLayout.SOUTH);	
		setStatusText(mspaApp.getStatus());

		WindowListener wndCloser = new WindowAdapter()
		{
			public void windowClosing(WindowEvent e) 
			{
				System.exit(0);
			}
		};
		addWindowListener(wndCloser);
		getContentPane().add(mainPanel);
			
		Utilities.centerFrame(this);	
		setVisible(true);		
						
		return true;
	}
	
	private boolean whichServerToCall()
	{
		try
		{
			if (!mspaApp.loadServerToCall())				
				return true;
		
			Vector listOfServers = mspaApp.getLineOfServerIpAndPublicCode();	
			ServerConnectionDlg dlg = new ServerConnectionDlg(this, listOfServers);
			dlg.show();
			
			if (mspaApp.getCurrentServerPublicCode().length() <=0)
				return false;
			
			mspaApp.setXMLRpcEnviornments();				
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidPublicKeyFileException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (PublicInformationInvalidException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SSLSocketSetupException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (InvalidBase64Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	protected JPanel createServerInfoPanel(String ipAddr, String accountId)
	{
		JPanel serverInfoPanel = new JPanel();
		serverInfoPanel.setLayout(new ParagraphLayout());
		try
		{		
			JTextField ipLabel = new JTextField(InetAddress.getByName(ipAddr).getHostAddress(),20);
			ipLabel.setEditable(false);
			ipLabel.setForeground(Color.BLUE);
				
			JTextField publicCodeLabel = new JTextField(mspaApp.getCurrentServerPublicCode(),20);
			publicCodeLabel.setEditable(false);
			publicCodeLabel.setForeground(Color.BLUE);
					
			JLabel title = new JLabel("MSPA Server Infomation: ");			
			serverInfoPanel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);			
			serverInfoPanel.add(title);	
			serverInfoPanel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
			serverInfoPanel.add(new JLabel("IP Address: "));			
			serverInfoPanel.add(ipLabel);
			serverInfoPanel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);	
			serverInfoPanel.add(new JLabel("Public Code:"));	
			serverInfoPanel.add(publicCodeLabel);
			serverInfoPanel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
			serverInfoPanel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
			JLabel accountListLabel = new JLabel("Martus Client accounts on this server:");			
			serverInfoPanel.add(accountListLabel);			
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());			
		mainPanel.add(serverInfoPanel, BorderLayout.NORTH);
		mainPanel.add(accountTree.getScrollPane(), BorderLayout.CENTER); 	
		return mainPanel;
	}

	protected JTextField createStatusInfo()
	{
		statusField = new JTextField(" ");
		statusField.setEditable(false);
		return statusField;
	}
	
	public void setStatusText(String msg)
	{
		statusField.setText(msg);
	}
	
	protected JTabbedPane createTabbedPaneRight()
	{
		tabPane = new JTabbedPane();				

		loadEmptyAccountDetailPanel();			
		tabPane.setTabPlacement(JTabbedPane.TOP);		
		
		return tabPane;
	}
	
	public void loadAccountDetailPanel(String accountId, String publicId)
	{
		Vector contactInfo = mspaApp.getContactInfo(accountId);
		Vector packetDir = mspaApp.getPacketDirNames(accountId);
		Vector accountAdmin = mspaApp.getAccountManageInfo(accountId);
		Vector hiddenBulletins = mspaApp.getListOfHiddenBulletins(accountId);

		tabPane.remove(0);
		tabPane.add(new AccountDetailPanel(this, accountId, contactInfo, hiddenBulletins, 
					packetDir, accountAdmin), "Account Detail");			
	}		
	
	public void loadEmptyAccountDetailPanel()
	{
		if (tabPane.getTabCount() > 0)
			tabPane.remove(0);
			
		tabPane.add(new JPanel(), "Account Detail");		
	}
	
	private void initializationErrorDlg(String message)
	{
		String title = "Error Starting MSPA";
		String cause = "Unable to start MSPA: " + message;
		String ok = "OK";
		String[] buttons = { ok };
		JOptionPane pane = new JOptionPane(cause, JOptionPane.INFORMATION_MESSAGE,
				 JOptionPane.DEFAULT_OPTION, null, buttons);
		JDialog dialog = pane.createDialog(null, title);
		dialog.show();
	}
	

	int signIn(int mode)
	{
		int seconds = 0;
		String iniPassword="";		
		UiBasicSigninDlg signinDlg = new UiBasicSigninDlg(localization, uiState, currentActiveFrame, mode, "", iniPassword.toCharArray());
				
		try
		{
			String userName = signinDlg.getName();
			char[] password = signinDlg.getPassword();
						
			int userChoice = signinDlg.getUserChoice();
			if (userChoice != UiBasicSigninDlg.SIGN_IN)
				return userChoice;

			if(mode == UiBasicSigninDlg.INITIAL)
			{	
				mspaApp.signIn(userName, password);
			}
		
			return UiBasicSigninDlg.SIGN_IN;
		}
		catch (Exception e)
		{			
			e.printStackTrace();
		}
		return seconds;
	}		

	protected JMenuBar createMenuBar()
	{
		final JMenuBar menuBar = new JMenuBar();
	
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic('f');
		mFile.add(new MenuItemExportPublicKey(this, "Export Public Key", getDefaultDirectoryPath()));
		mFile.addSeparator();		
		JMenuItem item = new JMenuItem("Open");				
		mFile.add(item);
		mFile.addSeparator();			
		mFile.add(new MenuItemExitApplication(this));
		menuBar.add(mFile);
		
		JMenu mEdit = new JMenu("Edit");
		mFile.setMnemonic('d');
		menuBar.add(mEdit);		
		
		JMenu mTool = new JMenu("Tools");
		mTool.add(new MenuItemMartusServerArgumentsConfig(this,"Martus Server Arguments Configuration"));
		mTool.addSeparator();
		mTool.add(new MenuItemServerCommands(this,START_MARTUS_SERVER));	
		mTool.add(new MenuItemServerCommands(this,STOP_MARTUS_SERVER));	
		mTool.addSeparator();
		mTool.add(new MenuItemManageMagicWords(this,"Magic Words"));	
		mTool.addSeparator();
		JMenu manageServer = new JMenu("Other Servers");	
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHO_WE_MIRROR));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WE_AMPLIFY));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHO_AMPLIFIES_US));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHO_MIRRORS_US));		
		mTool.add(manageServer);			
		menuBar.add(mTool);
		
						
		JMenu mHelp = new JMenu("Help");
		mHelp.setMnemonic('h');
		mHelp.add(new MenuItemAboutHelp(this, "About MSPA"));
		menuBar.add(mHelp);
		
		return menuBar;
	}	
	
	
	private void initalizeUiState()
	{
		uiState = new CurrentUiState();
		File uiStateFile = mspaApp.getUiStateFile();

		if(!uiStateFile.exists())
		{
			uiState.setCurrentLanguage(localization.getCurrentLanguageCode());
			uiState.setCurrentDateFormat(localization.getCurrentDateFormatCode());
			uiState.save(uiStateFile);
			return;
		}
		uiState.load(uiStateFile);
		localization.setCurrentDateFormatCode(uiState.getCurrentDateFormat());
	}
	
	public static File getDefaultDirectoryPath()
	{
		String dataDirectory = null;
		if(Version.isRunningUnderWindows())
			dataDirectory = "C:/MSPAClient/";
		else
			dataDirectory = System.getProperty("user.home")+"/.MSPAClient/";
		return new File(dataDirectory);
	}	
	
	public MSPAClient getMSPAApp()
	{
		return mspaApp;
	}
	
	public void exitNormally()
	{
		System.exit(0);
	}
	
	public static String START_MARTUS_SERVER ="Start Martus Server ...";
	public static String STOP_MARTUS_SERVER  ="Stop Martus Server ..."; 
	
	protected JSplitPane m_sp;
	protected MSPAClient mspaApp;
	JFrame currentActiveFrame;	
	JTabbedPane tabPane;
	JTextField statusField;
	AccountsTree accountTree;
	UiBasicLocalization localization;
	CurrentUiState 	uiState;
	String serverName;
	
}
