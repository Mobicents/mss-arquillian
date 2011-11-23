/**
 * 
 */
package org.jboss.arquillian.container.mss.extension;

import java.util.HashMap;

/**
 * Helper class to create a map of context parameters
 * 
 * @author gvagenas@gmail.com 
 * 
 */
public class ContextParamTool {
	
	private HashMap<String, String> contextMap;
	
	public ContextParamTool() {
		contextMap = new HashMap<String, String>();
	}
	
	public ContextParamTool put(String key, String value){
		contextMap.put(key, value);
		return this;
	}

	public HashMap<String, String> getMap(){
		return contextMap;
	}
}
