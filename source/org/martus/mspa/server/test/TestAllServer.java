
package org.martus.mspa.server.test;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TestAllServer
{
	public static void main(String[] args)
	{
		runTests();
	}

	public static void runTests ()
	{
		junit.textui.TestRunner.run (suite());
	}

	public static Test suite ( )
	{
		TestSuite suite= new TestSuite("All Server MSPA Tests");

//		suite.addTest(TestServer.suite());

		return suite;
	}
}
