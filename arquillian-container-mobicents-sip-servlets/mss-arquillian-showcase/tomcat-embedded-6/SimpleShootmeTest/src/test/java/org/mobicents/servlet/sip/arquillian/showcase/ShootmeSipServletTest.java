/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.showcase;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Semaphore;
import java.util.logging.Logger;

import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.AllowHeader;
import javax.sip.header.AuthenticationInfoHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.Header;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.RouteHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;
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
 * Arquillian is controlling the lifecycle of the container and the test archive. That means it starts and stops the container and deploys
 * the test archive when it is needed
 * 
 * @author gvagenas@gmail.com 
 * 
 */
@RunWith(Arquillian.class)
public class ShootmeSipServletTest extends SipTestCase 
{
	private final Logger logger = Logger.getLogger(ShootmeSipServletTest.class.getName());
	
	//SipUnit Elements
	private SipStack receiver;
	private SipCall sipCallReceiver;
	private SipPhone sipPhoneReceiver;
	private String receiverPort = "5058";
	private String receiverURI = "sip:receiver@sip-servlets.com";

	private SipStack sender;
	private SipCall sipCallSender;
	private SipPhone sipPhoneSender;
	private String senderPort = "5080";
	private String senderURI = "sip:sender@sip-servlets.com";

	private int proxyPort = 5070;

	private static final int TIMEOUT = 10000;	
	private static final int DIALOG_TIMEOUT = 40000;

	public final static String[] ALLOW_HEADERS = new String[] {"INVITE","ACK","CANCEL","OPTIONS","BYE","SUBSCRIBE","NOTIFY","REFER"};

	private static SipStackTool receiverSipStackTool;
	private static SipStackTool senderSipStackTool;
	private String testArchive = "simple";
	private Boolean isDeployed = false;

	long cseqInt = 0;
	Semaphore semaphore = new Semaphore(1);

	@BeforeClass
	public static void beforeClass(){
		receiverSipStackTool = new SipStackTool("receiver");
		senderSipStackTool = new SipStackTool("sender");
	}

	@Before
	public void setUp() throws Exception
	{
		//Initialize the SipUnit elements
		receiver = receiverSipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", receiverPort, "127.0.0.1:5070");
		sipPhoneReceiver = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, receiverURI);
		sipCallReceiver = sipPhoneReceiver.createSipCall();
		sipCallReceiver.listenForIncomingCall();
		
		//Initialize the SipUnit elements
		sender = senderSipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", senderPort, "127.0.0.1:5070");
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();
		sipCallSender.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		//Log last Error, Exception, and returnCode
		logger.info("Error Message: "+sipCallSender.getErrorMessage());
		logger.info("Exception Message: "+sipCallSender.getException());
		logger.info("Return Code: "+sipCallSender.getReturnCode());

		if(sipCallSender != null)	sipCallSender.disposeNoBye();
		if(sipPhoneSender != null) sipPhoneSender.dispose();
		if(sender != null) sender.dispose();

		if(sipCallReceiver != null) sipCallReceiver.disposeNoBye();
		if(sipPhoneReceiver != null) sipPhoneReceiver.dispose();
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

		sipCallSender.initiateOutgoingCall(receiverURI, null);

