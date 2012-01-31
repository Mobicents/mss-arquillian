/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.testsuite;

import java.text.ParseException;
import java.util.logging.Logger;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.jboss.arquillian.container.mobicents.api.annotations.GetDeployableContainer;
import org.jboss.arquillian.container.mss.extension.ContainerManagerTool;
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
 * @author gvagenas@gmail.com 
 * 
 */
@RunWith(Arquillian.class)
public class ShootmeSipServletTest extends SipTestCase 
{
	@ArquillianResource
	private Deployer deployer;

	private SipStack receiver;

	private SipCall sipCall;
	private SipPhone sipPhone;

	private static final int TIMEOUT = 10000;	
	private static final int TIMEOUT_CSEQ_INCREASE = 100000;
	private static final int DIALOG_TIMEOUT = 40000;
	
	
	private final Logger logger = Logger.getLogger(ShootmeSipServletTest.class.getName());

	@GetDeployableContainer
	private ContainerManagerTool containerManager = null;

	private static SipStackTool sipStackTool;
	private String testArchive = "simple";

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
		logger.info("About to un-deploy the application");
		deployer.undeploy(testArchive);
		sipStackTool.tearDown();
	}

	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootmesipservlet.war");
		webArchive.addClasses(SimpleSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	
	@Test
	public void testShootme() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		
//		String fromName = "sender";
//		String fromSipAddress = "sip-servlets.com";
//		SipURI fromAddress = senderProtocolObjects.addressFactory.createSipURI(
//				fromName, fromSipAddress);
//				
//		String toUser = "receiver";
//		String toSipAddress = "sip-servlets.com";
//		SipURI toAddress = senderProtocolObjects.addressFactory.createSipURI(
//				toUser, toSipAddress);
//		
//		sender.sendSipRequest("INVITE", fromAddress, toAddress, null, null, false);		
//		Thread.sleep(TIMEOUT);
//		assertTrue(sender.isAckSent());
//		assertTrue(sender.getOkToByeReceived());	
//		// test non regression for Issue 1687 : Contact Header is present in SIP Message where it shouldn't
//		Response response = sender.getFinalResponse();
//		assertNull(response.getHeader(ContactHeader.NAME));
	}
	
	
}
