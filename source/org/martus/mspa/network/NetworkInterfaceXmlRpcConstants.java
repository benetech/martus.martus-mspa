
package org.martus.mspa.network;


public interface NetworkInterfaceXmlRpcConstants
{
	public static final String serverObjectName = "MSPAServer";
	
	public final static String cmdPing = "ping";
	public static final String cmdGetAccountIds = "getAccountIds";
	public static final String cmdGetListPackets = "getListPackets";
	public static final String cmdGetContactInfo = "getContactInfo";
	public static final String cmdGetMagicWords = "getMagicWords";
	public static final String cmdGetMagicWordsFromMartus = "getMagicWordsFromMartus";
	public static final String cmdUpdateMagicWords = "updateMagicWords";
	public static final String cmdUpdateMagicWordsToMarus = "updateMagicWordsToMartus";
	
	public static final String cmdGetListOfServersWhoWeCall = "getServersWhoWeCallAccounts";
	public static final String cmdGetListOfAmplifyWhoWeCall = "getAmplifyWhoCallUsAccounts";
	public static final String cmdGetListOfServersWhoCallUs = "getServersWhoCallUsAccounts";
	

}
