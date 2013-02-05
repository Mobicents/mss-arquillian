package org.mobicents.arquillian.mediaserver.api;

/**
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */

public interface MgcpUnitRequest {

	void parseRequest();
	int getTxId();

}
