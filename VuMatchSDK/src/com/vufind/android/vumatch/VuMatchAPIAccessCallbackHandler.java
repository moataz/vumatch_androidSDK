package com.vufind.android.vumatch;

import android.os.Handler;
import android.os.Message;

/**
 * A class represents a callback handler that receives response from VuMatch client. You should extend this class and implement onSuccess and onFailure methods
 * @author Vufind
 * copyright (c) 2014 Vufind Inc. All Rights Reserved.
 *
 * Permission is granted to use and modify this software to vuMatch beta and premium customers as long as they abide by Vufind's 
 * current terms of service and Vufind's  eCommerce Conversion Optimization enterprise contract agreement.
 *
 *
 */
public abstract class VuMatchAPIAccessCallbackHandler extends Handler{
	protected static final int MESSAGE_SUCCESS = 0;
	protected static final int MESSAGE_FAILURE = 1;
	
	@Override
	public void handleMessage(Message msg) {		
		super.handleMessage(msg);
		switch (msg.what) {
		case MESSAGE_SUCCESS:
			this.onSuccess((VuMatchRecommendation[])msg.obj);
			break;
		case MESSAGE_FAILURE:
			this.onFailure((VuMatchAPIClientError)msg.obj);
			break;
		default:
			break;
		}
	}
	
	/**
	 * This function is called in case of success after contacting VuMatch API
	 * @param recommendationArray an array of recommendations returned from VuMatch API
	 */
	public abstract void onSuccess(VuMatchRecommendation[] recommendationArray);
	
	
	/**
	 * This function is called in case of failure after contacting VuMatch API
	 * @param error an error object with a code and message describes the cause of failure
	 */
	public abstract void onFailure(VuMatchAPIClientError error);
}
