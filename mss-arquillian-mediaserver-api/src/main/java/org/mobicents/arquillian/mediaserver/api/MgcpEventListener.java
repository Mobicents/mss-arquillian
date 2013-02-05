package org.mobicents.arquillian.mediaserver.api;

import java.util.Collection;

import org.mobicents.media.server.spi.listener.Listener;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public interface MgcpEventListener extends Listener {

	Collection<MgcpUnitRequest> getPlayAnnoRequestsReceived();
	boolean checkForSuccessfulResponse(int txId);

}
