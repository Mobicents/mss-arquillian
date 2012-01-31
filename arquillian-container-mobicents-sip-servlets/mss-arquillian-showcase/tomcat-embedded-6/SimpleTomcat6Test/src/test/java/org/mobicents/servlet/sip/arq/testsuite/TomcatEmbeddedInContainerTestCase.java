/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mobicents.servlet.sip.arq.testsuite;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests that Tomcat deployments into the Tomcat server work through the
 * Arquillian lifecycle
 *
 * @author Dan Allen
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class TomcatEmbeddedInContainerTestCase
{
	private static final String HELLO_WORLD_URL = "http://localhost:8888/test2/Test";
	private static final String STATIC_RESOURCE_URL = "http://localhost:8888/test2/index.html";
	private static final Logger log = Logger.getLogger(TomcatEmbeddedInContainerTestCase.class.getName());

	@ArquillianResource
	private Deployer deployer;
	
//	@ArquillianResource
//    URL deploymentUrl;

	@ArquillianResource
	private ContainerController containerController;

	/**
	 * Define the deployment
	 */
	@Deployment(name="first", managed=false)//, testable=false)
//	@Deployment(name="first")
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test2.war");
		webArchive.addClasses(MyServlet.class);
		webArchive.addAsWebInfResource("in-container-web.xml", "web.xml");
		webArchive.addAsWebResource("index.html");
		return webArchive;
	}

	// -------------------------------------------------------------------------------------||
	// Tests -------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||


	@Test
	public void testMethod1() throws Exception
	{
		containerController.start("tomcat");
		deployer.deploy("first");

		// Define the input and expected outcome
		final String expected = "hello";

		URL url = new URL(HELLO_WORLD_URL);
		InputStream in = url.openConnection().getInputStream();

		byte[] buffer = new byte[10000];
		int len = in.read(buffer);
		String httpResponse = "";
		for (int q = 0; q < len; q++)
		{
			httpResponse += (char) buffer[q];
		}

		// Test
		Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
		log.info("Got expected result from Http Servlet: " + httpResponse);

		deployer.undeploy("first");
		containerController.stop("tomcat");
		Thread.sleep(5000);
	}

	@Test
	public void testMethod2() throws Exception
	{
		containerController.start("tomcat");
		deployer.deploy("first");

		// Define the input and expected outcome
		final String expected = "hello";

		URL url = new URL(HELLO_WORLD_URL);
		InputStream in = url.openConnection().getInputStream();

		byte[] buffer = new byte[10000];
		int len = in.read(buffer);
		String httpResponse = "";
		for (int q = 0; q < len; q++)
		{
			httpResponse += (char) buffer[q];
		}

		// Test
		Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
		log.info("Got expected result from Http Servlet: " + httpResponse);

		deployer.undeploy("first");
		containerController.stop("tomcat");
		Thread.sleep(5000);
	}

	@Test
	public void testMethod3() throws Exception
	{
		containerController.start("tomcat");
		deployer.deploy("first");

		// Define the input and expected outcome
		final String expected = "hello";

		URL url = new URL(HELLO_WORLD_URL);
		InputStream in = url.openConnection().getInputStream();

		byte[] buffer = new byte[10000];
		int len = in.read(buffer);
		String httpResponse = "";
		for (int q = 0; q < len; q++)
		{
			httpResponse += (char) buffer[q];
		}

		// Test
		Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
		log.info("Got expected result from Http Servlet: " + httpResponse);

		deployer.undeploy("first");
		containerController.stop("tomcat");
		Thread.sleep(5000);
	}
	
	@Test
	public void testMethod4() throws Exception
	{
		containerController.start("tomcat");
		deployer.deploy("first");

		// Define the input and expected outcome
		final String expected = "hello";

		URL url = new URL(STATIC_RESOURCE_URL);
		InputStream in = url.openConnection().getInputStream();

		byte[] buffer = new byte[10000];
		int len = in.read(buffer);
		String httpResponse = "";
		for (int q = 0; q < len; q++)
		{
			httpResponse += (char) buffer[q];
		}

		// Test
		Assert.assertEquals("Expected output was not equal by value", expected, httpResponse);
		log.info("Got expected result from Http Servlet: " + httpResponse);

		deployer.undeploy("first");
		containerController.stop("tomcat");
		Thread.sleep(5000);
	}

}
