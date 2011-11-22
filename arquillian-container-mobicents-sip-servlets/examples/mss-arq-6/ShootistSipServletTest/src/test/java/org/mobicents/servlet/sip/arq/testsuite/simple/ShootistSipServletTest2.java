/**
 * 
 */
package org.mobicents.servlet.sip.arq.testsuite.simple;

import java.util.logging.Logger;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipStack;
import org.cafesip.sipunit.SipTestCase;
import org.jboss.arquillian.container.mss.extension.SipStackToolStatic;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
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
public class ShootistSipServletTest2 extends SipTestCase 
{	
	@ArquillianResource
    private Deployer deployer;

	private SipCall sipCall;

	private final int timeout = 10000;	

	private final Logger logger = Logger.getLogger(ShootistSipServletTest2.class.getName());

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
	
	
	@org.jboss.arquillian.container.mss.extension.lifecycle.api.BeforeDeploy
	public static void runBeforeDeploy() throws Exception{
		//Initialize SipStack and prepare SipCall before deployment
		SipStackToolStatic.getInstance().initializeSipStack(5080, 5070);
		SipStackToolStatic.getInstance().createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:LittleGuy@there.com");
		SipStackToolStatic.getInstance().prepareSipCall();
	}


//	@org.jboss.arquillian.container.mss.extension.api.AfterUnDeploy
//	public static void runAfterUnDeploy() throws Exception{
//		//Initialize SipStack and prepare SipCall before deployment
//		SipStackToolStatic.getInstance().initializeSipStack(5080, 5070);
//		SipStackToolStatic.getInstance().prepareSipCall();
//	}

	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||
	// 											Tests                                       ||
	// -------------------------------------------------------------------------------------||
	// -------------------------------------------------------------------------------------||


	@Test  
	public void testShootist() throws Exception {
		
		//Deploy the first test archive
		deployer.deploy("simple");
		
		sipCall = SipStackToolStatic.getInstance().getSipCall();

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
		Thread.sleep(timeout);
	}

	
	@Test
	public void testShootistCancel() throws Exception {

		//Deploy the second test archive
		deployer.deploy("cancel");
		
		sipCall = SipStackToolStatic.getInstance().getSipCall();
		
		sipCall.waitForIncomingCall(500);

		assertTrue(sipCall.sendIncomingCallResponse(Response.TRYING,"Trying", timeout));
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.RINGING,"RINGING",timeout));	
		Thread.sleep(100);

		assertTrue(sipCall.sendIncomingCallResponse(Response.OK, "OK", timeout));
		Thread.sleep(100);

		assertTrue(sipCall.listenForCancel());
		
		Thread.sleep(500);
		
		sipCall.waitForCancel(timeout);
		
		sipCall.respondToCancel(null, 200, "200 OK", timeout);
		
		Thread.sleep(500);
		
		sipCall.listenForDisconnect();
		assertTrue(sipCall.waitForDisconnect(timeout));
		sipCall.respondToDisconnect();
	
		Thread.sleep(500);
		
//		ArrayList<SipRequest> allMessagesContent = new ArrayList<SipRequest>();
//		//		allMessagesContent.addAll(sipCallA.getAllReceivedRequests());// .getAllMessagesContent();
//		allMessagesContent = sipCall.getAllReceivedRequests();
//
//		int sizeA = sipCall.getAllReceivedRequests().size();
//
//		assertTrue(sizeA>=0);
		Thread.sleep(500);
		deployer.undeploy("cancel");
	}

}
