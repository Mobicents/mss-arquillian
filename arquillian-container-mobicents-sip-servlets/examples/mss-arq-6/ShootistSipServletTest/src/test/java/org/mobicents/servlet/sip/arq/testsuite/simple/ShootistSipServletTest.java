/**
 * 
 */
package org.mobicents.servlet.sip.arq.testsuite.simple;

import static org.cafesip.sipunit.SipAssert.assertRequestReceived;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipRequest;
import org.cafesip.sipunit.SipResponse;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.cafesip.sipunit.SipTransaction;
import org.jboss.arquillian.container.mss.extension.ContextParamTool;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.mss.extension.SipStackToolStatic;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ConcurrencyControlMode;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParam;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParamMap;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.impl.client.container.ContainerRestarter;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @author gvagenas@gmail.com
 * 
 */

@RunWith(Arquillian.class)
public class ShootistSipServletTest extends SipTestCase 
{	
	@ArquillianResource
    private Deployer deployer;
	
	private SipStackTool receiver;

	private SipCall sipCall;
	private SipPhone sipPhone;
	
	private final int timeout = 10000;	

	private final Logger logger = Logger.getLogger(ShootistSipServletTest.class.getName());

	@Before
	public void setUp() throws Exception
	{
			//Nothing to do here
	}

	@After
	public void tearDown() throws Exception
	{
		//Nothing to do here
	}

	@Deployment(name="simple", managed=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservlet.war");
		webArchive.addClasses(ShootistSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	@Deployment(name="cancel", managed=false)
	public static WebArchive createTestArchiveCancel()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "shootistsipservletCancel.war");
		webArchive.addClasses(ShootistSipServlet.class);
		//We need a new sip.xml in order to set the desired param-value ("cancel","true")
		webArchive.addAsWebInfResource("in-container-sip-cancel.xml", "sip.xml");

		return webArchive;
	}	

	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||


	@Test  
	public void testShootist() throws Exception {
		
		//First create the sipCall and start listening for messages
		receiver = new SipStackTool(5080, 5070);
		
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
		
		//Deploy the first test archive
		deployer.deploy("simple");
		
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
		
		Thread.sleep(500);
		
		deployer.undeploy("simple");
		receiver.tearDown();
		Thread.sleep(timeout);
	}
	
	@Test @ContextParam(name="cancel",value="true")
	public void testShootistCancel() throws Exception {
		
		String testArchive = "simple";
		
		//First create the sipCall and start listening for messages
		receiver = new SipStackTool(5080, 5070);
		
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
		
		//Deploy the second test archive
		deployer.deploy(testArchive);
		
		sipCall.waitForIncomingCall(500);

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		Thread.sleep(100);
		
		sipCall.listenForCancel();
		Thread.sleep(500);
		
		SipTransaction trans1 = sipCall.waitForCancel(5000);	
        assertNotNull(trans1);
        assertRequestReceived("CANCEL NOT RECEIVED", SipRequest.CANCEL, sipCall);
        assertTrue(sipCall.respondToCancel(trans1, 200, "0K", -1));
		
		Thread.sleep(500);

        // close the INVITE transaction on the called leg
        assertTrue("487 NOT SENT", sipCall.sendIncomingCallResponse(
                SipResponse.REQUEST_TERMINATED, "Request Terminated", 0));
		
//		ArrayList<SipRequest> allMessagesContent = new ArrayList<SipRequest>();
//		//		allMessagesContent.addAll(sipCallA.getAllReceivedRequests());// .getAllMessagesContent();
//		allMessagesContent = sipCall.getAllReceivedRequests();
//
//		int sizeA = sipCall.getAllReceivedRequests().size();
//
//		assertTrue(sizeA>=0);
		Thread.sleep(500);
		deployer.undeploy(testArchive);
		receiver.tearDown();
	}

	
	//Annotate the field that represents the Map that contains the context parameters
	@ContextParamMap("aContextMap")
	private Map<String, String> params = new HashMap<String, String>();
	{
	params.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
	params.put("headerToAdd", "Authorization");
	}

	//Annotate the field that represents the Map that contains the context parameters
	@ContextParamMap("aContextMap2")
	private Map<String, String> params2 = new ContextParamTool().put("testName", "testValue").put("testName2", "testValue2").getMap();
	
	@Test 
	@ContextParamMap("aContextMap2")
	@ContextParam(name="cancel",value="true") 
	@ConcurrencyControlMode(org.mobicents.servlet.sip.annotation.ConcurrencyControlMode.SipApplicationSession)
	public void testNothing() throws InterruptedException{
		deployer.deploy("simple");
		Thread.sleep(1000);
		deployer.undeploy("simple");
	}
	
}
