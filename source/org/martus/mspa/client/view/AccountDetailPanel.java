
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.mspa.client.core.AccountAdminOptions;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTextArea;

public class AccountDetailPanel extends JPanel
{
	public AccountDetailPanel(MSPAClient app, String id, Vector contactInfo, Vector hiddenBulletins, 
			Vector bulletinIds, Vector manageAccount)
	{
		accountId = id;	
		mspaApp = app;		
		hiddenBulletinIds =	hiddenBulletins;
		originalBulletinIds = bulletinIds;
		
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());

		loadAccountAdminInfo(manageAccount);		
	
		add(buildTopPanel(contactInfo),BorderLayout.NORTH);
		add(loadBulletinDisplayPane(), BorderLayout.CENTER);
		add(buildButtonsPanel(), BorderLayout.SOUTH);		
	}

	private Vector loadBulletinIds(Vector bulletins)
	{
		Vector newBulletinIds = new Vector();		
		if (bulletins == null || bulletins.size() <=0)
			return newBulletinIds;
								
		for (int i=0; i<bulletins.size(); ++i)
		{	
			Vector bulletinInfo= (Vector) bulletins.get(i);
			String bulletinId = (String) bulletinInfo.get(0)+" \t"+(String)bulletinInfo.get(1);
			newBulletinIds.add(bulletinId);		
		}

		return newBulletinIds;
	}

	private void loadAccountAdminInfo(Vector accountManagement)
	{
		admOptions = new AccountAdminOptions();
		admOptions.setOptions(accountManagement);							
	} 

	private JPanel buildContactPanel(Vector contactInfo)
	{
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		centerPanel.add(buildContactInfoPanel(contactInfo));		
		centerPanel.add(buildCheckboxes());	
		return centerPanel;
	}

	private JPanel buildTopPanel(Vector contactInfo)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new ParagraphLayout());
		JLabel numOfDelBulletinLabel = new JLabel("Number of Deleted Bulletins: ");
		JTextField numOfDelBulletineField = new JTextField(Integer.toString(hiddenBulletinIds.size()),5);
		numOfDelBulletineField.setEditable(false);		

		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(numOfDelBulletinLabel);
		panel.add(numOfDelBulletineField);		
		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(buildContactPanel(contactInfo));
		
		return panel;	
	}

	private JPanel buildCheckboxes()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(new EmptyBorder(5,5,5,5));

		canUpload = new JCheckBox("Can Upload", admOptions.canUploadSelected());	
		canUpload.addActionListener(new CheckBoxHandler());	
		banned = new JCheckBox("Banned", admOptions.isBannedSelected());
		banned.addActionListener(new CheckBoxHandler());	
		canSendToAmp = new JCheckBox("Can Send to Amplify", admOptions.canSendToAmplifySelected());
		canSendToAmp.addActionListener(new CheckBoxHandler());	
		amp = new JCheckBox("Amplifier", admOptions.isAmplifierSelected());		
		amp.addActionListener(new CheckBoxHandler());	
		
		panel.add(canUpload);
		panel.add(banned);
		panel.add(canSendToAmp);
		panel.add(amp);
		
		return panel;

	}
		
	private JPanel buildContactInfoPanel(Vector contactInfo)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder (new LineBorder (Color.gray, 1)," Contact Info "));
		panel.setLayout(new ParagraphLayout());
		
		if (!contactInfo.isEmpty())
		{
			int index = 2;							
						
			panel.add(new JLabel("Author: ") , ParagraphLayout.NEW_PARAGRAPH);
			String fieldName = (String)contactInfo.get(index++);
			JTextField author = new JTextField(fieldName, 30);
			author.setEditable(false);
			panel.add(author);		
			panel.add(new JLabel("Organization: "),ParagraphLayout.NEW_PARAGRAPH);
			fieldName = (String)contactInfo.get(index++);
			JTextField  organization = new JTextField(fieldName, 30);
			organization.setEditable(false);
			panel.add(organization);				
			panel.add(new JLabel("Email Address: "),ParagraphLayout.NEW_PARAGRAPH);
			fieldName = (String)contactInfo.get(index++);
			JTextField email = new JTextField(fieldName, 30);
			email.setEditable(false);
			panel.add(email);				
			panel.add(new JLabel("Web Page: "),ParagraphLayout.NEW_PARAGRAPH);
			fieldName = (String)contactInfo.get(index++);
			JTextField web = new JTextField(fieldName, 30);
			web.setEditable(false);
			panel.add(web);	
			panel.add(new JLabel("Phone #: "),ParagraphLayout.NEW_PARAGRAPH);
			fieldName = (String)contactInfo.get(index++);
			JTextField phone = new JTextField(fieldName, 30);
			phone.setEditable(false);
			panel.add(phone);		
			panel.add(new JLabel("Mailing Address: "),ParagraphLayout.NEW_PARAGRAPH);
			UiTextArea address = new UiTextArea(4, 35);
			address.setText((String)contactInfo.get(index++));
			address.setEditable(false);
			JScrollPane addressScrollPane = new JScrollPane(address, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			panel.add(addressScrollPane,ParagraphLayout.NEW_LINE);
			
		}
				
		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();	
		panel.setLayout(new FlowLayout());
				
		viewActivity = new JButton("View Activity");
		viewActivity.addActionListener(new CommitButtonHandler());	
		panel.add(viewActivity);					
		
		viewStatistics = new JButton("View Statistics");
		viewStatistics.addActionListener(new CommitButtonHandler());
		panel.add(viewStatistics);
		saveButton = new JButton("Save");
		saveButton.addActionListener(new CommitButtonHandler());
		panel.add(saveButton);

		return panel;
	}	

	private DefaultListModel loadElementsToList(Vector items)
	{
		DefaultListModel listModel = new DefaultListModel();
		
		for (int i=0; i<items.size();++i)
			listModel.add(i, items.get(i));
			
		return listModel;
	}

	private void configureTabList(JList list)
	{
		TabListCellRenderer renderer = new TabListCellRenderer();
		renderer.setTabs(new int[] {130, 200, 300});
		list.setCellRenderer(renderer);
		
		TabListCellRenderer renderer2 = new TabListCellRenderer();
		renderer2.setTabs(new int[] {130, 200, 300});
		list.setCellRenderer(renderer);
	}	

	private JTabbedPane loadBulletinDisplayPane()
	{
		bulletinTabPane = new JTabbedPane();				
		bulletinTabPane.setTabPlacement(JTabbedPane.TOP);
		
		Vector formattedBulletinsIds = loadBulletinIds(originalBulletinIds);
		bulletinListModel = loadElementsToList(formattedBulletinsIds);
		bulletinList = createBulletinList(bulletinListModel);
		bulletinList.setName("bulletin");
		
		viewBulletinButton = new JButton("View");
		bulletinTabPane.add(getDisplayBulletinPanel(bulletinList, viewBulletinButton), 0);
		bulletinTabPane.setTitleAt(0, "List Of Bulletins");

		hiddenListModel = loadElementsToList(hiddenBulletinIds);
		hiddenList = createBulletinList(hiddenListModel);
		hiddenList.setName("hidden");
		viewHiddenButton = new JButton("View");
		bulletinTabPane.add(getDisplayBulletinPanel(hiddenList,viewHiddenButton), 1);
		bulletinTabPane.setTitleAt(1, "List Of Delete Bulletins");

		return bulletinTabPane;
	}

	private JList createBulletinList(DefaultListModel dataModel)
	{
		JList list = new JList(dataModel);
		list.setFixedCellWidth(220);
		configureTabList(list);

		return list;
	}

	private JPanel getDisplayBulletinPanel(JList list, JButton view)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));		
		panel.setLayout(new BorderLayout());		

		JPanel buttonPanel = new JPanel();
		FlowLayout layout = new FlowLayout();
		layout.setAlignment(FlowLayout.RIGHT);
		buttonPanel.setLayout(layout);
		
		view = new JButton("View");
		view.addActionListener(new CommitButtonHandler());	
		buttonPanel.add(view);

		if (list.getName().equals("bulletin"))
		{
			delBulletins = new JButton("Remove");
			delBulletins.addActionListener(new CommitButtonHandler());	
			buttonPanel.add(delBulletins);
		}		

		JScrollPane ps = createScrollPane();
		ps.setPreferredSize(new Dimension(220, 150));
		ps.setMinimumSize(new Dimension(220, 150));
		ps.setAlignmentX(LEFT_ALIGNMENT);			
		ps.getViewport().add(list);	

		panel.add(ps, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}	

	JScrollPane createScrollPane()
	{
		JScrollPane ps = new JScrollPane();		
		ps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ps.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return ps;
	}
	
	class CheckBoxHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{		
			admOptions = new AccountAdminOptions();											
			admOptions.setCanUploadOption(canUpload.isSelected());
			admOptions.setBannedOption(banned.isSelected());	
			admOptions.setCanSendOption(canSendToAmp.isSelected());	
			admOptions.setAmplifierOption(amp.isSelected());
			
		}
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(saveButton))				
				handleConfigurationAccountInfo();

			if (ae.getSource().equals(delBulletins))				
				handleDeleteBulletin();				
		}

		private void handleConfigurationAccountInfo()
		{								
			mspaApp.updateAccountManageInfo(accountId, admOptions.getOptions());			
		}

		private void handleDeleteBulletin()
		{								
			int selectItem = bulletinList.getSelectedIndex();	

			if (!bulletinList.isSelectionEmpty())
			{	
				String item = (String) bulletinList.getSelectedValue();
				String localId = item.substring(0,item.indexOf("\t"));
				mspaApp.removeBulletin(accountId,localId);	
				
				bulletinList.remove(selectItem);
			
			}			
		}
	}
	
	String accountId;
	JButton saveButton;
	AccountAdminOptions admOptions;
	
	JCheckBox canUpload;
	JCheckBox banned;	
	JCheckBox canSendToAmp;
	JCheckBox amp;
	
	JButton viewBulletinButton;
	JButton viewHiddenButton;
	JButton delBulletins;
	JButton viewActivity;
	JButton viewStatistics;

	JList bulletinList;	
	DefaultListModel bulletinListModel;
	JList hiddenList;
	DefaultListModel hiddenListModel;

	MSPAClient mspaApp;
	Vector originalBulletinIds;
	Vector hiddenBulletinIds;	
	JTabbedPane bulletinTabPane;
}
