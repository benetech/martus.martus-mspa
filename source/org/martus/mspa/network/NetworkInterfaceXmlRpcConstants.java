
package org.martus.mspa.network;


public interface NetworkInterfaceXmlRpcConstants
{
	public static final String serverObjectName = "MSPAServer";
	
	public final static String cmdPing = "ping";
	public final static String cmdGetCompliance = "getServerCompliance";
	public final static String cmdUpdateCompliance = "updateServerCompliance";
	public static final String cmdGetAccountIds = "getAccountIds";
	public static final String cmdGetListPackets = "getListPackets";
	public static final String cmdGetContactInfo = "getContactInfo";
	public static final String cmdGetAccountManageInfo = "getAccountManageInfo";
	
	public static final String cmdGetAllMagicWords = "getAllMagicWords";
	public static final String cmdGetActiveMagicWords = "getActiveMagicWords";
	public static final String cmdGetInActiveMagicWords = "getInactiveMagicWords";
	public static final String cmdUpdateMagicWords = "updateMagicWords";	
	
	public static final String cmdGetNumOfHiddenBulletins = "getNumOfHiddenBulletins";
	public static final String cmdGetListOfHiddenBulletinIds = "getListOfHiddenBulletinIds";
	public static final String cmdRemoveHiddenBulletins = "removeHiddenBulletins";	
	public static final String cmdGetListOfBulletinIds = "getListOfBulletinIds";
	public static final String cmdRecoverHiddenBulletins = "recoverHiddenBulletins";
		
	public static final String cmdUpdateAccountManageInfo = "updateAccountManageInfo";		
	public static final String cmdGetListOfAvailableServers = "getListOfAvailableServers";		
	public static final String cmdGetListOfAssignedServers = "getListOfAssignedServers";
	public static final String cmdAddAvailableMirrorServer = "addAvailableMirrorServer";
	public static final String cmdUpdateManagingMirrorServers = "updateManagingMirrorServers";
	
	public static final String cmdSendCommandToServer = "sendCommandToServer";
	
	public static final String cmdGetMartusServerArguments = "getMartusServerArguments";
	public static final String cmdUpdateMartusServerArguments = "updateMartusServerArguments";
		
}
