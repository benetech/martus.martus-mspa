
package org.martus.mspa.network;

import java.io.IOException;
import java.util.Vector;


public interface NetworkInterface 
{
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException;
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId) throws IOException;
	public Vector getInActiveMagicWords(String myAccountId) throws IOException;
	public Vector getActiveMagicWords(String myAccountId) throws IOException;
	public Vector getAllMagicWords(String myAccountId) throws IOException;
	public Vector updateMagicWords(String myAccountId, Vector magicWords) throws IOException;
}
