/**
 * 
 */
package org.jboss.arquillian.container.mss.extension.authentication;


/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */

//TODO Implement

public class CheckAuthentication {
	
//	private static Logger logger = Logger.getLogger(CheckAuthentication.class);
//
//	public static boolean checkProxyAuthorization(Request request) {
//        // Let Acks go through unchallenged.
//        ProxyAuthorizationHeader proxyAuthorization=
//                (ProxyAuthorizationHeader)request.getHeader(ProxyAuthorizationHeader.NAME);
//
//       if (proxyAuthorization==null) {
//    	   logger.error("Authentication failed: ProxyAuthorization header missing!");     
//           return false;
//       }else{
//    	   
//    	   if(nextNonce != null && !proxyAuthorization.getNonce().equals(nextNonce)) {
//    		   throw new IllegalArgumentException("Authentication failed: ProxyAuthorization nonce " + proxyAuthorization.getNonce() + " is different from the nextnonce  previously generated " + nextNonce);         		   
//    	   }
//    	   
//    	   if(nc > 0 && proxyAuthorization.getNonceCount() != nc) {
//    		   throw new IllegalArgumentException("Authentication failed: ProxyAuthorization nonceCount " + proxyAuthorization.getNonceCount() + " is different from the nextnonce  previously generated " + nc);
//    	   }
//    	   
//           String username=proxyAuthorization.getParameter("username");
//           //String password=proxyAuthorization.getParameter("password");
//   
//           try{
//                boolean res=dsam.doAuthenticate(username,proxyAuthorization,request);
//                if (res) logger.info("Authentication passed for user: "+username);
//                else logger.error("Authentication failed for user: "+username); 
//                return res;
//           }
//           catch(Exception e) {
//                e.printStackTrace();
//                return false;
//           }     
//       } 
//    }

}
