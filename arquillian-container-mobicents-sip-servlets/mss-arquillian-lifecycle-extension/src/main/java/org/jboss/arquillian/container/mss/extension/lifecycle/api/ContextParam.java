/**
 * 
 */
package org.jboss.arquillian.container.mss.extension.lifecycle.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.lang.model.type.NullType;

/**
 * Defines a context parameter that this test needs to set to the context. 
 * Both name and value attributes needs to be set
 * 
 * <pre>
 * {@code
 * @Test
 * @ContextParam(name="contextName", value="contextValue")
 * public void testMethod()
 * }
 * </pre>
 * 
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
