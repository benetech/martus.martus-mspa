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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.martus.mspa.main.UiMainWindow;
import org.martus.mspa.network.NetworkInterfaceConstants;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.UiWrappedTextArea;
import org.martus.swing.Utilities;


public class ServerCommandDlg extends JDialog
{
	public ServerCommandDlg(UiMainWindow owner, String nameOfButton)
	{
		super((JFrame)owner, nameOfButton+" Martus Server", true);		
		parent = owner;				
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());		
		mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
								
		mainPanel.add(buildArgumentsTextArea(), BorderLayout.CENTER);
		mainPanel.add(buildButtonsPanel(nameOfButton), BorderLayout.SOUTH);						

		getContentPane().add(mainPanel);
		Utilities.centerDlg(this);
		setResizable(false);
	}
	
	private JPanel buildArgumentsTextArea()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new ParagraphLayout());
		
		JLabel label = new JLabel("Enter Martus Arguments:");
		
		retrieveArguments = new UiWrappedTextArea(argumentTemplate);
		retrieveArguments.setBackground(Color.WHITE);
		retrieveArguments.setEditable(true);
		
		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(label); 
		panel.add(new JLabel("") , ParagraphLayout.NEW_PARAGRAPH);
		panel.add(retrieveArguments);
		
		return panel;
	}
	
	private JPanel buildButtonsPanel(String buttonName)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		submmitButton = new JButton(buttonName);	
		submmitButton.addActionListener(new CommitButtonHandler());	
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CommitButtonHandler());

		panel.add(submmitButton);
		panel.add(cancelButton);

		return panel;
	}	
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(cancelButton))				
				dispose();
			else if (ae.getSource().equals(submmitButton))
				handleSendCommand();
			
		}

		private void handleSendCommand()
		{					
			parent.setStatusText("Send start command to MSPA server ...");
			String startCommand = "java "+retrieveArguments.getText();
			String result = parent.getMSPAApp().sendCmdToServer(NetworkInterfaceConstants.START_SERVER,startCommand);
			parent.setStatusText("Start Martus Server Status: "+result);	
			dispose();			
		}
	}
	
	UiMainWindow parent;
	JButton submmitButton;
	JButton cancelButton;
	UiWrappedTextArea retrieveArguments;
	String argumentTemplate="--listeners-ip=10.10.220.41 --nopassword --amplifier-ip=127.0.0.1 --amplifier-indexing-minutes=5 --amplifier --client-listener --mirror-listener --amplifier-listener";
}
