package org.martus.mspa.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.common.Version;
import org.martus.common.clientside.CurrentUiState;
import org.martus.common.clientside.Localization;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.common.clientside.UiBasicSigninDlg;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.client.view.AccountDetailPanel;
import org.martus.mspa.client.view.AccountsTree;
import org.martus.mspa.client.view.MenuItemExitApplication;
import org.martus.mspa.client.view.MenuItemManageMagicWords;
import org.martus.mspa.client.view.MenuItemManagingMirrorServers;
import org.martus.swing.Utilities;

public class UiMainWindow extends JFrame
{
	public UiMainWindow()
	{		
		super("Martus Server Policy Administrator (MSPA)");		
		
		try
		{			
			localization  = new UiBasicLocalization(getDefaultDirectoryPath());	
			setLocalizationTranslation();			
			mspaApp = new MSPAClient(localization);		
			initalizeUiState();
		}
		catch(Exception e)
		{
			initializationErrorDlg(e.getMessage());
		}
		
		currentActiveFrame = this;						
	}
	
	private void setLocalizationTranslation()
	{
		for(int i=0; i < EnglishStrings.strings.length; ++i)
		{
			localization.addTranslation(Localization.ENGLISH, EnglishStrings.strings[i]);
		}					
		localization.currentLanguageCode = Localization.ENGLISH;
	}	
	
	public UiBasicLocalization getLocalization()
	{
		return localization;
	}
		
	public boolean run()	
	{
		int result = signIn(UiBasicSigninDlg.INITIAL); 
		if(result == UiBasicSigninDlg.CANCEL)
			return false;
		
		
		setSize(800, 550);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(),""));
	
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
	
		createTabbedPaneRight();
		Vector accounts = mspaApp.displayAccounts();
		accountTree = new AccountsTree(mspaApp.getCurrentServerPublicCode(), accounts, this);
								
		m_sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accountTree.getScrollPane(),tabPane);
		m_sp.setContinuousLayout(false);
		m_sp.setDividerLocation(220);
		m_sp.setDividerSize(5);		
		m_sp.setOneTouchExpandable(true);
		
		mainPanel.add(createServerInfoPanel(mspaApp.getCurrentServerIp(), mspaApp.getCurrentServerPublicCode()),BorderLayout.NORTH );
		mainPanel.add(m_sp, BorderLayout.CENTER);
		mainPanel.add(createStatusInfo(), BorderLayout.SOUTH);

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

	protected JPanel createServerInfoPanel(String ipAddr, String accountId)
	{
		JPanel serverInfoPanel = new JPanel();		
		serverInfoPanel.setLayout(new GridLayout(1,4));
		try
		{		
			JLabel ipLabel = new JLabel("Martus Server IP Address: "+InetAddress.getByName(ipAddr).getHostAddress());
			ipLabel.setForeground(Color.BLUE);					
			JLabel publicCodeLabel = new JLabel("Martus Server Public code: "+ mspaApp.getCurrentServerPublicCode());
			publicCodeLabel.setForeground(Color.BLUE);
			serverInfoPanel.add(ipLabel);	
			serverInfoPanel.add(publicCodeLabel);	
		}
		catch (UnknownHostException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		return serverInfoPanel;
	}

	protected JTextField createStatusInfo()
	{
		JTextField statusField = new JTextField("");
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
		tabPane.setTabPlacement(JTabbedPane.BOTTOM);		
		
		return tabPane;
	}
	
	public void loadAccountDetailPanel(String accountId, String publicId)
	{

		Vector contactInfo = mspaApp.getContactInfo(accountId);
		
		tabPane.remove(0);
		tabPane.add(new AccountDetailPanel(publicId, contactInfo,""), "Account Detail");			
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
		System.exit(1);
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
		}
		return seconds;
	}		

	protected JMenuBar createMenuBar()
	{
		final JMenuBar menuBar = new JMenuBar();
	
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic('f');
		JMenuItem item = new JMenuItem("Open");				
		mFile.add(item);
		mFile.addSeparator();			
		mFile.add(new MenuItemExitApplication(this));
		menuBar.add(mFile);
		
		JMenu mEdit = new JMenu("Edit");
		mFile.setMnemonic('d');
		menuBar.add(mEdit);
		
		JMenu mTool = new JMenu("Tools");		
		mTool.add(new MenuItemManageMagicWords(this,"Manage Magic Words"));		
		JMenu manageServer = new JMenu("Managing Mirror servers");	
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVER_WHO_WE_CALL));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVER_WE_AMPLIFY));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.AMP_WHO_CALLS_US));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.WHO_CALLS_US));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.LISTEN_FOR_CLIENTS));		
		mTool.add(manageServer);			
		menuBar.add(mTool);
		
						
		JMenu mHelp = new JMenu("Help");
		mHelp.setMnemonic('h');
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
