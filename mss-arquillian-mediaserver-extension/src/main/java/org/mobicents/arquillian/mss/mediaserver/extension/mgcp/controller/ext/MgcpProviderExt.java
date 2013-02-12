package org.mobicents.arquillian.mss.mediaserver.extension.mgcp.controller.ext;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.mobicents.arquillian.mediaserver.api.MgcpEventListener;
import org.mobicents.media.server.io.network.UdpManager;
import org.mobicents.media.server.mgcp.MgcpEvent;
import org.mobicents.media.server.mgcp.message.MgcpMessage;
import org.mobicents.media.server.scheduler.Scheduler;
import org.mobicents.media.server.spi.listener.TooManyListenersException;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MgcpProviderExt extends org.mobicents.media.server.mgcp.MgcpProvider{

//	private Logger logger = Logger.getLogger(MgcpProviderExt.class);
	
	List<MgcpEventListener> mgcpEventListeners = new ArrayList<MgcpEventListener>();
	
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
		for (MgcpEventListener listener : mgcpEventListeners) {
			listener.process(event);
		}
	}

	@Override
	public void send(MgcpMessage message, SocketAddress destination)
			throws IOException {
		super.send(message, destination);
		for (MgcpEventListener listener : mgcpEventListeners) {
			listener.process((org.mobicents.media.server.spi.listener.Event) message);
		}
	}

	@Override
	public void send(MgcpEvent event, SocketAddress destination)
			throws IOException {
		super.send(event, destination);
		for (MgcpEventListener listener : mgcpEventListeners) {
			listener.process(event);
		}
	}
}
