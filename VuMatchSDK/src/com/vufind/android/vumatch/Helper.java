package com.vufind.android.vumatch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings.Secure;

class Helper {
	
	protected static String getImagePath(Uri imageUri, Context context) {
		String scheme = imageUri.getScheme();
		if (scheme.equals("file"))
			return imageUri.getPath();
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(imageUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null)
				cursor.close();
		}
	   
	}
	
	protected static Uri scaleDown(Uri imageUri, int targetSize, Context context) {		
		System.gc();
		String imagePath = getImagePath(imageUri, context);
		Bitmap currentImage = BitmapFactory.decodeFile(imagePath);				
		
		int targetWidth = targetSize;
		int targetHeight = targetSize;
		
		int width = currentImage.getWidth();
		int height = currentImage.getHeight();
		
		if (width < targetWidth || height < targetHeight) {
			currentImage.recycle();
			currentImage = null;
			System.gc();
			return Uri.parse(imageUri.toString());
		}			
		
		height = (int)(height * (float)targetWidth/width);
		
		Bitmap scaledBitmap = Bitmap.createScaledBitmap(currentImage, targetWidth, height, false);
		
		if (currentImage != scaledBitmap) {
			currentImage.recycle();
			currentImage = null;
		}
		System.gc();
		File imageFile;
		try {			
			imageFile = new File(context.getCacheDir(), "vumatch-upload-00.jpeg");
			FileOutputStream output;
			
			output = new FileOutputStream(imageFile);
			boolean result = scaledBitmap.compress(CompressFormat.JPEG, 90, output);
			if (result) {
				scaledBitmap.recycle();
				scaledBitmap = null;
				return Uri.fromFile(imageFile);			
			} else {
				return null;
			}
		} catch (FileNotFoundException e) {			
			e.printStackTrace();
		}
		
		return null;
	}
	
	protected static String getFileKeyName (String extension, Context context) {
		String keyName = null;
		
		keyName = String.valueOf(System.currentTimeMillis());
		String deviceId = Secure.getString(context.getContentResolver(),
                Secure.ANDROID_ID); 
		keyName = deviceId + "_" + keyName + "." + extension;
		
		return keyName;
	}
}
