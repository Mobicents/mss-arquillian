/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1;

import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sip.SipFactory;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Engine;
import org.apache.catalina.Host;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.Loader;
import org.apache.catalina.Realm;
import org.apache.catalina.ServerFactory;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardEngine;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.core.StandardService;
import org.apache.catalina.loader.WebappLoader;
import org.apache.catalina.security.SecurityConfig;
import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.util.LifecycleSupport;
import org.apache.catalina.util.StringManager;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.util.IntrospectionUtils;
import org.apache.tomcat.util.log.SystemLogHandler;
import org.jboss.arquillian.container.mobicents.api.SipServletsEmbeddedContainer;
import org.mobicents.servlet.sip.catalina.SipProtocolHandler;
import org.mobicents.servlet.sip.catalina.SipStandardService;

/**
 * @author jean.deruelle@gmail.com
 *
 */
public class MobicentsSipServletsEmbeddedImpl implements Lifecycle, SipServletsEmbeddedContainer {
	private static Log log = LogFactory.getLog(MobicentsSipServletsEmbeddedImpl.class);

	private StandardService service;
	
    // ----------------------------------------------------------- Constructors


    /**
     * Construct a new instance of this class with default properties.
     */
    public MobicentsSipServletsEmbeddedImpl() {       
    	service = new StandardService(); 
    }


