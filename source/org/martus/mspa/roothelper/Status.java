/*

The Martus(tm) free, social justice documentation and
monitoring software. Copyright (C) 2001-2004, Beneficent
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



public class Status
{
	public String stdErrorMsg="";
	public String stdOutMsg="";
	public String status="";
	
	public Status()
	{
		this(SUCCESS);
	}
	
	public Status(String msgStatus) 
	{		
		status = msgStatus;
	}	
	
	public void setStatus(String returnStatus)
	{
		status = returnStatus;	
	}	
	
	public void setStdErrorMsg(String msg)
	{
		stdErrorMsg = msg;
	}
	
	public void setStdOutMsg(String msg)
	{
		stdOutMsg = msg;
	}
	
	public boolean isSuccess()
	{
		return (status.equals(SUCCESS))?true:false;
	}
		
	public String getAllMessages()
	{
		String newLine = System.getProperty("line.separator");
		StringBuffer message = new StringBuffer();
		if (stdErrorMsg != "")
			message.append(newLine).append("stderr: ").append(stdErrorMsg);
			
		if (stdOutMsg != "")	
			message.append(newLine).append("stdout: ").append(stdOutMsg);
			
		return message.toString();	
	}
	
	public String getStdErrorMsg() {return stdErrorMsg;}
	public String getStdOutMsg() {return stdOutMsg;}	
	public String getStatus() {return status;}

	public static final String SUCCESS = "success";
	public static final String FAILED = "failed";
}
