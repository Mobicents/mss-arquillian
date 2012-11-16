package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_6;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;

import javax.sip.InvalidArgumentException;
import javax.sip.SipException;
import javax.sip.message.Response;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
@RunWith(Arquillian.class)
public class MyTest1 {

	private Logger logger = Logger.getLogger(MyTest1.class);

	//SipUnit Elements
	private String receiverURI = "sip:receiver@sip-servlets.com";

	private SipStack sender;
	private SipCall sipCallSender;
	private SipPhone sipPhoneSender;
	private String senderPort = "5080";
	private String senderURI = "sip:sender@sip-servlets.com";

    @ArquillianResource
    URL deploymentUrl;

	private int proxyPort = 5070;

	private static final int TIMEOUT = 10000;	

	private static SipStackTool senderSipStackTool;

	@BeforeClass
	public static void beforeClass(){
		senderSipStackTool = new SipStackTool("sender");
	}

	@Before
	public void setUp() throws Exception
	{
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
	}


	@Deployment(testable=false)
	public static WebArchive createTestArchive(){
		WebArchive archive = ShrinkWrap.create(WebArchive.class, "sipServlets.war");
		archive.addClasses(MySipServlet.class);
		archive.addClass(EchoServlet.class);
		archive.addAsWebInfResource("web.xml");
		archive.addAsWebInfResource("sip.xml");
		
		//Issue 10: http://code.google.com/p/commtesting/issues/detail?id=10
		archive.addAsWebResource("testWebResource.txt", "myWebResources/testWebResource.txt");
		
		return archive;
	}

	@Test
	public void testShootme() throws MalformedURLException, IOException, Exception {

		sipCallSender.initiateOutgoingCall(receiverURI, null);

		assertTrue(sipCallSender.waitForAnswer(TIMEOUT));
		assertEquals(Response.OK, sipCallSender.getLastReceivedResponse().getStatusCode());

		assertTrue(sipCallSender.sendInviteOkAck());
		assertTrue(sipCallSender.disconnect());

		String requestUrl = deploymentUrl + EchoServlet.URL_PATTERN.substring(1) + "?" + EchoServlet.MESSAGE_PARAM + "=hello";
		String body = StreamReaderUtil.readAllAndClose(new URL(requestUrl).openStream());

		Assert.assertEquals("Verify that the servlet was deployed and returns expected result", "hello", body);

	}
	
	//Issue 10: http://code.google.com/p/commtesting/issues/detail?id=10
	@Test
	public void testWebResources() throws MalformedURLException, IOException, Exception{
		String requestUrl = deploymentUrl + "myWebResources/testWebResource.txt";
		String body = StreamReaderUtil.readAllAndClose(new URL(requestUrl).openStream());

		logger.info("Message from the static file: "+body);
		
		Assert.assertEquals("Verify that the servlet was deployed and returns expected result", "It Works!", body);
	}

}
