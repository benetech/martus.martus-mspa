
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.mspa.client.core.AccountAdminOptions;
import org.martus.mspa.client.core.MSPAClient;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTextArea;

public class AccountDetailPanel extends JPanel
{
	public AccountDetailPanel(MSPAClient app, String id, Vector contactInfo, String hiddenBulletins, 
			Vector dirNames, Vector manageAccount)
	{
		accountId = id;	
		mspaApp = app;
		numOfHiddenBulletins =	hiddenBulletins;
		setBorder(new EmptyBorder(5,5,5,5));
		setLayout(new BorderLayout());

		loadAccountAdminInfo(manageAccount);		
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		centerPanel.add(buildContactInfoPanel(contactInfo));		
		centerPanel.add(buildCheckboxes());	
	
		add(buildTopPanel(dirNames), BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(buildButtonsPanel(), BorderLayout.SOUTH);
	}

	private void loadAccountAdminInfo(Vector accountManagement)
	{
		admOptions = new AccountAdminOptions();
		admOptions.setOptions(accountManagement);		
	} 

	private JPanel buildTopPanel(Vector packetDirNames)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder (EtchedBorder.LOWERED));
		panel.setLayout(new ParagraphLayout());
		JLabel numOfDelBulletinLabel = new JLabel("Number of Delete Bulletins: ");
		JTextField numOfDelBulletineField = new JTextField(numOfHiddenBulletins,5);
		numOfDelBulletineField.setEditable(false);
		JLabel dirNameLabel = new JLabel("Directory Name: ");
		dirNameField = new GroupComboBox(packetDirNames);
		dirNameField.setEditable(false);

		panel.add(numOfDelBulletinLabel , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(numOfDelBulletineField);
		panel.add(dirNameLabel);
		panel.add(dirNameField);
		
		return panel;	
	}

	private JPanel buildCheckboxes()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

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
			UiTextArea address = new UiTextArea(5, 35);
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
		viewBulletins = new JButton("View Bulletins");
		viewBulletins.addActionListener(new CommitButtonHandler());	
		panel.add(viewBulletins);
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
	
	class CheckBoxHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if(canUpload.isSelected())
			{
				canUpload.setSelected(true);
				admOptions.setCanUploadOption(true);
			}
			else
				admOptions.setCanUploadOption(false);
			
			if(banned.isSelected())
			{
				banned.setSelected(true);
				admOptions.setBannedOption(true);
			}
			else
				admOptions.setBannedOption(false);
				
			if(canSendToAmp.isSelected())
			{
				canSendToAmp.setSelected(true);
				admOptions.setCanSendOption(true);
			}
			else
				admOptions.setCanSendOption(false);
				
			if(amp.isSelected())
			{
				amp.setSelected(true);
				admOptions.setAmplifierOption(true);
			}				
			else
				admOptions.setAmplifierOption(false);
			
		}
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(saveButton))				
				handleConfigurationAccountInfo();			
			
		}

		private void handleConfigurationAccountInfo()
		{				
			mspaApp.updateAccountManageInfo(accountId, admOptions.getOptions());			
		}
	}
	
	String accountId;
	String numOfHiddenBulletins;
	JButton saveButton;
	GroupComboBox dirNameField;
	AccountAdminOptions admOptions;
	
	JCheckBox canUpload;
	JCheckBox banned;	
	JCheckBox canSendToAmp;
	JCheckBox amp;
	
	JButton viewBulletins;
	JButton viewActivity;
	JButton viewStatistics;

	MSPAClient mspaApp;
}
