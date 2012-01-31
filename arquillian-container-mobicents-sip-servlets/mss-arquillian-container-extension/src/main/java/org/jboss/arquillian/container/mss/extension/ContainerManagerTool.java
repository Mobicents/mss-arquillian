/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Properties;

import org.apache.catalina.Manager;
import org.jboss.arquillian.container.mobicents.api.ContainerWrapper;
import org.jboss.arquillian.container.mobicents.api.MSSContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.shrinkwrap.api.Archive;
import org.mobicents.servlet.sip.SipConnector;
import org.mobicents.servlet.sip.catalina.SipStandardService;
import org.mobicents.servlet.sip.core.SipService;

/**
 * A wrapper to the Mobicents DeployableContainer providing access to the most common functionality for use in the test clients
 * 
 * @author gvagenas@gmail.com 
 * 
 */
public class ContainerManagerTool implements ContainerWrapper 
{
	private MSSContainer mssContainer;
	private DeployableContainer<?> deployableContainer;
	
	public ContainerManagerTool(DeployableContainer<?> deployableContainer) {
		this.deployableContainer = deployableContainer;
		if (deployableContainer instanceof MSSContainer)
			mssContainer = (MSSContainer) deployableContainer;
	}
	
	/*
	 * SipConnector related methods
	 */
	@Override
	public void addSipConnector(String ipAddress, int port, String transport) throws LifecycleException
	{
		SipConnector sipConnector = mssContainer.createSipConnector(ipAddress, port, transport);
		mssContainer.addSipConnector(sipConnector);
	}

	@Override
	public void removeSipConnector(String ipAddress, int port, String transport) throws LifecycleException{
		mssContainer.removeSipConnector(ipAddress, port, transport);
	}
	@Override
	public void removeSipConnector(SipConnector sipConnector) throws LifecycleException{
		String ipAddress = sipConnector.getIpAddress();
		int port = sipConnector.getPort();
		String transport = sipConnector.getTransport();
		
		removeSipConnector(ipAddress, port, transport);
	}
	
	@Override
	public List<SipConnector> getSipConnectors(){
		return mssContainer.getSipConnectors();
	}
	@Override
	public List<SipConnector> getSipConnectors(String sipConnectorString)
	{
		return mssContainer.getSipConnectors();
	}
	
	//Container related methods
	@Override
	public void startContainer() throws LifecycleException 
	{
		if (!mssContainer.isStarted()){
			deployableContainer.start();
		}
	}
	@Override
	public void startContainer(Properties sipStackProperties) throws UnknownHostException, org.apache.catalina.LifecycleException, LifecycleException
	{
		if (!mssContainer.isStarted()){
			mssContainer.startTomcatEmbedded(sipStackProperties);
		}
	}
	@Override
	public void stopContainer() throws LifecycleException
	{
		if(mssContainer.isStarted()){
			deployableContainer.stop();
		}
	}
	@Override
	public void restartContainer() throws LifecycleException
	{
		stopContainer();
		startContainer();
	}
	@Override
	public void restartContainer(Properties sipStackProperties) throws LifecycleException, UnknownHostException, org.apache.catalina.LifecycleException
	{
		stopContainer();
		startContainer(sipStackProperties);
	}
	
	@Override
	public Properties getSipStackProperties() {
		SipStandardService sipService = mssContainer.getSipStandardService();
		return sipService.getSipStackProperties();
	}
	
	@Override
	public void setSipStackProperties(Properties sipStackProperties){
		SipStandardService sipService = mssContainer.getSipStandardService();
		sipService.setSipStackProperties(sipStackProperties);
	}
	
	@Override
	public Manager getSipStandardManager()
	{
		return mssContainer.getSipStandardManager();
	}
	
	//Context related methods
	@Override
	public void reloadContext() throws DeploymentException
	{
		Archive<?> archive = mssContainer.getArchive();
		deployableContainer.undeploy(archive);
		deployableContainer.deploy(archive);
	}

}
