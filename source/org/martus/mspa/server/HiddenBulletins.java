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
package org.martus.mspa.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.martus.common.LoggerInterface;
import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.packet.UniversalId;
import org.martus.util.InputStreamWithSeek;
import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;
import org.martus.util.Base64.InvalidBase64Exception;


public class HiddenBulletins
{
		
	public HiddenBulletins(Database databaseToUse, 
			MartusCrypto securityToUse, LoggerInterface loggerToUse, File hiddenFileLocation)
	{
		database = databaseToUse;	
		hiddenUids = Collections.synchronizedSet(new HashSet());
		security = securityToUse;
		logger = loggerToUse;
		hiddenFile = hiddenFileLocation;
		
		readLineOfHiddenBulletinsFromFile();
	}

	public synchronized void addHiddenBulletinUids(UniversalId uid, String status) 
	{
		if (containHiddenUids(uid))
			return;
			
		String lineOfPacket = getLineOfHiddenPacket(uid, status);
		if (lineOfPacket != "")
		{
			HiddenBulletinInfo hiddenUid = new HiddenBulletinInfo(uid);
			hiddenUid.setLineOfHiddenBulletins(lineOfPacket);				
			hiddenUids.add(hiddenUid);					
			database.hide(uid);
		}
	}
	
	public Vector getListOfHiddenBulletins(String accountId)
	{
		Vector bulletins = new Vector();	
		if (hiddenUids == null) 
			return bulletins;
					
		for (Iterator itr = hiddenUids.iterator(); itr.hasNext();)
		{
			HiddenBulletinInfo uid = (HiddenBulletinInfo) itr.next();
			String localId = uid.getLocalId();
				
			if (uid.isSameAccountId(accountId) && uid.isBulletinHeaderPacket())			
				bulletins.add(localId);
		}				
		return bulletins;
	}
	
	public synchronized void writeLineOfHiddenBulletinsToFile(String accountId, UnicodeWriter writer) throws Exception
	{
		if (hiddenUids.size() <=0)
			return;
		
		for (Iterator itr = hiddenUids.iterator(); itr.hasNext();)
		{
			HiddenBulletinInfo bulletinInfo = (HiddenBulletinInfo) itr.next();
			String targetId = bulletinInfo.getAccountId();
			if (targetId.equals(accountId))
				writer.writeln("    "+bulletinInfo.getLineOfHiddenBulletins());	
		}					
	}

	private synchronized void readLineOfHiddenBulletinsFromFile()
	{				
		try
		{
			UnicodeReader reader = new UnicodeReader(hiddenFile);
			readRawPackets(reader);
		}
		catch(FileNotFoundException nothingToWorryAbout)
		{
			logger.log("Deleted packets file not found: " + hiddenFile.getName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.log("Error loading Deleted Packets file: " + hiddenFile.getName());
		}		
		
	}
		
	void readRawPackets(UnicodeReader reader) throws Exception, InvalidBase64Exception
	{	
		try
		{				
			String accountId = null;
			String lineOfPacketIds = null;			
		
			while(true)
			{
				String thisLine = reader.readLine();								
				if(thisLine == null)
					return;
		
				if(thisLine.startsWith(" "))
				{											
					lineOfPacketIds = thisLine;															
					String[] packetIds = lineOfPacketIds.trim().split("\\s+");			
		
					String localId = packetIds[0].trim();
					UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);

					lineOfPacketIds = removeExtraSpaces(thisLine);
					if (lineOfPacketIds != "")
					{
						HiddenBulletinInfo hiddenUid = new HiddenBulletinInfo(uid);
						hiddenUid.setLineOfHiddenBulletins(lineOfPacketIds);
						hiddenUids.add(hiddenUid);				
						
						hidePackets(accountId, packetIds);
					}		
				}
				else
					accountId = thisLine;			
			}		
		}
		finally
		{
			reader.close();
		}
	}
	
