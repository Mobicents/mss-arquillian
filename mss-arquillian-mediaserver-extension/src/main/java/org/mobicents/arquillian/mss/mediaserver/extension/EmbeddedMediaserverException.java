package org.mobicents.arquillian.mss.mediaserver.extension;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class EmbeddedMediaserverException extends RuntimeException {
	
	private static final long serialVersionUID = 3365070216072279685L;
	
	private String message;
	
	public EmbeddedMediaserverException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	
	
	
}
