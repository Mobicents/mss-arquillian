/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.showcase;

import java.util.List;
import java.util.Map;

import javax.sip.header.AuthorizationHeader;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.jboss.arquillian.container.mobicents.api.annotations.ContextParam;
import org.jboss.arquillian.container.mobicents.api.annotations.ContextParamMap;
import org.jboss.arquillian.container.mobicents.api.annotations.GetDeployableContainer;
import org.jboss.arquillian.container.mss.extension.ContainerManagerTool;
import org.jboss.arquillian.container.mss.extension.ContextParamMapConstructTool;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
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
public class SimpleShootistTest extends SipTestCase{

	private static Logger logger = Logger.getLogger(SimpleShootistTest.class);

	@ArquillianResource
	private Deployer deployer;
	
	@GetDeployableContainer
	private ContainerManagerTool containerManager = null;
	
	//SipUnit Elements
	private SipStack receiver;
	private SipCall sipCall;
	private SipPhone sipPhone;

	private final int TIMEOUT = 10000;	

	//Helper tool class for SipUnit emelements
	private static SipStackTool sipStackTool;
	private String testArchive = "simple";

	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool("SimpleShootistTest");
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
		logger.info("About to un-deploy the application");
		deployer.undeploy(testArchive);
		if(sipCall != null)	sipCall.disposeNoBye();
		if(sipPhone != null) sipPhone.dispose();
		if(receiver != null) receiver.dispose();
	}

	/*
	 * Define the test archive here.
	 * Pay attention to the properties of the Deployment annotation
	 * --name: the arquillian Deployer, you can deploy/undeploy this archive by using the name here
	 * --managed: the framework WILL NOT manage the lifecycle of this archive, and we have to deploy the archive when the sip client is ready
	 * --testable: as-client mode (https://docs.jboss.org/author/display/ARQ/Test+run+modes) 
	 */
	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "simplesipservlet.war");
		webArchive.addClasses(ShootistSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}

	/*
	 * Tests 
	 */

	@Test
	public void testShootist() throws InterruptedException 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",TIMEOUT));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT));

		if (sipCall.getLastReceivedResponse() != null)
			logger.info("sipCallB lastReceivedResponse: "+sipCall.getLastReceivedResponse().toString());

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		sipCall.respondToDisconnect();
	}
		
	@Test
	public void testShootistSetContact() throws Exception 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		SipRequest inviteRequest = sipCall.getLastReceivedRequest();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT));

		assertTrue(inviteRequest.getMessage().getHeader("Contact").toString().contains("uriparam=urivalue"));
		assertTrue((inviteRequest.getMessage().getHeader("Contact").toString().contains("headerparam1=headervalue1")));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		sipCall.respondToDisconnect();
	}

	@Test @ContextParam(name="toTag", value="callernwPort1241042500479")
	public void testShootistSetToTag() throws Exception 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		sipCall.respondToDisconnect();	
	}
	
	@Test @ContextParam(name="testServletListener", value="true")
	public void testShootistSipServletListener() throws Exception 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(TIMEOUT));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", TIMEOUT));
		Thread.sleep(TIMEOUT);

		deployer.undeploy(testArchive);

		Thread.sleep(TIMEOUT);

		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(TIMEOUT));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", TIMEOUT));
		Thread.sleep(TIMEOUT);

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(2,allMessagesContent.size());

		//Reload Context
		containerManager.reloadContext();

		assertTrue(sipCall.waitForMessage(TIMEOUT));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", TIMEOUT));
		allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(3,allMessagesContent.size());
	}

	@Test @ContextParam(name="testContentLength", value="testContentLength")
	public void testShootistContentLength() throws Exception 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(TIMEOUT));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", TIMEOUT));
		assertNotNull(sipCall.getLastReceivedMessageRequest());	
	}

	@Test
	public void testShootistCallerSendsBye() throws Exception 
	{
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",TIMEOUT));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT));

		sipCall.disconnect();

		Thread.sleep(TIMEOUT);

		assertTrue(sipCall.getReturnCode()==200);
	}
	
	@ContextParamMap("testShootistAuthorization")
	private Map<String, String> contextMap7 = new ContextParamMapConstructTool()
	.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"")
	.put("headerToAdd", "Authorization").getMap();

	@Test @ContextParamMap("testShootistAuthorization")
	public void testShootistAuthorization() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", TIMEOUT));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",TIMEOUT));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", TIMEOUT));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(TIMEOUT));
		sipCall.respondToDisconnect();

		assertNotNull(request.getMessage().getHeader(AuthorizationHeader.NAME));	
	}
}
