/**
 * 
 */
package org.jboss.arquillian.container.mobicents.api;

import org.jboss.arquillian.container.spi.ConfigurationException;
import org.jboss.arquillian.container.spi.client.container.ContainerConfiguration;

/**
 * @author gvagenas@gmail.com 
 * 
 */

public abstract class MobicentsSipServletsConfigurationBase implements ContainerConfiguration
{
	private String bindAddress = "localhost";

	private int bindHttpPort = 8080;

	private String tomcatHome = null;

	private String appBase = "webapps";
	
	private String confBase = "conf";

	private String workDir = null;

	private String serverName = "mss-arquillian-container";

	private boolean unpackArchive = false;

	//		private HashMap<String, String> contextParam = new HashMap<String, String>();

	private String contextParam = null;
	private String paramSeparator = null;
	private String valueSeparator = null;

	private String concurrencyControl = null;

	public static final String MOBICENTS_DEFAULT_AR_CLASS_NAME = "org.mobicents.servlet.sip.router.DefaultApplicationRouterProvider";
	private String sipConnectors = ":5080";
	private String sipApplicationRouterProviderClassName = MOBICENTS_DEFAULT_AR_CLASS_NAME;

	/**
	 * @param sipApplicationRouterProviderClassName the sipApplicationRouterProviderClassName to set
	 */
	public void setSipApplicationRouterProviderClassName(
			String sipApplicationRouterProviderClassName) {
		this.sipApplicationRouterProviderClassName = sipApplicationRouterProviderClassName;
	}

	/**
	 * @return the sipApplicationRouterProviderClassName
	 */
	public String getSipApplicationRouterProviderClassName() {
		return sipApplicationRouterProviderClassName;
	}

	/**
	 * @param sipConnectors the sipConnectors to set
	 */
	public void setSipConnectors(String sipConnectors) {
		this.sipConnectors = sipConnectors;
	}

	/**
	 * @return the sipConnectors
	 */
	public String getSipConnectors() {
		return sipConnectors;
	}

//	@Override
	public void validate() throws ConfigurationException
	{
	}

	public String getBindAddress()
	{
		return bindAddress;
	}

	public void setBindAddress(String bindAddress)
	{
		this.bindAddress = bindAddress;
	}

	public int getBindHttpPort()
	{
		return bindHttpPort;
	}

	/**
	 * Set the HTTP bind port.
	 *
	 * @param httpBindPort
	 *            HTTP bind port
	 */
	public void setBindHttpPort(int bindHttpPort)
	{
		this.bindHttpPort = bindHttpPort;
	}

	public void setTomcatHome(String jbossHome)
	{
		this.tomcatHome = jbossHome;
	}

	public String getTomcatHome()
	{
		return tomcatHome;
	}

	/**
	 * @param appBase the directory where the deployed webapps are stored within the Tomcat installation
	 */
	public void setAppBase(String tomcatAppBase)
	{
		this.appBase = tomcatAppBase;
	}

	public String getAppBase()
	{
		return appBase;
	}

	/**
	 * @param confBase the directory where the conf is stored within the Tomcat installation
	 */
	public void setConfBase(String confBase) {
		this.confBase = confBase;
	}
	
	public String getConfBase() {
		return confBase;
	}
	
	/**
	 * @param workDir the directory where the compiled JSP files and session serialization data is stored
	 */
	public void setWorkDir(String tomcatWorkDir)
	{
		this.workDir = tomcatWorkDir;
	}

	public String getTomcatWorkDir()
	{
		return workDir;
	}

	/**
	 * @param serverName the serverName to set
	 */
	public void setServerName(String serverName)
	{
		this.serverName = serverName;
	}

	/**
	 * @return the serverName
	 */
	public String getServerName()
	{
		return serverName;
	}

	/**
	 * @return a switch indicating whether the WAR should be unpacked
	 */
	public boolean isUnpackArchive()
	{
		return unpackArchive;
	}

	/**
	 * Sets the WAR to be unpacked into the java.io.tmpdir when deployed.
	 * Unpacking is required if you are using Weld to provide CDI support
	 * in a servlet environment.
	 *
	 * @param a switch indicating whether the WAR should be unpacked
	 */
	public void setUnpackArchive(boolean unpack)
	{
		this.unpackArchive = unpack;
	}

	public String getContextParam() {
		return contextParam;
	}

	public void setContextParam(String contextParam) {
		this.contextParam = contextParam;
	}

	public String getParamSeparator() {
		return paramSeparator;
	}

	public void setParamSeparator(String paramSeparator) {
		this.paramSeparator = paramSeparator;
	}

	public String getValueSeparator() {
		return valueSeparator;
	}

	public void setValueSeparator(String valueSeparator) {
		this.valueSeparator = valueSeparator;
	}

	public String getConcurrencyControl() {
		return concurrencyControl;
	}

	public void setConcurrencyControl(String concurrencyControl) {
		this.concurrencyControl = concurrencyControl;
	}
}
