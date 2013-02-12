package org.mobicents.arquillian.mediaserver.api;

import java.util.Collection;

import org.mobicents.media.server.spi.listener.Event;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public interface MgcpEventListener {

	Collection<MgcpUnitRequest> getPlayAnnoRequestsReceived();
	boolean checkForSuccessfulResponse(int txId);
	boolean verifyAll();
	void clearResponses();
	void clearRequests();
	void clearAll();
	Collection<MgcpUnitRequest> getNotifyRequestsReceived();
	boolean verifyNotify();
	void process(Event event);
}
