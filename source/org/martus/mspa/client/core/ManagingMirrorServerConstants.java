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
	public static int SERVERS_WHO_WE_MIRROR		=100;
	public static int SERVERS_WE_AMPLIFY		=101;
	public static int SERVERS_WHO_AMPLIFY_US	=102;
	public static int SERVERS_WHO_MIRROR_US	=103;

	public static String SERVERS_WHO_WE_MIRROR_TITLE			="Servers Who We Mirror";
	public static String SERVERS_WE_AMPLIFY_TITLE				="Servers We Amplify";
	public static String SERVERS_WHO_AMPLIFY_US_TITLE			="Servers Who Amplify Us";
	public static String SERVERS_WHO_MIRROR_US_TITLE			="Servers Who Mirror Us";
	
	public static String SERVERS_WHO_WE_MIRROR_HEADER			="Backup My Data to Other Servers";
	public static String SERVERS_WHO_WE_MIRROR_ALLOWED_LABEL	="Can Backup My Data:";
	public static String SERVERS_WE_AMPLIFY_HEADER				="All My Data to Amplify";
	public static String SERVERS_WE_AMPLIFY_ALLOWED_LABEL		="Can Amplify to My Data:";
	public static String SERVERS_WHO_AMPLIFY_US_HEADER			="Be an Amplifier";
	public static String SERVERS_WHO_AMPLIFY_US_ALLOWED_LABEL	="Amplier Data:";
	public static String SERVERS_WHO_MIRROR_US_HEADER			="Act as A Backup For Other Servers";
	public static String SERVERS_WHO_MIRROR_US_ALLOWED_LABEL	="Can Upload Data";	
	
	public static String AVAILABLE_SERVER_LABEL				="Available Servers:";
	public static String AVAILABLE_MAGIC_LABEL				="Available Magic Words:";	
	
}
