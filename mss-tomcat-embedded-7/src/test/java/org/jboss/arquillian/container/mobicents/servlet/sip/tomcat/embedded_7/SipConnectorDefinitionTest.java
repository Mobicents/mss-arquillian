package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_7;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.mobicents.servlet.sip.SipConnector;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SipConnectorDefinitionTest {
    private Logger logger = Logger.getLogger(SipConnectorDefinitionTest.class);

    @Test
    public void testSipConnectorDefinitionWithDefaults () {
        String sipConnectorStr = ":5070";

        MobicentsSipServletsContainer mobicentsSipServletsContainer = new MobicentsSipServletsContainer();
        List<SipConnector> sipConnectors = mobicentsSipServletsContainer.getSipConnectors(sipConnectorStr);

        assertEquals(1, sipConnectors.size());
        assertEquals("127.0.0.1", sipConnectors.get(0).getIpAddress());
        assertEquals(5070, sipConnectors.get(0).getPort());
        assertEquals("UDP", sipConnectors.get(0).getTransport());
    }

    @Test
    public void testSipConnectorDefinition () {
        String sipConnectorStr = "192.168.1.111:5070/TCP";

        MobicentsSipServletsContainer mobicentsSipServletsContainer = new MobicentsSipServletsContainer();
        List<SipConnector> sipConnectors = mobicentsSipServletsContainer.getSipConnectors(sipConnectorStr);

        assertEquals(1, sipConnectors.size());
        assertEquals("192.168.1.111", sipConnectors.get(0).getIpAddress());
        assertEquals(5070, sipConnectors.get(0).getPort());
        assertEquals("TCP", sipConnectors.get(0).getTransport());
    }

    @Test
    public void testTwoSipConnectorDefinition () {
        String sipConnectorStr = "192.168.1.111:5070/TCP,:5070";

        MobicentsSipServletsContainer mobicentsSipServletsContainer = new MobicentsSipServletsContainer();
        List<SipConnector> sipConnectors = mobicentsSipServletsContainer.getSipConnectors(sipConnectorStr);

        assertEquals(2, sipConnectors.size());
        assertEquals("192.168.1.111", sipConnectors.get(0).getIpAddress());
        assertEquals(5070, sipConnectors.get(0).getPort());
        assertEquals("TCP", sipConnectors.get(0).getTransport());

        assertEquals("127.0.0.1", sipConnectors.get(1).getIpAddress());
        assertEquals(5070, sipConnectors.get(1).getPort());
        assertEquals("UDP", sipConnectors.get(1).getTransport());
    }

    @Test
    public void testSipConnectorDefinition_withDefaults_withLB () {
        String sipConnectorStr = ":5070-127.0.0.1::2000";

        MobicentsSipServletsContainer mobicentsSipServletsContainer = new MobicentsSipServletsContainer();
        List<SipConnector> sipConnectors = mobicentsSipServletsContainer.getSipConnectors(sipConnectorStr);

        assertEquals(1, sipConnectors.size());
        assertEquals("127.0.0.1", sipConnectors.get(0).getIpAddress());
        assertEquals(5070, sipConnectors.get(0).getPort());
        assertEquals("UDP", sipConnectors.get(0).getTransport());

        assertEquals("127.0.0.1", sipConnectors.get(0).getLoadBalancerAddress());
        assertEquals(5060, sipConnectors.get(0).getLoadBalancerSipPort());
        assertEquals(2000, sipConnectors.get(0).getLoadBalancerRmiPort());
    }

    @Test
    public void testTwoSipConnectorDefinition_withDefaults_withLB () {
        String sipConnectorStr = ":5070-127.0.0.1::2000,:5070/TCP-127.0.0.1::2000";

        MobicentsSipServletsContainer mobicentsSipServletsContainer = new MobicentsSipServletsContainer();
        List<SipConnector> sipConnectors = mobicentsSipServletsContainer.getSipConnectors(sipConnectorStr);

        assertEquals(2, sipConnectors.size());
        assertEquals("127.0.0.1", sipConnectors.get(0).getIpAddress());
        assertEquals(5070, sipConnectors.get(0).getPort());
        assertEquals("UDP", sipConnectors.get(0).getTransport());

        assertEquals("127.0.0.1", sipConnectors.get(0).getLoadBalancerAddress());
        assertEquals(5060, sipConnectors.get(0).getLoadBalancerSipPort());
        assertEquals(2000, sipConnectors.get(0).getLoadBalancerRmiPort());



        assertEquals("127.0.0.1", sipConnectors.get(1).getIpAddress());
        assertEquals(5070, sipConnectors.get(1).getPort());
        assertEquals("TCP", sipConnectors.get(1).getTransport());

        assertEquals("127.0.0.1", sipConnectors.get(1).getLoadBalancerAddress());
        assertEquals(5060, sipConnectors.get(1).getLoadBalancerSipPort());
        assertEquals(2000, sipConnectors.get(1).getLoadBalancerRmiPort());
    }

}
