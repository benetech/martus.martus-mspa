
package org.martus.mspa.common;

import java.util.Vector;


public class AccountAdminOptions
{
	public boolean canUploadSelected() {return uploadOption;}
	public boolean isBannedSelected(){return bannedOption;}
	public boolean canSendToAmplifySelected() {return canSendOption;}
//	public boolean isAmplifierSelected() {return amplifierOption;}


	public void setCanUploadOption(boolean option) 
	{
		uploadOption=option;
	}

	public void setBannedOption(boolean option) 
	{
		bannedOption=option;
	}

	public void setCanSendOption(boolean option)
	{
		canSendOption = option;
	}	
	
	public Vector getOptions()
	{
		Vector options = new Vector();
		options.add(CAN_UPLOAD, new Boolean(uploadOption));
		options.add(BANNED, new Boolean(bannedOption));
		options.add(CAN_SEND, new Boolean(canSendOption));
		
		return options;
	}	
	
	public void setOptions(Vector options)
	{
		Boolean canUpload = (Boolean) options.get(CAN_UPLOAD);
		Boolean banned = (Boolean) options.get(BANNED);
		Boolean canSendToAmp = (Boolean) options.get(CAN_SEND);
							
		setBannedOption(banned.booleanValue());
		setCanSendOption(canSendToAmp.booleanValue());
		setCanUploadOption(canUpload.booleanValue());
		
	}

	public AccountAdminOptions(){}

	boolean uploadOption=false;
	boolean bannedOption=false;
	boolean canSendOption=false;
//	boolean amplifierOption=false;
	
	public static int CAN_UPLOAD =0;
	public static int BANNED	 = 1;
	public static int CAN_SEND = 2;
//	public static int AMPLIFIER = 3; 
	
}
