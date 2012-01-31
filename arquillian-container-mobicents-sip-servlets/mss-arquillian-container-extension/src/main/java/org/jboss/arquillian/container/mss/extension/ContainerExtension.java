/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 * @author gvagenas@gmail.com 
 * 
 */
public class ContainerExtension implements LoadableExtension 
{

	   @Override
	   public void register(ExtensionBuilder builder)
	   {
	      builder.observer(ContainerProducer.class);
	   }
}
