
package org.martus.mspa.client.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.martus.common.LoggerToConsole;
import org.martus.common.MagicWordEntry;
import org.martus.common.MagicWords;
import org.martus.mspa.main.UiMainWindow;
import org.martus.swing.ParagraphLayout;
import org.martus.swing.Utilities;


public class MagicWordsDlg extends JDialog
{
	public MagicWordsDlg(UiMainWindow owner, Vector magicWords)
	{
		super((JFrame)owner, "Manage Magic Words", true);
		parent = owner;		
		
		magicWordsInfo = new MagicWords(new LoggerToConsole());
		loadMagicWordElementsToList(magicWords);
			
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(buildMagicWordPanel(), BorderLayout.NORTH);
		getContentPane().add(viewMagicPanel(), BorderLayout.CENTER);
		getContentPane().add(buildButtonsPanel(), BorderLayout.SOUTH);						

		Utilities.centerDlg(this);
		setResizable(false);
		resetFields();
		
	}	
	
	private JPanel buildMagicWordPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), "Manage Magic Word and Group"));
		panel.setLayout(new ParagraphLayout());	
				
		addMagicWordsField = new JTextField(20);
		addMagicWordsField.requestFocus();	
		
		groupComboField = new GroupComboBox(groupList);		
		groupComboField.setEditable(true);	
	
		addMagicWordButton = new JButton("Add");
		addMagicWordButton.addActionListener(new MagicWordButtonHandler());	
		removeMagicWordButton = new JButton("Remove");
		removeMagicWordButton.addActionListener(new MagicWordButtonHandler());	
		updateMagicWordButton = new JButton("Update");
		updateMagicWordButton.addActionListener(new MagicWordButtonHandler());						
				
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel("Magic word:"));
		
		panel.add(addMagicWordsField);
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel("Assign to which group:"));
		panel.add(groupComboField);				
						
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);		
		panel.add(addMagicWordButton);	
		panel.add(removeMagicWordButton);
		panel.add(updateMagicWordButton);
			
		return panel;
	}

	private JPanel viewMagicPanel()
	{
		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder (EtchedBorder.RAISED));
		panel.setLayout(new ParagraphLayout());	
				
		activeWords = new JList(magicListModel);
		activeWords.setFixedCellWidth(320);	
		
		TabListCellRenderer renderer = new TabListCellRenderer();
		renderer.setTabs(new int[] {180, 200, 300});
		activeWords.setCellRenderer(renderer);
		activeWords.addMouseListener( new MouseAdapter() {
		  public void mouseClicked(MouseEvent e) 
		  {
			String item = (String) activeWords.getSelectedValue();	
			selectedMagicWord = getMagicWord(item);
			String group = 	getGroupName(item);		
			groupComboField.setSelectedItem(group);
			addMagicWordsField.setText(selectedMagicWord);				
		  }
		});	
	    
		JScrollPane ps = new JScrollPane();
		ps.getViewport().add(activeWords);	
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);
		panel.add(new JLabel("View manage words and group assigned:"));	
		panel.add(new JLabel(""), ParagraphLayout.NEW_PARAGRAPH);			
		panel.add(ps);

		return panel;
	}
	
	private JPanel buildButtonsPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());		
		
		saveButton = new JButton("Save");	
		saveButton.addActionListener(new CommitButtonHandler());	
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new CommitButtonHandler());
		panel.add(saveButton);
		panel.add(cancelButton);

		return panel;
	}
	
	private void loadMagicWordElementsToList(Vector items)
	{
		magicListModel = new DefaultListModel();
		groupList = new Vector();
		
		for (int i=0; i<items.size();++i)
		{			
			String word = (String) items.get(i);
			MagicWordEntry entry = magicWordsInfo.add(word);	
			magicListModel.add(i, entry.getLineOfMagicWordNoSign());					
			
			String groupName = getGroupName(word);
			if (groupName != null && !isValidGroupName(groupName))			
				groupList.add(getGroupName(word));			
		}		
	}		
	
	private String getGroupName(String lineOfMagicWord)
	{
		return MagicWords.getGroupNameFromLineEntry(lineOfMagicWord);
	}
	
	private String getMagicWord(String lineOfMagicWord)
	{		
		return lineOfMagicWord.substring(0, lineOfMagicWord.indexOf(MagicWords.FIELD_DELIMITER));
	}
	
	private boolean isValidGroupName(String groupName)
	{
		return groupList.contains(groupName);
	}
	
	private void resetFields()
	{
		addMagicWordsField.setText("");
		groupComboField.setSelectedItem(null);		
	}
	
	private boolean isValidMagicWord(String word)
	{	
		return magicWordsInfo.contains(word);	
	}	
	
	class MagicWordButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			
			if (ae.getSource().equals(addMagicWordButton))
				handleAddNewMagicWord();			
			else if (ae.getSource().equals(removeMagicWordButton))
				handleRemoveMagicWords();
			else if (ae.getSource().equals(updateMagicWordButton))
				handleUpdateMagicWords();	
		}
		
		private void handleUpdateMagicWords()
		{
			int selectedItem = activeWords.getSelectedIndex();
							
			if (!activeWords.isSelectionEmpty())
			{				
				String item = (String) activeWords.getSelectedValue();				
				String orgWord = getMagicWord(item);												
				
				String newWord = addMagicWordsField.getText();
				String assignedGroup = (String) groupComboField.getSelectedItem();
									
				magicListModel.remove(selectedItem);				
				MagicWordEntry entry = magicWordsInfo.getMagicWordEntry(orgWord);				
				if (orgWord.equals(newWord))
				{							
					entry.setGroupname(assignedGroup);
					magicListModel.addElement(entry.getLineOfMagicWordNoSign());					
				}
				else
				{
					magicWordsInfo.remove(orgWord);	
					handleAddNewMagicWord();
				}												
			}			
				
			
		}
		
		private void handleAddNewMagicWord()
		{
			String word = addMagicWordsField.getText();
			String assignedGroup = (String) groupComboField.getSelectedItem();
			
			if (word.startsWith(MagicWords.INACTIVE_SIGN))
			{			
				JOptionPane.showMessageDialog(parent, MagicWords.INACTIVE_SIGN+" denotes an inactive magic words", "Invalid character", JOptionPane.ERROR_MESSAGE);				
				resetFields();					
				return;
			}
			
			if (isValidMagicWord(word))
			{
				JOptionPane.showMessageDialog(parent, "The magic word already exists. Please try other magic word ..", "Duplicate Magic word", JOptionPane.ERROR_MESSAGE);				
				resetFields();					
				return;
			}	
			
			if (assignedGroup == null)			
				assignedGroup = word;
									
			MagicWordEntry entry = magicWordsInfo.add(word, assignedGroup);
			entry.setActive(false);
			String lineOfMagicWord = entry.getLineOfMagicWordNoSign();	
			if (!magicListModel.contains(lineOfMagicWord))
			{	
				magicListModel.addElement(lineOfMagicWord);			
				groupComboField.add(assignedGroup);
			}
			resetFields();	

		}		
		
		private void handleRemoveMagicWords()
		{			
			int selectItem = activeWords.getSelectedIndex();
				
			if (!activeWords.isSelectionEmpty())
			{	
				String item = (String) activeWords.getSelectedValue();	
				
				magicListModel.remove(selectItem);				
				magicWordsInfo.remove(getMagicWord(item));
				groupList.remove(getGroupName(item));
			}	
			resetFields();						
		}		
	}
	
	class CommitButtonHandler implements ActionListener
	{
		public void actionPerformed(ActionEvent ae)
		{
			if (ae.getSource().equals(cancelButton))				
				dispose();
			else if (ae.getSource().equals(saveButton))
				handleUpdateMagicWords();
			
		}

		private void handleUpdateMagicWords()
		{			
			parent.getMSPAApp().updateMagicWords(magicWordsInfo.getAllMagicWords());	
			dispose();			
		}
	}
	
	class GroupComboBox  extends JComboBox
	{
		public static final int MAX_MEM_LEN = 30;

		public GroupComboBox(Vector groups)
		{
			super(groups);			
			setEditable(true);
		}

		public void add(String item)
		{
			removeItem(item);
			insertItemAt(item, 0);
			setSelectedItem(item);
			if (getItemCount() > MAX_MEM_LEN)
				removeItemAt(getItemCount()-1);
		}
	}

	
	JButton saveButton;
	JButton cancelButton;
	JButton removeMagicWordButton;
	JButton addMagicWordButton;
	JButton updateMagicWordButton;
	JList activeWords;

	DefaultListModel magicListModel;
	DefaultListModel groupListModel;
	JTextField addMagicWordsField;
	GroupComboBox groupComboField;

	UiMainWindow parent;
	MagicWords magicWordsInfo;
	Vector groupList;
	String selectedMagicWord;	
}
