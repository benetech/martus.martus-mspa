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
import java.awt.Font;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.martus.mspa.client.core.ManagingMirrorServerConstants;
import org.martus.mspa.client.core.MirrorServerMessageConverter;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class ManagingMirrorServersDlg extends JDialog
{
	public ManagingMirrorServersDlg(UiMainWindow owner, int manageType, 
			String serverToManage, String serverToManagePublicCode,
			Vector allList, Vector currentList)
	{
		super((JFrame)owner, "Managing Server Mirroring: "+MirrorServerMessageConverter.getTitle(manageType) , true);
		parent = owner;
		serverManageType = manageType;
		availableList = allList;
		assignedList = currentList;	
	
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(getTopPanel(), BorderLayout.NORTH);
		getContentPane().add(getCenterPanel(), BorderLayout.CENTER);
		getContentPane().add(getCommitButtonsPanel(), BorderLayout.SOUTH);
						
		Utilities.centerDlg(this);
	}
	
	private JPanel getTopPanel()
	{
		JPanel panel = new JPanel();				
		panel.setLayout(new ParagraphLayout());
		panel.setBorder(new LineBorder (Color.gray, 1));	

		JLabel manageTypeLabel = new JLabel(MirrorServerMessageConverter.getHeader(serverManageType));		
		manageTypeLabel.setFont(manageTypeLabel.getFont().deriveFont(Font.BOLD));
		
		JLabel manageIPAddrLabel = new JLabel("Manage IP Address: ");				
		manageIPAddr = new JTextField(10);
		manageIPAddr.requestFocus();
		JLabel managePublicCodeLabel = new JLabel("Public Code: ");
		managePublicCode = new JTextField(20);
		
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(manageTypeLabel);
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(manageIPAddrLabel);
		panel.add(manageIPAddr);
		panel.add(managePublicCodeLabel);
		panel.add(managePublicCode);
		
		return panel;				
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
		
		availableListModel = loadElementsToList(availableList);
		availableServers = new JList(availableListModel);
		availableServers.setFixedCellWidth(200);    
		JScrollPane ps = new JScrollPane();
		ps.getViewport().add(availableServers);
		JLabel availableLabel = new JLabel(MirrorServerMessageConverter.getAvailabledLabel(serverManageType));
				
		panel.add(availableLabel);
		panel.add(ps);

		return panel;
	}	
	
	private JPanel getAllowedPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		allowedListModel = loadElementsToList(assignedList);
		allowedServers = new JList(allowedListModel);
		allowedServers.setFixedCellWidth(200);    
		JScrollPane ps = new JScrollPane();
		ps.getViewport().add(allowedServers);
		JLabel allowedLabel = new JLabel(MirrorServerMessageConverter.getAllowedLabel(serverManageType));
				
		panel.add(allowedLabel);
		panel.add(ps);

		return panel;
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
		
	
		loadAvailableServerButton = createButton("Load Available Server");	
		viewComplainButton = createButton("View Compliance");			
		updateButton = createButton("Update");				
		cancelButton = createButton("Cancel");		
				
		if (serverManageType != ManagingMirrorServerConstants.ACT_AS_CLIENT)	
			panel.add(loadAvailableServerButton);
				
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
		}
		
		private void handleAddToAllowedList()
		{
			int selectItem = availableServers.getSelectedIndex();	
			if (!availableServers.isSelectionEmpty())
			{	
				String item = (String) availableServers.getSelectedValue();
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
			Vector magicWords = new Vector();			
			for (int i=0;i<items.length;i++)
				magicWords.add(items[i]);
							
			parent.getMSPAApp().updateMagicWordsToMartus(magicWords);
			dispose();							
		}
		
		private void handleRemoveFromAllowedList()
		{			
			int selectItem = allowedServers.getSelectedIndex();	
			if (!allowedServers.isSelectionEmpty())
			{	
				String item = (String) allowedServers.getSelectedValue();				
		
				allowedListModel.remove(selectItem);
				if (!availableListModel.contains(item))				
					availableListModel.addElement(item);
			}							
		}				
	}
	
	UiMainWindow parent; 	
	
	JTextField manageIPAddr;
	JTextField managePublicCode;
	
	JButton addButton;
	JButton removeButton;
	JButton viewComplainButton;
	JButton updateButton;
	JButton cancelButton;
	JButton loadAvailableServerButton;
	
	Vector availableList;
	Vector assignedList;
	
	
	JList availableServers;
	JList allowedServers;	
	DefaultListModel availableListModel;
	DefaultListModel allowedListModel;
	
	int serverManageType;
	
}
