/**
 * 
 */
package org.jboss.arquillian.container.mss.extension.lifecycle.api;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a map of context parameters that this test needs to set to the context.
 * The annotation needs to be defined in both the field representing the map and the method that will consume it.
 * Value attribute at method and field must be the same.
 *   
 * <pre>
 * {@code
 * 	@ContextParamMap("aContextMap")
 *	Map<String, String> params = new HashMap<String, String>();
 *	
 *	{
 *	params.put("auth-header", "Digest username=\"1001\", realm=\"172.16.0.37\", algorithm=MD5, uri=\"sip:66621@172.16.0.37;user=phone\", qop=auth, nc=00000001, cnonce=\"b70b470bedf75db7\", nonce=\"1276678944:394f0b0b049fbbda8c94ae28d08f2301\", response=\"561389d4ce5cb38020749b8a27798343\"");
 *	params.put("headerToAdd", "Authorization");
 *	}
 *
 * @Test
 * @ContextParamMap("aContextMap")
 * public void testMethod()
 * }
 * </pre>
 * @author gvagenas@gmail.com 
 * 
 */


@Documented
@Retention(RUNTIME)
@Target({ElementType.METHOD, ElementType.FIELD})
public @interface ContextParamMap {

	String value();
}
