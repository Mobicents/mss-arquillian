package org.mobicents.arquillian.mss.mediaserver.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class MediaServerExtension implements LoadableExtension {

	@Override
	public void register(ExtensionBuilder builder) {
		builder.observer(MediaServerProducer.class);
	}

}
