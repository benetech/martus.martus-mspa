
package org.martus.mspa.main;

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.martus.mspa.client.core.MSPAClient;


public class MSPAMain
{
	public static void main(String[] args)
	{		
		final String javaVersion = System.getProperty("java.version");
		final String minimumJavaVersion = "1.4.1";
		if(javaVersion.compareTo(minimumJavaVersion) < 0)
		{
			final String errorMessage = "Requires Java version " + minimumJavaVersion + " or later!";
			System.out.println(errorMessage);
			Toolkit.getDefaultToolkit().beep();
			JOptionPane.showMessageDialog(null, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
							
			MSPAClient app = new MSPAClient(DEFAULT_HOST, DEFAULT_PORT);	
			UiMainWindow window = new UiMainWindow(DEFAULT_HOST, app);
			if(!window.run())
				System.exit(0);						
							
		}
		catch(Exception e)
		{
			System.out.println(e);
			//e.printStatckTrace(System.out);
		}		
	}	
	
	final static int DEFAULT_PORT = 443;
	final static String DEFAULT_HOST = "localHost";	
}
