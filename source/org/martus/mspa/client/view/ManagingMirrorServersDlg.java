/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
Technology, Inc. (Benetech).

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
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.client.core.MirrorServerLabelFinder;
import org.martus.mspa.client.core.MirrorServerLabelInfo;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class ManagingMirrorServersDlg extends JDialog
{
	public ManagingMirrorServersDlg(UiMainWindow owner, int manageType, 
			String serverToManage, String serverToManagePublicCode,
			Vector allList, Vector currentList)
	{
		super((JFrame)owner);
		msgLabelInfo = MirrorServerLabelFinder.getMessageInfo(manageType);
		setTitle("Managing Server Mirroring: "+ msgLabelInfo.getTitle());
		parent = owner;
		serverManageType = manageType;
		availableItems = allList;
		assignedItems = currentList;	
	
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getTopPanel(), BorderLayout.NORTH);
		getContentPane().add(getCenterPanel(), BorderLayout.CENTER);
		getContentPane().add(getCommitButtonsPanel(), BorderLayout.SOUTH);
						
		Utilities.centerDlg(this);
	}
	
	private JPanel getTopPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(),msgLabelInfo.getHeader()));			
		panel.setLayout(new ParagraphLayout());
		
		manageIPAddr = new JTextField(20);
		manageIPAddr.requestFocus();		
		managePublicCode = new JTextField(20);				
		
		panel.add(new JLabel("Manage IP Address: "), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(manageIPAddr);
		panel.add(new JLabel("Public Code: "), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(managePublicCode);
		mirrorServerPort = new JTextField(20);
		panel.add(new JLabel("Which Port: (optional)"), ParagraphLayout.NEW_PARAGRAPH);	
		panel.add(mirrorServerPort);			
		
		if (serverManageType == ManagingMirrorServerConstants.LISTEN_FOR_CLIENTS)
			collectMagicInfo(panel);
		else
			collectMirrorInfo(panel);			
		
		return panel;				
	}
	
	private void collectMagicInfo(JPanel panel)
	{	
		JLabel magicWordLabel = new JLabel("Enter new magic word:");
		magicWordLabel.setForeground(Color.BLUE);					
		addMagicWordsField = new JTextField(20);
		addMagicWordsField.requestFocus();	
	
		addNewMagicWord = new JButton("Add");
		addNewMagicWord.addActionListener(new ButtonHandler());	
				
		panel.add(magicWordLabel,ParagraphLayout.NEW_PARAGRAPH);
		panel.add(addMagicWordsField);
		panel.add(addNewMagicWord);			
		
		removeMagicWord = new JButton("Remove");
		removeMagicWord.addActionListener(new ButtonHandler());	
				
		panel.add(removeMagicWord);		
	}
	
	private void collectMirrorInfo(JPanel panel)
	{
		addNewMirrorServer = createButton("add");						
		panel.add(addNewMirrorServer);		
		
	}
	
	private JPanel getCenterPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new FlowLayout());
				
		panel.add(getAvailablePanel());
		panel.add(getShiftButtons());
		panel.add(getAllowedPanel());

		return panel;
	}
	
	private JPanel getAvailablePanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		availableListModel = loadElementsToList(availableItems);
		availableList = new JList(availableListModel);
		availableList.setFixedCellWidth(200);   
		 
		JScrollPane ps = createScrollPane();			
		ps.getViewport().add(availableList);
		JLabel availableLabel = new JLabel(msgLabelInfo.getAvailableLabel());
				
		panel.add(availableLabel);
		panel.add(ps);

		return panel;
	}	
	
	private JPanel getAllowedPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		allowedListModel = loadElementsToList(assignedItems);
		allowedList = new JList(allowedListModel);
		allowedList.setFixedCellWidth(200);
		    
		JScrollPane ps = createScrollPane();
		ps.getViewport().add(allowedList);
		JLabel allowedLabel = new JLabel( msgLabelInfo.getAllowedLabel());
				
		panel.add(allowedLabel);
		panel.add(ps);

		return panel;
	}
	
	JScrollPane createScrollPane()
	{
		JScrollPane ps = new JScrollPane();		
		ps.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		ps.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		return ps;
	}
	
	private DefaultListModel loadElementsToList(Vector items)
	{
		DefaultListModel listModel = new DefaultListModel();
		
		for (int i=0; i<items.size();++i)
			listModel.add(i, items.get(i));
			
		return listModel;
	}	
	
	private JPanel getShiftButtons()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		addButton = createButton(">>");					
		removeButton = createButton("<<");		
		
		panel.add(addButton);
		panel.add(removeButton);
		
		return panel;

	}
	
	
	private JPanel getCommitButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
				
		viewComplainButton = createButton("View Compliance");			
		updateButton = createButton("Update");				
		cancelButton = createButton("Close");						
				
		panel.add(viewComplainButton);
		panel.add(updateButton);
		panel.add(cancelButton);
		
		return panel;
	}
	
	private JButton createButton(String label)
	{
		JButton button = new JButton(label);
		button.addActionListener(new ButtonHandler());
		return button;
	}
	
	String generateFileName(String ip, String publicKey)
	{
		String fileName ="ip="+ip+"-code="+publicKey+".txt";
		return fileName.trim();
	}
	
	class ButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(cancelButton))				
				dispose();
			else if (ae.getSource().equals(addButton))
				handleAddToAllowedList();
			else if (ae.getSource().equals(updateButton))
				handleUpdateMirrorServerInfo();
			else if (ae.getSource().equals(removeButton))
				handleRemoveFromAllowedList();
			else if (ae.getSource().equals(addNewMirrorServer))
				handleRequestAddNewMirrorServer();
			else if (ae.getSource().equals(addNewMagicWord))
				handleAddNewMagicWord();
			else if (ae.getSource().equals(removeMagicWord))
				handleRemoveMagicWords();		
		}
		
		private void handleRequestAddNewMirrorServer()
		{
			Vector mirrorServerInfo = new Vector();
			String mirrorIP = manageIPAddr.getText();
			String mirrorPublicCode = managePublicCode.getText();
			String port = mirrorServerPort.getText();
			
			if (mirrorIP.length()<=0 || 
				mirrorPublicCode.length()<=0 || 
				port.length()<=0)
			{	
				JOptionPane.showMessageDialog(parent, "Ip address, public code and port are required.", 
					"Missing Infomation", JOptionPane.ERROR_MESSAGE);
				return;
			}				
			
			mirrorServerInfo.add(mirrorIP);
			mirrorServerInfo.add(mirrorPublicCode);		
			mirrorServerInfo.add(port);	
			
			mirrorFileName = generateFileName(mirrorIP,mirrorPublicCode); 
			mirrorServerInfo.add(mirrorFileName);										
				
			boolean result = parent.getMSPAApp().addMirrorServer(mirrorServerInfo);
			if (result)
			{			
				availableItems.add(mirrorFileName);
				availableListModel.addElement(mirrorFileName);				
			}
			else
			{
				JOptionPane.showMessageDialog(parent, "Error no response from server.", 
					"Server Info", JOptionPane.ERROR_MESSAGE);
			}	
			
			manageIPAddr.setText("");
			managePublicCode.setText("");
			mirrorServerPort.setText("");					
							
		}		
		
		private void handleAddToAllowedList()
		{
			int selectItem = availableList.getSelectedIndex();	
			if (!availableList.isSelectionEmpty())
			{	
				String item = (String) availableList.getSelectedValue();
				if (!allowedListModel.contains(item))
				{						
					allowedListModel.addElement(item);
					availableListModel.remove(selectItem);
				}				
			}
		}
		
		private void handleUpdateMirrorServerInfo()
		{
			Object[] items = allowedListModel.toArray();
			Vector itemCollection = new Vector();			
			for (int i=0;i<items.length;i++)
				itemCollection.add(items[i]);
				
			if (serverManageType == ManagingMirrorServerConstants.LISTEN_FOR_CLIENTS)
			{									
				items = availableListModel.toArray();	
				for (int i=0;i<items.length;i++)
				{
					String itemString = (String) items[i];
					if (serverManageType == MirrorServerLabelFinder.LISTEN_FOR_CLIENTS)	
							itemCollection.add("#"+itemString);
					else
						itemCollection.add(itemString);
				}							
				parent.getMSPAApp().updateMagicWords(itemCollection);
			}
			else
			{					
				parent.getMSPAApp().updateManageMirrorAccounts(itemCollection, serverManageType);
			}
		
			dispose();							
		}
		
		private void handleRemoveFromAllowedList()
		{			
			int selectItem = allowedList.getSelectedIndex();	
			if (!allowedList.isSelectionEmpty())
			{	
				String item = (String) allowedList.getSelectedValue();				
		
				allowedListModel.remove(selectItem);
				if (!availableListModel.contains(item))								
					availableListModel.addElement(item);
			}							
		}
		
		private void handleAddNewMagicWord()
		{
			String newMagicWords = addMagicWordsField.getText();
			
			if (newMagicWords.startsWith("#"))
			{			
				JOptionPane.showMessageDialog(parent, "'#' denotes an inactive magic words", "Invalid character", JOptionPane.ERROR_MESSAGE);				
				addMagicWordsField.setText("");					
				return;
			}					
					
			if (!availableListModel.contains(newMagicWords))
				availableListModel.addElement(newMagicWords);					
				
			addMagicWordsField.setText("");		
		}
		
		private void handleRemoveMagicWords()
		{							
			if (availableList.isSelectionEmpty()) 
			{
				JOptionPane.showMessageDialog(parent, "No item being selected.", "Warning", JOptionPane.ERROR_MESSAGE);
				return;	
			}	
			 		
			int selectItem = availableList.getSelectedIndex();				
			availableListModel.remove(selectItem);															
		}							
	}
	
	UiMainWindow parent; 	
	
	JTextField manageIPAddr;
	JTextField managePublicCode;
	JTextField mirrorServerPort;
	JTextField addMagicWordsField;
	
	JButton addButton;
	JButton removeButton;
	JButton viewComplainButton;
	JButton updateButton;
	JButton cancelButton;
	JButton addNewMirrorServer;
	JButton addNewMagicWord;
	JButton removeMagicWord;
	
	Vector availableItems;
	Vector assignedItems;	
	
	JList availableList;
	JList allowedList;	
	DefaultListModel availableListModel;
	DefaultListModel allowedListModel;
	
	int serverManageType;
	MirrorServerLabelInfo msgLabelInfo;
	String mirrorFileName;
	
}
