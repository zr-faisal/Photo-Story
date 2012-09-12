package com.photostory.utl;

import android.util.DisplayMetrics;

public class AppConstant {

//	public static final int MAX_INPUT_FOODNAME_LENGTH = 25;
//	public static final int MAX_INPUT_PRICE_LENGTH = 8;
//	public static final int MAX_INPUT_COMMENT_LENGTH = 40;
	
	 public static final String API_URL = "localhost/img_upload.php";

	public static final int PREFER_SCREEN_WIDTH = 480;
	public static final int PREFER_SCREEN_HEIGHT = 800;

	public static final int PREFER_DENSITY = 196;

	public static int RUNTIME_SCREEN_WIDTH;
	public static int RUNTIME_SCREEN_HEIGHT;

	public static float ratio;

	private static boolean initialed = false;

	public static void initializeStaticVariable(DisplayMetrics display) {

		if (initialed)   
			return;
		initialed = true;

		RUNTIME_SCREEN_WIDTH = display.widthPixels;
		RUNTIME_SCREEN_HEIGHT = display.heightPixels;
		
		if(RUNTIME_SCREEN_WIDTH>800)
			RUNTIME_SCREEN_WIDTH=800;

		ratio = RUNTIME_SCREEN_WIDTH * 1.0f / PREFER_SCREEN_WIDTH;

//		int prefWidth = (int) (ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_WIDTH * 1.0f * ratio);
//		ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_WIDTH = prefWidth;
//		ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_HEIGHT = prefWidth;

	}
}
