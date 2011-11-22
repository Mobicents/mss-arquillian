/**
 * 
 */
package org.jboss.arquillian.container.mss.extension.lifecycle.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines that this method needs to set a specific context parameter 
 * @author gvagenas@gmail.com 
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ContextParam {
	
	/**
	 * Name of the parameter
	 * 
	 * @return the name of the context param
	 */
	String name();
	
	/**
	 * Value of the parameter
	 * 
	 * @return the value of the context param
	 */
	String value();

}
