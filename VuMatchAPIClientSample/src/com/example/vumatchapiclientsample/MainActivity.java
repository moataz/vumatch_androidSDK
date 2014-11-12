package com.example.vumatchapiclientsample;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.vufind.android.vumatch.VuMatchAPIAccessCallbackHandler;
import com.vufind.android.vumatch.VuMatchAPIClientError;
import com.vufind.android.vumatch.VuMatchClient;
import com.vufind.android.vumatch.VuMatchRecommendation;

public class MainActivity extends ActionBarActivity implements OnClickListener {

	private static final String TAG = "VuMatch API Client Sample";
	private static final int REQUEST_CODE_PICKUP_PHOTO = 1;
	
	// replace the following 4 constants with your correct values
	private static final String CUSTOMER_ID = "your cusromer id";
	private static final String APP_KEY = "your application key";
	private static final String APP_TOKEN = "your application token";
	private static final String CATEGORY = "category name";

	private ListView listRecommendations;
	private Button buttonPickImage;
	private ProgressBar progressProcessing;
	private ArrayAdapter<String> listAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonPickImage = (Button) findViewById(R.id.button_pick_image);
		listRecommendations = (ListView) findViewById(R.id.list_recommendations);
		progressProcessing = (ProgressBar) findViewById(R.id.progress_processing);

		listAdapter = new ArrayAdapter<String>(this, R.layout.list_item);
		
		listRecommendations.setAdapter(listAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		// TODO Auto-generated method stub
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
}
