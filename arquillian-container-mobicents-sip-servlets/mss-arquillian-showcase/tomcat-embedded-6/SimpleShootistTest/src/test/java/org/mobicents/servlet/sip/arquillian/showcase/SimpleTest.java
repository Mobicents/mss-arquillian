/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.showcase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
 * Arquillian is controlling the lifecycle of the container and the test archive. That means it starts and stops the container and deploys
 * the test archive when it is needed
 * 
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */
@RunWith(Arquillian.class)
public class SimpleTest 
{

	private static Logger logger = Logger.getLogger(SimpleTest.class);

	private SipStack receiver;
	private SipCall sipCall;
	private SipPhone sipPhone;

	private final int TIMEOUT = 10000;	

	private static SipStackTool sipStackTool;

	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool();
	}

	@Before
	public void setUp() throws Exception
	{	
		//Create the sipCall and start listening for messages
		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		if(sipCall != null)	sipCall.disposeNoBye();
		if(sipPhone != null) sipPhone.dispose();
		if(receiver != null) receiver.dispose();
	}

	/*
	 * Define the test archive here.
	 * Pay attention to the properties of the Deployment annotation
	 * --name: the arquillian Deployer, you can deploy/undeploy this archive by using the name here
	 * --managed: the framework WILL manage the lifecycle of this archive
	 * --testable: as-client mode (https://docs.jboss.org/author/display/ARQ/Test+run+modes) 
	 */
	@Deployment(name="simple", managed=true, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "simplesipservlet.war");
		webArchive.addClasses(SimpleSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	/*
	 * Tests 
	 */
	
	@Test
	public void testInvite() throws InterruptedException
	{
		logger.info("testInvite test");
		String toUri = "sip:BigGuy@127.0.0.1:5070";
		
		assertTrue(sipCall.initiateOutgoingCall(toUri, null));
		
		assertTrue(sipCall.waitForAnswer(TIMEOUT));
		assertTrue(sipCall.sendInviteOkAck());
		
		Thread.sleep(100);
		sipCall.disconnect();
		Thread.sleep(1000);
		assertEquals(200, sipCall.getLastReceivedResponse().getStatusCode());
	}
	
	@Test
	public void testRegister()
	{
		logger.info("testRegister test");
		assertTrue(sipPhone.register("sip:LittleGuy@there.com", 3600));
	}
}
