/**
 * 
 */
package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_7;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * Will register the container to the arquillian core
 * 
 * @author gvagenas 
 * gvagenas@gmail.com / devrealm.org
 * 
 */
public class MobicentsExtension implements LoadableExtension 
{

	/* (non-Javadoc)
	 * @see org.jboss.arquillian.core.spi.LoadableExtension#register(org.jboss.arquillian.core.spi.LoadableExtension.ExtensionBuilder)
	 */
	@Override
	public void register(ExtensionBuilder builder) {
		builder.service(DeployableContainer.class, MobicentsSipServletsContainer.class);
	}
}
