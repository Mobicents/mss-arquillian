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
package org.mobicents.servlet.sip.testsuite.click2call;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.GenericArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.DependencyResolvers;
import org.jboss.shrinkwrap.resolver.api.maven.MavenDependencyResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tests that SIP Servlets deployments into the  Mobicents Sip Servlets server work through the
 * Arquillian lifecycle
 * 
 * This test is currently commented since the Weld integration in Mobicents Sip Servlets has not yet been done
 * 
 * @author gvagenas@gmail.com / devrealm.org
 * @author Jean Deruelle
 * @version $Revision: $
 */
@RunWith(Arquillian.class)
public class MobicentsSipServletsEmbeddedInContainerTestCase extends SipTestCase
{	

	private SipStack sipStackA;
	private SipStack sipStackB;

	private SipPhone ua;
	private SipPhone ub;

	private int myPortA=5061;
	private int myPortB=5062;

	private static String testProtocol;

	private static final int timeout = 10000;

	String CLICK2DIAL_URL = "http://127.0.0.1:8888/click2call/call";
	String RESOURCE_LEAK_URL = "http://127.0.0.1:8888/click2call/index.html";
	String EXPIRATION_TIME_PARAMS = "?expirationTime";
	String CLICK2DIAL_PARAMS = "?from=sip:from@127.0.0.1:5061&to=sip:to@127.0.0.1:5062";

	private static final Properties defaultProperties = new Properties();	    
	static
	{
		String host = "localhost";

		defaultProperties.setProperty("javax.sip.IP_ADDRESS", host);
		defaultProperties.setProperty("javax.sip.STACK_NAME", "testAgent");
		defaultProperties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		defaultProperties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"testAgent_debug.txt");
		defaultProperties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"testAgent_log.txt");
		defaultProperties
		.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
		defaultProperties.setProperty(
				"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

		defaultProperties.setProperty("sipunit.trace", "true");
		defaultProperties.setProperty("sipunit.test.protocol", "udp");
		defaultProperties.setProperty(
				"gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER",
				"true");

		testProtocol = defaultProperties.getProperty("sipunit.test.protocol");
	}

	public SipStack makeStack(String transport, int port) throws Exception {
		Properties properties = new Properties();

		String host = "localhost";
		properties.setProperty("javax.sip.IP_ADDRESS", host);
		String peerHostPort1 = "localhost:5080";
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ "udp");
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ port);
		properties.setProperty("sipunit.BINDADDR", "localhost");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/simplesipservlettest_debug_port" + port + ".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/simplesipservlettest_log_port" + port + ".xml");
		properties.setProperty("sipunit.trace", "true");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");
		return new SipStack(transport, port, properties);
	}

	@Before
	public void setUp() throws Exception
	{
		try
		{
			log.info("About to start creating sipStacks");
			sipStackA = makeStack(SipStack.PROTOCOL_UDP, myPortA);
			log.info("SipStackA created!");
			sipStackB = makeStack(SipStack.PROTOCOL_UDP, myPortB);
			log.info("SipStackB created!");

			SipStack.setTraceEnabled(defaultProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| defaultProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));

		}
		catch (Exception ex)
		{
			log.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}

		try
		{
			log.info("About to start creating sipPhones");
			ua = sipStackA.createSipPhone("localhost",SipStack.PROTOCOL_UDP,5080, "sip:from@sip-servlets.org");
			log.info("SipPhone A created!");
			ub = sipStackB.createSipPhone("localhost",SipStack.PROTOCOL_UDP,5080, "sip:to@sip-servlets.org");
			log.info("SipPhone B created!");
		}
		catch (Exception ex)
		{
			fail("Exception creating SipPhone: " + ex.getClass().getName()
					+ ": " + ex.getMessage());
			throw ex;
		}
	}

	@After
	public void tearDown() throws Exception
	{
		ua.dispose();
		sipStackA.dispose();
		ub.dispose();
		sipStackB.dispose();
	}





	// -------------------------------------------------------------------------------------||
	// Class Members
	// ----------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||

	/**
	 * Logger
	 */
	private static final Logger log = Logger.getLogger(MobicentsSipServletsEmbeddedInContainerTestCase.class.getName());

	// -------------------------------------------------------------------------------------||
	// Instance Members
	// -------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||	

	/**
	 * Define the deployment
	 */
	@Deployment
	public static WebArchive createTestArchive()
	{

		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "click2call.war");
		webArchive.addClasses(Click2DialSipServlet.class, SimpleWebServlet.class);
		webArchive.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
				.artifact("org.mobicents.servlet.sip.ctf.core:ctf-core:1.0.0-SNAPSHOT").resolveAs(GenericArchive.class));
		webArchive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		webArchive.addAsManifestResource("in-container-context.xml", "context.xml");
		webArchive.addAsWebInfResource("in-container-web.xml", "web.xml");
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}

	// -------------------------------------------------------------------------------------||
	// Tests
	// ------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||


	@Test
	public void testClickToCallNoConvergedSession() throws Exception {

		SipCall sipCallA = ua.createSipCall();
		SipCall sipCallB = ub.createSipCall();

		sipCallA.listenForIncomingCall();
		sipCallB.listenForIncomingCall();

		log.info("Trying to reach url : " + CLICK2DIAL_URL
				+ CLICK2DIAL_PARAMS);

		URL url = new URL(CLICK2DIAL_URL + CLICK2DIAL_PARAMS);
		InputStream in = url.openConnection().getInputStream();

		byte[] buffer = new byte[10000];
		int len = in.read(buffer);
		String httpResponse = "";
		for (int q = 0; q < len; q++)
			httpResponse += (char) buffer[q];
		log.info("Received the follwing HTTP response: " + httpResponse);

		sipCallB.waitForIncomingCall(timeout);

		assertTrue(sipCallB.sendIncomingCallResponse(Response.RINGING,
				"Ringing", 0));
		assertTrue(sipCallB.sendIncomingCallResponse(Response.OK, "OK",
				0));

		sipCallA.waitForIncomingCall(timeout);

		assertTrue(sipCallA.sendIncomingCallResponse(Response.RINGING,
				"Ringing", 0));
		assertTrue(sipCallA.sendIncomingCallResponse(Response.OK, "OK",
				0));

		assertTrue(sipCallA.waitForAck(timeout));
		assertTrue(sipCallB.waitForAck(timeout));

		assertTrue(sipCallB.disconnect());
		assertTrue(sipCallA.waitForDisconnect(timeout));
		assertTrue(sipCallA.respondToDisconnect());
	}

