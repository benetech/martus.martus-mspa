
package org.martus.mspa.client.view;

import java.util.Vector;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;


public class CreateAccountsTree
{
	public CreateAccountsTree(String server, Vector accounts)
	{	
		String whichServerToView = server;		
			
		DefaultTreeModel model = null;
		JTree tree = null;

		Object[] nodes = new Object[accounts.size()+1];
		DefaultMutableTreeNode top = new DefaultMutableTreeNode(new AccountNode(1, whichServerToView, ""));
		DefaultMutableTreeNode parent = top;
		nodes[0] = top;
		
		loadAccountsToTreeNode(nodes, accounts.toArray(), parent);
		 		
		model = new DefaultTreeModel(top);
		tree = new JTree(model);

		model = new DefaultTreeModel(top);
		tree = new JTree(model);

		tree.setShowsRootHandles(true); 
		tree.setEditable(false);
		TreePath path = new TreePath(nodes);
		tree.setSelectionPath(path);

		scrollPane = new JScrollPane();
		scrollPane.getViewport().add(tree);
		 
	}
	
	private void loadAccountsToTreeNode(Object[] nodes, Object[] accountArray, DefaultMutableTreeNode parent)
	{
		for (int i=0;i<accountArray.length;i++)
		{		
			DefaultMutableTreeNode account = new DefaultMutableTreeNode(new AccountNode(0, (String) accountArray[i], ""));
			parent.add(account);		
			nodes[i+1] = account;
		}
	}

	public JScrollPane getScrollPane()
	{
		return scrollPane;
	}
	
	JScrollPane scrollPane;
}

class AccountNode
{
	public AccountNode(int id, String name, String status)
	{
		nodeId = id;
		nodeName = name;
		accountStatus = status;
	}

	public int getNodeId() 
	{ 
		return nodeId;
	}

	public String getNodeName() 
	{ 
		return nodeName;
	}
	
	public String getAccountStatus()
	{
		return accountStatus;
	}

	public String toString() 
	{ 
		return nodeName;
	}
	
	protected int    nodeId;
	protected String nodeName;
	protected String accountStatus;
}
