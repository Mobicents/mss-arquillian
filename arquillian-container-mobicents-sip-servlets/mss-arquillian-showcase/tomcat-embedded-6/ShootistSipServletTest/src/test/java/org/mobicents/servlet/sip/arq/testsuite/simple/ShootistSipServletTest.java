/**
 * 
 */
package org.mobicents.servlet.sip.arq.testsuite.simple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;
import org.jboss.arquillian.container.mobicents.api.annotations.ConcurrencyControlMode;
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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.address.RFC2396UrlDecoder;

/**
 * @author gvagenas@gmail.com
 * 
 */

@RunWith(Arquillian.class)
public class ShootistSipServletTest extends SipTestCase 
{	

	@ArquillianResource
	private Deployer deployer;

	private SipStack receiver;

	private SipCall sipCall;
	private SipPhone sipPhone;

	private final int timeout = 10000;	

	private final Logger logger = Logger.getLogger(ShootistSipServletTest.class.getName());

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
		//        System.setProperty( "javax.net.ssl.keyStore",  ClassLoader.getSystemClassLoader().getResource("testkeys").getPath() );
		//        System.setProperty( "javax.net.ssl.trustStore", ClassLoader.getSystemClassLoader().getResource("testkeys").getPath() );
		//        System.setProperty( "javax.net.ssl.keyStorePassword", "passphrase" );
		//        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
		//	
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
		//TODO: Deployer should return boolean on successful deploy so we can use it to proceed to undeploy
		deployer.undeploy(testArchive);
//		sipStackTool.tearDown();
		if(sipCall != null)	sipCall.disposeNoBye();
		if(sipPhone != null) sipPhone.dispose();
		if(receiver != null) receiver.dispose();
	}

	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservlet.war");
		webArchive.addClasses(ShootistSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}

	//	@Deployment(name="cancel", managed=false)
	//	public static WebArchive createTestArchiveCancel()
	//	{
	//		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservletCancel.war");
	//		webArchive.addClasses(ShootistSipServlet.class);
	//		//We need a new sip.xml in order to set the desired param-value ("cancel","true")
	//		webArchive.addAsWebInfResource("in-container-sip-cancel.xml", "sip.xml");
	//
	//		return webArchive;
	//	}	

	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||

	@Test
	public void testShootist() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		//First create the sipCall and start listening for messages
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");

		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();

		//Deploy the first test archive
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		Thread.sleep(100);

		if (sipCall.getLastReceivedResponse() != null)
			logger.info("sipCallB lastReceivedResponse: "+sipCall.getLastReceivedResponse().toString());

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@Test @ContextParam(name="cancel",value="true")
	public void testShootistCancel() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");

		Address addr = sipPhone.getParent().getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhone.getParent().getHeaderFactory().createContactHeader(addr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		sipCall.waitForIncomingCall(60000);
		sipCall.listenForCancel();

		//		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout,null,replacedHeaders,null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout,null,replacedHeaders,null));

		//	https://lists.cs.columbia.edu/pipermail/sip-implementors/2005-December/011525.html
		//	An early dialog is only established by a 1xx response with a to-tag.
		//
		//	A CANCEL can be sent as along as any 1xx response has been received. 
		//	Typically, a 100-Trying will be received from the next hop proxy. This 
		//	100-Trying will not usually have a to-tag since it is generated by a proxy 
		//	and not a UAS.
		//
		//	The CANCEL applies to all branches of the INVITE. If any proxy along the 
		//	path forked the INVITE toward multiple User Agents, there will be multiple 
		//	branches and possibly multiple early dialogs depending on how many UAS's 
		//	sent back a 1xx with a to-tag.
		//
		//	You use CANCEL to terminate the INVITE transaction and all branches and 
		//	early dialogs it created. That is why the CANCEL must match the INVITE 
		//	exactly.
		//
		//	You use BYE if you wish to terminate a specific early dialog, but let any 
		//	other branches or early dialogs complete.
		//
		//	In the case of a re-INVITE on an existing confirmed dialog, the INVITE will 
		//	not be forked and will reach the other UA in the dialog. A CANCEL could be 
		//	used to cancel the re-INVITE without terminating the dialog. However, this 
		//	may not be very useful since most UAs will send back a final response to the 
		//	re-INVITE fairly quickly and the CANCEL would not reach the UA in time.

		SipTransaction trans1 = sipCall.waitForCancel(timeout);	
		assertNotNull(trans1);

		assertRequestReceived("CANCEL NOT RECEIVED", SipRequest.CANCEL, sipCall);
		assertTrue(sipCall.respondToCancel(trans1, 200, "0K", -1));

		// close the INVITE transaction on the called leg
		assertTrue("487 NOT SENT", sipCall.sendIncomingCallResponse(SipResponse.REQUEST_TERMINATED, "Request Terminated", 0));

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertTrue(allMessagesContent.size()>=2);
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@ContextParamMap("testShootistCancelServletTimerCancelConcurrency")
	private Map<String, String> contextMap1 = new ContextParamMapConstructTool()
	.put("cancel", "true")
	.put("servletTimer", "500").getMap();

	@Test @ContextParamMap("testShootistCancelServletTimerCancelConcurrency") 
	@ConcurrencyControlMode(org.mobicents.servlet.sip.annotation.ConcurrencyControlMode.SipApplicationSession)
	public void testShootistCancelServletTimerCancelConcurrency() throws Exception {

		//		String testArchive = "simple";
		//
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");

		Address addr = sipPhone.getParent().getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhone.getParent().getHeaderFactory().createContactHeader(addr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		sipCall.waitForIncomingCall(60000);
		sipCall.listenForCancel();

		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout,null,replacedHeaders,null));

		SipTransaction trans1 = sipCall.waitForCancel(timeout);	
		assertNotNull(trans1);

		assertRequestReceived("CANCEL NOT RECEIVED", SipRequest.CANCEL, sipCall);
		assertTrue(sipCall.respondToCancel(trans1, 200, "0K", -1));

		// close the INVITE transaction on the called leg
		assertTrue("487 NOT SENT", sipCall.sendIncomingCallResponse(SipResponse.REQUEST_TERMINATED, "Request Terminated", 0));

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));


		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@ContextParamMap("testShootistCancelServletTimerConcurrency")
	private Map<String, String> contextMap2 = new ContextParamMapConstructTool()
	.put("cancel", "true")
	.put("servletTimer", "0").getMap();

	@Test @ContextParamMap("testShootistCancelServletTimerConcurrency")
	@ConcurrencyControlMode(org.mobicents.servlet.sip.annotation.ConcurrencyControlMode.SipApplicationSession)
	public void testShootistCancelServletTimerConcurrency() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");

		Address addr = sipPhone.getParent().getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhone.getParent().getHeaderFactory().createContactHeader(addr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		sipCall.waitForIncomingCall(60000);
		sipCall.listenForCancel();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout,null,replacedHeaders,null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout,null,replacedHeaders,null));

		SipTransaction trans1 = sipCall.waitForCancel(timeout);	
		assertNotNull(trans1);

		assertRequestReceived("CANCEL NOT RECEIVED", SipRequest.CANCEL, sipCall);
		assertTrue(sipCall.respondToCancel(trans1, 200, "0K", -1));

		// close the INVITE transaction on the called leg
		assertTrue("487 NOT SENT", sipCall.sendIncomingCallResponse(SipResponse.REQUEST_TERMINATED, "Request Terminated", 0));

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}
	//
	@Test @ContextParam(name="cancel", value="true")
	public void testShootistEarlyMediaChange() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");

		Address addr = sipPhone.getParent().getAddressFactory().createAddress("Shootme <sip:127.0.0.1:5080;transport=udp>");	
		ContactHeader contactHeader = sipPhone.getParent().getHeaderFactory().createContactHeader(addr);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(contactHeader);

		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		sipCall.listenForCancel();

		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing", timeout,null,replacedHeaders,null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.SESSION_PROGRESS,"Session Progress"+System.nanoTime(), timeout,null,replacedHeaders,null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.SESSION_PROGRESS,"Session Progress"+System.nanoTime(), timeout,null,replacedHeaders,null));
		assertTrue(sipCall.sendIncomingCallResponse(Response.SESSION_PROGRESS,"Session Progress"+System.nanoTime(), timeout,null,replacedHeaders,null));

		SipTransaction trans1 = sipCall.waitForCancel(timeout);	
		assertNotNull(trans1);

		assertRequestReceived("CANCEL NOT RECEIVED", SipRequest.CANCEL, sipCall);
		assertTrue(sipCall.respondToCancel(trans1, 200, "0K", -1));

		// close the INVITE transaction on the called leg
		assertTrue("487 NOT SENT", sipCall.sendIncomingCallResponse(SipResponse.REQUEST_TERMINATED, "Request Terminated", 0));

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();

		assertTrue(sipCall.getMessageRequest().getHeader("EarlyMediaResponses").toString().contains("3"));
		assertTrue(allMessagesContent.size()>=2);
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}
	//	
	@Test
	public void testShootistSetContact() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		SipRequest inviteRequest = sipCall.getLastReceivedRequest();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		assertTrue(inviteRequest.getMessage().getHeader("Contact").toString().contains("uriparam=urivalue"));
		assertTrue((inviteRequest.getMessage().getHeader("Contact").toString().contains("headerparam1=headervalue1")));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@Test @ContextParam(name="toTag", value="callernwPort1241042500479")
	public void testShootistSetToTag() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();		
	}

	@Test @ContextParam(name="toParam", value="http://yaris.research.att.com:23280/vxml/test.jsp")
	public void testShootistSetToParam() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@ContextParamMap("testShootistSetToWithParam")
	private Map<String, String> contextMap3 = new ContextParamMapConstructTool()
	.put("username", "sip:+34666666666@127.0.0.1:5080;pres-list=mylist")
	.put("useStringFactory", "true").getMap();

	@Test
	@ContextParamMap("testShootistSetToWithParam")
	public void testShootistSetToWithParam() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		SipRequest inviteRequest = sipCall.getLastReceivedRequest();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		ToHeader toHeader = (ToHeader) inviteRequest.getMessage().getHeader(ToHeader.NAME);
		assertEquals("To: <sip:+34666666666@127.0.0.1:5080;pres-list=mylist>", 
				toHeader.toString().trim());

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@Test @ContextParam(name="toParam", value="http://yaris.research.att.com:23280/vxml/test.jsp?toto=tata")
	public void testShootistSetEscapedParam() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		SipRequest inviteRequest = sipCall.getLastReceivedRequest();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		ToHeader toHeader = (ToHeader) inviteRequest.getMessage().getHeader(ToHeader.NAME);
		String toParam = ((SipURI)toHeader.getAddress().getURI()).getParameter("toParam");
		logger.info(toParam);
		assertEquals("http://yaris.research.att.com:23280/vxml/test.jsp?toto=tata" , RFC2396UrlDecoder.decode(toParam));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}


	@Test
	public void testJainSipAckCreationViaParams() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//		
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		//Deploy the second test archive
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		SipRequest inviteRequest = sipCall.getLastReceivedRequest();

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));

		ViaHeader viaHeader = (ViaHeader) inviteRequest.getMessage().getHeader(ViaHeader.NAME); 
		viaHeader.setParameter("testAckViaParam", "true");
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(viaHeader);

		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout,null,replacedHeaders,null));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		Thread.sleep(500);

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	/**
	 * non regression test for Issue 1025 http://code.google.com/p/mobicents/issues/detail?id=1025
	 * sipservletlistner called twice on redeploy
	 */
	@Test @ContextParam(name="testServletListener", value="true")
	public void testShootistSipServletListener() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		Thread.sleep(timeout);

		deployer.undeploy(testArchive);

		Thread.sleep(timeout);

		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		Thread.sleep(timeout);

		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(2,allMessagesContent.size());

		//Reload Context
		containerManager.reloadContext();

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		allMessagesContent = sipCall.getAllReceivedMessagesContent();
		assertEquals(3,allMessagesContent.size());

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	/**
	 * non regression test for Issue 1090 http://code.google.com/p/mobicents/issues/detail?id=1090
	 * 	Content-Type is not mandatory if Content-Length is 0
	 */
	@Test @ContextParam(name="testContentLength", value="testContentLength")
	public void testShootistContentLength() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();		

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		assertNotNull(sipCall.getMessageRequest());	

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@Test
	public void testShootistCallerSendsBye() throws Exception {

		//		String testArchive = "simple";
		//		
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.disconnect();

		Thread.sleep(timeout);

		assertTrue(sipCall.getReturnCode()==200);

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	@Test
	public void testShootistUserAgentHeader() throws Exception {

		//Prepare the new sipStackProperties
		Properties sipStackProperties = new Properties();
		sipStackProperties.setProperty("javax.sip.STACK_NAME", "mss-127.0.0.1" + "-" + 5070);
		sipStackProperties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT",
				"off");
		sipStackProperties.setProperty(
				"gov.nist.javax.sip.DELIVER_UNSOLICITED_NOTIFY", "true");
		sipStackProperties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE",
				"64");
		sipStackProperties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER",
				"true");
		sipStackProperties.setProperty("org.mobicents.servlet.sip.USER_AGENT_HEADER",
				"MobicentsSipServletsUserAgent");

		//		//Name of the testArchive
		//		String testArchive = "simple";
		//		
		//		//Create the sipSphone
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		logger.info("Create the sipCall and start listening for incoming calls");
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();

