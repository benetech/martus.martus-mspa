package org.martus.mspa.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.mspa.client.core.MSPAClient;
import org.martus.mspa.client.view.AccountDetailPanel;
import org.martus.mspa.client.view.AccountsTree;
import org.martus.mspa.client.view.MenuItemManageMagicWords;

public class UiMainWindow extends JFrame
{
	public UiMainWindow(String serverToView, MSPAClient app)
	{		
		super("Martus Server Policy Administrator (MSPA)");
		currentActiveFrame = this;	
		mspaApp = app;
		
		setSize(800, 550);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(),""));
	
		JMenuBar menuBar = createMenuBar();
		setJMenuBar(menuBar);
	
		createTabbedPaneRight();
		Vector accounts = app.displayAccounst();
		accountTree = new AccountsTree(serverToView, accounts, this);
								
		m_sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, accountTree.getScrollPane(),tabPane);
		m_sp.setContinuousLayout(false);
		m_sp.setDividerLocation(220);
		m_sp.setDividerSize(5);		
		m_sp.setOneTouchExpandable(true);
		
		mainPanel.add(createServerInfoPanel("", serverToView),BorderLayout.NORTH );
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
			
		setVisible(true);		
	}	
	
	protected JPanel createServerInfoPanel(String ipAddr, String accountId)
	{
		JPanel serverInfoPanel = new JPanel();		
		serverInfoPanel.setLayout(new GridLayout(1,4));
		try
		{		
			JLabel ipLabel = new JLabel("Martus Server IP Address: "+InetAddress.getByName(ipAddr).getHostAddress());
			ipLabel.setForeground(Color.BLUE);					
			JLabel publicCodeLabel = new JLabel("Martus Server Public code: "+ mspaApp.getPublicCode(accountId));
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
	
	public boolean run()
	{
		return true;
	}

	int signIn(int mode)
	{
		int seconds = 0;
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
		item = new JMenuItem("Exit");		
		mFile.add(item);
		menuBar.add(mFile);
		
		JMenu mEdit = new JMenu("Edit");
		mFile.setMnemonic('d');
		menuBar.add(mEdit);
		
		JMenu mTool = new JMenu("Tools");		
		mTool.add(new MenuItemManageMagicWords(this,"Manage Magic Words"));			
		menuBar.add(mTool);
						
		JMenu mHelp = new JMenu("Help");
		mHelp.setMnemonic('h');
		menuBar.add(mHelp);
		
		return menuBar;
	}
	
	public MSPAClient getMSPAApp()
	{
		return mspaApp;
	}
	
	protected JSplitPane m_sp;
	protected MSPAClient mspaApp;
	JFrame currentActiveFrame;	
	JTabbedPane tabPane;
	JTextField statusField;
	AccountsTree accountTree;	
}
