/**
 * 
 */
package org.jboss.arquillian.container;

import java.net.InetAddress;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Realm;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardService;

/**
 * @author gvagenas@gmail.com 
 * 
 */
public interface SipServletsEmbeddedContainer {

	/**
	 * Return true if naming is enabled.
	 */
	public abstract boolean isUseNaming();

	/**
	 * Enables or disables naming support.
	 *
	 * @param useNaming The new use naming value
	 */
	public abstract void setUseNaming(boolean useNaming);

	/**
	 * Return true if redirection of standard streams is enabled.
	 */
	public abstract boolean isRedirectStreams();

	/**
	 * Enables or disables naming support.
	 *
	 * @param useNaming The new use naming value
	 */
	public abstract void setRedirectStreams(boolean redirectStreams);

	/**
	 * Return the default Realm for our Containers.
	 */
	public abstract Realm getRealm();

	/**
	 * Set the default Realm for our Containers.
	 *
	 * @param realm The new default realm
	 */
	public abstract void setRealm(Realm realm);

	public abstract void setAwait(boolean b);

	public abstract boolean isAwait();

	public abstract void setCatalinaHome(String s);

	public abstract void setCatalinaBase(String s);

	public abstract String getCatalinaHome();

	public abstract String getCatalinaBase();

	/**
	 * Add a new Connector to the set of defined Connectors.  The newly
	 * added Connector will be associated with the most recently added Engine.
	 *
	 * @param connector The connector to be added
	 *
	 * @exception IllegalStateException if no engines have been added yet
	 */
	public abstract void addConnector(Connector connector);
	
	public Connector addSipConnector(String connectorName, String ipAddress, int port, String transport) throws Exception;

	/**
	 * Add a new Engine to the set of defined Engines.
	 *
	 * @param engine The engine to be added
	 */
	public abstract void addEngine(Engine engine);

	/**
	 * Create, configure, and return a new TCP/IP socket connector
	 * based on the specified properties.
	 *
	 * @param address InetAddress to bind to, or <code>null</code> if the
	 * connector is supposed to bind to all addresses on this server
	 * @param port Port number to listen to
	 * @param secure true if the generated connector is supposed to be
	 * SSL-enabled, and false otherwise
	 */
	public abstract Connector createConnector(InetAddress address, int port,
			boolean secure);

	public abstract Connector createConnector(String address, int port,
			boolean secure);

	public abstract Connector createConnector(InetAddress address, int port,
			String protocol);

	public abstract Connector createConnector(String address, int port,
			String protocol);

	/**
	 * Create, configure, and return a Context that will process all
	 * HTTP requests received from one of the associated Connectors,
	 * and directed to the specified context path on the virtual host
	 * to which this Context is connected.
	 * <p>
	 * After you have customized the properties, listeners, and Valves
	 * for this Context, you must attach it to the corresponding Host
	 * by calling:
	 * <pre>
	 *   host.addChild(context);
	 * </pre>
	 * which will also cause the Context to be started if the Host has
	 * already been started.
	 *
	 * @param path Context path of this application ("" for the default
	 *  application for this host, must start with a slash otherwise)
	 * @param docBase Absolute pathname to the document base directory
	 *  for this web application
	 *
	 * @exception IllegalArgumentException if an invalid parameter
	 *  is specified
	 */
	public abstract Context createContext(String path, String docBase);
	
	/**
	 * Create, configure, and return an Engine that will process all
	 * HTTP requests received from one of the associated Connectors,
	 * based on the specified properties.
	 */
	public abstract Engine createEngine();

	/**
	 * Create, configure, and return a Host that will process all
	 * HTTP requests received from one of the associated Connectors,
	 * and directed to the specified virtual host.
	 * <p>
	 * After you have customized the properties, listeners, and Valves
	 * for this Host, you must attach it to the corresponding Engine
	 * by calling:
	 * <pre>
	 *   engine.addChild(host);
	 * </pre>
	 * which will also cause the Host to be started if the Engine has
	 * already been started.  If this is the default (or only) Host you
	 * will be defining, you may also tell the Engine to pass all requests
	 * not assigned to another virtual host to this one:
	 * <pre>
	 *   engine.setDefaultHost(host.getName());
	 * </pre>
	 *
	 * @param name Canonical name of this virtual host
	 * @param appBase Absolute pathname to the application base directory
	 *  for this virtual host
	 *
	 * @exception IllegalArgumentException if an invalid parameter
	 *  is specified
	 */
	public abstract Host createHost(String name, String appBase);

	/**
	 * Create and return a class loader manager that can be customized, and
	 * then attached to a Context, before it is started.
	 *
	 * @param parent ClassLoader that will be the parent of the one
	 *  created by this Loader
	 */
	public abstract Loader createLoader(ClassLoader parent);

	/**
	 * Return descriptive information about this Server implementation and
	 * the corresponding version number, in the format
	 * <code>&lt;description&gt;/&lt;version&gt;</code>.
	 */
	public abstract String getInfo();

	/**
	 * Remove the specified Context from the set of defined Contexts for its
	 * associated Host.  If this is the last Context for this Host, the Host
	 * will also be removed.
	 *
	 * @param context The Context to be removed
	 */
	public abstract void removeContext(Context context);

	/**
	 * Remove the specified Engine from the set of defined Engines, along with
	 * all of its related Hosts and Contexts.  All associated Connectors are
	 * also removed.
	 *
	 * @param engine The Engine to be removed
	 */
	public abstract void removeEngine(Engine engine);

	/**
	 * Remove the specified Host, along with all of its related Contexts,
	 * from the set of defined Hosts for its associated Engine.  If this is
	 * the last Host for this Engine, the Engine will also be removed.
	 *
	 * @param host The Host to be removed
	 */
	public abstract void removeHost(Host host);

	/*
	 * Maps the specified login method to the specified authenticator, allowing
	 * the mappings in org/apache/catalina/startup/Authenticators.properties
	 * to be overridden.
	 *
	 * @param authenticator Authenticator to handle authentication for the
	 * specified login method
	 * @param loginMethod Login method that maps to the specified authenticator
	 *
	 * @throws IllegalArgumentException if the specified authenticator does not
	 * implement the org.apache.catalina.Valve interface
	 */
	public abstract void addAuthenticator(Authenticator authenticator,
			String loginMethod);

	/**
	 * Add a lifecycle event listener to this component.
	 *
	 * @param listener The listener to add
	 */
	public abstract void addLifecycleListener(LifecycleListener listener);

	/**
	 * Get the lifecycle listeners associated with this lifecycle. If this 
	 * Lifecycle has no listeners registered, a zero-length array is returned.
	 */
	public abstract LifecycleListener[] findLifecycleListeners();

	/**
	 * Remove a lifecycle event listener from this component.
	 *
	 * @param listener The listener to remove
	 */
	public abstract void removeLifecycleListener(LifecycleListener listener);

	/**
	 * Prepare for the beginning of active use of the public methods of this
	 * component.  This method should be called after <code>configure()</code>,
	 * and before any of the public methods of the component are utilized.
	 *
	 * @exception LifecycleException if this component detects a fatal error
	 *  that prevents this component from being used
	 */
	public abstract void start() throws LifecycleException;

	/**
	 * Gracefully terminate the active use of the public methods of this
	 * component.  This method should be the last one called on a given
	 * instance of this component.
	 *
	 * @exception LifecycleException if this component detects a fatal error
	 *  that needs to be reported
	 */
	public abstract void stop() throws LifecycleException;

	/**
	 * @param service the service to set
	 */
	public abstract void setService(StandardService service);

	/**
	 * @return the service
	 */
	public abstract StandardService getService();

}