//		//Upon start of the container reads the configuration and adds the connectors defined there, so the remove should be done
//		//again after the container start and before the addSipConnector
//		//Remove the existing SipConnectors from the container
//		logger.info("Stop the SipConnectors");
//		for (SipConnector sipConnector : sipConnectors) {
//			containerManager.removeSipConnector(sipConnector);
//		}

		//TODO: Add this reminder to the documentation
		/*
		 * Remember that on startup the container is using the configuration to setup again, so that means that the 
		 * SipConnectors will be created again. In this specific case, we need to remove the sipConnectors again and then add then one 
		 * we wish.
		 * 
		 * If this automation (automatically add sipConnector from configuration) would happen, then the developer would have to add them 
		 * manually in every test case.
		 *  
		 */

		logger.info("Restart the container and pass new SipStack properties");
		containerManager.restartContainer(sipStackProperties);
		
		Thread.sleep(timeout);
		
		//Get the list of SipConnectors
		List<SipConnector> sipConnectors = containerManager.getSipConnectors();

		//Upon start of the container reads the configuration and adds the connectors defined there, so the remove should be done
		//again after the container start and before the addSipConnector
		//Remove the existing SipConnectors from the container
		logger.info("Stop the SipConnectors");
		for (SipConnector sipConnector : sipConnectors) {
			containerManager.removeSipConnector(sipConnector);
		}

		logger.info("Add a new SipConnector");
		containerManager.addSipConnector("127.0.0.1", 5070, ListeningPoint.UDP);

		Thread.sleep(timeout);
		
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		UserAgentHeader userAgentHeader = (UserAgentHeader) request.getMessage().getHeader(UserAgentHeader.NAME);
		assertNotNull(userAgentHeader);
		assertTrue(userAgentHeader.toString().contains("MobicentsSipServletsUserAgent"));

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	/**
	 * non regression test for Issue 1150 http://code.google.com/p/mobicents/issues/detail?id=1150
	 * 	Contact header contains "transport" parameter even when there are two connectors (UDP and TCP)
	 */
	@Test
	public void testShootistContactTransport() throws Exception {

		//		//Name of the testArchive
		//		String testArchive = "simple";
		//		
		//		//Create the sipSphone
		//		SipStackTool sipStackTool = new SipStackTool();
		//		
		//		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		//		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		//
		//		logger.info("Create the sipCall and start listening for incoming calls");
		//		sipCall = sipPhone.createSipCall();
		//		sipCall.listenForIncomingCall();

		containerManager.addSipConnector("127.0.0.1", 5071, ListeningPoint.TCP);

		containerManager.restartContainer();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		ContactHeader contactHeader = (ContactHeader) request.getMessage().getHeader(ContactHeader.NAME);	
		assertFalse(((SipURI)contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
		String contact = contactHeader.getAddress().toString();
		assertTrue(contact.contains("BigGuy@"));
		assertTrue(contact.contains("from display"));

		//		logger.info("About to un-deploy the application");
		//		deployer.undeploy(testArchive);
		//		sipStackTool.tearDown();
	}

	//TODO: Implement TLS for SipUnit
	@Ignore @Test @ContextParam(name="secureRURI", value="true")
	public void testShootistContactTlsTransport() throws Exception {
	
//		fail("SipUnit currently doesn't support TLS transport");
		
//		containerManager.restartContainer();

		containerManager.addSipConnector("localhost", 5071, ListeningPoint.TCP);
		containerManager.addSipConnector("localhost", 5072, ListeningPoint.TLS);

		receiver = sipStackTool.initializeSipStack("tls", "localhost", "5080", "localhost:5070");
		sipPhone = receiver.createSipPhone("localhost", "tls", 5070, "sip:LittleGuy@there.com");
		
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"Ringing",timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));

		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();

		ContactHeader contactHeader = (ContactHeader) request.getMessage().getHeader(ContactHeader.NAME);	
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sips:"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
		String viaString = request.getMessage().getHeader(ViaHeader.NAME).toString();
		assertTrue(viaString.toLowerCase().contains("tls"));
		assertTrue(viaString.toLowerCase().contains("5072"));
	}

	//TODO: Implement TLS for SipUnit 
	/**
	 * non regression test for Issue 2269 http://code.google.com/p/mobicents/issues/detail?id=2269
	 * Wrong Contact header scheme URI in case TLS call with 'sip:' scheme
	 */
	@Ignore @Test @ContextParam(name="trasnportRURI", value="tls")
	public void testShootistContactNonSecureURITlsTransport() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		assertTrue(true);
		
