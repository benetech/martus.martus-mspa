
package org.martus.mspa.main;

import java.awt.Toolkit;

import javax.swing.JOptionPane;
import javax.swing.UIManager;


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
			int defaultPort = 443;
			String defaultHost = "localHost";	
			String serverPublicCode = "2343.8324.4616.1545.5585";
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());										
			UiMainWindow window = new UiMainWindow(defaultHost, defaultPort, serverPublicCode);
			if(!window.run())
				System.exit(0);													
							
		}
		catch(Exception e)
		{
			System.out.println(e);
			//e.printStatckTrace(System.out);
		}
		
		
	}	
	

}
