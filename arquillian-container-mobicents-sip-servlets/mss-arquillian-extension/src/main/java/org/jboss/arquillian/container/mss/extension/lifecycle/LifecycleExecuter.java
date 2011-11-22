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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.config.descriptor.api.ContainerDef;
import org.jboss.arquillian.container.impl.ContainerImpl;
import org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParam;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.container.DeploymentException;
import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.arquillian.container.spi.event.SetupContainer;
import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.impl.ManagerImpl;
import org.jboss.arquillian.core.spi.Manager;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Configuration;
import org.jboss.shrinkwrap.descriptor.api.Descriptor;

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


	public void executeBeforeDeploy(@Observes BeforeDeploy event, TestClass testClass) throws LifecycleException
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

	@Inject
	private Instance<ArquillianDescriptor> descriptor;
//	@Inject
//	private Instance<DeployableContainer<?>> deployableContainer;
	@Inject
	private Instance<Manager> manager;
	@Inject
	private Instance<ContainerImpl> containerImpl;
//	@ArquillianResource
//	private Container container;

	
	public void executeBeforeTest(@Observes Before event, TestClass testClass){

		String contextParamName = null;
		String contextParamValue = null;
		Annotation contextParam = event.getTestMethod().getAnnotation(org.jboss.arquillian.container.mss.extension.lifecycle.api.ContextParam.class);
		if (contextParam != null){
			contextParamName = ((ContextParam)contextParam).name();
			contextParamValue = ((ContextParam)contextParam).value();
			
			List containerDefs = descriptor.get().getContainers();
			Iterator iter = containerDefs.iterator();
			
			while (iter.hasNext()){
				ContainerDef containerDef = (ContainerDef) iter.next();
				containerDef.property("contextParamName", contextParamName);
				containerDef.property("contextParamValue", contextParamValue);
			}
		}
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
