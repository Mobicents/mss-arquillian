package org.mobicents.arquillian.mss.mediaserver.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mediaserver.api.EndpointType;
import org.mobicents.media.core.ResourcesPool;
import org.mobicents.media.core.Server;
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

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class EmbeddedMediaserverImpl implements EmbeddedMediaserver {
	private Logger logger = Logger.getLogger(EmbeddedMediaserverImpl.class);

	//clock and scheduler
	protected Clock clock;
	protected Scheduler scheduler;

	protected ChannelsManager channelsManager;

	protected UdpManager udpManager;
	protected DspFactoryImpl dspFactory = new DspFactoryImpl();

	private Controller controller;
	private ResourcesPool resourcesPool;

	//	//user and ivr endpoint
	//	private IvrEndpoint user, ivr;    

	private List<Endpoint> endpoints;

	private Server server;

	private String bindAddress = "127.0.0.1";
	private int controllerPort = 2427;

	private boolean isServerStarted;

	private int ivrCounter;

	private int confCounter;

	private int relayCounter;

	public EmbeddedMediaserverImpl() {
		endpoints = Collections.synchronizedList(new ArrayList<Endpoint>());
	}

	@Override
	public void startServer() throws Exception {
		logger.info("Starting server");
		//use default clock
		clock = new DefaultClock();

		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.ulaw.Encoder");
		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.ulaw.Decoder");

		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.alaw.Encoder");
		dspFactory.addCodec("org.mobicents.media.server.impl.dsp.audio.g711.alaw.Decoder");

		//create single thread scheduler
		scheduler = new Scheduler();
		scheduler.setClock(clock);
		scheduler.start();

		udpManager = new UdpManager(scheduler);
		udpManager.setBindAddress(bindAddress);
		udpManager.start();

		channelsManager = new ChannelsManager(udpManager);
		channelsManager.setScheduler(scheduler);

		resourcesPool=new ResourcesPool(scheduler, channelsManager, dspFactory);
//		resourcesPool.setDefaultDtmfDetectors(10);
//		resourcesPool.setDefaultDtmfGenerators(10);
//		resourcesPool.setDefaultLocalConnections(10);
//		resourcesPool.setDefaultPlayers(10);
//		resourcesPool.setDefaultRecorders(10);
//		resourcesPool.setDefaultRemoteConnections(10);
//		resourcesPool.setDefaultSignalDetectors(10);
//		resourcesPool.setDefaultSignalGenerators(10);
//		resourcesPool.start();

		server=new Server();
		server.setClock(clock);
		server.setScheduler(scheduler);
		server.setUdpManager(udpManager);
		server.setResourcesPool(resourcesPool);        

		controller=new Controller();
		controller.setUdpInterface(udpManager);
		controller.setPort(controllerPort);
		controller.setScheduler(scheduler); 
		controller.setServer(server);        
		controller.setConfigurationByURL(this.getClass().getResource("/mgcp-conf.xml"));
//		controller.setPoolSize(15);

		controller.start();
		isServerStarted = true;
          
	}

	@Override
	public void stopServer() {
		logger.info("Stopping server");
		controller.stop();
		server.stop();                 

		if(!endpoints.isEmpty()) {
			for(Endpoint endpoint: endpoints){
				endpoint.stop();
			}
//			Iterator<Endpoint> iterator = endpoints.iterator();
//			while(iterator.hasNext()){
//				Endpoint endpoint = iterator.next();
//				if (endpoint != null){
//					endpoint.stop();
//					endpoints.remove(endpoint);
//				}
//			}
		}
	}

	@Override
	public synchronized void installEndpoint(Endpoint endpoint) {
		server.install(endpoint);
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
			break;
		case CONFERENCE:
			for (int i = 0; i < count; i++) {
				confCounter++;
				ConferenceEndpoint conf = new ConferenceEndpoint("mobicents/cnf/"+confCounter);
				installEndpoint(conf);
			}
			break;
		case PACKETRELAY:
			for (int i = 0; i < count; i++) {
				relayCounter++;
				PacketRelayEndpoint relay = new PacketRelayEndpoint("mobicents/relay/"+relayCounter);
				installEndpoint(relay);
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

}
