/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
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
package org.martus.mspa.mail;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.martus.common.MartusLogger;

public class MailSender
{
	public MailSender()
	{
		
	}
	
	public void setRecipients(String smtpHostName, String[] recipients) throws Exception
	{
		smtpHost = smtpHostName;
		recipient = new InternetAddress(recipients[0]);
		
		Properties props = System.getProperties();
		props.put("mail.smtp.host", smtpHost);
		session = Session.getDefaultInstance(props, null);
	}
	
	public void sendMail(String actionSummary) throws Exception
	{
		MartusLogger.log("MAIL: Queueing email to send: " + actionSummary);
		MimeMessage message = createMessage(actionSummary);
		new SenderThread(message).start();
	}

	static class SenderThread extends Thread
	{
		public SenderThread(MimeMessage messageToSend)
		{
			message = messageToSend;
		}
		
		public void run()
		{
			try
			{
				Transport.send(message);
				MartusLogger.log("MAIL: Send completed");
			} 
			catch (Exception e)
			{
				MartusLogger.log("MAIL: Send failed");
				MartusLogger.logException(e);
			}
		}
		
		MimeMessage message;
	}
	
	MimeMessage createMessage(String actionSummary) throws AddressException, MessagingException
	{
		MimeMessage message = new MimeMessage(session);
		message.setFrom(recipient);
		message.setRecipient(RecipientType.TO, recipient);
		message.setSubject("[MSPA] " + actionSummary);
		message.setText(actionSummary);
		return message;
	}
	
	private String smtpHost;
	private InternetAddress recipient;
	private Session session;
}
