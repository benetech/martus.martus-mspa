
package org.martus.mspa.network;

import java.io.IOException;
import java.util.Vector;


public interface NetworkInterface 
{
	public Vector getAccountIds(String myAccountId, Vector parameters, String signature) throws IOException;
	public Vector getContactInfo(String myAccountId, Vector parameters, String signature, String accountId) throws IOException;	
	public Vector getAccountManageInfo(String myAccountId, String manageAccountId) throws IOException;
	public Vector updateAccountManageInfo(String myAccount,String manageAccountId, Vector accountInfo) throws IOException;	
	
	public Vector getListOfBulletinIds(String myAccountId) throws IOException;
	public Vector getListOfHiddenBulletinIds(String myAccountId, String manageAccountId) throws IOException;
	public Vector removeHiddenBulletins(String myAccountId, String manageAccountId, Vector localIds) throws IOException;
	
	public Vector getInactiveMagicWords(String myAccountId) throws IOException;
	public Vector getActiveMagicWords(String myAccountId) throws IOException;
	public Vector getAllMagicWords(String myAccountId) throws IOException;
	public Vector updateMagicWords(String myAccountId, Vector magicWords) throws IOException;
	
	public Vector addAvailableMirrorServer(String myAccountId, Vector mirrorInfo) throws IOException;
	public Vector getListOfAvailableServers(String myAccountId) throws IOException;
	public Vector getListOfAssignedServers(String myAccountId,int mirrorType) throws IOException;
	public Vector updateManagingMirrorServers(String myAccountId, Vector mirrorInfo, int mirrorType) throws IOException;
	
	public Vector getServerCompliance(String myAccountId) throws IOException;
	public Vector updateServerCompliance(String myAccountId, String compliantsMsg) throws IOException;
		
	public Vector sendCommandToServer(String myAccountId, String type, String cmd) throws IOException;
	
	public Vector getMartusServerArguments(String myAccountId) throws IOException;
	public Vector updateMartusServerArguments(String myAccountId, Vector args) throws IOException;
	
}
