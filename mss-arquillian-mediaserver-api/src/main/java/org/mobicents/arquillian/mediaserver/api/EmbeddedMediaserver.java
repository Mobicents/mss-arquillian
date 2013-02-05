package org.mobicents.arquillian.mediaserver.api;

import java.util.Collection;

import org.mobicents.media.server.spi.Endpoint;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public interface EmbeddedMediaserver {

	void startServer() throws Exception;
	void stopServer();
	void installEndpoint(Endpoint endpoint);
	void removeEndpoint(Endpoint endpoint);
	void setBindAddress(String bindAddress);
	void setControllerPort(int controllerPort) throws RuntimeException;
	Collection<Endpoint> getEndpoints();
	void installEndpoint(EndpointType type, int count);
	void removeAllEndpoints();
	boolean isStarted();
	String getStatus();
	void setId(String id);
	String getId();
	void registerListener(MgcpEventListener listener);
	void unregisterListener(MgcpEventListener listener);
}
