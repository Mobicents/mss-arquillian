/**
 * 
 */
package org.jboss.arquillian.container.mobicents.api;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.apache.catalina.Manager;
import org.apache.catalina.core.StandardContext;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.catalina.SipStandardService;

/**
 * Interface to supplement the DeployableContainer interface for a MobicentsSipServlets arquillian container. 
 * @author gvagenas@gmail.com 
 * 
 */
public interface MSSContainer
{
	/**
	 * Return the list of SipConnectors
	 * 
	 * @return List<SipConnector>
	 */
	List<SipConnector> getSipConnectors();

	/**
	 * Return the list of SipConnectors given the sipConnector string from configuration (arquillian.xml)
	 * 
	 * @param sipConnectorString
	 * @return List<SipConnector>
	 */
	List<SipConnector> getSipConnectors(String sipConnectorString);

	/**
	 * Add a SipConnector to the SipStandardService
	 * 
	 * @param sipConnector
	 * @throws LifecycleException
	 */
	void addSipConnector(SipConnector sipConnector) throws LifecycleException;

	/**
	 * Create a SipConnector
	 * 
	 * @param ipAddress
	 * @param port
	 * @param transport
	 * @return SipConnector
	 */
	SipConnector createSipConnector(String ipAddress, int port, String transport);

	/**
	 * Remove a SipConnector from the SipStandardService
	 * 
	 * @param ipAddress
	 * @param port
	 * @param transport
	 * @throws LifecycleException
	 */
	void removeSipConnector(String ipAddress, int port, String transport) throws LifecycleException;

	/**
	 * Start Mobicents embedded container
	 * 
	 * @throws UnknownHostException
	 * @throws org.apache.catalina.LifecycleException
	 * @throws LifecycleException
	 */
	void startTomcatEmbedded() throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException;

	/**
	 * Start Mobicents embedded container passing some initial properties.
	 * 
	 * @param sipStackProperties
	 * @throws UnknownHostException
	 * @throws org.apache.catalina.LifecycleException
	 * @throws LifecycleException
	 */
	void startTomcatEmbedded(Properties sipStackProperties) throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException;

	/**
	 * Stop Mobicents embedded container  
	 * 
	 * @throws org.jboss.arquillian.container.spi.client.container.LifecycleException
	 * @throws org.apache.catalina.LifecycleException
	 */
	void stopTomcatEmbedded() throws org.jboss.arquillian.container.spi.client.container.LifecycleException, org.apache.catalina.LifecycleException;

	/**
	 * Delete unpacked WAR archive
	 * 
	 * @param standardContext
	 */
	void deleteUnpackedWAR(StandardContext standardContext);

	/**
	 * Returns whether or not the Mobicents embedded container was started
	 * 
	 * @return boolean
	 */
	boolean isStarted();

	/**
	 * Return the archive object
	 * 
	 * @return Archive<?>
	 */
	Archive<?> getArchive();

	/**
	 * Return the SipService
	 * 
	 * @return SipService
	 */
	SipStandardService getSipStandardService();

	/**
	 * @return SipStandardManager
	 */
	Manager getSipStandardManager();

	/**
	 * @param sipStandardManager
	 */
	void setSipStandardManager(Manager sipStandardManager);
	
}
