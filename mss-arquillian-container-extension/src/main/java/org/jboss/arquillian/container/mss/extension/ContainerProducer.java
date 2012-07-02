/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.jboss.arquillian.container.mobicents.api.SipServletsEmbeddedContainer;
import org.jboss.arquillian.container.mobicents.api.annotations.GetDeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * Responsible to provide ContainerManagerTool (DeployableContainer wrapper) to the required test class fields
 * 
 * @author gvagenas@gmail.com 
 * 
 */
public class ContainerProducer {

	private boolean isGetDeployableContainerAnnoPresent = false;
	private List<Field> deployableContainerFields = new ArrayList<Field>();

	private DeployableContainer<?> deployableContainer;
	private Object testInstance;

	/*
	 * Use @BeforeClass event in order to scan the test class for annotation we might be interesting.
	 * Event fired Before the Class execution.
	 */
	public void executeBeforeClass(@Observes BeforeClass event, TestClass testClass){
		testClass = event.getTestClass();
		Field[] fields = testClass.getJavaClass().getDeclaredFields();
		for (Field field : fields) {
			if (field.isAnnotationPresent(GetDeployableContainer.class)){
				if (field.getType().isAssignableFrom(ContainerManagerTool.class)){
					isGetDeployableContainerAnnoPresent = true;
					deployableContainerFields.add(field);
				}
			}
		}
	}

	//Event fired After the DeployableContainer is setup.
	public void executeAfterSetup(@Observes AfterSetup event){
		deployableContainer = event.getDeployableContainer();
	}

	// Event fired before the execution of a test
	public void executeBeforeTest(@Observes Before event, TestClass testClass) throws IllegalArgumentException, IllegalAccessException
	{
		testInstance = event.getTestInstance();
		if (isGetDeployableContainerAnnoPresent) {
			ContainerManagerTool containerWrapper = new ContainerManagerTool(deployableContainer);
			setContainer(testInstance, containerWrapper, deployableContainerFields);
		}
	}

	public void setContainer(Object testInstance, ContainerManagerTool containerWrapper, List<Field> fields) throws IllegalArgumentException, IllegalAccessException
	{

		for (Field field : fields) {
			Boolean flag = field.isAccessible();
			field.setAccessible(true);
			//Throws IllegalArgumentException cause we don't have access to the TestClass instance
			SipServletsEmbeddedContainer fieldValue = (SipServletsEmbeddedContainer) field.get(testInstance);
			if (fieldValue == null){
				// If value is null then set it to the appropriate Embedded Container. 
				field.set(testInstance, containerWrapper);
			} else {
				//if value is null then set it to NULL. 
				field.set(testInstance, null);
			}
			field.setAccessible(flag);

		}
	}

}
