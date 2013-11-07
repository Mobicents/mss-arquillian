package org.mobicents.arquillian.mediaserver.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import org.mobicents.arquillian.mediaserver.api.MediaserverConfMode;


/**
 * Annotation to define Embedded Mediaserver. The Annotation has 4 parameters to define endpoints and ID.
 * 
 * If the Annotation is defined in the Class or Method level, you loose control over it, 
 * the Mediaserver will be started/stopped automatically.
 * 
 * 
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface Mediaserver {
	
	//Define if the server will be in auto or manual configuration mode. - Availlable only when annoation is defined in Field level -
	MediaserverConfMode ConfMode() default MediaserverConfMode.AUTO;
	
	//Number of endpoints and default values
	int IVR() default 2;
	int CONF() default 2;
	int RELAY() default 2;
	int BRIDGE() default 2;
	
	//Id of the server
	String ID() default "Embedded Mediaserver";
}
