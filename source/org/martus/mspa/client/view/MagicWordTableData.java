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
package org.martus.mspa.client.view;

import java.util.Date;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWordEntry;
import org.martus.common.MagicWords;


public class MagicWordTableData extends AbstractTableModel 
{
	public MagicWordTableData(Vector magicLines) 
	{	  		
		fRowDataList = new Vector();
		groupList = new Vector();
		loadMagicWords(magicLines);
	}

	private void loadMagicWords(Vector items) 
	{
	  fRowDataList.removeAllElements();
	  MagicWords magicWordsInfo = new MagicWords(new LoggerToConsole());
	
	  for (int i=0; i<items.size();++i)
	  {			
		  String word = (String) items.get(i);
		  MagicWordEntry entry = magicWordsInfo.add(word);					
		  fRowDataList.addElement(new MagicWordData(" ", entry.isActive(), entry.getMagicWord(),entry.getGroupName()));
		  
		  String groupName = getGroupName(word);
		  if (groupName != null && !isValidGroupName(groupName))			
		  	groupList.add(getGroupName(word));				
	  }		
	}

	public boolean containMagicWord(String word)
	{
		for (int i=0; i<fRowDataList.size();++i)
		{
			MagicWordData data = (MagicWordData)fRowDataList.get(i);
			if (data.fWord.equals(word))
				return true;
		}

		return false;
	}

	public Vector populatedDataFromTable()
	{
		MagicWords magicWordsInfo = new MagicWords(new LoggerToConsole());
		for (int row=0; row < getRowCount(); row++)
		{					
			magicWordsInfo.add(dataMaping(row));
		}
		return magicWordsInfo.getAllMagicWords();				
	}

	private MagicWordEntry dataMaping(int row)
	{
		MagicWordData data = (MagicWordData)fRowDataList.elementAt(row);

		MagicWordEntry entry = new MagicWordEntry(data.fWord, data.fGroup);
		entry.setActive(data.fActived.booleanValue());
		entry.setCreationDate(data.fDate);
	
		return entry;
	}
	
	private String getGroupName(String lineOfMagicWord)
	{
		return MagicWords.getGroupNameFromLineEntry(lineOfMagicWord);
	}	
	
	private boolean isValidGroupName(String groupName)
	{
		return groupList.contains(groupName);
	}
	
	public Vector getGroupList()
	{
		return groupList;
	} 

	public int getRowCount() 
	{
	  return fRowDataList==null ? 0 : fRowDataList.size(); 
	}

	public int getColumnCount() 
	{ 
	  return MagicWordColumnInfo.m_columns.length; 
	} 

	public String getColumnName(int column) 
	{ 
	  return MagicWordColumnInfo.m_columns[column].fTitle; 
	}
 
	public boolean isCellEditable(int nRow, int nCol) 
	{
		if (nCol == MagicWordColumnInfo.COL_STATUS)
			return true;

		return false;
	}

	public Object getValueAt(int nRow, int nCol) 
	{
		if (nRow < 0 || nRow>=getRowCount())
			return "";
		
		MagicWordData row = (MagicWordData)fRowDataList.elementAt(nRow);
		switch (nCol) 
		{
			case MagicWordColumnInfo.COL_DATE		: return row.fDate;
			case MagicWordColumnInfo.COL_STATUS		: return row.fActived;
			case MagicWordColumnInfo.COL_WORD		: return row.fWord;
			case MagicWordColumnInfo.COL_GROUPNAME	: return row.fGroup;
		}
		return "";
	}
	
	public void setValueAt(Object value, int nRow, int nCol) 
	{
		if (nRow < 0 || nRow >= getRowCount())
		  return;
		  
		MagicWordData row = (MagicWordData)fRowDataList.elementAt(nRow);
		String svalue = value.toString();

		switch (nCol) 
		{
		  case MagicWordColumnInfo.COL_DATE: 
			row.fDate = ""; 
			break;
		  case MagicWordColumnInfo.COL_STATUS:		
			row.fActived = new Boolean(svalue);			
			break;
		  case MagicWordColumnInfo.COL_WORD:
			row.fWord = svalue;
			break;
		  case MagicWordColumnInfo.COL_GROUPNAME:
			row.fGroup = svalue; 
			break;		
		}
	}

	public void insert(int row, String word, String group) 
  	{
		if (row < 0)
			row = 0;
	  
		if (row > fRowDataList.size())
			row = fRowDataList.size();
	
		if (containMagicWord(word))
			return;	

		if (isValidGroupName(group))
			groupList.add(group);

		fRowDataList.insertElementAt(new MagicWordData("", false,word, group), row);
	}
	  
	public void update(int row, String word, String group)
	{
		if (row < 0)
		  row = 0;
		  
		if (row > fRowDataList.size())
		  row = fRowDataList.size();
		  
		if (containMagicWord(word))
			return;	
	
		MagicWordData selectedData = (MagicWordData) fRowDataList.get(row);
		if (word == null && word.length()<=1)
			selectedData.fWord = word;
		selectedData.fGroup = group;
		
		fRowDataList.setElementAt(selectedData, row);

		if (isValidGroupName(group))
			groupList.add(group);							
	}

	public boolean delete(int row) 
	{
		if (row < 0 || row >= fRowDataList.size())
		  return false;
		  
		fRowDataList.remove(row);
		  return true;
	}	  
	
	class MagicWordData
	{
		String fDate;
		Boolean fActived;
		String fWord;
		String fGroup;
		
		public MagicWordData(String date, boolean actived, String word, String group)
		{
			fDate = date;
			fActived = new Boolean(actived);
			fWord = word;
			fGroup = group;
		}		
	}
	
	Vector fRowDataList;
	Date   fDate;
	Vector groupList;
}


