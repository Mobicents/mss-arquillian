/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
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
package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_7;

import java.io.IOException;
import java.util.UUID;

import org.apache.catalina.startup.Constants;
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.mobicents.servlet.sip.startup.SipContextConfig;
import org.mobicents.servlet.sip.startup.SipStandardContext;

/**
 * A custom {@link ContextConfig} for use in the Embedded Tomcat
 * container integration for Arquillian.
 *
 * <p>This configuration adds processing of the META-INF/context.xml
 * descriptor in the web application root when the context is started.
 * This implementation also marks an unpacked WAR for deletion when
 * the context is stopped.</p>
 *
 * @author Dan Allen
 * @author George Vagenas (gvagenas@gmail.com)
 */
public class EmbeddedContextConfig extends SipContextConfig
{
	/**
	 * Initialize the context config so to disable processing of the default
	 * global web.xml.  As an embedded container we lack the stock config file
	 * compliment.
	 */
	public EmbeddedContextConfig()
	{
		super();

		setDefaultWebXml(Constants.NoDefaultWebXml);
	}

	/**
	 * Override to apply the equivalent of the stock
	 * "$CATALINA_BASE/conf/web.xml" to contexts programmatically.
	 */
	@Override
	protected synchronized void beforeStart()
	{
		super.beforeStart();

		((SipStandardContext)context).setJ2EEServer("MSS-Arquillian-7-" + UUID.randomUUID().toString());
		Tomcat.initWebappDefaults(context);
	};

	/**
	 * Overridde to assign an internal field that will trigger the removal
	 * of the unpacked WAR when the context is closed.
	 */
	@Override
	protected void fixDocBase() throws IOException
	{
		super.fixDocBase();
		// If this field is not null, the unpacked WAR is removed when
		// the context is closed. This is normally used by the antiLocking
		// feature, though it should have been the normal behavior, at
		// least for an embedded container.
		originalDocBase = context.getDocBase();
	}

}
