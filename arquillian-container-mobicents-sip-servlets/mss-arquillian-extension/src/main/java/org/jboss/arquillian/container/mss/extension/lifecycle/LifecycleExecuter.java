/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.arquillian.container.mss.extension.lifecycle;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.SipServletsEmbeddedContainer;
import org.jboss.arquillian.container.mss.extension.EmbeddedContainerTool;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ConcurrencyControlMode;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParam;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParamMap;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.GetEmbeddedContainer;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterSetup;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

/**
 * LifecycleExecuter
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class LifecycleExecuter
{

	//Using these events, we can get access to the deployableContainer so we can start/stop, deploy/undeploy, 
	//setup with a new configuration and also we have access to the deployment

	public void executeBeforeDeploy(@Observes BeforeDeploy event, TestClass testClass)
	{
		execute(
				testClass.getMethods(
						org.jboss.arquillian.container.mss.extension.lifecycle.api.BeforeDeploy.class));
	}

	public void executeAfterDeploy(@Observes AfterDeploy event, TestClass testClass)
	{
		execute(
				testClass.getMethods(
						org.jboss.arquillian.container.mss.extension.lifecycle.api.AfterDeploy.class));
	}

	public void executeBeforeUnDeploy(@Observes BeforeUnDeploy event, TestClass testClass)
	{
		execute(
				testClass.getMethods(
						org.jboss.arquillian.container.mss.extension.lifecycle.api.BeforeUnDeploy.class));
	}

	public void executeAfterUnDeploy(@Observes AfterUnDeploy event, TestClass testClass)
	{
		execute(
				testClass.getMethods(
						org.jboss.arquillian.container.mss.extension.lifecycle.api.AfterUnDeploy.class));
	}

		/*
		 * #2 Use @BeforeClass event in order to scan the class for annotation we might be interesting.
		 */
		public void BeforeThat(@Observes BeforeClass event, TestClass testClass){
			testClass = event.getTestClass();
			Field[] fields = testClass.getJavaClass().getDeclaredFields();
			for (Field field : fields) {
				if (field.isAnnotationPresent(GetEmbeddedContainer.class)){
					if (field.getType().isAssignableFrom(SipServletsEmbeddedContainer.class)){
						isGetEmbeddedContainerAnnoPresent = true;
						embeddedContainerFields.add(field);
					}
				}
			}
		}

	private DeployableContainer<?> deployableContainer;
	private Object testInstance;

	/*
	 * #1 Observe Container's AfterSetup to get the DeployableContainer 
	 */
	public void executeAfterSetup(@Observes AfterSetup event){
		deployableContainer = event.getDeployableContainer();
	}


