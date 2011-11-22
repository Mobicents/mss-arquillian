/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.util.Properties;
import java.util.logging.Logger;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;


/**
 * @author gvagenas@gmail.com
 * 
 */
public class SipStackTool {

	public SipStackTool() {
	}
	
	public SipStackTool(int myPort, int proxyPort) throws Exception {
		initializeSipStack(myPort, proxyPort);
	}

	private Boolean initialized = false;

	private SipStack sipStack;
	private SipPhone sipPhone;
	private SipCall sipCall;

	private final Logger logger = Logger.getLogger(SipStackTool.class.getName());
	private Properties properties = new Properties();

	
	private SipStack makeStack(String transport, int myPort, Properties myProperties) throws Exception{
		return new SipStack(transport, myPort, myProperties);
	}

	private SipStack makeStack(String transport, int myPort, int proxyPort, String host) throws Exception {

		if (host == null) host = "127.0.0.1";
		if (transport == null) transport = SipStack.PROTOCOL_UDP;

		properties.setProperty("javax.sip.IP_ADDRESS", host);
		properties.setProperty("javax.sip.STACK_NAME", "testAgent_"+transport+myPort);
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"testAgent_debug"+myPort+".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"testAgent_log"+myPort+".txt");
		properties
		.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
		properties.setProperty(
				"gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");

		properties.setProperty("sipunit.trace", "true");
		properties.setProperty("sipunit.test.protocol", transport);
		properties.setProperty(
				"gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER",
				"true");

		String peerHostPort1 = host+proxyPort;
		properties.setProperty("javax.sip.OUTBOUND_PROXY", peerHostPort1 + "/"
				+ transport);
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + transport + "_"
				+ myPort);
		properties.setProperty("sipunit.BINDADDR", host);
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
				"logs/simplesipservlettest_debug_port" + myPort + ".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG",
				"logs/simplesipservlettest_log_port" + myPort + ".xml");
		properties.setProperty("sipunit.trace", "true");
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL",
				"32");

		properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "off");

		System.setProperty("org.mobicents.testsuite.testhostaddr", host);

		return new SipStack(transport, myPort, properties);
	}

	public void initializeSipStack(int myPort, int proxyPort) throws Exception {
		initializeSipStack(myPort, proxyPort, null);
	}

	public void initializeSipStack(int myPort, int proxyPort, String host) throws Exception {
		initializeSipStack(null, myPort, proxyPort, host);
	}

	public void initializeSipStack(String transport, int myPort, int proxyPort, String host) throws Exception {
		
		//Clear objects that might left from previous tests
		if (sipStack!=null){
			tearDown();
		}

		try
		{
			sipStack = makeStack(SipStack.PROTOCOL_UDP, myPort, proxyPort, host);
			logger.info("SipStack created!");

			SipStack.setTraceEnabled(properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));
		}
		catch (Exception ex)
		{
			logger.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}

		initialized = true;
	}	

	public void initializeSipStack(String transport, int myPort, Properties myProperties) throws Exception {
		if (sipStack!=null){
			tearDown();
		}

		try
		{
			sipStack = makeStack(SipStack.PROTOCOL_UDP, myPort, myProperties);
			logger.info("SipStack created!");

			SipStack.setTraceEnabled(properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| properties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));
		}
		catch (Exception ex)
		{
			logger.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}
	}

	public SipPhone createSipPhone(String proxyHost, String proxyProtocol, int proxyPort, String myURI) throws Exception{ 

		try
		{
			sipPhone = sipStack.createSipPhone(proxyHost, proxyProtocol, proxyPort, myURI);
			logger.info("SipPhone created with address "+sipPhone.getAddress().toString());			
		}
		catch (Exception ex)
		{
			logger.info("Exception creating SipPhone: " + ex.getClass().getName()
					+ ": " + ex.getMessage());
			throw ex;
		}

		return sipPhone;
	}

	public void prepareSipCall() throws Exception{
		if (sipPhone == null){
			throw new Exception("SipPhone is not initialized");
		}
		
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
	}
	
	public SipStack getSipStack() throws Exception{
		if (!initialized){
			throw new Exception("SipStack is null");
		}
		return sipStack;
	}

	public SipPhone getSipPhone() throws Exception{
		if (sipPhone==null){
			throw new Exception("SipPhone is null");
		}
		return sipPhone;
	}
	
	public SipCall getSipCall() throws Exception{
		if (sipCall==null){
			throw new Exception("SipCall is null");
		}
		return sipCall;
	}
	
	public void tearDown(){
		if (sipCall!=null) sipCall.disposeNoBye();
		if (sipPhone!=null) sipPhone.dispose();
		if (sipStack!=null) sipStack.dispose();
	}

}

