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
 * Defines the concurrency control mode that this test needs to set to the context. 
 * 
 * <pre>
 * {@code
 * @Test
 * @ConcurrencyControlMode(org.mobicents.servlet.sip.annotation.ConcurrencyControlMode.SipApplicationSession)
 * public void testMethod()
 * }
 * </pre>
 * @author gvagenas@gmail.com 
 * 
 */
@Documented
@Retention(RUNTIME)
@Target(ElementType.METHOD)
public @interface ConcurrencyControlMode {
	
	org.mobicents.servlet.sip.annotation.ConcurrencyControlMode value();

}