//		public void beforeThis(@Observes AfterDeploy event, DeploymentDescription deployment){
//			event.getDeployableContainer();
//		}



	/*
	 * Remember that for the ContainerController to be able to control the container, the container have to be  
	 * defined with mode="manual" at the arquillian.xml
	 * 
	 * <container qualifier="mss-tomcat-embedded-6" default="true" mode="manual">
	 *	<configuration>
	 *	.....
	 *	</configuration>
	 * 
	 */

	@Inject
	private Instance<ArquillianDescriptor> descriptor;
	@Inject
	private Instance<ContainerController> containerController;

	private Annotation contextParam; 
	private Annotation contextParamMap; 
	private Annotation concurencyControl;
	private boolean isGetEmbeddedContainerAnnoPresent = false;
	private List<Field> embeddedContainerFields = new ArrayList<Field>();
	
	
	private EmbeddedContainerTool embeddedContainerTool; 
	
	// #3
	public void executeBeforeTest(@Observes Before event, TestClass testClass) throws IllegalArgumentException, IllegalAccessException{

		testInstance = event.getTestInstance();

		Map<String, String> parameters = new HashMap<String, String>();
		List<ContainerDef> containerDefs = descriptor.get().getContainers();
		Iterator<ContainerDef> iter = containerDefs.iterator();
		
		if (checkForAnnotations(event))
			parameters = getParameters(event);

		while (iter.hasNext()){
			ContainerDef containerDef = (ContainerDef) iter.next();
			String containerName = containerDef.getContainerName();
			if (parameters.isEmpty()){
				containerController.get().start(containerName);
			} else {
				containerController.get().start(containerName,parameters);
			}
		}

		if (isGetEmbeddedContainerAnnoPresent){
			embeddedContainerTool = new EmbeddedContainerTool();
			embeddedContainerTool.setEmbedded(testInstance, deployableContainer, embeddedContainerFields);
		}
	}

	public void executeAfterTest(@Observes After event, TestClass testClass) throws IllegalArgumentException, IllegalAccessException{

		List<ContainerDef> containerDefs = descriptor.get().getContainers();
		Iterator<ContainerDef> iter = containerDefs.iterator();

		while (iter.hasNext()){
			ContainerDef containerDef = (ContainerDef) iter.next();
			String containerName = containerDef.getContainerName();
			containerController.get().stop(containerName);
		}
		//		if (setEmbeddedContainer){
		//			tool.setEmbedded(testInstance, testClass, deployableContainer);
		//		}
	}

	private boolean checkForAnnotations(Before event) {
		Boolean result = false;

		contextParam = event.getTestMethod().getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParam.class);
		contextParamMap = event.getTestMethod().getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParamMap.class);
		concurencyControl = event.getTestMethod().getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ConcurrencyControlMode.class);

		if (contextParam != null || contextParamMap != null || concurencyControl != null)
			result = true;

		return result;
	}

	private Map<String, String> getParameters(Before event) {

		String contextParamName = null;
		String contextParamValue = null;

		Map<String, String> parameters = new HashMap<String, String>();
		String paramSeparator = "---";
		String valueSeparator = "-%%-";		
		parameters.put("paramSeparator",paramSeparator);
		parameters.put("valueSeparator", valueSeparator);

		HashMap<String, String> paramMap = new HashMap<String, String>();

		if (contextParamMap != null){
			String mapName = ((ContextParamMap)contextParamMap).value();
			Field[] fields = event.getTestClass().getJavaClass().getDeclaredFields(); 
			for (Field field : fields) {
				if (field.getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParamMap.class) != null 
						&& field.getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParamMap.class).value().equals(mapName) ){
					try {
						Boolean flag = field.isAccessible();
						field.setAccessible(true);
						paramMap = (HashMap<String, String>) field.get(event.getTestInstance());
						field.setAccessible(flag);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				} 
			}

			//Throw exception if after scanning all the fields we don't find the map we are looking for.
			if (paramMap.isEmpty())
				throw new UnsupportedOperationException("There is no declared field in the class with annotation " +
						"@ContextParamΜap(\""+mapName+"\") as in the test method: "+event.getTestMethod().getName());
		}

		StringBuffer values = new StringBuffer();

		if (contextParam != null){
			contextParamName = ((ContextParam)contextParam).name();
			contextParamValue = ((ContextParam)contextParam).value();
			values.append(contextParamName+valueSeparator+contextParamValue+paramSeparator);
		}
		if (!paramMap.isEmpty()){
			Iterator<String> iterator = paramMap.keySet().iterator();

			String firstKey = iterator.next();
			values.append(firstKey+valueSeparator+paramMap.get(firstKey));

			while(iterator.hasNext()){
				String key = iterator.next();
				values.append(paramSeparator+key+valueSeparator+paramMap.get(key));
			}
		}

		parameters.put("contextParam", values.toString());

		if(concurencyControl != null){
			parameters.put("concurrencyControl", ((ConcurrencyControlMode)concurencyControl).value().toString());
		}

		return parameters;
	}

	private void execute(Method[] methods)
	{
		if(methods == null)
		{
			return;
		}
		for(Method method : methods)
		{
			try
			{
				method.invoke(null);
			}
			catch (Exception e) 
			{
				throw new RuntimeException("Could not execute method: " + method, e);
			}
		}
	}
}
