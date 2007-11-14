/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2007, Beneficent
Technology, Inc. (Benetech).

Martus is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either
version 2 of the License, or (at your option) any later
version with the additions and exceptions described in the
accompanying Martus license file entitled "license.txt".

It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, including warranties of fitness of purpose or
merchantability.  See the accompanying Martus License and
GPL license for more details on the required license terms
for this software.

You should have received a copy of the GNU General Public
License along with this program; if not, write to the Free
Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA 02111-1307, USA.

*/
package org.martus.mspa.roothelper;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;

public class RootHelperHandler
{

	public RootHelperHandler(LoggerInterface loggerToUse)
	{
		logger = loggerToUse;
	}
	
	public String startServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.startServices");
		return RESULT_ERROR;		
	}
	
	public String restartServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.restartServices");
		return RESULT_ERROR;		
	}
	
	public String stopServices()
	{
		logger.logDebug("RootHelper.stopServices");
		int result = executeAndWait(SERVICE_STOP);		
		if(result == 0)
			return RESULT_OK;
		
		MartusLogger.logError("Error stopping service, exit code: " + result);
		return RESULT_ERROR;		
	}

	public String getStatus()
	{
		logger.logDebug("RootHelper.getStatus");
		int result = executeAndWait(SERVICE_STATE);
		if(result == 0)
			return RESULT_OK;
		
		MartusLogger.logError("Error stopping service, exit code: " + result);
		return RESULT_ERROR;		
	}
	
	private int executeAndWait(String command)
	{
		try
		{
			Process p = Runtime.getRuntime().exec(MARTUS_SERVICE + " " + command);
			return p.waitFor();
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return -1;
		}
		
	}
	
	public static String RootHelperObjectName = "RootHelper";
	public static String RootHelperStartServicesCommand = "startServices";
	public static String RootHelperRestartServicesCommand = "restartServices";
	public static String RootHelperStopServicesCommand = "stopServices";
	public static String RootHelperGetStatusCommand = "getStatus";
	
	private static final String MARTUS_SERVICE = "/etc/init.d/martus";
	private static final String SERVICE_STOP = "stop";
	private static final String SERVICE_STATE = "state";
	
	public static final String RESULT_OK = "OK";
	public static final String RESULT_ERROR = "ERROR";

	LoggerInterface logger;
}