
package org.martus.mspa.network;

import java.io.IOException;
import java.util.Vector;


public interface NetworkInterface
{
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException;
}