//	public void testClickToCallHttpSessionLeak()
//			throws Exception {
//
//		final int sessionsNumber = manager.getActiveSessions();
//
//		logger.info("Trying to reach url : " + RESOURCE_LEAK_URL);
//
//		URL url = new URL(RESOURCE_LEAK_URL);
//		InputStream in = url.openConnection().getInputStream();
//
//		byte[] buffer = new byte[10000];
//		int len = in.read(buffer);
//		String httpResponse = "";
//		for (int q = 0; q < len; q++)
//			httpResponse += (char) buffer[q];
//		logger.info("Received the follwing HTTP response: " + httpResponse);
//
//		assertEquals(sessionsNumber, manager.getActiveSessions());
//	}
//
//	public void testClickToCallExpirationTime()
//			throws Exception {				
//
//		logger.info("Trying to reach url : " + CLICK2DIAL_URL + EXPIRATION_TIME_PARAMS);
//
//		URL url = new URL(CLICK2DIAL_URL + EXPIRATION_TIME_PARAMS);
//		InputStream in = url.openConnection().getInputStream();
//
//		byte[] buffer = new byte[10000];
//		int len = in.read(buffer);
//		String httpResponse = "";
//		for (int q = 0; q < len; q++)
//			httpResponse += (char) buffer[q];
//		logger.info("Received the follwing HTTP response: " + httpResponse);
//
//		assertFalse("0".equals(httpResponse.trim()));
//	}
}