	void hidePackets(String accountId, String[] packetList) throws InvalidBase64Exception
	{	
		String publicCode = MartusCrypto.getFormattedPublicCode(accountId);
							
		for (int i = 0; i < packetList.length; i++)
		{
			String localId = packetList[i].trim();
			UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);					
			database.hide(uid);
			logger.log("Deleting " + publicCode + ": " + localId);
		}
	}

	private static String removeExtraSpaces(String str)
	{
		StringTokenizer st = new StringTokenizer(str);
		StringBuffer strBuffer = new StringBuffer();
		while (st.hasMoreTokens()) 
		{
			strBuffer.append(st.nextToken()).append(" ");
		}
		return strBuffer.toString().trim();
	}
	
	private boolean containHiddenUids(UniversalId uid)
	{
		for (Iterator itr = hiddenUids.iterator(); itr.hasNext();)
		{
			HiddenBulletinInfo id = (HiddenBulletinInfo) itr.next();
			if (id.isSameUid(uid))
				return true;
		}				

		return false;
	}
		
	private String getLineOfDetailsBulletin(BulletinHeaderPacket bhp) 
	{
		String bulletinLocalId = bhp.getLocalId();
		String dataLocalId = bhp.getFieldDataPacketId();
		String privateLocalId = bhp.getPrivateFieldDataPacketId();
		
		return (bulletinLocalId+ "  "+ dataLocalId+ "  "+ privateLocalId);				
	}
			
	public synchronized boolean hideBulletins(String accountId, Vector localIds)
	{									
		if (localIds == null || localIds.size() <=0)
			return false;
			
		for (int i=0;i<localIds.size();++i)
		{					
			String item = (String) localIds.get(i);
			int tabSepeartor = item.indexOf("\t");		
			String localId = item.substring(0,tabSepeartor-1);
			String status = item.substring(tabSepeartor+1);
			if (BulletinHeaderPacket.isValidLocalId(localId))
			{					
				UniversalId uid = UniversalId.createFromAccountAndLocalId(accountId, localId);			 		
				addHiddenBulletinUids(uid, status);
			}
		}
		return true;		
	}

	private String getLineOfHiddenPacket(UniversalId uid, String status) 
	{
		BulletinHeaderPacket bhp = new BulletinHeaderPacket(uid);	
		try
		{			
			DatabaseKey headerKey = DatabaseKey.createKey(uid, status);
			if (!database.doesRecordExist(headerKey))
			{
				String errorMessage = "The bulletin ("+uid.getLocalId()+" ) does not exist in database.\n" ;
				JOptionPane.showMessageDialog(null, errorMessage, "MSPA Listener:", JOptionPane.ERROR_MESSAGE);
				return "";
			}					

			InputStreamWithSeek in = database.openInputStream(headerKey, security);				
			bhp.loadFromXml(in, security);
			in.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.log("geLineOfHiddenPacket(): " + e.toString());
		}	
																
		return getLineOfDetailsBulletin(bhp);
	}

	class HiddenBulletinInfo
	{	
		public HiddenBulletinInfo (UniversalId uId)
		{		
			hiddenUid = uId;
		}

		public boolean isSameAccountId(String accountId)
		{
			return (getAccountId().equals(accountId));
		}		

		public boolean isBulletinHeaderPacket()
		{
			return BulletinHeaderPacket.isValidLocalId(hiddenUid.getLocalId());
		}

		public boolean isSameUid(UniversalId uid)
		{
			return (isSameAccountId(hiddenUid.getAccountId()) &&
				uid.getLocalId().equals(hiddenUid.getLocalId()));
		}		
		
		public void setLineOfHiddenBulletins(String hiddenBulletins)
		{
			lineOfHiddenBulletins = hiddenBulletins;
		}
				
		public UniversalId getUid() {return hiddenUid;}
		public String getAccountId(){return hiddenUid.getAccountId();}
		public String getLocalId(){return hiddenUid.getLocalId();}
		public String getLineOfHiddenBulletins() {return lineOfHiddenBulletins;}		

		UniversalId hiddenUid;
		String lineOfHiddenBulletins;
	}
		
	Database database;
	Set hiddenUids;
	LoggerInterface logger;
	MartusCrypto security;
	File hiddenFile;
}
