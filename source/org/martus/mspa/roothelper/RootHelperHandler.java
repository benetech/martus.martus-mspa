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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.martus.common.LoggerInterface;
import org.martus.common.MartusLogger;

public class RootHelperHandler
{

	public RootHelperHandler(LoggerInterface loggerToUse)
	{
		logger = loggerToUse;
	}
	
	public Vector startServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.startServices");
		Status currentState = new Status(getStatus());
		if(!currentState.isSuccess())
			return currentState.toVector();
		if(!currentState.getDetailText().equals("down"))
			return Status.createFailure("Service is already starting or up").toVector();
		return executeWithoutWaiting(SERVICE_START, martusServicePassword).toVector();
	}
	
	public Vector restartServices(String martusServicePassword)
	{
		logger.logDebug("RootHelper.restartServices");
		Status currentState = new Status(getStatus());
		if(!currentState.isSuccess())
			return currentState.toVector();
		if(!currentState.getDetailText().equals("up"))
			return Status.createFailure("Service is not currently up").toVector();
		return executeWithoutWaiting(SERVICE_RESTART, martusServicePassword).toVector();
	}
	
	public Vector stopServices()
	{
		logger.logDebug("RootHelper.stopServices");
		Status currentState = new Status(getStatus());
		if(!currentState.isSuccess())
			return currentState.toVector();
		if(!currentState.getDetailText().equals("up"))
			return Status.createFailure("Service is not currently up").toVector();
		Vector result = executeAndWait(SERVICE_STOP, null).toVector();		
		return result;
	}

	public Vector getStatus()
	{
		logger.logDebug("RootHelper.getStatus");
		Vector result = executeAndWait(SERVICE_STATE, null).toVector();
		return result;
	}
	
	private Status executeWithoutWaiting(String command, String password)
	{
		try
		{
			Process process = executeProcess(command, password);		
			int exitCode = process.waitFor();
	
			if (exitCode == 0)
				return Status.createSuccess("");
			
			return Status.createFailure(Integer.toString(exitCode));
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return Status.createFailure(e.getMessage());
		}
		
	}
	
	private Status executeAndWait(String command, String password)
	{
		try
		{
			Process process = executeProcess(command, password);		
	
			StreamGobbler errorGobbler = new StreamGobbler(process.getErrorStream(), "stderr");
			
			StreamGobbler outputGobbler = new StreamGobbler(process.getInputStream(), "stdout");
			errorGobbler.start();
			outputGobbler.start();
			
			int exitCode = process.waitFor();
			errorGobbler.join(2000);
			outputGobbler.join(2000);
			MartusLogger.log("Closing streams");
			process.getOutputStream().close();
			outputGobbler.close();
			errorGobbler.close(); 
			MartusLogger.log("Finished closing");

			if (exitCode == 0)
				return Status.createSuccess(outputGobbler.getTextBuffer());
			
			return Status.createFailure(Integer.toString(exitCode) + ": " + errorGobbler.getTextBuffer());
		}
		catch (Exception e)
		{
			MartusLogger.logException(e);
			return Status.createFailure(e.getMessage());
		}
	}

	private Process executeProcess(String command, String password) throws IOException
	{
		String commandLine = MARTUS_SERVICE + " " + command;
		MartusLogger.log("Executing: " + commandLine);							
		Process process = Runtime.getRuntime().exec(commandLine);

		if (password != null)
		{	
			BufferedWriter buffStdin = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
			buffStdin.write(password + "\r");
			buffStdin.flush(); 
		}
		
		return process;
	}

	public static String RootHelperObjectName = "RootHelper";
	public static String RootHelperStartServicesCommand = "startServices";
	public static String RootHelperRestartServicesCommand = "restartServices";
	public static String RootHelperStopServicesCommand = "stopServices";
	public static String RootHelperGetStatusCommand = "getStatus";
	
	private static final String MARTUS_SERVICE = "/etc/init.d/martus -p";
	private static final String SERVICE_START = "start";
	private static final String SERVICE_RESTART = "restart";
	private static final String SERVICE_STOP = "stop";
	private static final String SERVICE_STATE = "state";
	
	public static final String RESULT_OK = "OK";
	public static final String RESULT_ERROR = "ERROR";
	
	public static final String ERROR_DETAIL_NOT_IMPLEMENTED_YET = "Not Implemented Yet";

	LoggerInterface logger;
}