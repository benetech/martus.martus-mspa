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

package org.martus.mspa.network.roothelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.util.Vector;

import org.martus.util.UnicodeReader;
import org.martus.util.UnicodeWriter;


public class FileTransfer implements Serializable
{
	public FileTransfer(String from, String to) 
	{
		fromFileName = from;
		toFileName = to;
	}
	
	public void setInputStream(FileInputStream input)
	{
		inputStream = input;
	}
	
	public void setOutputStream(FileOutputStream output)
	{
		outputStream = output;
	}
	
	public void setLineOfEntries(Vector entries)
	{
		lineOfEntries = entries;
	}
	
	public Vector getLineOfEntries()
	{
		if (inputStream != null)
		{
			try
			{
				return FileTransfer.readDataStreamFromFile(fromFileName, inputStream);
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
		return lineOfEntries;
	}
	
	public String getFromFileName() {return fromFileName;}
	public String getToFileName() {return toFileName;}
	public FileInputStream getInputStream() {return inputStream;}
	public FileOutputStream getOutputStream() {return outputStream;}				
	
	
	
	public static void copyFile(File in, File out) throws IOException 
	{
		streamTransfer(new FileInputStream(in), new FileOutputStream(out));
	}
	
	public static void streamTransfer(FileInputStream in, FileOutputStream out) throws IOException 
	{
		 FileChannel sourceChannel = in.getChannel();
		 FileChannel destinationChannel = out.getChannel();
		 sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
		
		 sourceChannel.close();
		 destinationChannel.close();
	}
	
	public static void writeDataToFile(File fileName, Vector lineEntries) throws IOException
	{		
		UnicodeWriter writer = new UnicodeWriter(fileName);
		for (int i=0;i<lineEntries.size();++i)
		{
			writer.writeln((String)lineEntries.get(i));
		}								
		writer.close();			
	}	
	
	public static Vector readDataFromFile(File adminFile) throws IOException
	{
		return FileTransfer.readDataStreamFromFile(adminFile.getPath(), new FileInputStream(adminFile));
	}

	public static Vector readDataStreamFromFile(String fileName, InputStream inputStream) throws IOException
	{
		Vector list = new Vector();
		UnicodeReader reader = new UnicodeReader(inputStream);
		String line = null;
		while( (line = reader.readLine()) != null)
		{
			if(line.trim().length() == 0)
				System.out.println("Warning: Found blank line in " + fileName);
			else
				list.add(line);					
		}
		
		reader.close();
		
		return list;
	}

	
	private String fromFileName="";
	private String toFileName="";
	private FileInputStream inputStream;
	private FileOutputStream outputStream;
	private Vector lineOfEntries = new Vector();
}
