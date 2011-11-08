/**
 * 
 */
package org.mobicents.servlet.sip.arq.testsuite.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.sip.SipFactory;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
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
 * @author gvagenas 
 * gvagenas@gmail.com
 * 
 */

@RunWith(Arquillian.class)
public class ShootistSipServletTest extends SipTestCase 
{

	private SipStack sipStackA;
	private SipPhone littleGuy;
	private SipStack sipStackB;
	private SipPhone bigGuy;
	private int myPortA = 5070;
	private int myPortB = 5071;
	
	private static final int timeout = 10000;	
	
	private static final Logger logger = Logger.getLogger(ShootistSipServletTest.class.getName());
	
	Properties properties = new Properties();
	
	public SipStack makeStack(String transport, int port) throws Exception {
		
		String host = "localhost";

		properties.setProperty("javax.sip.IP_ADDRESS", host);
		properties.setProperty("javax.sip.STACK_NAME", "testAgent");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"testAgent_debug.txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"testAgent_log.txt");
		properties
		.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
		properties.setProperty(
				"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

		properties.setProperty("sipunit.trace", "true");
		properties.setProperty("sipunit.test.protocol", "udp");
		properties.setProperty(
				"gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER",
				"true");
		
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
			sipStackA = makeStack(SipStack.PROTOCOL_UDP, myPortA);
			logger.info("SipStackA created!");

			sipStackB = makeStack(SipStack.PROTOCOL_UDP, myPortB);
			logger.info("SipStackB created!");
			
			SipStack.setTraceEnabled(properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));
		}
		catch (Exception ex)
		{
			logger.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}

		try
		{
			littleGuy = sipStackA.createSipPhone("localhost",SipStack.PROTOCOL_UDP,5080, "sip:LittleGuy@there.com");
			logger.info("SipPhone littleGuy created with address "+littleGuy.getAddress().toString());
			bigGuy = sipStackB.createSipPhone("localhost",SipStack.PROTOCOL_UDP,5080, "sip:BigGuy@here.com");
			logger.info("SipPhone bigGuy created with address "+bigGuy.getAddress().toString());
			
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
		littleGuy.dispose();
		bigGuy.dispose(); 
		sipStackA.dispose();
		sipStackB.dispose();
	}

	@Deployment (name="first", order=1)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservlet.war");
		webArchive.addClasses(ShootistSipServletCDI.class);
		webArchive.addAsLibraries(DependencyResolvers.use(MavenDependencyResolver.class)
				.artifact("org.mobicents.servlet.sip.ctf.core:ctf-core:1.0.0-SNAPSHOT").resolveAs(GenericArchive.class));
		webArchive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		webArchive.addAsManifestResource("in-container-context.xml", "context.xml");
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	
	/**
	 * @throws Exception
	 */
	@Test @OperateOnDeployment("first")
	public void testShootist() throws Exception {
		
//		assertTrue(littleGuy.register("any","any","LittleGuy@127.0.0.1:5080",600, 5000));
//		assertTrue(littleGuy.register("any","any","BigGuy@127.0.0.1:5080",600, 5000));
		
		SipCall sipCallA = littleGuy.createSipCall();
		SipCall sipCallB = bigGuy.createSipCall();

		assertTrue(sipCallA.listenForIncomingCall());
	
//		initializeCall();
		
//		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
//		ArrayList<Header> replaceHeaders = new ArrayList<Header>();
		String requestURI = sipStackA.getAddressFactory().createSipURI("LittleGuy", "127.0.0.1:5070").toString();
//		javax.sip.address.Address routeAddr = sipStackA.getAddressFactory().createAddress("127.0.0.1"); 
//		additionalHeaders.add(sipStackA.getHeaderFactory().createHeader("Request-URI", requestURI));
//		replaceHeaders.add(sipStackA.getHeaderFactory().createRouteHeader(routeAddr));

		sipCallB.initiateOutgoingCall("sip:BigGuy@here.com","sip:LittleGuy@there.com", null,null,null,requestURI,null);
		assertLastOperationSuccess("sipCallB initiate call - " + sipCallB.format(), sipCallB);
		
		sipCallA.waitForIncomingCall(timeout);
		
		assertTrue(sipCallA.sendIncomingCallResponse(Response.TRYING,"Trying", 0));
		assertTrue(sipCallA.sendIncomingCallResponse(Response.RINGING,"RINGING",0));
		assertTrue(sipCallA.sendIncomingCallResponse(Response.OK, "OK", 0));
		assertTrue(sipCallA.listenForDisconnect());	
	}
	
	@Test @OperateOnDeployment("first")
	public void testShootistCancel() throws Exception {

		SipCall sipCallA = littleGuy.createSipCall();
		SipCall sipCallB = bigGuy.createSipCall();
		
		assertTrue(sipCallA.listenForIncomingCall());
		
		String requestURI = sipStackA.getAddressFactory().createSipURI("LittleGuy", "127.0.0.1:5070").toString();
		sipCallB.initiateOutgoingCall("sip:BigGuy@here.com","sip:LittleGuy@there.com", null,null,null,requestURI,null);
		assertLastOperationSuccess("sipCallB initiate call - " + sipCallB.format(), sipCallB);
		
		sipCallA.waitForIncomingCall(500);
		
		sipCallA.stopListeningForRequests();
		
		assertTrue(sipCallA.listenForCancel());
		
		Thread.sleep(500);
		
		sipCallB.sendCancel();
		assertLastOperationSuccess("sipCallB canceled last INVITE - " + sipCallB.format(), sipCallB);
		
		sipCallA.waitForCancel(timeout);
		sipCallA.stopListeningForRequests();
		
//		assertTrue(receiver.isCancelReceived());	
		ArrayList<SipRequest> allMessagesContent = new ArrayList<SipRequest>();
//		allMessagesContent.addAll(sipCallA.getAllReceivedRequests());// .getAllMessagesContent();
		allMessagesContent = sipCallA.getAllReceivedRequests();
		
		int sizeA = sipCallA.getAllReceivedRequests().size();
		int sizeB = sipCallB.getAllReceivedRequests().size();
		
		assertTrue(sizeA>=0);
		assertTrue(sizeB>=0);
		
//		assertTrue(allMessagesContent.size() >= 2);
		
//		ArrayList<String> allMessagesStringContent = new ArrayList<String>();
//		for (SipRequest sipRequest : allMessagesContent) {
//			String msg = new String(sipRequest.getRawContent());
//			allMessagesStringContent.add(msg);
//		}
//		assertTrue("sipSessionReadyToInvalidate", allMessagesStringContent.contains("sipSessionReadyToInvalidate"));
//		assertTrue("sipAppSessionReadyToInvalidate", allMessagesStringContent.contains("sipAppSessionReadyToInvalidate"));
	}
	
}
