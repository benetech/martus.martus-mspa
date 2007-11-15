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

import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;
import org.martus.mspa.common.network.NetworkInterfaceConstants;

public class RootHelperHandler
{

	public RootHelperHandler(LoggerInterface loggerToUse)
	{
		logger = loggerToUse;
	}
	
	public Vector startServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.startServices");

		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.EXEC_ERROR);
		result.add(ERROR_DETAIL_NOT_IMPLEMENTED_YET);
		return result;		
	}
	
	public Vector restartServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.restartServices");

		Vector result = new Vector();
		result.add(NetworkInterfaceConstants.EXEC_ERROR);
		result.add(ERROR_DETAIL_NOT_IMPLEMENTED_YET);
		return result;		
	}
	
	public Vector stopServices()
	{
		logger.logDebug("RootHelper.stopServices");
		Vector result = executeAndWait(SERVICE_STOP);		
		return result;
	}

	public Vector getStatus()
	{
		logger.logDebug("RootHelper.getStatus");
		Vector result = executeAndWait(SERVICE_STATE);
		return result;
	}
	
	private Vector executeAndWait(String command)
	{
		Vector result = new Vector();
		try
		{
			Process p = Runtime.getRuntime().exec(MARTUS_SERVICE + " " + command);
			int exitCode = p.waitFor();
			if(exitCode == 0)
			{
				result.add(RESULT_OK);
				result.add("status output goes here");
			}
			else
			{
				result.add(RESULT_ERROR);
				result.add(Integer.toString(exitCode));
			}
		} 
		catch (Exception e)
		{
			MartusLogger.logException(e);
			result.add(RESULT_ERROR);
			result.add(e.getMessage());
		}
		
		return result;
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
	
	public static final String ERROR_DETAIL_NOT_IMPLEMENTED_YET = "Not Implemented Yet";

	LoggerInterface logger;
}