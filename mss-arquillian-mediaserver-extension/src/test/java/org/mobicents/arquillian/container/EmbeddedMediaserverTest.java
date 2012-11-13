package org.mobicents.arquillian.container;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mediaserver.api.EndpointType;
import org.mobicents.arquillian.mss.mediaserver.extension.EmbeddedMediaserverImpl;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class EmbeddedMediaserverTest {

	private Logger logger = Logger.getLogger(EmbeddedMediaserverTest.class);
	
	private MgcpCallAgent callAgent;
	private EmbeddedMediaserver embeddedMediaserver;
	
	public EmbeddedMediaserverTest() {
	}

	@Before
	public void setUp() throws Exception {
//		embeddedMediaserver = new EmbeddedMediaserverImpl();
//		embeddedMediaserver.startServer();
//		embeddedMediaserver.installEndpoint(EndpointType.IVR, 2);
//		embeddedMediaserver.installEndpoint(EndpointType.CONFERENCE, 2);
//		embeddedMediaserver.installEndpoint(EndpointType.PACKETRELAY, 2);
		
		callAgent = new MgcpCallAgent();
	}
	
	@Test
	public void testSendCRCX(){
		callAgent.sendCRCX();
	}
	
}
