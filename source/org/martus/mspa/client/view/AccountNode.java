package org.martus.mspa.client.view;

import org.martus.common.crypto.MartusCrypto;
import org.martus.util.Base64.InvalidBase64Exception;


public class AccountNode
{
	public AccountNode(String accountString, String status)
	{		
		accountId = accountString;
		try
		{																		
			publicCode = MartusCrypto.getFormattedPublicCode(accountString);
		}
		catch (InvalidBase64Exception e)
		{						
			e.printStackTrace();					
		}		
		accountStatus = status;
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

	protected String publicCode;
	protected String accountStatus;
	protected String accountId;
}
