
package org.martus.mspa.client.test;

import junit.framework.Test;
import junit.framework.TestSuite;


public class TestClient
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
		TestSuite suite= new TestSuite("All Client MSPA Tests");

		suite.addTest(new TestSuite(TestClientSideXMLRPCHandler.class));
		return suite;
	}
}
