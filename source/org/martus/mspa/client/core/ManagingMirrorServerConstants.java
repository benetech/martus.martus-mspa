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
package org.martus.mspa.client.core;


public interface ManagingMirrorServerConstants
{
	public static int SERVER_WHO_WE_CALL	=100;
	public static int SERVER_WE_AMPLIFY		=101;
	public static int AMP_WHO_CALLS_US		=102;
	public static int WHO_CALLS_US			=103;
	public static int ACT_AS_CLIENT			=104;
	
	public static String SERVER_WHO_WE_CALL_TITLE			="Server Who We Call";
	public static String SERVER_WE_AMPLIFY_TITLE			="Server We Amplify";
	public static String AMP_WHO_CALLS_US_TITLE				="Amplify Who Calls Us";
	public static String WHO_CALLS_US_TITLE					="Who Calls Us";
	public static String ACT_AS_CLIENT_TITLE				="Act as Client Listener";
	
	public static String SERVER_WHO_WE_CALL_HEADER			="Backup My Data to Other Server";
	public static String SERVER_WHO_WE_CALL_ALLOWED_LABEL	="Can Backup My Data:";
	public static String SERVER_WE_AMPLIFY_HEADER			="All My Data to Amplify";
	public static String SERVER_WHO_AMPLIFY_ALLOWED_LABEL	="Can Amplify to My Data:";
	public static String AMP_WHO_WE_CALL_HEADER				="Be an Amplifier";
	public static String AMP_WHO_WE_CALL_ALLOWED_LABEL		="Amplier Data:";
	public static String WHO_CALLS_US_HEADER				="Act as A Backup For Other Server";
	public static String WHO_CALLS_US_ALLOWED_LABEL			="Can Upload Data";	
	public static String ACT_AS_CLIENT_HEADER				="Act as Client Backup Server";
	public static String ACT_AS_CLIENT_ALLOWED_LABEL		="Active Magic Words:";
	
	public static String AVAILABLE_SERVER_LABEL				="Available Servers:";
	public static String AVAILABLE_MAGIC_LABEL				="Available Magic Words:";	
	
}
