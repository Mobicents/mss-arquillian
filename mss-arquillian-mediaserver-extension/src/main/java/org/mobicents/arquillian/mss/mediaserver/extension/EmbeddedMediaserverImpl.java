package org.mobicents.arquillian.mss.mediaserver.extension;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.Logger;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mediaserver.api.EndpointType;
import org.mobicents.arquillian.mediaserver.api.MediaserverStatus;
import org.mobicents.arquillian.mediaserver.api.MgcpEventListener;
import org.mobicents.arquillian.mss.mediaserver.extension.mgcp.controller.ext.MgcpControllerExt;
import org.mobicents.arquillian.mss.mediaserver.extension.mgcp.controller.ext.MgcpProviderExt;
import org.mobicents.media.core.ResourcesPool;
import org.mobicents.media.core.Server;
import org.mobicents.media.core.endpoints.impl.BridgeEndpoint;
import org.mobicents.media.core.endpoints.impl.ConferenceEndpoint;
import org.mobicents.media.core.endpoints.impl.IvrEndpoint;
import org.mobicents.media.core.endpoints.impl.PacketRelayEndpoint;
import org.mobicents.media.server.component.DspFactoryImpl;
import org.mobicents.media.server.impl.rtp.ChannelsManager;
import org.mobicents.media.server.io.network.UdpManager;
import org.mobicents.media.server.mgcp.controller.Controller;
import org.mobicents.media.server.scheduler.Clock;
import org.mobicents.media.server.scheduler.DefaultClock;
import org.mobicents.media.server.scheduler.Scheduler;
import org.mobicents.media.server.spi.Endpoint;
import org.mobicents.media.server.spi.listener.TooManyListenersException;

