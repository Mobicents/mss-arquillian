/**
 * 
 */
package org.jboss.arquillian.container.mobicents.api.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation to get the StandardContext.
 * The field annotated with this will get set after deployment
 * 
 * @author gvagenas@gmail.com 
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.FIELD)
public @interface GetStandardContext {

}
