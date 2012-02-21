/**
 * 
 */
package org.mobicents.servlet.sip.arquillian.showcase;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */
@javax.servlet.sip.annotation.SipServlet(loadOnStartup=1, applicationName="SimpleSipApplication")
public class SimpleSipServlet extends SipServlet  
{
	private Logger logger = Logger.getLogger(SimpleSipServlet.class);
	private static final long serialVersionUID = -214960970071316544L;
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,IOException 
	{
		logger.info("Got an INVITE request: "+req);
		req.createResponse(100).send();
		req.createResponse(180).send();
		req.createResponse(200).send();
	}
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException,IOException 
	{
		logger.info("Got a BYE request: "+req);
		req.createResponse(200).send();
	}
	
	@Override
	protected void doRegister(SipServletRequest req) throws ServletException,IOException 
	{
		logger.info("Got a REGISTER request: "+req);
		req.createResponse(200).send();
	}
}
