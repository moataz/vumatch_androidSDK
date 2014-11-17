package com.vufind.android.vumatch;
/**
 * Class describes errors that occurs while contacting VuMatch API
 * @author Vufind
 *
 */
public class VuMatchAPIClientError {
	
	private String errorCode;
	private String errorMessage;
	
	protected VuMatchAPIClientError(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	
	/**
	 * Get error code
	 * @return a string represents error code
	 */
	public String getErrorCode() {
		return errorCode;
	}
	
	protected void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
	 * Get a message that describes the error
	 * @return a string of message
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	protected void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	@Override
	public String toString() {		
		return String.format("Error %1$s: %2$s", errorCode, errorMessage);
	}
}
