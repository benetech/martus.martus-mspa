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


public class MirrorServerMessageConverter implements ManagingMirrorServerConstants
{	
	public static String getAllowedLabel(int manageTag)
	{
		switch(manageTag)
		{
			case SERVER_WHO_WE_CALL:
				return SERVER_WHO_WE_CALL_ALLOWED_LABEL;
			case SERVER_WE_AMPLIFY:
				return SERVER_WHO_AMPLIFY_ALLOWED_LABEL;
			case AMP_WHO_CALLS_US:
				return AMP_WHO_WE_CALL_ALLOWED_LABEL;
			case WHO_CALLS_US:
				return WHO_CALLS_US_ALLOWED_LABEL;
			case ACT_AS_CLIENT:
				return ACT_AS_CLIENT_ALLOWED_LABEL;
			default:
				return "Unknown Label";
		}
	}
		
	public static String getTitle(int manageTag)
	{
		switch(manageTag)
		{
			case SERVER_WHO_WE_CALL:
				return SERVER_WHO_WE_CALL_TITLE;
			case SERVER_WE_AMPLIFY:
				return SERVER_WE_AMPLIFY_TITLE;
			case AMP_WHO_CALLS_US:
				return AMP_WHO_CALLS_US_TITLE;
			case WHO_CALLS_US:
				return WHO_CALLS_US_TITLE;
			case ACT_AS_CLIENT:
				return ACT_AS_CLIENT_TITLE;
			default:
				return "Unknown Title";
		}
	}
	
	public static String getAvailabledLabel(int manageTag)
	{
		switch(manageTag)
		{
			case SERVER_WHO_WE_CALL:			
			case SERVER_WE_AMPLIFY:			
			case AMP_WHO_CALLS_US:				
			case WHO_CALLS_US:
				return AVAILABLE_SERVER_LABEL;
			case ACT_AS_CLIENT:
				return AVAILABLE_MAGIC_LABEL;
			default:
				return "Unknown Label";
		}
	}		
	
	public static String getHeader(int manageTag)
	{
		switch(manageTag)
		{
			case SERVER_WHO_WE_CALL:
				return SERVER_WHO_WE_CALL_HEADER;
			case SERVER_WE_AMPLIFY:
				return SERVER_WE_AMPLIFY_HEADER;
			case AMP_WHO_CALLS_US:
				return AMP_WHO_WE_CALL_HEADER;
			case WHO_CALLS_US:
				return WHO_CALLS_US_HEADER;
			case ACT_AS_CLIENT:
				return ACT_AS_CLIENT_HEADER;
			default:
				return "Unknown Header";
		}
	}
	

}
