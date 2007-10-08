/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2005-2007, Beneficent
Technology, Inc. (The Benetech Initiative).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/

package org.martus.mspa.main;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import org.martus.clientside.CurrentUiState;
import org.martus.clientside.UiBasicSigninDlg;
import org.martus.clientside.UiLocalization;
import org.martus.common.MartusLogger;
import org.martus.common.Version;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.client.view.AccountDetailPanel;
import org.martus.mspa.client.view.AccountsTree;
import org.martus.mspa.client.view.ServerConnectionDlg;
import org.martus.mspa.client.view.menuitem.MenuItemAboutHelp;
import org.martus.mspa.client.view.menuitem.MenuItemExitApplication;
import org.martus.mspa.client.view.menuitem.MenuItemExportPublicKey;
import org.martus.mspa.client.view.menuitem.MenuItemManageMagicWords;
import org.martus.mspa.client.view.menuitem.MenuItemManagingMirrorServers;
import org.martus.mspa.client.view.menuitem.MenuItemMartusServerCompliance;
import org.martus.mspa.client.view.menuitem.MenuItemServerCommands;
import org.martus.mspa.common.ManagingMirrorServerConstants;
import org.martus.swing.MartusParagraphLayout;
import org.martus.swing.UiLabel;
import org.martus.swing.Utilities;

public class UiMainWindow extends JFrame
{
	public UiMainWindow()
	{		
		super("Martus Server Policy Administrator (MSPA)");	
		
		try
		{
			getDefaultDirectoryPath().mkdirs();
			localization  = new MSPALocalization(getDefaultDirectoryPath(), EnglishStrings.strings);
			mspaApp = new MSPAClient(localization);		
			initalizeUiState();
		}
		catch(Exception e)
		{
			initializationErrorDlg(e.getMessage());
		}
		
		currentActiveFrame = this;						
	}
	
	public UiLocalization getLocalization()
	{
		return localization;
	}
		
	public boolean run()
	{
		
		try
		{
			if(!mspaApp.getKeypairFile().exists())
			{
				initializationErrorDlg("Missing keypair file: " + mspaApp.getKeypairFile());
				return false;
			}
			
			int result = signIn(UiBasicSigninDlg.INITIAL); 
			if(result == UiBasicSigninDlg.CANCEL)
				return false;

			if (result != UiBasicSigninDlg.SIGN_IN)
			{
				String msg = "User Name and Passphrase do not match.";
				initializationErrorDlg(msg);					
				return false;
			}

			if (!whichServerToCall())
			{
				initializationErrorDlg("Exiting because no server was selected");	
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
		catch (Exception e)
		{
			MartusLogger.logException(e);
			initializationErrorDlg("Exiting due to an unexpected error");
			return false;
		}
	}
	
	private boolean whichServerToCall() throws Exception
	{
		if (!mspaApp.loadServerToCall())				
			return true;
	
		Vector listOfServers = mspaApp.getLineOfServerIpAndPublicCode();	
		ServerConnectionDlg dlg = new ServerConnectionDlg(this, listOfServers);
		dlg.setVisible(true);
		
		if (mspaApp.getCurrentServerPublicCode().length() <=0)
			return false;
		
		mspaApp.setXMLRpcEnviornments();				
		return true;
	}
	
	protected JPanel createServerInfoPanel(String ipAddr, String accountId)
	{
		JPanel serverInfoPanel = new JPanel();
		serverInfoPanel.setLayout(new MartusParagraphLayout());
		try
		{		
			JTextField ipLabel = new JTextField(InetAddress.getByName(ipAddr).getHostAddress(),20);
			ipLabel.setEditable(false);
			ipLabel.setForeground(Color.BLUE);
				
			JTextField publicCodeLabel = new JTextField(mspaApp.getCurrentServerPublicCode(),20);
			publicCodeLabel.setEditable(false);
			publicCodeLabel.setForeground(Color.BLUE);
					
			JLabel title = new UiLabel("MSPA Server Infomation: ");			
			serverInfoPanel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);			
			serverInfoPanel.add(title);	
			serverInfoPanel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
			serverInfoPanel.add(new UiLabel("IP Address: "));			
			serverInfoPanel.add(ipLabel);
			serverInfoPanel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);	
			serverInfoPanel.add(new UiLabel("Public Code:"));	
			serverInfoPanel.add(publicCodeLabel);
			serverInfoPanel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
			serverInfoPanel.add(new UiLabel("") , MartusParagraphLayout.NEW_PARAGRAPH);
			JLabel accountListLabel = new UiLabel("Martus Client accounts on this server:");			
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
		dialog.setVisible(true);
	}
	

	private int signIn(int mode) throws Exception
	{
		String iniPassword="";		
		UiBasicSigninDlg signinDlg = new UiBasicSigninDlg(localization, uiState, currentActiveFrame, mode, "", iniPassword.toCharArray());
				
		String userName = signinDlg.getNameText();
		char[] password = signinDlg.getPassword();
					
		int userChoice = signinDlg.getUserChoice();
		if (userChoice != UiBasicSigninDlg.SIGN_IN)
			return userChoice;

		if(mode == UiBasicSigninDlg.INITIAL)
			mspaApp.signIn(userName, password);
	
		return UiBasicSigninDlg.SIGN_IN;
	}		

	protected JMenuBar createMenuBar()
	{
		final JMenuBar menuBar = new JMenuBar();
	
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic('f');
		mFile.add(new MenuItemExportPublicKey(this, "Export Public Key", getDefaultDirectoryPath()));
		mFile.addSeparator();			
		mFile.add(new MenuItemExitApplication(this));
		menuBar.add(mFile);
		
		JMenu mTool = new JMenu("Tools");
		mTool.add(new MenuItemMartusServerCompliance(this,"View Server Compliance"));
		mTool.addSeparator();
		mTool.add(new MenuItemServerCommands(this,START_MARTUS_SERVER));	
		mTool.add(new MenuItemServerCommands(this,STOP_MARTUS_SERVER));	
		mTool.addSeparator();
		mTool.add(new MenuItemManageMagicWords(this,"Magic Words"));	
		mTool.addSeparator();
		JMenu manageServer = new JMenu("Other Servers");	
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHOSE_DATA_WE_BACKUP));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
						ManagingMirrorServerConstants.SERVERS_WHO_BACKUP_OUR_DATA));
		manageServer.addSeparator();												
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHOSE_DATA_WE_AMPLIFY));
		manageServer.add(new MenuItemManagingMirrorServers(this,				
				ManagingMirrorServerConstants.SERVERS_WHO_AMPLIFY_OUR_DATA));
		
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
			dataDirectory = System.getProperty("user.home")+"/MSPAClient/";
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
	MSPALocalization localization;
	CurrentUiState 	uiState;
	String serverName;
	
}
