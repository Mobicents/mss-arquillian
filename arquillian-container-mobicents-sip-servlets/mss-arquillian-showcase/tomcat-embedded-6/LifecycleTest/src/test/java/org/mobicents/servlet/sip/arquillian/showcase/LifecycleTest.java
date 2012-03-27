/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.showcase;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.logging.Logger;

import javax.sip.message.Request;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mobicents.api.annotations.AfterDeploy;
import org.jboss.arquillian.container.mobicents.api.annotations.AfterUnDeploy;
import org.jboss.arquillian.container.mobicents.api.annotations.BeforeDeploy;
import org.jboss.arquillian.container.mobicents.api.annotations.BeforeUnDeploy;
import org.jboss.arquillian.container.mobicents.api.annotations.ConcurrencyControlMode;
import org.jboss.arquillian.container.mobicents.api.annotations.ContextParam;
import org.jboss.arquillian.container.mobicents.api.annotations.ContextParamMap;
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
 * The purpose of this test is to demonstrate the Lifecycle extension ability to execute methods annotated with the
 * appropriate annotation according to the lifecycle of the test archive.
 * 
 * The annotations are
 * <code>
 * @org.jboss.arquillian.container.mobicents.api.annotations.BeforeDeploy
 * @org.jboss.arquillian.container.mobicents.api.annotations.AfterUnDeploy
 * @org.jboss.arquillian.container.mobicents.api.annotations.BeforeUnDeploy
 * @org.jboss.arquillian.container.mobicents.api.annotations.AfterUnDeploy
 * </code>
 * 
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */
@RunWith(Arquillian.class)
public class LifecycleTest {

	private final static Logger logger = Logger.getLogger(LifecycleTest.class.getName());

	@ArquillianResource
	private Deployer deployer;

	//SipUnit Elements
	private SipStack receiver;
	private SipCall sipCall;
	private SipPhone sipPhone;

	private final int TIMEOUT = 10000;	

	private static SipStackTool sipStackTool;
	private String testArchive = "simple";


	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool("receiver");
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
	 * --managed: the framework WILL NOT manage the lifecycle of this archive, the developer is responsible to deploy/undeploy
	 * --testable: as-client mode (https://docs.jboss.org/author/display/ARQ/Test+run+modes) 
	 */
	@Deployment(name="simple", managed=false, testable=false)
	public static WebArchive createTestArchive()
	{
		WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "lifecyclesipservlet.war");
		webArchive.addClasses(LifecycleSipServlet.class);
		webArchive.addAsWebInfResource("in-container-sip.xml", "sip.xml");

		return webArchive;
	}
	
	/*
	 *Lifecycle methods 
	 */
	
	@BeforeDeploy
	public static void executeBeforeDeploy()
	{
		logger.info("This is executed BEFORE DEPLOYMENT");
	}

	@BeforeUnDeploy
	public static void executeBeforeUnDeploy()
	{
		logger.info("This is executed BEFORE UNDEPLOYMENT");
	}
	
	@AfterDeploy
	public static void executeAfterDeploy()
	{
		logger.info("This is executed AFTER DEPLOYMENT");
	}
	
	@AfterUnDeploy
	public static void executeAfterUndeploy()
	{
		logger.info("This is executed AFTER UNDEPLOYMENT");
	}
	
	/*
	 * Tests
	 */
	
	@Test
	public void testLifecycleExecution() throws InterruptedException
	{
		logger.info("About to deploy the test archive");
		deployer.deploy(testArchive);
		
		//Wait for a call
		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));
		
		//Respond to the incoming call
		assertTrue(sipCall.sendIncomingCallResponse(100, "TRYING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(180, "RINGING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(200, "OK", -1));
		
		Thread.sleep(100);
		//Disconnect
		assertTrue(sipCall.disconnect());
		
		Thread.sleep(1000);
	}
	
	@Test @ContextParam(name="myParam", value="myParamValue")
	public void testContextParam() throws InterruptedException
	{
		logger.info("About to deploy the test archive");
		deployer.deploy(testArchive);
		
		//Wait for a call
		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));
		
		Request request = sipCall.getLastReceivedRequest().getRequestEvent().getRequest();
		
		//Respond to the incoming call
		assertTrue(sipCall.sendIncomingCallResponse(100, "TRYING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(180, "RINGING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(200, "OK", -1));
		
		Thread.sleep(100);
		//Disconnect
		assertTrue(sipCall.disconnect());
		
		assertTrue(request.getHeader("Additional-Header").toString().contains("myParamValue")); 
	
		Thread.sleep(1000);
	}
	
	@ContextParamMap("testContextParamMap")
	private Map<String, String> contextMap1 = new ContextParamMapConstructTool()
	.put("myParam", "myParamValue")
	.put("myParam2", "myParamValue2").getMap();
	
	@Test @ContextParamMap("testContextParamMap")
	public void testContextParamMap() throws InterruptedException
	{
		logger.info("About to deploy the test archive");
		deployer.deploy(testArchive);
		
		//Wait for a call
		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));
		
		Request request = sipCall.getLastReceivedRequest().getRequestEvent().getRequest();
		
		//Respond to the incoming call
		assertTrue(sipCall.sendIncomingCallResponse(100, "TRYING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(180, "RINGING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(200, "OK", -1));
		
		Thread.sleep(100);
		//Disconnect
		assertTrue(sipCall.disconnect());
		
		assertTrue(request.getHeader("Additional-Header").toString().contains("myParamValue"));
		assertTrue(request.getHeader("Additional-Header2").toString().contains("myParamValue2"));
	
		Thread.sleep(1000);	
	}
	
	@Test @ConcurrencyControlMode(org.mobicents.servlet.sip.annotation.ConcurrencyControlMode.SipApplicationSession)
	public void testConcurrencyControlMode() throws InterruptedException
	{
		logger.info("About to deploy the test archive");
		deployer.deploy(testArchive);
		
		//Wait for a call
		assertTrue(sipCall.waitForIncomingCall(TIMEOUT));
		
		Request request = sipCall.getLastReceivedRequest().getRequestEvent().getRequest();
		
		//Respond to the incoming call
		assertTrue(sipCall.sendIncomingCallResponse(100, "TRYING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(180, "RINGING", -1));
		assertTrue(sipCall.sendIncomingCallResponse(200, "OK", -1));
		
		Thread.sleep(100);
		//Disconnect
		assertTrue(sipCall.disconnect());
		
		assertTrue(request.getHeader("ConcurrencyControlMode").toString().contains("SipApplicationSession"));
		
		Thread.sleep(1000);	
	}
}
