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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class ServerConnectionDlg extends JDialog
{
	public ServerConnectionDlg(UiMainWindow owner, Vector availableServers) 
	{				
		super((JFrame)owner, "Server to Connect: ", true);
		parent = owner;		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
		mainPanel.add(displayAvailableServerPane(availableServers), BorderLayout.CENTER);
		mainPanel.add(buildButtonsPanel(), BorderLayout.SOUTH);						
		
		getContentPane().add(mainPanel);
		Utilities.centerDlg(this);
		setResizable(false);
	}
	
	private JTabbedPane displayAvailableServerPane(Vector availableServers)
	{
		availableServerTabPane = new JTabbedPane();				
		availableServerTabPane.setTabPlacement(JTabbedPane.TOP);
					
		availabelServerListModel = loadElementsToList(availableServers);
		availabelServerList = createServerList(availabelServerListModel);
		availabelServerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);						
		
		availableServerTabPane.add(createAddServerPane(), 0);
		availableServerTabPane.setTitleAt(0, "Add Server");

		availableServerTabPane.add(createDisplayServerListPane(), 1);
		availableServerTabPane.setTitleAt(1, "Which Server");
		
		if (availableServers.size() >0)
			availableServerTabPane.setSelectedIndex(1);
	
		return availableServerTabPane;
	}
	
	private JPanel createAddServerPane()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));		
		panel.setLayout(new ParagraphLayout());
		
		serverIPField = new JTextField("", 20);
		serverPublicCodeField = new JTextField("", 20);
		
		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel("Enter Server IP: ")); 
		panel.add(serverIPField);
		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel("Enter Server Public Code: ")); 
		panel.add(serverPublicCodeField);
		
		return panel;
	}
	
	private JPanel createDisplayServerListPane()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5,5,5,5));		
		panel.setLayout(new BorderLayout());
		
		JScrollPane sp = new JScrollPane(availabelServerList);
		panel.add(sp, BorderLayout.CENTER);
		return panel;
	}
	
	private JList createServerList(DefaultListModel dataModel)
	{
		JList list = new JList(dataModel);
		list.setFixedCellWidth(220);
		configureTabList(list);

		return list;
	}
	
	private DefaultListModel loadElementsToList(Vector items)
	{
		DefaultListModel listModel = new DefaultListModel();
		
		for (int i=0; i<items.size();i++)
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
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();		
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT));
							
		ok = new JButton("Ok");
		ok.addActionListener(new CommitButtonHandler());	
		panel.add(ok);					
		
		cancel = new JButton("Cancel");
		cancel.addActionListener(new CommitButtonHandler());
		panel.add(cancel);

		return panel;
	}	
	
	public String getServerIPToUse()
	{
		return serverIpToUse;
	}
	
	public String getServerPublicCodeToUse()
	{
		return serverPublicCodeToUse;
	}	
	
	public boolean isErrorMessagePopped()
	{
		if (serverIpToUse.length() <=0 || serverPublicCodeToUse.length() <=0)
		{
			JOptionPane.showMessageDialog(parent, "MSPA Server IP and Public code are required.", "MSPA Error Message", JOptionPane.ERROR_MESSAGE);
			return true;					
		}	
		
		return false;
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(ok))				
				handleServerToCall();

			if (ae.getSource().equals(cancel))				
				handleCancel();				
		}
		
		private void handleServerToCall()
		{
			if (availableServerTabPane.getSelectedIndex()==0)
			{
				serverIpToUse = serverIPField.getText();
				serverPublicCodeToUse = serverPublicCodeField.getText();
				
				if (!isErrorMessagePopped())
				{
					parent.getMSPAApp().setCurrentServerIp(serverIpToUse);
					parent.getMSPAApp().setCurrentServerPublicCode(serverPublicCodeToUse);
					dispose();
				}				
			}	
			else 
			{	
				if (!availabelServerList.isSelectionEmpty())
				{
					String item = (String) availabelServerList.getSelectedValue();
					serverIpToUse = item.substring(0, item.indexOf("\t"));
					serverPublicCodeToUse= item.substring(item.indexOf("\t")+1);
					parent.getMSPAApp().setCurrentServerIp(serverIpToUse.trim());
					parent.getMSPAApp().setCurrentServerPublicCode(serverPublicCodeToUse.trim());
					dispose();
				}
				else
					isErrorMessagePopped();	
			}
		}
		
		private void handleCancel()
		{			
			if (!isErrorMessagePopped())
				 dispose();
		}
	}

	
	UiMainWindow parent;
	JList availabelServerList;	
	DefaultListModel availabelServerListModel;
	
	JTabbedPane availableServerTabPane;
	JTextField serverIPField;
	JTextField serverPublicCodeField;
	
	JButton ok;
	JButton cancel;
	
	String serverIpToUse="";
	String serverPublicCodeToUse=""; 
}
