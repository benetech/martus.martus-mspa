
package org.martus.mspa.client.core;


public class AccountAdminOptions
{
	public boolean isCanUploadSelected() {return updateOption;}
	public boolean isBannedSelected(){return bannedOption;}
	public boolean isCanSendToAmplifySelected() {return canSendOption;}
	public boolean isAmplifierSelected() {return amplifierOption;}


	public void setCanUpdateOption(boolean option) 
	{
		updateOption=option;
	}

	public void setBannedOption(boolean option) 
	{
		bannedOption=option;
	}

	public void setCanSendOption(boolean option)
	{
		canSendOption = option;
	}	

	public void setAmplifierOption(boolean option)
	{
		amplifierOption = option;
	}	

	public AccountAdminOptions(){}

	boolean updateOption;
	boolean bannedOption;
	boolean canSendOption;
	boolean amplifierOption;
	
}
