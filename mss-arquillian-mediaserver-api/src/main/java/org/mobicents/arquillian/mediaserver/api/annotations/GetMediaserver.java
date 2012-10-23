package org.mobicents.arquillian.mediaserver.api.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;


/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GetMediaserver {

}
