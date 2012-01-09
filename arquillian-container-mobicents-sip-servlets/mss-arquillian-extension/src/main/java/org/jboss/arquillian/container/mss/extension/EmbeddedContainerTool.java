/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.container.SipServletsEmbeddedContainer;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.EmbeddedContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * Helper tool to provide the EmbeddedContainer
 * 
 * @author gvagenas@gmail.com 
 * 
 */
public class EmbeddedContainerTool {

//	private DeployableContainer<?> deployableContainer;
	
	public void setEmbedded(Object testInstance, DeployableContainer<?> deployableContainer, List<Field> fields) throws IllegalArgumentException, IllegalAccessException {

		for (Field field : fields) {
			Boolean flag = field.isAccessible();
			field.setAccessible(true);
			//Throws IllegalArgumentException cause we don't have access to the TestClass instance
			SipServletsEmbeddedContainer fieldValue = (SipServletsEmbeddedContainer) field.get(testInstance);
			if (fieldValue == null){
				// If value is null then set it to the appropriate Embedded Container. 
				field.set(testInstance, getEmbedded(deployableContainer));
			} else {
				//if value is null then set it to NULL. 
				field.set(testInstance, null);
			}
			field.setAccessible(flag);
		
		}
		
//		Field[] fieldss = testClass.getJavaClass().getDeclaredFields();
//		for (Field field : fieldss) {
//			if (field.isAnnotationPresent(EmbeddedContainer.class)){
//				if (field.getType().isAssignableFrom(SipServletsEmbeddedContainer.class)){
//					Boolean flag = field.isAccessible();
//					field.setAccessible(true);
//					//Throws IllegalArgumentException cause we don't have access to the TestClass instance
//					SipServletsEmbeddedContainer fieldValue = (SipServletsEmbeddedContainer) field.get(testInstance);
//					if (fieldValue == null){
//						// If value is null then set it to the appropriate Embedded Container. 
//						field.set(testInstance, getEmbedded(deployableContainer));
//					} else {
//						//if value is null then set it to NULL. 
//						field.set(testInstance, null);
//					}
//					field.setAccessible(flag);
//				}
//			}
//		}
	}
	
	private SipServletsEmbeddedContainer getEmbedded(DeployableContainer<?> deployableContainer) throws IllegalArgumentException, IllegalAccessException {

		SipServletsEmbeddedContainer embedded = null;

		Field[] fields = deployableContainer.getClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.getType().isAssignableFrom(SipServletsEmbeddedContainer.class)){
				Boolean flag = field.isAccessible();
				field.setAccessible(true);
				embedded = (SipServletsEmbeddedContainer) field.get(deployableContainer);
				field.setAccessible(flag);
			}
		}

		return embedded;
	}
	
}