//		fail("SipUnit currently doesn't support TLS transport");
		
//		receiverProtocolObjects =new ProtocolObjects(
//				"tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);				
//		
//		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
//		SipProvider senderProvider = receiver.createProvider();			
//		
//		senderProvider.addSipListener(receiver);
//		
//		receiverProtocolObjects.start();
//		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
//		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
//		tomcat.startTomcat();
//		deployApplication("transportRURI", "tls");
//		Thread.sleep(TIMEOUT);
//		assertTrue(receiver.getByeReceived());
//		ContactHeader contactHeader = (ContactHeader) receiver.getInviteRequest().getHeader(ContactHeader.NAME);	
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sip:"));
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
//		String viaString = receiver.getInviteRequest().getHeader(ViaHeader.NAME).toString();
//		assertTrue(viaString.toLowerCase().contains("tls"));
//		assertTrue(viaString.toLowerCase().contains("5072"));
	}
	
	/**
	 * non regression test for Issue 1150 http://code.google.com/p/mobicents/issues/detail?id=1150
	 * 	Contact header contains "transport" parameter even when there are two connectors (UDP and TCP)
	 */
	@Test @ContextParam(name="outboundInterface", value="tcp")
	public void testShootistOutboundInterfaceTransport() throws Exception {
		
		containerManager.addSipConnector("127.0.0.1", 5071, ListeningPoint.TCP);
		
		//Re-initialize the sipPhone and sipCall for the specific test
		sipCall.disposeNoBye();
		sipPhone.dispose();
		receiver.dispose();
		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_TCP, "127.0.0.1", "5080", "127.0.0.1:5070");
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_TCP, 5070, "sip:LittleGuy@there.com");
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
		
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());
		
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		
		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();
		
		ContactHeader contactHeader = (ContactHeader) request.getMessage().getHeader(ContactHeader.NAME);	
		assertFalse(((SipURI)contactHeader.getAddress().getURI()).toString().contains("transport=udp"));
	}
	
	
	/*
	 * Work with SipPhone where the sipCall cannot help
	 */
	
	/**
	 * non regression test for Issue 1412 http://code.google.com/p/mobicents/issues/detail?id=1412
	 * Contact header is added to REGISTER request by container
	 */
	@Test @ContextParam(name="method", value="REGISTER")
	public void testShootistRegister() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		
		assertTrue(sipPhone.listenRequestMessage());
		
		RequestEvent requestEvent = sipPhone.waitRequest(timeout);
		Request request = requestEvent.getRequest();
		assertTrue(((Request)request).getMethod().equals(Request.REGISTER));
		
		sipPhone.sendUnidirectionalResponse(requestEvent, Response.OK, "OK", null, null, timeout);
		
		assertNull(request.getHeader(ContactHeader.NAME));		
	}
		
	/**
	 * non regression test for http://code.google.com/p/mobicents/issues/detail?id=2288
	 * SipServletRequest.send() throws IllegalStateException instead of IOException
	 */
	@Test @ContextParam(name="testIOException", value="example.com")
	public void testShootistIOException() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		
		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		
		assertEquals(1,allMessagesContent.size());
		assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));		
	}
	
	@ContextParamMap("testShootistIOExceptionTransportChange")
	private Map<String, String> contextMap4 = new ContextParamMapConstructTool()
	.put("transportRURI", "tcp")
	.put("testIOException", "127.0.0.1").getMap();
	
	/**
	 * non regression test for http://code.google.com/p/mobicents/issues/detail?id=2288
	 * SipServletRequest.send() throws IllegalStateException instead of IOException
	 */
	@Test @ContextParamMap(value = "testShootistIOExceptionTransportChange")
	public void testShootistIOExceptionTransportChange() throws Exception {
		
		containerManager.addSipConnector("127.0.0.1", 5070, ListeningPoint.TCP);
		
		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendMessageResponse(Response.OK, "OK", timeout));
		
		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		
		assertEquals(1,allMessagesContent.size());
		assertTrue("IOException not thrown", allMessagesContent.contains("IOException thrown"));	
		
	} 

	@ContextParamMap("testShootistRegisterContactNonSecureURITlsTransport")
	private Map<String, String> contextMap5 = new ContextParamMapConstructTool()
	.put("transportRURI", "tls")
	.put("method", "REGISTER").getMap();
	
