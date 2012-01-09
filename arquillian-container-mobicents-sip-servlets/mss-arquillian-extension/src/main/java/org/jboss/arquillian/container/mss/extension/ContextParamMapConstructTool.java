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
public class ContextParamMapConstructTool {
	
	private HashMap<String, String> contextMap;
	
	public ContextParamMapConstructTool() {
		contextMap = new HashMap<String, String>();
	}
	
	public ContextParamMapConstructTool put(String key, String value){
		contextMap.put(key, value);
		return this;
	}

	public HashMap<String, String> getMap(){
		return contextMap;
	}
}
