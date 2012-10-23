package org.mobicents.arquillian.mss.mediaserver.extension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mediaserver.api.annotations.GetMediaserver;

/**
 * MediaServerProducer is used along with @GetMediaserver annotation in order to inject a Mediaserver
 * instance to the test class
 * 
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class MediaServerProducer {

	private Boolean isGetMediaserverAnnotationPresent = false;
	private List<Field> deployableContainerFields = new ArrayList<Field>();
	private Object testInstance;
	
	/*
	 * Use @BeforeClass event in order to scan the test class for annotation we might be interesting.
	 * Event fired Before the Class execution.
	 */
	public void executeBeforeClass(@Observes BeforeClass event, TestClass testClass){
		testClass = event.getTestClass();
		Field[] fields = testClass.getJavaClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(GetMediaserver.class)){
				if (field.getType().isAssignableFrom(EmbeddedMediaserver.class)){
					isGetMediaserverAnnotationPresent = true;
					deployableContainerFields.add(field);
				}
			}
		}
	}
	
	// Event fired before the execution of a test
	public void executeBeforeTest(@Observes Before event, TestClass testClass) throws IllegalArgumentException, IllegalAccessException
	{
		testInstance = event.getTestInstance();
		if (isGetMediaserverAnnotationPresent) {
			EmbeddedMediaserver embeddedMediaserver = new EmbeddedMediaserverImpl();
			setMediaserver(testInstance, embeddedMediaserver, deployableContainerFields);
		}
	}

	public void setMediaserver(Object testInstance, EmbeddedMediaserver embeddedMediaserver, List<Field> fields) throws IllegalArgumentException, IllegalAccessException
	{
		for (Field field : fields) {
			Boolean flag = field.isAccessible();
			field.setAccessible(true);
			//Throws IllegalArgumentException cause we don't have access to the TestClass instance
			EmbeddedMediaserver fieldValue = (EmbeddedMediaserver) field.get(testInstance);
			if (fieldValue == null){
				// If value is null then set it to the appropriate Embedded Container. 
				field.set(testInstance, embeddedMediaserver);
			} else {
				//if value is not null then set it to NULL. 
				field.set(testInstance, null);
			}
			field.setAccessible(flag);

		}
	}
	
}
