/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.util.Properties;
import java.util.logging.Logger;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;


/**
 * @author gvagenas@gmail.com
 * 
 */
public class SipStackToolStatic {

	private static SipStackTool instance;
	
	private SipStackToolStatic(){
	}

	public static SipStackTool getInstance(){
		if (instance == null){
			instance = new SipStackTool("static");
		} 
		return instance;
	}
	
}

