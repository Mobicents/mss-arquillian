package org.mobicents.arquillian.mss.mediaserver.extension.mgcp.controller.ext;

import java.io.IOException;
import java.net.SocketAddress;

import org.mobicents.arquillian.mediaserver.api.MgcpEventListener;
import org.mobicents.media.server.io.network.UdpManager;
import org.mobicents.media.server.mgcp.MgcpEvent;
import org.mobicents.media.server.mgcp.message.MgcpMessage;
import org.mobicents.media.server.scheduler.Scheduler;
import org.mobicents.media.server.spi.listener.Listeners;
import org.mobicents.media.server.spi.listener.TooManyListenersException;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MgcpProviderExt extends org.mobicents.media.server.mgcp.MgcpProvider{

//	private Logger logger = Logger.getLogger(MgcpProviderExt.class);
	
	Listeners<MgcpEventListener> mgcpEventListeners = new Listeners<MgcpEventListener>();
	
	public void addMgcpEventListener(MgcpEventListener listener) throws TooManyListenersException{
		mgcpEventListeners.add(listener);
	}
	
	public void removeMgcpEventListener(MgcpEventListener listener){
		mgcpEventListeners.remove(listener);
	}
	
	public MgcpProviderExt(UdpManager udpManager, int port, Scheduler scheduler) {
		super(udpManager, port, scheduler);
	}

	protected MgcpProviderExt(String name, UdpManager udpManager, int port, Scheduler scheduler) {
		super(name, udpManager, port, scheduler);
	}

	@Override
	public void activate() {
		super.activate();
	}
	
	@Override
	public void send(MgcpEvent event) throws IOException {
		super.send(event);
//		logger.info(event.getMessage()+" ,eventId: "+event.getEventID()+" ,eventAdres: "+event.getAddress());
		mgcpEventListeners.dispatch(event);
	}

	@Override
	public void send(MgcpMessage message, SocketAddress destination)
			throws IOException {
		super.send(message, destination);
//		logger.info(message);
		mgcpEventListeners.dispatch((org.mobicents.media.server.spi.listener.Event) message);
	}

	@Override
	public void send(MgcpEvent event, SocketAddress destination)
			throws IOException {
		super.send(event, destination);
//		logger.info(event.getMessage());
		mgcpEventListeners.dispatch(event);
		
	}
}
