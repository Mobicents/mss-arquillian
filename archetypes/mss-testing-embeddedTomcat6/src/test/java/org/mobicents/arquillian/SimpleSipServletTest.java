package org.mobicents.arquillian;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

@RunWith(Arquillian.class)
public class SimpleSipServletTest {

	private static Logger logger = Logger.getLogger(SimpleSipServletTest.class);
	
	//SipUnit related components
	private SipStack sipUnitStack;
	private SipCall sipUnitCall;
	private SipPhone sipUnitPhone;
	private static SipStackTool sipStackTool;
	
	private static String toUri = "sip:sipservletapp@127.0.0.1:5070";
	
	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool("receiver");
	}

	@Before
	public void setUp() throws Exception
	{	
		logger.info("Setting up SipUnit");
		//Create the sipCall and start listening for messages
		
		//.SipStackTool.initializeSipStack(String myTransport, String myHost, String myPort, String outboundProxy)
		sipUnitStack = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//SipStack.createSipPhone(String proxyHost, String proxyProto, int proxyPort, String myURI)
		sipUnitPhone = sipUnitStack.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:sipunit@here.com");
		sipUnitCall = sipUnitPhone.createSipCall();
		sipUnitCall.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		logger.info("Tear down SipUnit");
		if(sipUnitCall != null)	sipUnitCall.disposeNoBye();
		if(sipUnitPhone != null) sipUnitPhone.dispose();
		if(sipUnitStack != null) sipUnitStack.dispose();
	}
	
	/*
	 * Define the test archive here.
	 * Pay attention to the properties of the Deployment annotation
	 * --name: the arquillian Deployer, you can deploy/undeploy this archive by using the name here
	 * --managed: if this is FALSE then the framework WILL NOT manage the lifecycle of this archive, the developer is responsible to deploy/undeploy
	 * --testable: as-client mode (https://docs.jboss.org/author/display/ARQ/Test+run+modes) 
	 */
	@Deployment(name="simple", managed=true, testable=false)
	public static WebArchive createTestArchive()
	{
		//Create a test archive named: simplesipservlet.war
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "simplesipservlet.war");
		//Include the SimpleSipServlet.class from /src/main/java
		webArchive.addClasses(SimpleSipServlet.class);
		//Include as WEB-INF resource sip.xml the in-container-sip.xml from src/test/resources
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	/*
	 *****************************
	 *Tests 
	 *****************************
	 **/
	
	@Test
	public void testInitiateCall() {
		
		//Initiate a new call
		assertTrue(sipUnitCall.initiateOutgoingCall(toUri, null));
		
		//Wait for answer
		assertTrue(sipUnitCall.waitForAnswer(5000));
		assertEquals(SipServletResponse.SC_OK, sipUnitCall.getLastReceivedResponse().getStatusCode());
		
		//Send ACK to 200 OK
		assertTrue(sipUnitCall.sendInviteOkAck());
		
		//Disconnect call and wait for 200 ok to BYE
		assertTrue(sipUnitCall.disconnect());
		assertEquals(SipServletResponse.SC_OK,sipUnitCall.getLastReceivedResponse().getStatusCode());
		
		
	}
	
}
