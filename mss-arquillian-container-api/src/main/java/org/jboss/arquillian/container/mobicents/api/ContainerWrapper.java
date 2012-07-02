/**
 * 
 */
package org.jboss.arquillian.container.mobicents.api;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.apache.catalina.Manager;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.catalina.SipStandardService;

/**
 * Container wrapper interface to provide functionallity to the test client
 * 
 * @author gvagenas@gmail.com 
 * 
 */
public interface ContainerWrapper {

	//SipConnector related methods
	public void addSipConnector(String ipAddress, int port, String transport) throws LifecycleException;

	public void removeSipConnector(String ipAddress, int port, String transport) throws LifecycleException;
	
	public void removeSipConnector(SipConnector sipConnector) throws LifecycleException;

	public List<SipConnector> getSipConnectors();

	public List<SipConnector> getSipConnectors(String sipConnectorString);

	//Container related methods
	public void startContainer() throws LifecycleException;

	public void startContainer(Properties sipStackProperties)
			throws UnknownHostException,
			org.apache.catalina.LifecycleException, LifecycleException;

	public void stopContainer() throws LifecycleException;

	public void restartContainer() throws LifecycleException;

	public void restartContainer(Properties sipStackProperties)
			throws LifecycleException, UnknownHostException,
			org.apache.catalina.LifecycleException;

	public Properties getSipStackProperties();
	
	public void setSipStackProperties(Properties sipStackProperties);
	
	public Manager getSipStandardManager();
	
	public SipStandardService getSipStandardService();
	
	//Context related methods
	public void reloadContext() throws DeploymentException;

}