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

import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import org.martus.common.VersionBuildDate;
import org.martus.common.clientside.UiBasicLocalization;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.Utilities;

public class AboutDlg extends JDialog implements ActionListener
{
	public AboutDlg(UiMainWindow owner) throws HeadlessException
	{
		super(owner, "" , true);
		UiBasicLocalization localization = owner.getLocalization();
		
		setTitle(localization.getWindowTitle("about"));
		
		JPanel panel = new JPanel();		
		panel.setBorder(new EmptyBorder(8,8,8,8));

		String versionInfo = UiConstants.programName;
		versionInfo += " " + localization.getFieldLabel("aboutDlgVersionInfo");
		versionInfo += " " + UiConstants.versionLabel;

		String buildDate = localization.getFieldLabel("aboutDlgBuildDate");
		buildDate += " " + VersionBuildDate.getVersionBuildDate();

		JButton ok = new JButton(localization.getButtonLabel("ok"));
		ok.addActionListener(this);			
				
		Box vBoxVersionInfo = Box.createVerticalBox();
		vBoxVersionInfo.add(new JLabel(versionInfo));
		vBoxVersionInfo.add(new JLabel(" "));
		vBoxVersionInfo.add(new JLabel(" "));
		vBoxVersionInfo.add(new JLabel(UiConstants.copyright));
		vBoxVersionInfo.add(new JLabel(UiConstants.website));
		vBoxVersionInfo.add(new JLabel(" "));
		vBoxVersionInfo.add(new JLabel(buildDate, JLabel.CENTER));
		vBoxVersionInfo.add(new JLabel(" "));

		Box hBoxVersionAndIcon = Box.createHorizontalBox();
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		hBoxVersionAndIcon.add(vBoxVersionInfo);
		hBoxVersionAndIcon.add(Box.createHorizontalGlue());
		
		Box hBoxOk = Box.createHorizontalBox();
		hBoxOk.add(Box.createHorizontalGlue());	
		hBoxOk.add(ok);
		hBoxOk.add(Box.createHorizontalGlue());							
		
		Box vBoxAboutDialog = Box.createVerticalBox();
		vBoxAboutDialog.add(hBoxVersionAndIcon);
		vBoxAboutDialog.add(hBoxOk);		
		
		panel.add(vBoxAboutDialog);
		getContentPane().add(panel);
		Utilities.centerDlg(this);
		show();
	}	

	public void actionPerformed(ActionEvent ae)
	{
		dispose();
	}
}
