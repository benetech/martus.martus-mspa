
package org.martus.mspa.client.view;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.martus.mspa.main.UiMainWindow;

public class AccountsTree
{
	public AccountsTree(Vector accounts, UiMainWindow mainWin)
	{		

		parentWindow = mainWin;
			
		DefaultTreeModel model = null;
		JTree tree = null;

		Object[] nodes = new Object[accounts.size()+1];
	
		DefaultMutableTreeNode top = new DefaultMutableTreeNode();		
		DefaultMutableTreeNode parent = top;
		nodes[0] = top;
		
		loadAccountsToTreeNode(nodes, accounts.toArray(), parent);
		 		
		model = new DefaultTreeModel(top);		
		tree = new JTree(model);
		tree.setRootVisible(false);

		tree.addTreeSelectionListener(new AccountNodeSelectionListener());
		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION); 	
		tree.setShowsRootHandles(true); 
		tree.setEditable(false);
		TreePath path = new TreePath(nodes[0]);
		tree.setSelectionPath(path);		

		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		
		tree.setSelectionRow(0);		

	}
	
	DefaultMutableTreeNode getTreeNode(TreePath path)
	{
		return (DefaultMutableTreeNode)(path.getLastPathComponent());
	}
	private void loadAccountsToTreeNode(Object[] nodes, Object[] accountArray, DefaultMutableTreeNode parent)
	
	{
		for (int i=0;i<accountArray.length;i++)
		{		
			DefaultMutableTreeNode account = new DefaultMutableTreeNode(new AccountNode((String) accountArray[i], ""));
			parent.add(account);		
			nodes[i+1] = account;
		}
	}

	public JScrollPane getScrollPane()
	{
		return scrollPane;
	}
	
	class AccountNodeSelectionListener implements TreeSelectionListener 
	{
		public void valueChanged(TreeSelectionEvent e)
		{				
			DefaultMutableTreeNode node = getTreeNode(e.getPath());
			if (node != null)
			{
				AccountNode selectedAccountNode = (AccountNode) node.getUserObject();
				if (node.isRoot())
				 parentWindow.loadEmptyAccountDetailPanel();
				else	
				 parentWindow.loadAccountDetailPanel(selectedAccountNode.getAccountId(), selectedAccountNode.getDisplayName());		
			}		
		}
	}

	
	JScrollPane scrollPane;	
	UiMainWindow parentWindow;
}

