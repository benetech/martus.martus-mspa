package org.martus.mspa.client.view;

import org.martus.common.crypto.MartusSecurity;
import org.martus.util.Base64.InvalidBase64Exception;


public class AccountNode
{
	public AccountNode(int id, String accountString, String status)
	{
		nodeId = id;
		accountId = accountString;
		try
		{																		
			publicCode = MartusSecurity.getFormattedPublicCode(accountString);
		}
		catch (InvalidBase64Exception e)
		{						
			e.printStackTrace();					
		}		
		accountStatus = status;
	}

	public int getNodeId() 
	{ 
		return nodeId;
	}

	public String getDisplayName() 
	{ 
		return publicCode;
	}

	public String getAccountStatus()
	{
		return accountStatus;
	}
	
	public String getAccountId()
	{
		return accountId;
	}
	

	public String toString() 
	{ 
		return publicCode;
	}

	protected int    nodeId;
	protected String publicCode;
	protected String accountStatus;
	protected String accountId;
}
