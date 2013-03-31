#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package};

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.sip.SipServlet;
import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;

import org.apache.log4j.Logger;

/**
 * Simple SipServlet
 * 
 * @author <a href="mailto:gvagenas@gmail.com">gvagenas</a>
 */
@javax.servlet.sip.annotation.SipServlet(loadOnStartup=1, applicationName="SimpleSipServletApplication")
public class SimpleSipServlet extends SipServlet {

	private static final long serialVersionUID = -7779271585280911979L;
	private static Logger logger = Logger.getLogger(SimpleSipServlet.class);
	
	@Override
	protected void doInvite(SipServletRequest req) throws ServletException,IOException {
		logger.info("INVITE message received: "+req.toString());
		
		logger.info("Sending 100 TRYING");
		SipServletResponse trying = req.createResponse(SipServletResponse.SC_TRYING);
		trying.send();
		
		logger.info("Sending 180 RINGING");
		SipServletResponse ringing = req.createResponse(SipServletResponse.SC_RINGING);
		ringing.send();
		
		logger.info("Sending 200 OK");
		SipServletResponse ok = req.createResponse(SipServletResponse.SC_OK);
		ok.send();
	}
	
	@Override
	protected void doAck(SipServletRequest req) throws ServletException, IOException {
		logger.info("ACK received: "+req.toString());
		super.doAck(req);
	}
	
	@Override
	protected void doBye(SipServletRequest req) throws ServletException, IOException {
		logger.info("BYE received: "+req.toString());
		SipServletResponse ok = req.createResponse(SipServletResponse.SC_OK);
		ok.send();
	}
}