		Thread.sleep(TIMEOUT);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendInviteOkAck());
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(TIMEOUT);
		// test non regression for Issue 1687 : Contact Header is present in SIP Message where it shouldn't
		SipResponse response = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.OK, response.getStatusCode());
		assertNull(response.getMessage().getHeader(ContactHeader.NAME));
	}

	@Test
	public void testShootmeSendBye() throws InterruptedException, SipException, ParseException, InvalidArgumentException {

		//Replace toHeader, so toURI can be used as RequestURI
		Address toAddr = sipPhoneSender.getParent().getAddressFactory().createAddress("<sip:receiver@sip-servlets.com>");	
		ToHeader toHeader = sipPhoneSender.getParent().getHeaderFactory().createToHeader(toAddr, null);
		ArrayList<Header> replacedHeaders = new ArrayList<Header>();
		replacedHeaders.add(toHeader);

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:SSsendBye@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();	

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, replacedHeaders, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.sendInviteOkAck());

		sipCallSender.listenForDisconnect();

		assertTrue(sipCallSender.waitForDisconnect(TIMEOUT*2));
		assertTrue(sipCallSender.respondToDisconnect(200,"ΟΚ",null,replacedHeaders,null));

		Thread.sleep(TIMEOUT*4);
	}

	@Test
	public void testShootmeRegister() throws Exception {

		SipURI requestURI = sender.getAddressFactory().createSipURI("sender","127.0.0.1:5070;transport=udp");
		assertTrue(sipPhoneSender.register(requestURI, "no_user", "no_password", "sip:sender@127.0.0.1:5080;transport=udp;lr", TIMEOUT, TIMEOUT));
	}

	@Test
	public void testShootmeCancel() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:cancel@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();		

		String requestURI = "sip:receiver@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());
		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.RINGING, sipCallSender.getLastReceivedResponse().getStatusCode());

		SipTransaction sipTrans1 = sipCallSender.sendCancel();
		assertTrue(sipCallSender.waitForCancelResponse(sipTrans1, TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.REQUEST_TERMINATED, sipCallSender.getLastReceivedResponse().getStatusCode());

		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		assertTrue(sipCallSender.waitForMessage(TIMEOUT));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();
		assertTrue(allMessagesContent.contains("cancelReceived"));		
	}

	@Test
	public void testShootmeMultipleValueHeaders() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:TestAllowHeader@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();	

		String requestURI = "sip:TestAllowHeader@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		assertEquals(Response.TRYING, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.waitOutgoingCallResponse());
		SipResponse finalResponse = sipCallSender.getLastReceivedResponse();
		assertEquals(Response.METHOD_NOT_ALLOWED, finalResponse.getStatusCode());

		//Issue 1164 non regression test
		ListIterator<AllowHeader> allowHeaders = (ListIterator<AllowHeader>) finalResponse.getMessage().getHeaders(AllowHeader.NAME);
		assertNotNull(allowHeaders);

		List<String> allowHeadersList = new ArrayList<String>();
		while (allowHeaders.hasNext()) {
			allowHeadersList.add(allowHeaders.next().getMethod());
		}
		assertTrue(Arrays.equals(ALLOW_HEADERS, (String[])allowHeadersList.toArray(new String[allowHeadersList.size()])));
	}

	@Test
	public void testShootmeToTag() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:TestToTag@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();
		
		String requestURI = "sip:TestAllowHeader@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	@Test
	public void testSubscriberURI() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testSubscriberUri@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();	

		String requestURI = "sip:testSubscriberUri@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	@Test
	public void testFlagParameter() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testFlagParameter@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();	

		String requestURI = "sip:testFlagParameter@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());
	}

	@Test
	public void testSessionRetrieval() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:testSessionRetrieval@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		String requestURI = "sip:testSessionRetrieval@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertTrue(sipCallSender.sendInviteOkAck());

		Thread.sleep(2000);
		assertTrue(sipCallSender.disconnect());
		Thread.sleep(1000);
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());		
	}

	@Test
	public void testNoAckReceived() throws Exception {

		int nbRetrans = 0;

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		//Override default options for sender
		senderURI = "sip:noAckReceived@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();	

		String requestURI = "sip:noAckReceived@127.0.0.1:5070;transport=udp";

		sipCallSender.initiateOutgoingCall(senderURI, requestURI, null, additionalHeaders, null, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));

		//Override default options for sender
		senderURI = "sip:receiver@sip-servlets.com";
		sipPhoneSender = sender.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, proxyPort, senderURI);
		sipCallSender = sipPhoneSender.createSipCall();

		sipCallSender.listenForMessage();

		// test http://code.google.com/p/mobicents/issues/detail?id=1681
		// Make sure we get the 10 retrans for 200 to INVITE when no ACK is sent
		// corresponding to Timer G				
		assertTrue(sipCallSender.waitForMessage(DIALOG_TIMEOUT+TIMEOUT));
		assertTrue(sipCallSender.sendMessageResponse(200, "OK", -1));
		List<String> allMessagesContent = sipCallSender.getAllReceivedMessagesContent();		

		assertEquals(1,allMessagesContent.size());
		assertEquals("noAckReceived", allMessagesContent.get(0));

		nbRetrans = sender.getRetransmissions();
		logger.info("nbRetrans: "+nbRetrans);
		assertTrue(nbRetrans >= 9);
	}

	@Test
	public void testShootmeAuthenticationInfoHeader() throws Exception {

		//Add custom Header
		Header extraHeader = sender.getHeaderFactory().createHeader("REM", "RRRREM");
		ArrayList<Header> additionalHeaders = new ArrayList<Header>();
		additionalHeaders.add(extraHeader);

		SipURI requestURI = sender.getAddressFactory().createSipURI("receiver","127.0.0.1:5070;transport=udp");
		SipURI proxyUri = sender.getAddressFactory().createSipURI(null,"127.0.0.1:5070;lr;transport=udp");

		// build the INVITE Request message
		AddressFactory addr_factory = sipPhoneSender.getParent().getAddressFactory();
		HeaderFactory hdr_factory = sipPhoneSender.getParent().getHeaderFactory();

		CallIdHeader callIdHeader = sipPhoneSender.getParent().getSipProvider().getNewCallId();
		CSeqHeader cSeqHeader = hdr_factory.createCSeqHeader(1, Request.REGISTER);
		Address from_address = addr_factory.createAddress("sip:authenticationInfoHeader@sip-servlets.com");
		FromHeader fromHeader = hdr_factory.createFromHeader(from_address, sipPhoneSender.generateNewTag());

		Address contactAddr = addr_factory.createAddress("sip:authenticationInfoHeader@127.0.0.1:5080;transport=udp;lr");
		ContactHeader contactHeader = hdr_factory.createContactHeader(contactAddr);

		Address to_address = addr_factory.createAddress(addr_factory.createURI("sip:receiver@sip-servlets.com"));
		ToHeader toHeader = hdr_factory.createToHeader(to_address, null);

		MaxForwardsHeader maxForwards = hdr_factory.createMaxForwardsHeader(5);
		ArrayList<ViaHeader> viaHeaders = sipPhoneSender.getViaHeaders();

		Request request = sender.getMessageFactory().createRequest(requestURI, "REGISTER", callIdHeader, cSeqHeader,
				fromHeader, toHeader, viaHeaders, maxForwards);

		Address address = addr_factory.createAddress(proxyUri);
		RouteHeader routeHeader = hdr_factory.createRouteHeader(address);

		request.addHeader(extraHeader);
		request.addHeader(contactHeader);
		request.addHeader(routeHeader);

		// send the Request message
		SipTransaction trans = sipPhoneSender.sendRequestWithTransaction(request, false, null);
		assertNotNull(sipPhoneSender.format(), trans);

		javax.sip.ResponseEvent respEvent = (ResponseEvent) sipPhoneSender.waitResponse(trans, TIMEOUT);
		assertEquals(Response.UNAUTHORIZED, respEvent.getResponse().getStatusCode());



		AuthenticationInfoHeader authenticationInfoHeader = (AuthenticationInfoHeader) respEvent.getResponse().getHeader(AuthenticationInfoHeader.NAME);
		assertEquals("Authentication-Info: NTLM rspauth=\"01000000000000005CD422F0C750C7C6\",srand=\"0B9D33A2\",snum=\"1\",opaque=\"BCDC0C9D\",qop=\"auth\",targetname=\"server.contoso.com\",realm=\"SIP Communications Service\"",
				authenticationInfoHeader.toString().trim());

	}
}