    /**
     * Construct a new instance of this class with specified properties.
     *
     * @param realm Realm implementation to be inherited by all components
     *  (unless overridden further down the container hierarchy)
     */
    public MobicentsSipServletsEmbeddedImpl(Realm realm) {

        setRealm(realm);
        setSecurityProtection();
        
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * Is naming enabled ?
     */
    protected boolean useNaming = true;


    /**
     * Is standard streams redirection enabled ?
     */
    protected boolean redirectStreams = true;


    /**
     * The set of Engines that have been deployed in this server.  Normally
     * there will only be one.
     */
    protected Engine engines[] = new Engine[0];


    /**
     * Custom mappings of login methods to authenticators
     */
    protected HashMap authenticators;


    /**
     * Descriptive information about this server implementation.
     */
    protected static final String info =
        "org.apache.catalina.startup.Embedded/1.0";


    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);
    
    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);


    /**
     * The default realm to be used by all containers associated with
     * this compoennt.
     */
    protected Realm realm = null;


    /**
     * The string manager for this package.
     */
    protected static StringManager sm =
        StringManager.getManager(Constants.Package);


    /**
     * Has this component been started yet?
     */
    protected boolean started = false;

    /**
     * Use await.
     */
    protected boolean await = false;
    
    protected StandardContext context;


    // ------------------------------------------------------------- Properties


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#isUseNaming()
	 */
    @Override
	public boolean isUseNaming() {

        return (this.useNaming);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setUseNaming(boolean)
	 */
    @Override
	public void setUseNaming(boolean useNaming) {

        boolean oldUseNaming = this.useNaming;
        this.useNaming = useNaming;
        support.firePropertyChange("useNaming", new Boolean(oldUseNaming),
                                   new Boolean(this.useNaming));

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#isRedirectStreams()
	 */
    @Override
	public boolean isRedirectStreams() {

        return (this.redirectStreams);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setRedirectStreams(boolean)
	 */
    @Override
	public void setRedirectStreams(boolean redirectStreams) {

        boolean oldRedirectStreams = this.redirectStreams;
        this.redirectStreams = redirectStreams;
        support.firePropertyChange("redirectStreams", new Boolean(oldRedirectStreams),
                                   new Boolean(this.redirectStreams));

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#getRealm()
	 */
    @Override
	public Realm getRealm() {

        return (this.realm);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setRealm(org.apache.catalina.Realm)
	 */
    @Override
	public void setRealm(Realm realm) {

        Realm oldRealm = this.realm;
        this.realm = realm;
        support.firePropertyChange("realm", oldRealm, this.realm);

    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setAwait(boolean)
	 */
    @Override
	public void setAwait(boolean b) {
        await = b;
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#isAwait()
	 */
    @Override
	public boolean isAwait() {
        return await;
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setCatalinaHome(java.lang.String)
	 */
    @Override
	public void setCatalinaHome( String s ) {
        System.setProperty( "catalina.home", s);
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setCatalinaBase(java.lang.String)
	 */
    @Override
	public void setCatalinaBase( String s ) {
        System.setProperty( "catalina.base", s);
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#getCatalinaHome()
	 */
    @Override
	public String getCatalinaHome() {
        return System.getProperty("catalina.home");
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#getCatalinaBase()
	 */
    @Override
	public String getCatalinaBase() {
        return System.getProperty("catalina.base");
    }


    // --------------------------------------------------------- Public Methods

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#addConnector(org.apache.catalina.connector.Connector)
	 */
    @Override
	public synchronized void addConnector(Connector connector) {

        if( log.isDebugEnabled() ) {
            log.debug("Adding connector (" + connector.getInfo() + ")");
        }

        // Make sure we have a Container to send requests to
        if (engines.length < 1)
            throw new IllegalStateException
                (sm.getString("embedded.noEngines"));

        /*
         * Add the connector. This will set the connector's container to the
         * most recently added Engine
         */
        service.addConnector(connector);
    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#addEngine(org.apache.catalina.Engine)
	 */
    @Override
	public synchronized void addEngine(Engine engine) {

        if( log.isDebugEnabled() )
            log.debug("Adding engine (" + engine.getInfo() + ")");

        // Add this Engine to our set of defined Engines
        Engine results[] = new Engine[engines.length + 1];
        for (int i = 0; i < engines.length; i++)
            results[i] = engines[i];
        results[engines.length] = engine;
        engines = results;

        // Start this Engine if necessary
        if (started && (engine instanceof Lifecycle)) {
            try {
                ((Lifecycle) engine).start();
            } catch (LifecycleException e) {
                log.error("Engine.start", e);
            }
        }

        service.setContainer(engine);
    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createConnector(java.net.InetAddress, int, boolean)
	 */
    @Override
	public Connector createConnector(InetAddress address, int port,
                                     boolean secure) {
	return createConnector(address != null? address.toString() : null,
			       port, secure);
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createConnector(java.lang.String, int, boolean)
	 */
    @Override
	public Connector createConnector(String address, int port,
                                     boolean secure) {
        String protocol = "http";
        if (secure) {
            protocol = "https";
        }

        return createConnector(address, port, protocol);
    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createConnector(java.net.InetAddress, int, java.lang.String)
	 */
    @Override
	public Connector createConnector(InetAddress address, int port,
                                     String protocol) {
	return createConnector(address != null? address.toString() : null,
			       port, protocol);
    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createConnector(java.lang.String, int, java.lang.String)
	 */
    @Override
	public Connector createConnector(String address, int port,
				     String protocol) {

        Connector connector = null;

	if (address != null) {
	    /*
	     * InetAddress.toString() returns a string of the form
	     * "<hostname>/<literal_IP>". Get the latter part, so that the
	     * address can be parsed (back) into an InetAddress using
	     * InetAddress.getByName().
	     */
	    int index = address.indexOf('/');
	    if (index != -1) {
		address = address.substring(index + 1);
	    }
	}

	if (log.isDebugEnabled()) {
            log.debug("Creating connector for address='" +
		      ((address == null) ? "ALL" : address) +
		      "' port='" + port + "' protocol='" + protocol + "'");
	}

        try {

            if (protocol.equals("ajp")) {
                connector = new Connector("org.apache.jk.server.JkCoyoteHandler");
            } else if (protocol.equals("memory")) {
                connector = new Connector("org.apache.coyote.memory.MemoryProtocolHandler");
            } else if (protocol.equals("http")) {
                connector = new Connector();
            } else if (protocol.equals("https")) {
                connector = new Connector();
                connector.setScheme("https");
                connector.setSecure(true);
                connector.setProperty("SSLEnabled","true");
                // FIXME !!!! SET SSL PROPERTIES
            } else {
                connector = new Connector(protocol);
            }

            if (address != null) {
                IntrospectionUtils.setProperty(connector, "address", 
                                               "" + address);
            }
            IntrospectionUtils.setProperty(connector, "port", "" + port);

        } catch (Exception e) {
            log.error("Couldn't create connector.");
        } 

        return (connector);

    }

    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createContext(java.lang.String, java.lang.String)
	 */
    @Override
	public Context createContext(String path, String docBase) {

        if( log.isDebugEnabled() )
            log.debug("Creating context '" + path + "' with docBase '" +
                       docBase + "'");

        context = new StandardContext();

        context.setDocBase(docBase);
        context.setPath(path);

        ContextConfig config = new ContextConfig();
        config.setCustomAuthenticators(authenticators);
        ((Lifecycle) context).addLifecycleListener(config);

        return (context);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createEngine()
	 */
    @Override
	public Engine createEngine() {

        if( log.isDebugEnabled() )
            log.debug("Creating engine");

        StandardEngine engine = new StandardEngine();

        // Default host will be set to the first host added
        engine.setRealm(realm);         // Inherited by all children

        return (engine);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createHost(java.lang.String, java.lang.String)
	 */
    @Override
	public Host createHost(String name, String appBase) {

        if( log.isDebugEnabled() )
            log.debug("Creating host '" + name + "' with appBase '" +
                       appBase + "'");

        StandardHost host = new StandardHost();

        host.setAppBase(appBase);
        host.setName(name);

        return (host);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#createLoader(java.lang.ClassLoader)
	 */
    @Override
	public Loader createLoader(ClassLoader parent) {

        if( log.isDebugEnabled() )
            log.debug("Creating Loader with parent class loader '" +
                       parent + "'");

        WebappLoader loader = new WebappLoader(parent);
        return (loader);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#getInfo()
	 */
    @Override
	public String getInfo() {

        return (info);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#removeContext(org.apache.catalina.Context)
	 */
    @Override
	public synchronized void removeContext(Context context) {

        if( log.isDebugEnabled() )
            log.debug("Removing context[" + context.getPath() + "]");

        boolean isContextExists = isContextExists(context);
        if(!isContextExists)
        	return;
        
        // Remove this Context from the associated Host
        if( log.isDebugEnabled() )
            log.debug(" Removing this Context");
        context.getParent().removeChild(context);

    }


	private boolean isContextExists(Context context) {
		// Is this Context actually among those that are defined?
        for (int i = 0; i < engines.length; i++) {
            Container hosts[] = engines[i].findChildren();
            for (int j = 0; j < hosts.length; j++) {
                Container contexts[] = hosts[j].findChildren();
                for (int k = 0; k < contexts.length; k++) {
                    if (context == (Context) contexts[k]) {
                        return true;
                    }
                }                
            }           
        }
        return false;
	}


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#removeEngine(org.apache.catalina.Engine)
	 */
    @Override
	public synchronized void removeEngine(Engine engine) {

        if( log.isDebugEnabled() )
            log.debug("Removing engine (" + engine.getInfo() + ")");

        // Is the specified Engine actually defined?
        int j = -1;
        for (int i = 0; i < engines.length; i++) {
            if (engine == engines[i]) {
                j = i;
                break;
            }
        }
        if (j < 0)
            return;

        // Remove any Connector that is using this Engine
        if( log.isDebugEnabled() )
            log.debug(" Removing related Containers");
        while (true) {
            int n = -1;
            Connector[] connectors = service.findConnectors();
            for (int i = 0; i < connectors.length; i++) {
                if (connectors[i].getContainer() == (Container) engine) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                break;
            service.removeConnector(connectors[n]);
        }

        // Stop this Engine if necessary
        if (engine instanceof Lifecycle) {
            if( log.isDebugEnabled() )
                log.debug(" Stopping this Engine");
            try {
                ((Lifecycle) engine).stop();
            } catch (LifecycleException e) {
                log.error("Engine.stop", e);
            }
        }

        // Remove this Engine from our set of defined Engines
        if( log.isDebugEnabled() )
            log.debug(" Removing this Engine");
        int k = 0;
        Engine results[] = new Engine[engines.length - 1];
        for (int i = 0; i < engines.length; i++) {
            if (i != j)
                results[k++] = engines[i];
        }
        engines = results;

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#removeHost(org.apache.catalina.Host)
	 */
    @Override
	public synchronized void removeHost(Host host) {

        if( log.isDebugEnabled() )
            log.debug("Removing host[" + host.getName() + "]");

        // Is this Host actually among those that are defined?
        boolean found = false;
        for (int i = 0; i < engines.length; i++) {
            Container hosts[] = engines[i].findChildren();
            for (int j = 0; j < hosts.length; j++) {
                if (host == (Host) hosts[j]) {
                    found = true;
                    break;

                }
            }
            if (found)
                break;
        }
        if (!found)
            return;

        // Remove this Host from the associated Engine
        if( log.isDebugEnabled() )
            log.debug(" Removing this Host");
        host.getParent().removeChild(host);

    }


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
    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#addAuthenticator(org.apache.catalina.Authenticator, java.lang.String)
	 */
    @Override
	public void addAuthenticator(Authenticator authenticator,
                                 String loginMethod) {
        if (!(authenticator instanceof Valve)) {
            throw new IllegalArgumentException(
                sm.getString("embedded.authenticatorNotInstanceOfValve"));
        }
        if (authenticators == null) {
            synchronized (this) {
                if (authenticators == null) {
                    authenticators = new HashMap();
                }
            }
        }
        authenticators.put(loginMethod, authenticator);
    }


    // ------------------------------------------------------ Lifecycle Methods


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#addLifecycleListener(org.apache.catalina.LifecycleListener)
	 */
    @Override
	public void addLifecycleListener(LifecycleListener listener) {

        lifecycle.addLifecycleListener(listener);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#findLifecycleListeners()
	 */
    @Override
	public LifecycleListener[] findLifecycleListeners() {

        return lifecycle.findLifecycleListeners();

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#removeLifecycleListener(org.apache.catalina.LifecycleListener)
	 */
    @Override
	public void removeLifecycleListener(LifecycleListener listener) {

        lifecycle.removeLifecycleListener(listener);

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#start()
	 */
    @Override
	public void start() throws LifecycleException {

        if( log.isInfoEnabled() )
            log.info("Starting tomcat server");

        // Validate the setup of our required system properties
        initDirs();

        // Initialize some naming specific properties
        initNaming();

        // Validate and update our current component state
        if (started)
            throw new LifecycleException
                (sm.getString("embedded.alreadyStarted"));
        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;
        service.initialize();

        // Start our defined Engines first
        for (int i = 0; i < engines.length; i++) {
            if (engines[i] instanceof Lifecycle)
                ((Lifecycle) engines[i]).start();
        }

        // Start our defined Connectors second
        Connector[] connectors = service.findConnectors();
        for (int i = 0; i < connectors.length; i++) {
            connectors[i].initialize();
            if (connectors[i] instanceof Lifecycle)
                ((Lifecycle) connectors[i]).start();
        }

    }


    /* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#stop()
	 */
    @Override
	public void stop() throws LifecycleException {

        if( log.isDebugEnabled() )
            log.debug("Stopping embedded server");

        // Validate and update our current component state
        if (!started)
            throw new LifecycleException
                (sm.getString("embedded.notStarted"));
        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Stop our defined Connectors first
        Connector[] connectors = service.findConnectors();
        for (int i = 0; i < connectors.length; i++) {
            if (connectors[i] instanceof Lifecycle){
            	service.removeConnector(connectors[i]);
            	((Lifecycle) connectors[i]).stop();
            }
                
        }

        // Stop our defined Engines second
        for (int i = 0; i < engines.length; i++) {
            if (engines[i] instanceof Lifecycle)
                ((Lifecycle) engines[i]).stop();
        }

        /*
         * Service should be stopped and removed otherwise in between start/stop/start of the container,
         * javax.management.InstanceAlreadyExistsException: mss-tomcat-embedded-6:type=SipApplicationDispatcher
         * exception will occur, which is related to the JMX MBeanServer, when tomcat embedded tries to register the MBean, the 
         * instance is already there.
         */
        
		if(service != null) {			
			try {
				service.stop();
			} catch (LifecycleException e) {
				log.error("service already stopped ", e);
			}		
			ServerFactory.getServer().removeService(service);
			/*
			 * Issue: http://code.google.com/p/mobicents/issues/detail?id=3116
			 * 
			 * We need to force SipFactory to create a new SipStack each time it is asked. By default SipFactory will return the previously 
			 * created SipStack if it exists. This will create a problem in a scenario like:
			 * 
			 * 1. container starts -> SipFactory creates sipStack
			 * 2. System.setProperties such as as properties related to SSL
			 * 3. container.restart -> SipFactory returns previously created SipStack that is not aware of the new System properties
			 * 
			 * Thats why the SipFactory needs to be reset in order to create a new SipStack every time the 
			 * SipFactory.getInstance().createSipFactory (SipStandardService.initialize()) method is called.  
			 */
			SipFactory.getInstance().resetFactory();
		}

    }


    // ------------------------------------------------------ Protected Methods


    /** Initialize naming - this should only enable java:env and root naming.
     * If tomcat is embeded in an application that already defines those -
     * it shouldn't do it.
     *
     * XXX The 2 should be separated, you may want to enable java: but not
     * the initial context and the reverse
     * XXX Can we "guess" - i.e. lookup java: and if something is returned assume
     * false ?
     * XXX We have a major problem with the current setting for java: url
     */
    protected void initNaming() {
        // Setting additional variables
        if (!useNaming) {
            log.info( "Catalina naming disabled");
            System.setProperty("catalina.useNaming", "false");
        } else {
            System.setProperty("catalina.useNaming", "true");
            String value = "org.apache.naming";
            String oldValue =
                System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
            if (oldValue != null) {
                value = value + ":" + oldValue;
            }
            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, value);
            if( log.isDebugEnabled() )
                log.debug("Setting naming prefix=" + value);
            value = System.getProperty
                (javax.naming.Context.INITIAL_CONTEXT_FACTORY);
            if (value == null) {
                System.setProperty
                    (javax.naming.Context.INITIAL_CONTEXT_FACTORY,
                     "org.apache.naming.java.javaURLContextFactory");
            } else {
                log.debug( "INITIAL_CONTEXT_FACTORY alread set " + value );
            }
        }
    }


    protected void initDirs() {

        String catalinaHome = System.getProperty("catalina.home");
        if (catalinaHome == null) {
            // Backwards compatibility patch for J2EE RI 1.3
            String j2eeHome = System.getProperty("com.sun.enterprise.home");
            if (j2eeHome != null) {
                catalinaHome=System.getProperty("com.sun.enterprise.home");
            } else if (System.getProperty("catalina.base") != null) {
                catalinaHome = System.getProperty("catalina.base");
            } else {
                // Use IntrospectionUtils and guess the dir
                catalinaHome = IntrospectionUtils.guessInstall
                    ("catalina.home", "catalina.base", "catalina.jar");
                if (catalinaHome == null) {
                    catalinaHome = IntrospectionUtils.guessInstall
                        ("tomcat.install", "catalina.home", "tomcat.jar");
                }
            }
        }
        // last resort - for minimal/embedded cases. 
        if(catalinaHome==null) {
            catalinaHome=System.getProperty("user.dir");
        }
        if (catalinaHome != null) {
            File home = new File(catalinaHome);
            if (!home.isAbsolute()) {
                try {
                    catalinaHome = home.getCanonicalPath();
                } catch (IOException e) {
                    catalinaHome = home.getAbsolutePath();
                }
            }
            System.setProperty("catalina.home", catalinaHome);
        }

        if (System.getProperty("catalina.base") == null) {
            System.setProperty("catalina.base",
                               catalinaHome);
        } else {
            String catalinaBase = System.getProperty("catalina.base");
            File base = new File(catalinaBase);
            if (!base.isAbsolute()) {
                try {
                    catalinaBase = base.getCanonicalPath();
                } catch (IOException e) {
                    catalinaBase = base.getAbsolutePath();
                }
            }
            System.setProperty("catalina.base", catalinaBase);
        }
        
        String temp = System.getProperty("java.io.tmpdir");
        if (temp == null || (!(new File(temp)).exists())
                || (!(new File(temp)).isDirectory())) {
            log.error(sm.getString("embedded.notmp", temp));
        }

    }

    
    protected void initStreams() {
        if (redirectStreams) {
            // Replace System.out and System.err with a custom PrintStream
            SystemLogHandler systemlog = new SystemLogHandler(System.out);
            System.setOut(systemlog);
            System.setErr(systemlog);
        }
    }
    

    // -------------------------------------------------------- Private Methods

    /**
     * Set the security package access/protection.
     */
    protected void setSecurityProtection(){
        SecurityConfig securityConfig = SecurityConfig.newInstance();
        securityConfig.setPackageDefinition();
        securityConfig.setPackageAccess();
    }


	/* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#setService(org.apache.catalina.core.StandardService)
	 */
	@Override
	public void setService(StandardService service) {
		this.service = service;
	}


	/* (non-Javadoc)
	 * @see org.jboss.arquillian.container.mobicents.servlet.sip.embedded_1.MobicentsSipServletsEmbedded#getService()
	 */
	@Override
	public StandardService getService() {
		return service;
	}


	/* 
	 * Add SipConnector
	 */
	@Override
	public Connector addSipConnector(String connectorName, String ipAddress, int port, String transport) throws Exception {

		Connector sipConnector = new Connector(SipProtocolHandler.class.getName());
		SipProtocolHandler sipProtocolHandler = (SipProtocolHandler) sipConnector
				.getProtocolHandler();
		sipProtocolHandler.setPort(port);
		sipProtocolHandler.setIpAddress(ipAddress);
		sipProtocolHandler.setSignalingTransport(transport);		

		((SipStandardService)service).addConnector(sipConnector);
		return sipConnector;
	}


	//Return the SipConnectors
	@Override
	public List<Connector> getSipConnectors() {
		List<Connector> connectors = new ArrayList<Connector>();
		
		Connector[] conns = service.findConnectors();
		
		for (Connector conn : conns) {
			if (conn.getProtocolHandler() instanceof SipProtocolHandler){
				connectors.add(conn);
			}
		}
		
		return connectors;
	}


	@Override
	public void removeConnector(Connector connector) {

		service.removeConnector(connector);
		
	}
}