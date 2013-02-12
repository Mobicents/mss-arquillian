package org.mobicents.arquillian.mss.mediaserver.extension.mgcp.controller.ext;

import java.util.ArrayList;
import java.util.List;

import org.mobicents.arquillian.mediaserver.api.MgcpEventListener;
import org.mobicents.media.server.mgcp.MgcpEvent;
import org.mobicents.media.server.mgcp.controller.Controller;
import org.mobicents.media.server.mgcp.tx.GlobalTransactionManager;
import org.mobicents.media.server.spi.listener.TooManyListenersException;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public class MgcpControllerExt extends Controller{

	List<MgcpEventListener> mgcpEventListeners = new ArrayList<MgcpEventListener>();

	public void addMgcpEventListener(MgcpEventListener listener) throws TooManyListenersException{
		mgcpEventListeners.add(listener);
	}
	
	public void removeMgcpEventListener(MgcpEventListener listener){
		mgcpEventListeners.remove(listener);
	}
	
	@Override
	public void createProvider() {
		if (mgcpProvider == null)
			mgcpProvider = new MgcpProviderExt(udpInterface, port, scheduler);
	}

	public GlobalTransactionManager getGlobalTxManager(){
		if(txManager == null)
			createGlobalTransactionManager();
		return txManager;
	}
	
	@Override
	public void process(MgcpEvent event) {
		super.process(event);
		for (MgcpEventListener listener : mgcpEventListeners) {
			listener.process(event);
		}
	}

	public MgcpProviderExt getProvider() {
		return (MgcpProviderExt) mgcpProvider;
	}

	
}
