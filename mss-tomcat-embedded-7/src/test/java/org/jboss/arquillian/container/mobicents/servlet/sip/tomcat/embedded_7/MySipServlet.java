package org.jboss.arquillian.container.mobicents.servlet.sip.tomcat.embedded_7;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */
@javax.servlet.sip.annotation.SipServlet(loadOnStartup=1, applicationName="org.mobicents.servlet.sip.arquillian.testsuite.SimpleApplication")
public class MySipServlet extends SipServlet {

	private static final long serialVersionUID = 8597921677937746570L;

	Logger logger = Logger.getLogger(MySipServlet.class);
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException, IOException {
		logger.info("INVITE req received FROM: "+req.getFrom().toString());
		SipServletResponse resp = req.createResponse(200);
		resp.send();
	}
	
}
