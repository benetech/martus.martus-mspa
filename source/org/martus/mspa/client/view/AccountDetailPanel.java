
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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

import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiTextArea;

public class AccountDetailPanel extends JPanel
{
	public AccountDetailPanel(String id, Vector contactInfo, String numOfDelBulletins)
	{
		accountPublicCode = id;
		setBorder(new TitledBorder (new EmptyBorder(5,5,5,5)));
		setLayout(new BorderLayout());			
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new FlowLayout());
		centerPanel.add(buildContactInfoPanel(contactInfo));
		centerPanel.add(buildCheckboxes());	
	
		add(buildTopPanel(numOfDelBulletins, ""), BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
		add(buildButtonsPanel(), BorderLayout.SOUTH);
	}

	private JPanel buildTopPanel(String numOfDelBulletins, String packetDirName)
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder (EtchedBorder.LOWERED));
		panel.setLayout(new ParagraphLayout());
		JLabel numOfDelBulletinLabel = new JLabel("Number of Delete Bulletins: ");
		JTextField numOfDelBulletineField = new JTextField(numOfDelBulletins,5);
		numOfDelBulletineField.setEditable(false);
		JLabel dirNameLabel = new JLabel("Directory Name: ");
		JTextField dirNameField = new JTextField(packetDirName,15);
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

		JCheckBox canUpload = new JCheckBox("Can Upload");
		JCheckBox banned = new JCheckBox("Banned");
		JCheckBox canSendToAmp = new JCheckBox("Can Send to Amplify");
		JCheckBox amp = new JCheckBox("Amplifier");		
		
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
		JButton viewBulletins = new JButton("View Bulletins");
		panel.add(viewBulletins);
		JButton viewActivity = new JButton("View Activity");
		panel.add(viewActivity);
		JButton viewStatistics = new JButton("View Statistics");
		panel.add(viewStatistics);
		saveButton = new JButton("Save");
		panel.add(saveButton);

		return panel;
	}
	
	
	String accountPublicCode;
	JButton saveButton;	
}
