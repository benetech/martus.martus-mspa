
package org.martus.mspa.network;

import java.util.Vector;

import org.martus.common.crypto.MartusCrypto;
import org.martus.common.database.Database;
import org.martus.common.database.DatabaseKey;
import org.martus.common.packet.BulletinHeaderPacket;
import org.martus.common.utilities.MartusServerUtilities;
import org.martus.mspa.server.MSPAServer;
import org.martus.util.ByteArrayInputStreamWithSeek;
;

public class ServerSideHandler implements NetworkInterface
{
	public ServerSideHandler(MSPAServer serverToUse)
	{
		server = serverToUse;	
	}
	
	public Vector ping()
	{
		String version = server.ping();
		Vector data = new Vector();
		data.add(version);
		
		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.OK);
		result.add(data);
			
		return result;	
	}

	public Vector getAccountIds(String myAccountId, Vector parameters, String signature)
	{
		
		class AccountVisitor implements Database.AccountVisitor
		{
			AccountVisitor()
			{
				accounts = new Vector();
			}
	
			public void visit(String accountString)
			{
				LocallIdOfPublicBulletinsCollector collector = new LocallIdOfPublicBulletinsCollector();				
				Database db = server.getDatabase();
				db.visitAllRecordsForAccount(collector, accountString);
				
				if(collector.infos.size() > 0 && ! accounts.contains(accountString))
					accounts.add(accountString);
			}
	
			public Vector getAccounts()
			{
				return accounts;
			}
			Vector accounts;
		}
		
		Vector result = new Vector();
		if(!isSignatureOk(myAccountId, parameters, signature, server.getSecurity()))
		{
			result.add(NetworkInterfaceConstants.SIG_ERROR);				
			return result;
		}
					
		AccountVisitor visitor = new AccountVisitor();
		
		result.add(NetworkInterfaceConstants.OK);
		result.add(visitor.getAccounts());		
		return result;
	}
		
	class LocallIdOfPublicBulletinsCollector implements Database.PacketVisitor
	{
		public void visit(DatabaseKey key)
		{
			try
			{
				if(! key.getLocalId().startsWith("B-") )
					return;
				if(key.isDraft())
					return;
				
				DatabaseKey burKey = MartusServerUtilities.getBurKey(key);
				String burInDatabase = server.getDatabase().readRecord(burKey, server.getSecurity());
				if(!MartusServerUtilities.wasBurCreatedByThisCrypto(burInDatabase, server.getSecurity()))
					return;				
							
				String headerXml = server.getDatabase().readRecord(key, server.getSecurity());
				byte[] headerBytes = headerXml.getBytes("UTF-8");
				
				ByteArrayInputStreamWithSeek headerIn = new ByteArrayInputStreamWithSeek(headerBytes);
				BulletinHeaderPacket bhp = new BulletinHeaderPacket("");
				bhp.loadFromXml(headerIn, null);
				if(! bhp.isAllPrivate())
				{
					infos.add(key.getLocalId());
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		Vector infos = new Vector();
	}

	private boolean isSignatureOk(String myAccountId, Vector parameters, String signature, MartusCrypto verifier)
	{
		return verifier.verifySignatureOfVectorOfStrings(parameters, myAccountId, signature);
	}	
			
	MSPAServer server;
}