//	//TODO: Implement TLS for SipUnit
	/**
	 * non regression test for Issue 2269 http://code.google.com/p/mobicents/issues/detail?id=2269
	 * Wrong Contact header scheme URI in case TLS call with request URI 'sip:' scheme and contact is uri is secure with "sips"
	 */
	@Ignore @Test @ContextParamMap(value="testShootistRegisterContactNonSecureURITlsTransport")
	public void testShootistRegisterContactNonSecureURITlsTransport() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		assertTrue(true);
		
//		fail("SipUnit currently doesn't support TLS transport");
		
//		//		receiver.sendInvite();
//		receiverProtocolObjects =new ProtocolObjects(
//				"tls_receiver", "gov.nist", "TLS", AUTODIALOG, null, null, null);				
//		
//		receiver = new TestSipListener(5080, 5070, receiverProtocolObjects, false);
//		SipProvider senderProvider = receiver.createProvider();			
//		
//		senderProvider.addSipListener(receiver);
//		
//		receiverProtocolObjects.start();
//		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5071, ListeningPoint.TCP);
//		sipConnector = tomcat.addSipConnector(serverName, sipIpAddress, 5072, ListeningPoint.TLS);
//		tomcat.startTomcat();
//		Map<String, String> params = new HashMap<String, String>();
//		params.put("transportRURI", "tls");
//		params.put("method", "REGISTER");
//		deployApplication(params);
//		tomcat.startTomcat();
//		Thread.sleep(TIMEOUT);
//		ContactHeader contactHeader = (ContactHeader) receiver.getRegisterReceived().getHeader(ContactHeader.NAME);
//		assertNotNull(contactHeader);	
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("sip:"));
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().contains("5072"));
//		assertTrue(((SipURI)contactHeader.getAddress().getURI()).toString().toLowerCase().contains("transport=tls"));
	}

	@ContextParamMap("testShootistProxyAuthorization")
	private Map<String, String> contextMap6 = new ContextParamMapConstructTool()
	.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"")
	.put("headerToAdd", "Proxy-Authorization").getMap();
	
	/**
	 * non regression test for Issue 1547 http://code.google.com/p/mobicents/issues/detail?id=1547
	 * Can't add a Proxy-Authorization using SipServletMessage.addHeader
	 */
	@Test @ContextParamMap("testShootistProxyAuthorization")
	public void testShootistProxyAuthorization() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());
		
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		
		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();
		
		assertNotNull(request.getMessage().getHeader(ProxyAuthorizationHeader.NAME));		
		assertNotNull(request.getMessage().getHeader(ProxyAuthenticateHeader.NAME));
	}
	
	@ContextParamMap("testShootistAuthorization")
	private Map<String, String> contextMap7 = new ContextParamMapConstructTool()
	.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"")
	.put("headerToAdd", "Authorization").getMap();
	
	/**
	 * non regression test for Issue 2798 http://code.google.com/p/mobicents/issues/detail?id=2798
	 * Can't add an Authorization using SipServletMessage.addHeader
	 */
	@Test @ContextParamMap("testShootistAuthorization")
	public void testShootistAuthorization() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());
		
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		
		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();
		
		assertNotNull(request.getMessage().getHeader(AuthorizationHeader.NAME));	
	}
	
	// Tests Issue 1693 http://code.google.com/p/mobicents/issues/detail?id=1693
	@Test @ContextParam(name="testErrorResponse", value="true")
	public void testShootistErrorResponse() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());
		
		assertTrue(sipCall.sendIncomingCallResponse(Response.SERVER_INTERNAL_ERROR,"SERVER_INTERNAL_ERROR", timeout));

		assertTrue(sipCall.waitForAck(timeout));
		
		Thread.sleep(timeout+40000);
		
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		
		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();

		assertEquals(2,allMessagesContent.size());
		assertTrue("sipSessionReadyToInvalidate", allMessagesContent.contains("sipSessionReadyToInvalidate"));
		assertTrue("sipAppSessionReadyToInvalidate", allMessagesContent.contains("sipAppSessionReadyToInvalidate"));

	}
	
	// Test for SS spec 11.1.6 transaction timeout notification
	// Test Issue 2580 http://code.google.com/p/mobicents/issues/detail?id=2580
	@Test
	public void testTransactionTimeoutResponse() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);
		
		Thread.sleep(40000);
		
		assertTrue(sipCall.waitForMessage(timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		List<String> allMessagesContent = sipCall.getAllReceivedMessagesContent();
		
		Iterator<String> allMessagesIterator = allMessagesContent.iterator();		
		while (allMessagesIterator.hasNext()) {
			String message = (String) allMessagesIterator.next();
			logger.info(message);
			assertTrue(message.contains("408 received"));
		}
	}	
	
	/*
	 * http://code.google.com/p/mobicents/issues/detail?id=2902
	 */
	@Test @ContextParam(name="testRemoteAddrAndPort", value="true")
	public void testShootistRemoteAddrAndPort() throws Exception {

		logger.info("About to deploy the application");
		deployer.deploy(testArchive);

		assertTrue(sipCall.waitForIncomingCall(timeout));
		SipRequest request = sipCall.getLastReceivedRequest();
		assertTrue(request.isInvite());
		
		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		
		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();	
	}
	
}
