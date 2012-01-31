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
 * @author gvagenas@gmail.com 
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface GetEmbeddedContainer {

}
