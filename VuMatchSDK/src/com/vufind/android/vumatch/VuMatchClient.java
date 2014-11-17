package com.vufind.android.vumatch;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * This class contains all functionality needed to contact VuMatch API
 * @author Vufind
 * copyright (c) 2014 Vufind Inc. All Rights Reserved.
 *
 * Permission is granted to use and modify this software to vuMatch beta and premium customers as long as they abide by Vufind's 
 * current terms of service and Vufind's  eCommerce Conversion Optimization enterprise contract agreement.
 *
 */
public class VuMatchClient {
	
	private static final int IMAGE_SCALE_SIZE = 320; 
	
	private String customerId;
	private String appKey;
	private String appToken;
	private VuMatchAPIAccessCallbackHandler callback;
	private String category;
	private Uri imageUri;
	private WeakReference<Context> context;

	
	/**
	 * Construct a client object used to connect to VuMatch API
	 * @param context valid Android context
	 * @param customerId your customer id resistered to VuMatch API
	 * @param appKey your application key resistered to VuMatch API
	 * @param appToken your application token resistered to VuMatch API
	 */
	public VuMatchClient(Context context, String customerId, String appKey,
			String appToken) {
		this.context = new WeakReference<Context>(context.getApplicationContext());
		this.customerId = customerId;
		this.appKey = appKey;
		this.appToken = appToken;
	}

	/**
	 * Post an image to VuMatch API and get a list of recommendations
	 * @param category product category
	 * @param imageUri Uri to the image to be uploaded
	 * @param callback callback handler to receive response from VuMatch client while processing your request
	 */
	public void postImage(String category, Uri imageUri,
			VuMatchAPIAccessCallbackHandler callback) {
		this.category = category;		
		this.callback = callback;
		this.imageUri = imageUri;
		this.imageUri = Helper.scaleDown(imageUri, IMAGE_SCALE_SIZE, context.get());		
		Thread thread = new Thread(new APIAccessRunnable());
		thread.start();
	}

	private static class S3Client {
		private static final String BUCKET_NAME = "vufind.shop";
		
		protected static String generateImageURL(String fileKey) {
			return String.format("http://%1$s.s3.amazonaws.com/%2$s", S3Client.BUCKET_NAME, fileKey);
		}
		
		protected static void uploadFile(String key, Uri imageUri,
				Context context) throws AmazonServiceException,
				AmazonClientException {
			new AmazonS3Client((AWSCredentials) null);
			AmazonS3Client s3Client = new AmazonS3Client((AWSCredentials) null);

			PutObjectRequest por = new PutObjectRequest(BUCKET_NAME, key,
					new java.io.File(Helper.getImagePath(imageUri, context)));			
			try {
				s3Client.putObject(por);
			} catch (AmazonServiceException ex) {
				throw ex;
			} catch (AmazonClientException ex) {
				throw ex;
			}
		}
	}

	private class APIAccessRunnable implements Runnable {

		private static final String TAG = "VuMatch API Access Runnable";
		private static final String API_URI = "http://api3.vufind.com/vumatch/vumatch_getscores.php?customer_id=%1$s&cat=%2$s&url=%3$s&app_key=%4$s&token=%5$s";

		@Override
		public void run() {

			MimeTypeMap mime = MimeTypeMap.getSingleton();
			String fileExt = mime.getExtensionFromMimeType(context.get()
					.getContentResolver().getType(
							VuMatchClient.this.imageUri));
			fileExt = fileExt != null ? fileExt : "jpeg";
			String fileKey = Helper.getFileKeyName(fileExt, context.get());
			try {
				S3Client.uploadFile(fileKey, VuMatchClient.this.imageUri,
						context.get());
			} catch(AmazonServiceException ex) {
				VuMatchAPIClientError error = new VuMatchAPIClientError("1010", context.get().getString(R.string.error_1010));
				callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
				return;
			} catch(AmazonClientException ex) {
				VuMatchAPIClientError error = new VuMatchAPIClientError("1020", context.get().getString(R.string.error_1020));
				callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
				return;
			}
			String imageUrl = S3Client.generateImageURL(fileKey);
			try {
				String url = String.format(API_URI, URLEncoder.encode(
						VuMatchClient.this.customerId, "utf-8"), URLEncoder
						.encode(VuMatchClient.this.category, "utf-8"),
						URLEncoder.encode(imageUrl, "utf-8"), URLEncoder.encode(
								VuMatchClient.this.appKey, "utf-8"), URLEncoder
								.encode(VuMatchClient.this.appToken, "utf-8"));
				URL api;
				api = new URL(url);
				HttpURLConnection conn = (HttpURLConnection) api
						.openConnection();

				conn.setDoOutput(true);
				conn.setUseCaches(false);

				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e(TAG, "Invalid response code!");
					VuMatchAPIClientError error = new VuMatchAPIClientError("2020", context.get().getString(R.string.error_2020));										
					callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
					return;
				}
				int bufferSize = 1024;
				StringBuffer b = new StringBuffer();
				InputStream is = conn.getInputStream();
				byte[] data = new byte[bufferSize];
				int leng = -1;
				while ((leng = is.read(data)) != -1) {
					b.append(new String(data, 0, leng));
				}
				is.close();
				conn.disconnect();
				String result = b.toString();
				result = result.trim();
				VuMatchRecommendation[] recommendations = parseResonse(result);

				if (recommendations != null) {
					callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_SUCCESS, recommendations).sendToTarget();
				} else {
					VuMatchAPIClientError error = new VuMatchAPIClientError("2020", context.get().getString(R.string.error_2020));										
					callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
				}

			} catch (MalformedURLException e) {
				VuMatchAPIClientError error = new VuMatchAPIClientError("2020", context.get().getString(R.string.error_2020));										
				callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
				e.printStackTrace();
			} catch (IOException e) {
				VuMatchAPIClientError error = new VuMatchAPIClientError("2010", context.get().getString(R.string.error_2010));										
				callback.obtainMessage(VuMatchAPIAccessCallbackHandler.MESSAGE_FAILURE, error).sendToTarget();
				e.printStackTrace();
			}
			
			callback = null;
		}

		private VuMatchRecommendation[] parseResonse(String response) {

			response = response.trim();	
			VuMatchRecommendation[] recommendations;
			try {
				JSONObject json = new JSONObject(response);
				boolean status = json.optBoolean("Status");
				if (status) {
					String recommendationsJSONArrayString = json.getJSONObject("Data")
							.optString("VufindRecommends");
					if (recommendationsJSONArrayString == null)
						return null;
					recommendationsJSONArrayString = recommendationsJSONArrayString.replace("\\", "");
					JSONArray recommendationsJSONArray = new JSONArray(recommendationsJSONArrayString);
					recommendations = new VuMatchRecommendation[recommendationsJSONArray.length()];
					for (int i = 0; i < recommendationsJSONArray.length(); i++) {
						JSONObject item = recommendationsJSONArray.getJSONObject(i);
						recommendations[i] = new VuMatchRecommendation(item.optString("id"), (float)item.optDouble("score"));
					}
					return recommendations;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

	}
}