/**
 * EmbeddedMediaserver can be used either with @Mediaserver annotation in an Arquillian test or 
 * outside simply by instantiating a new instance.
 * 
 * Based on
 * Media Server Embedded Example: 
 * http://code.google.com/p/mediaserver/source/browse/bootstrap/src/test/java/org/mobicents/media/server/test/RecordingTest.java
 * 
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class EmbeddedMediaserverImpl implements EmbeddedMediaserver {
	private Logger logger = Logger.getLogger(EmbeddedMediaserverImpl.class);

	//clock and scheduler
	protected Clock clock;
	protected Scheduler scheduler;

	protected ChannelsManager channelsManager;

	protected UdpManager udpManager;
	protected DspFactoryImpl dspFactory;

	private Controller controller;
	private ResourcesPool resourcesPool;   

	private List<Endpoint> endpoints;
	private List<MgcpEventListener> mgcpEventListeners;

	private Server server;

	private String bindAddress = "127.0.0.1";
	private int controllerPort = 2427;

	private boolean isServerStarted;

	private int ivrCounter;

	private int confCounter;

	private int relayCounter;
	
	private int bridgeCounter;

	private MediaserverStatus status;

	private String id;

	public EmbeddedMediaserverImpl() {
		this.status = MediaserverStatus.CREATED;
	}

	public void init() throws IOException{
		if(this.status != MediaserverStatus.CREATED)
			destroy();
		//use default clock
		clock = new DefaultClock();
		mgcpEventListeners = new CopyOnWriteArrayList<MgcpEventListener>();
		dspFactory = new DspFactoryImpl();
		//create single thread scheduler
		scheduler = new Scheduler();
		udpManager = new UdpManager(scheduler);
		channelsManager = new ChannelsManager(udpManager);
		resourcesPool = new ResourcesPool(scheduler, channelsManager, dspFactory);
		server = new Server();
		controller = new MgcpControllerExt();

		this.status = MediaserverStatus.INITIALIZED;

	}

	public void destroy() {
		clock = null;

		if(endpoints != null && !endpoints.isEmpty()) {
			for(Endpoint endpoint: endpoints){
				endpoint.stop();
			}
			endpoints = null;
		}
		
		if(!mgcpEventListeners.isEmpty()){
			for(MgcpEventListener listener: mgcpEventListeners){
				unregisterListener(listener);
			}
		}
		
		endpoints = null;
		dspFactory = null;
		scheduler.stop(); 
		scheduler = null;
		udpManager.stop();
		udpManager = null;
		channelsManager = null;
		resourcesPool = null;

		controller.stop();
		controller = null;
		server.stop();

		server = null;
		this.status = MediaserverStatus.STOPPED;
	}

	@Override
	public void startServer() throws Exception {
		logger.info("Starting server");

		if(this.status != MediaserverStatus.INITIALIZED)
			init();
		
		endpoints = new CopyOnWriteArrayList<Endpoint>();
		
		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.ulaw.Encoder");
		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.ulaw.Decoder");

		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.alaw.Encoder");
		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.alaw.Decoder");

		scheduler.setClock(clock);
		scheduler.start();

		udpManager.setBindAddress(bindAddress);
		udpManager.start();

		channelsManager.setScheduler(scheduler);

		resourcesPool=new ResourcesPool(scheduler, channelsManager, dspFactory);

		server.setClock(clock);
		server.setScheduler(scheduler);
		server.setUdpManager(udpManager);
		server.setResourcesPool(resourcesPool);        

		controller.setUdpInterface(udpManager);
		controller.setPort(controllerPort);
		controller.setScheduler(scheduler); 
		controller.setServer(server);        
		controller.setConfigurationByURL(this.getClass().getResource("/mgcp-conf.xml"));

		controller.start();
		isServerStarted = true;
		status = MediaserverStatus.STARTED;

	}

	@Override
	public void registerListener(MgcpEventListener listener) {
		
		MgcpProviderExt provider = ((MgcpControllerExt) controller).getProvider();

		try {
			provider.addMgcpEventListener(listener);
			((MgcpControllerExt) controller).addMgcpEventListener(listener);
			mgcpEventListeners.add(listener);
		} catch (TooManyListenersException e) {
			logger.debug(e);
			stopServer();
			status = MediaserverStatus.FAILED;
		}
	}

	@Override
	public void unregisterListener(MgcpEventListener listener) {

		MgcpProviderExt provider = ((MgcpControllerExt) controller).getProvider();

		try {
			provider.removeMgcpEventListener(listener);
			((MgcpControllerExt) controller).removeMgcpEventListener(listener);
			mgcpEventListeners.remove(listener);
		} catch (Exception e) {
			logger.debug(e);
			stopServer();
			status = MediaserverStatus.FAILED;
		}
	}
	
	@Override
	public void stopServer() {
		logger.info("Stopping server");
		destroy();
		status = MediaserverStatus.STOPPED;
	}

	@Override
	public synchronized void installEndpoint(Endpoint endpoint) {
		server.install(endpoint,null);
		endpoints.add(endpoint);
	}

	@Override
	public synchronized void installEndpoint(EndpointType type, int count){
		switch (type) {
		case IVR:
			for (int i = 0; i < count; i++) {
				ivrCounter++;
				IvrEndpoint ivr = new IvrEndpoint("mobicents/ivr/"+ivrCounter);
				installEndpoint(ivr);
			}
			status = MediaserverStatus.CONFIGURED;
			break;
		case CONFERENCE:
			for (int i = 0; i < count; i++) {
				confCounter++;
				ConferenceEndpoint conf = new ConferenceEndpoint("mobicents/cnf/"+confCounter);
				installEndpoint(conf);
			}
			status = MediaserverStatus.CONFIGURED;
			break;
		case PACKETRELAY:
			for (int i = 0; i < count; i++) {
				relayCounter++;
				PacketRelayEndpoint relay = new PacketRelayEndpoint("mobicents/relay/"+relayCounter);
				installEndpoint(relay);
			}
			status = MediaserverStatus.CONFIGURED;
			break;
		case BRIDGE:
			for (int i = 0; i < count; i++) {
				bridgeCounter++;
				BridgeEndpoint bridge = new BridgeEndpoint("mobicents/bridge/"+relayCounter);
				installEndpoint(bridge);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public Collection<Endpoint> getEndpoints(){
		return server.getEndpoints();
	}

	@Override
	public synchronized void removeEndpoint(Endpoint endpoint) {
		server.uninstalls(endpoint.getLocalName());
		endpoint.stop();
		endpoints.remove(endpoint);
	}

	@Override
	public synchronized void removeAllEndpoints(){
		Iterator<Endpoint> iterator = endpoints.listIterator();
		while(iterator.hasNext()){
			//			iterator.remove();
			Endpoint endpoint = iterator.next();
			removeEndpoint(endpoint);
		}
		status = MediaserverStatus.UNCONFIGURED;
	}

	public Server getServer() {
		return server;
	}

	public Controller getController() {
		return controller;
	}

	@Override
	public void setBindAddress(String bindAddress) {
		this.bindAddress = bindAddress;
	}

	@Override
	public void setControllerPort(int controllerPort) throws RuntimeException {
		if(isServerStarted)
			throw new RuntimeException("Server already started");
		this.controllerPort = controllerPort;
	}

	@Override
	public boolean isStarted() {
		return isServerStarted;
	}

	@Override
	public String getStatus() {
		return status.name();
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

}
