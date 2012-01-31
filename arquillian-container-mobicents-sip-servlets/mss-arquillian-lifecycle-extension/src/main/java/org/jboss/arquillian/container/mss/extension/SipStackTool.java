/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.util.Map;
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
	
	private Boolean initialized = false;

	private SipStack sipStack;
	private SipPhone sipPhone;
	private SipCall sipCall;

	private final Logger logger = Logger.getLogger(SipStackTool.class.getName());
//	private Properties properties = new Properties();
	
	public SipStackTool() {
	}
	
	// Return SipStack
	private Properties makeProperties(String myTransport,String myHost, String myPort, String outboundProxy, Boolean myAutoDialog, 
			String threadPoolSize, String reentrantListener, Map<String, String> additionalProperties) throws Exception {

		Properties properties = new Properties();
		
		if (myHost == null) myHost = "127.0.0.1";
		if (myTransport == null) myTransport = SipStack.PROTOCOL_UDP;

		properties.setProperty("javax.sip.IP_ADDRESS", myHost);
		properties.setProperty("javax.sip.STACK_NAME", "UAC_" + myTransport + "_"+ myPort);
		properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", (myAutoDialog ? "on" : "off"));
		
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/testAgent_debug"+myPort+".txt");
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/testAgent_log"+myPort+".xml");
		properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
		properties.setProperty("gov.nist.javax.sip.CACHE_SERVER_CONNECTIONS", "false");
		properties.setProperty("gov.nist.javax.sip.PASS_INVITE_NON_2XX_ACK_TO_LISTENER", "true");
		
		properties.setProperty("sipunit.trace", "true");
		properties.setProperty("sipunit.test.protocol", myTransport);
		properties.setProperty("sipunit.test.port", myPort);
		properties.setProperty("sipunit.BINDADDR", myHost);


		if (outboundProxy != null){
			properties.setProperty("javax.sip.OUTBOUND_PROXY", outboundProxy + "/"
					+ myTransport);
		}
		
		properties.setProperty("gov.nist.javax.sip.THREAD_POOL_SIZE", threadPoolSize == null ? "1" : threadPoolSize);
		properties.setProperty("gov.nist.javax.sip.REENTRANT_LISTENER", reentrantListener == null ? "false" : reentrantListener);		

		if(additionalProperties != null) {
			properties.putAll(additionalProperties);
		}
		
		System.setProperty("org.mobicents.testsuite.testhostaddr", myHost);

		return properties;
	}
	
	// Initialize SipStack for no proxy use	
	public SipStack initializeSipStack(String myTransport, String myHost, String myPort) throws Exception{
		return initializeSipStack(myTransport, myHost, myPort, null, true, null, null, null);
	}
	
	// Initialize SipStack using outbound proxy
	public SipStack initializeSipStack(String myPort, String outboundProxy) throws Exception {
		return initializeSipStack(null, null, myPort, outboundProxy, true, null, null, null);
	}

	// Initialize SipStack using outbound proxy
	public SipStack initializeSipStack(String myTransport, String myHost, String myPort, String outboundProxy) throws Exception {
		return initializeSipStack(myTransport, myHost, myPort, outboundProxy, true, null, null, null);
	}
	
	public SipStack initializeSipStack(String myTransport, String myHost, String myPort, String outboundProxy, Boolean myAutoDialog,
			String threadPoolSize, String reentrantListener, Map<String, String> additionalProperties) throws Exception {
		
		//Clear objects that might left from previous tests
		if (sipStack!=null){
//			tearDown();
//			sipStack.dispose();
			sipStack = null;
		}
		
		try
		{
			Properties myProperties = makeProperties(myTransport, myHost, myPort, outboundProxy, myAutoDialog, threadPoolSize, 
					reentrantListener, additionalProperties);
			sipStack = new SipStack(myTransport, Integer.valueOf(myPort), myProperties);
			logger.info("SipStack created!");

			SipStack.setTraceEnabled(myProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| myProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));
		}
		catch (Exception ex)
		{
			logger.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}

		initialized = true;
		return sipStack;
	}	

	// Initialize SipStack using provided properties
	public SipStack initializeSipStack(String transport, String myPort, Properties myProperties) throws Exception {
		if (sipStack!=null){
//			tearDown();
			sipStack = null;
		}

		try
		{
			sipStack = new SipStack(SipStack.PROTOCOL_UDP, Integer.valueOf(myPort), myProperties);
			logger.info("SipStack created!");

			SipStack.setTraceEnabled(myProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("true")
					|| myProperties.getProperty("sipunit.trace")
					.equalsIgnoreCase("on"));
		}
		catch (Exception ex)
		{
			logger.info("Exception: " + ex.getClass().getName() + ": "
					+ ex.getMessage());
			throw ex;
		}
		
		initialized = true;
		return sipStack;
	}
	
//	public SipPhone createSipPhone(String myURI) {
//		return createSipPhone(null);
//	}
//
//	// Create a SipPhone for proxy use
//	public SipPhone createSipPhone(String proxyHost, String proxyProtocol, int proxyPort, String myURI) throws Exception{ 
//
//		try
//		{
//			sipPhone = sipStack.createSipPhone(proxyHost, proxyProtocol, proxyPort, myURI);
//			logger.info("SipPhone created with address "+sipPhone.getAddress().toString());			
//		}
//		catch (Exception ex)
//		{
//			logger.info("Exception creating SipPhone: " + ex.getClass().getName()
//					+ ": " + ex.getMessage());
//			throw ex;
//		}
//
//		return sipPhone;
//	}

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

