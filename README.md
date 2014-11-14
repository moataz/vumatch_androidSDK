VuMatch Android SDK
==================

Android SDK for making calls to the VuMatch API.

#Getting Started

*    Download [VuMatch SDK](https://developers.vufind.com/) to start developing.
*    Register VuMatch API to get your customer id, application key and application token.

#Integration Steps

Follow the following steps to use VuMatch SDK with any Android project.

*    Create your Android project and add a reference to VuMatchSDK project.

![VuMatch SDK](https://github.com/moataz/vumatch_androidSDK/blob/master/Tutorial/images/screen1.png)

*    Create VuMatchClient object to call VuMatch API.

```java
VuMatchClient vuClient = new VuMatchClient(context,CUSTOMER_ID, APP_KEY, APP_TOKEN);
```

This constructor takes four parameters: a valid Android context, your customer id, application key and application token.

*    Now you can post an image to VuMatch API by calling method postImage.

```java
vuClient.postImage(CATEGORY, imageUri,new VuMatchAPIAccessCallbackHandler() {

	@Override
	public void onSuccess(VuMatchRecommendation[] recommendationArray) {
		//Your image was posted successfully and an array of recommandations is returned
	}

	@Override
	public void onFailure(VuMatchAPIClientError error) {
		//Ab error has occurred and which is described by error object
	}
```

This method takes three parameters: category name, a content uri of the image to be sent to VuMatch API and a callback handler to receive response from postImage.

After this method finishes it calls either onSuccess or onFinish method of your callback handler object passed in the third parameter depending on the state returned. Those two methods will run in your main thread while postImage itself runs in the background so that you shouldn't wait until you receive a response.

In case of success you will get a list of VuMatch recommendations through the array passed to onSuccess method. Each object of the array will has two attributes (id, score) that you can read them by calling ```getId()``` and ```getScore()``` methods of each object.

In case of unexpected error occurred the method onFailure will be called with an object describes the error. Error has two attributes; code and message that you can read by call ```getErrorCode()``` and ```getErrorMessage()``` methods.

```java
@Override
public void onSuccess(VuMatchRecommendation[] recommendationArray) {
	for (int i = 0; i < recommendationArray.length; i++) {
		String id = recommendationArray[i].getId(); 
		float score = recommendationArray[i].getScore();
	}
}

@Override
public void onFailure(VuMatchAPIClientError error) {
	String code = error.getErrorCode();
	String message = error.getErrorMessage();
}
```

#Sample Code

You can download sample project that uses VuMatchSDK to post an image stored on device to VuMatch API and show a list of recommendations.

*    Import VuMatchSDK and VuMatch and VuMatchAPIClientSample projects into your workspace.
*    Import project appcompat_v7 from Android SDK into your workspace.
*    Open MainActivity.java and replace the four constant attributes (CUSTOMER_ID, APP_KEY, APP_TOKEN, CATEGORY) with your values.

```java
// replace the following 4 constants with your correct values
private static final String CUSTOMER_ID = "your cusromer id";
private static final String APP_KEY = "your application key";
private static final String APP_TOKEN = "your application token";
private static final String CATEGORY = "category name";
```

*    After you run this sample project you should see the following:


![VuMatch SDK](https://github.com/moataz/vumatch_androidSDK/blob/master/Tutorial/images/screen2.png)

*    Tap "Pick an Image" button to select an image.

```java
@Override
public void onClick(View v) {

	switch (v.getId()) {
	case R.id.button_pick_image:
		pickImage();
		break;

	default:
		break;
	}
}

private void pickImage() {
	Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	intent.setType("image/*");
	startActivityForResult(intent, REQUEST_CODE_PICKUP_PHOTO);
}
```

*    After you select the image the sample will post it to VuMatch API and show the list of recommendations.

```java
@Override
protected void onActivityResult(int requestCode, int resultCode,
		Intent intent) {
	super.onActivityResult(requestCode, resultCode, intent);

	if (requestCode == REQUEST_CODE_PICKUP_PHOTO) {

		if (resultCode == RESULT_OK) {
			progressProcessing.setVisibility(View.VISIBLE);
			listRecommendations.setVisibility(View.GONE);
			Uri imageUri = intent.getData();
			if (imageUri != null) {
				VuMatchClient vuClient = new VuMatchClient(this,
						CUSTOMER_ID, APP_KEY, APP_TOKEN);
				buttonPickImage.setEnabled(false);
				vuClient.postImage(CATEGORY, imageUri,
						new VuMatchAPIAccessCallbackHandler() {

							@Override
							public void onSuccess(
									VuMatchRecommendation[] recommendationArray) {
								listAdapter.clear();
								for (int i = 0; i < recommendationArray.length; i++) {
									listAdapter.add(String.format(
											"%1$s : %2$f",
											recommendationArray[i].getId(),
											recommendationArray[i]
													.getScore()));
								}
								listAdapter.notifyDataSetChanged();
								progressProcessing.setVisibility(View.GONE);
								listRecommendations.setVisibility(View.VISIBLE);
								buttonPickImage.setEnabled(true);
							}

							@Override
							public void onFailure(
									VuMatchAPIClientError error) {
								Log.e(TAG, "Error: " + error.toString());
								Toast.makeText(MainActivity.this,
										"Error occured", Toast.LENGTH_LONG)
										.show();
								progressProcessing.setVisibility(View.GONE);
								listRecommendations.setVisibility(View.GONE);
								buttonPickImage.setEnabled(false);
							}
						});
			}
		}
	}
}
```

![VuMatch SDK](https://github.com/moataz/vumatch_androidSDK/blob/master/Tutorial/images/screen3.png)