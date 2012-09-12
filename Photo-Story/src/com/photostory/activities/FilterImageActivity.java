package com.photostory.activities;

import java.io.File;

import x.br.com.dina.ui.custom.activity.util.BitmapManager;
import x.br.com.dina.ui.custom.activity.util.Debug;
import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.network.l.Uploader;
import com.photostory.communication.AlternativeBackProcess;
import com.photostory.communication.AlternativeRequest;
import com.photostory.communication.AlternativeRequestReview;
import com.photostory.imagefilter.FilterBackprocess;
import com.photostory.imagefilter.FilterCallback;
import com.photostory.imageprocessing.Auto;
import com.photostory.imageprocessing.Brightness;
import com.photostory.imageprocessing.ColorFirst;
import com.photostory.imageprocessing.ColorSecond;
import com.photostory.imageprocessing.Saturation;
import com.photostory.imageprocessing.Sharpen;
import com.photostory.utl.ActionRequest;
import com.photostory.utl.AppConstant;
import com.photostory.utl.CameraAction;
import com.photostory.utl.FileCache;
import com.photostory.utl.ImageLoader;

public class FilterImageActivity extends Activity implements OnSeekBarChangeListener, FilterCallback, ActionRequest {
	
	private int PREVIEW_WIDTH;
	private int PREVIEW_HEIGHT;
	
	private int GIVEN_HEIGHT = getWindow().getWindowManager().getDefaultDisplay().getHeight();
	private int GIVEN_WIDTH = getWindow().getWindowManager().getDefaultDisplay().getWidth();
	
	private final int MIDDLE_SEEK_VALUE = 50;

	private View autoGroup;
	private View controllGroup;
	private View brightnessGroup;
	private View colorGroup;
	private View satulationGroup;
	private View backFilter;

	private SeekBar brightnessSeek;
	private SeekBar colorFirstSeek;
	private SeekBar colorSecondSeek;
	private SeekBar satulationSeek;

	private ImageView imagePreview;
	private Bitmap bitmapPreview;
	private Bitmap tmpResult;

	private int seekValue;
	private boolean applyBitmap;

	private Auto auto;
	private Sharpen sharpen;
	private Brightness bright;
	private Saturation satulation;
	private ColorFirst colorFirst;
	private ColorSecond colorSecond;

	private String imagePath;

	private String private_id = "322";
	private String upload_path = null;
	private boolean rotate;

	private View save;
	private View postnow;
	private View cancelfilterimage;
	boolean uploaded;
	private String category_id;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle b = getIntent().getExtras();
		category_id = b.getString("category_id");
		System.gc();
		setContentView(R.layout.filter_layout);
//		initFrame();
		initViewFromId();
		initImageProcessing();
		showGroup(autoGroup);

		private_id = getPrivateId() + "";
		uploaded = false;

		imagePath = getIntent().getStringExtra("image_url");
		rotate = getIntent().getBooleanExtra("rotate", false);
		Debug.debug(getClass(), "path = " + imagePath + " rot = " + rotate);

		// LinearLayout.LayoutParams scrollparam = new
		// LayoutParams(ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_WIDTH,
		// ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_HEIGHT);
		// imagePreview.setLayoutParams(scrollparam);

//		Debug.debug(getClass(), "thumbnail width = " + ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_WIDTH);
//		ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_HEIGHT = ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_WIDTH;
//		Debug.debug(getClass(), "thumbnail height = " + ThumbZoomHelper.LARGE_THUMBNAIL_IMAGE_HEIGHT);

		RelativeLayout.LayoutParams reParam = new RelativeLayout.LayoutParams(GIVEN_WIDTH, GIVEN_HEIGHT);
		RelativeLayout.LayoutParams linParam = new RelativeLayout.LayoutParams(GIVEN_WIDTH - 10, GIVEN_HEIGHT - 10);
		RelativeLayout previewBorder = (RelativeLayout) findViewById(R.id.previewBorder);
		reParam.addRule(RelativeLayout.CENTER_IN_PARENT, 1);
		linParam.addRule(RelativeLayout.CENTER_IN_PARENT, 1); 
		previewBorder.setLayoutParams(reParam);
		imagePreview.setLayoutParams(linParam); 

		loadBitmap();
		nofifyUpdatePreview(imagePreview, bitmapPreview);
	}

	private int getPrivateId() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		int id = Integer.parseInt(prefs.getString("private_id", "-1"));
		return id;
	}

//	private void initFrame() {
//		Header header = (Header) findViewById(R.id.layoutHeader);
//		header.setActivity(this);
//		header.setLayout(R.layout.top_bar_login);
//		TextView pageName = (TextView) findViewById(R.id.page_name);
//		pageName.setText(getResources().getString(R.string.filter_page_title));
//	}

	private void loadBitmap() {
		BitmapManager.safeRecycle(bitmapPreview);
		BitmapManager.safeRecycle(tmpResult);
		System.gc();
		if (imagePath != null) {
			File f = null;

			if (imagePath.startsWith("http")) {
				f = new FileCache(this).getFile(imagePath);
			} else {
				f = new File(imagePath);
			}

			// bitmapPreview = decodeBitmap(f);
			bitmapPreview = ImageLoader.getInstance(this).decodeFile(f, ImageLoader.REQUIRED_SIZE_HEIGHT);

			// if (rotate) {
			// Matrix matrix = new Matrix();
			// matrix.postRotate(90);
			// Bitmap out = Bitmap.createBitmap(bitmapPreview, 0, 0,
			// bitmapPreview.getWidth(), bitmapPreview.getHeight(), matrix,
			// true);
			// bitmapPreview.recycle();
			// bitmapPreview = out;
			// }

			PREVIEW_WIDTH = bitmapPreview.getWidth();
			PREVIEW_HEIGHT = bitmapPreview.getHeight();

		} else {

		}

		Debug.debug(getClass(), "width = " + PREVIEW_WIDTH + " height = " + PREVIEW_HEIGHT);

	}

	// private Bitmap decodeBitmap(File f) {
	// try {
	// // decode image size
	// BitmapFactory.Options o = new BitmapFactory.Options();
	// o.inJustDecodeBounds = true;
	// BitmapFactory.decodeStream(new FileInputStream(f), null, o);
	//
	// // Find the correct scale value. It should be the power of 2.
	// final int REQUIRED_SIZE = 200;
	// int width_tmp = o.outWidth, height_tmp = o.outHeight;
	// int scale = 1;
	// while (true) {
	// if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
	// break;
	// width_tmp /= 2;
	// height_tmp /= 2;
	// scale *= 2;
	// }
	//
	// // decode with inSampleSize
	// BitmapFactory.Options o2 = new BitmapFactory.Options();
	// o2.inSampleSize = scale;
	// return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	// } catch (FileNotFoundException e) {
	// }
	// return null;
	// }

	private void initImageProcessing() {
		auto = new Auto();
		sharpen = new Sharpen();
		bright = new Brightness();
		satulation = new Saturation();
		colorFirst = new ColorFirst();
		colorSecond = new ColorSecond();
	}

	private void initViewFromId() {

		save = findViewById(R.id.save);
		cancelfilterimage = findViewById(R.id.cancelimagefilter);
		postnow = findViewById(R.id.postnow);
		postnow.setBackgroundResource(R.drawable.usethisimage);

		imagePreview = (ImageView) findViewById(R.id.filter_preview);

		autoGroup = findViewById(R.id.control_auto);
		controllGroup = findViewById(R.id.control_layout);
		brightnessGroup = findViewById(R.id.control_brightness);
		colorGroup = findViewById(R.id.control_color);
		satulationGroup = findViewById(R.id.control_satulation);

		backFilter = findViewById(R.id.back_filter);

		brightnessSeek = (SeekBar) findViewById(R.id.brightness_seek);
		colorFirstSeek = (SeekBar) findViewById(R.id.color_first_seek);
		colorSecondSeek = (SeekBar) findViewById(R.id.color_second_seek);
		satulationSeek = (SeekBar) findViewById(R.id.satulation_seek);

		resetValue();

		brightnessSeek.setOnSeekBarChangeListener(this);
		colorFirstSeek.setOnSeekBarChangeListener(this);
		colorSecondSeek.setOnSeekBarChangeListener(this);
		satulationSeek.setOnSeekBarChangeListener(this);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		boolean showSave = prefs.getBoolean("showsavebutton", true);
		if (!showSave) {
			save.setVisibility(View.INVISIBLE);
		}

		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("showsavebutton", true);
		editor.commit();

	}

	private void resetValue(){
		applyBitmap = false;
		brightnessSeek.setProgress(50);
		colorFirstSeek.setProgress(50);
		colorSecondSeek.setProgress(50);
		satulationSeek.setProgress(50);
	}
	private void showGroup(View target) {
		autoGroup.setVisibility(View.GONE);
		controllGroup.setVisibility(View.GONE);
		brightnessGroup.setVisibility(View.GONE);
		colorGroup.setVisibility(View.GONE);
		satulationGroup.setVisibility(View.GONE);
		if (target != null) {
			target.setVisibility(View.KEEP_SCREEN_ON);
		}
		if (target == autoGroup) {
			backFilter.setVisibility(View.GONE);
		} else {
			backFilter.setVisibility(View.KEEP_SCREEN_ON);
		}

		boolean enabled = false;
		if (target == autoGroup || target == controllGroup) {
			enabled = true;

		}
		save.setEnabled(enabled);
		postnow.setEnabled(enabled);
		cancelfilterimage.setEnabled(enabled);

	}

	private void nofifyUpdatePreview(ImageView imgView, Bitmap bmp) {
		imgView.setImageBitmap(bmp);
	}

	// -----------------------------------------------------------------------
	// click
	// -----------------------------------------------------------------------
	public void clickBack(View view) {
		applyBitmap();
		if (controllGroup.getVisibility() != View.VISIBLE) {
			showGroup(controllGroup);
		} else {
			showGroup(autoGroup);
		}
	}

	public void clickBrightness(View view) {
		showGroup(brightnessGroup);
	}

	public void clickColor(View view) {
		showGroup(colorGroup);
	}

	public void clickSaturation(View view) {
		showGroup(satulationGroup);
	}

	public void clickSharpen(View view) {
		applyBitmap = true;
		sharpen.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, 11);
		new FilterBackprocess(this, this).execute(sharpen);
	}

	public void clickFooterSave(View view) {
		if (uploaded == false) {
			uploadSavedImage();
		}
	}

	public void clickFooterPost(View view) {
	//	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	//	Boolean signup = prefs.getBoolean("signup", false);
		CameraActivity.bmResult = bitmapPreview;

		CameraAction camAct = new CameraAction();
		camAct.setCameraTakenAction(this, true);
		if (!camAct.readCallbackState(this)) {
//			Intent n = new Intent(this, PostNewTopicsFromPicture.class);
//			n.putExtra("upload_path", upload_path);
//			n.putExtra("category_id", category_id);
//			startActivity(n);
			Toast.makeText(this, "Post the taken pic...", 1000);
		}

		finish();
	}

	public void clickFooterBack(View view) {
		loadBitmap();
		resetValue();
		sharpen.reset();
		nofifyUpdatePreview(imagePreview, bitmapPreview);
	}

	public void clickAutoFilter(View view) {

		loadBitmap();
		nofifyUpdatePreview(imagePreview, bitmapPreview);

		applyBitmap = true;
		auto.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, 50, 30, 30);
		new FilterBackprocess(this, this).execute(auto);

	}

	public void clickManualFilter(View view) {
		showGroup(controllGroup);
	}

	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		seekValue = progress - MIDDLE_SEEK_VALUE;
		// Debug.debug(getClass(), "seek value " + seekValue);
	}

	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		applyBitmap = false;
		switch (seekBar.getId()) {
		case R.id.brightness_seek:
			bright.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, seekValue);
			new FilterBackprocess(this, this).execute(bright);
			break;
		case R.id.color_first_seek:
			colorFirst.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, seekValue);
			new FilterBackprocess(this, this).execute(colorFirst);
			break;
		case R.id.color_second_seek:
			colorSecond.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, seekValue);
			new FilterBackprocess(this, this).execute(colorSecond);
			break;
		case R.id.satulation_seek:
			satulation.setUp(bitmapPreview, PREVIEW_WIDTH, PREVIEW_HEIGHT, seekValue);
			new FilterBackprocess(this, this).execute(satulation);
			break;
		}

	}

	public void onFinish(Bitmap result) {
		nofifyUpdatePreview(imagePreview, result);
		freeOldResult(result);
		applyBitmap();
	}

	private void applyBitmap() {
		if (!applyBitmap || tmpResult == null) {
			applyBitmap = true;
			return;
		}
		BitmapManager.safeRecycle(bitmapPreview);
		bitmapPreview = tmpResult;
		tmpResult = null;
		nofifyUpdatePreview(imagePreview, bitmapPreview);
	}

	private void freeOldResult(Bitmap result) {
		BitmapManager.safeRecycle(tmpResult);
		tmpResult = result;
	}

	private void uploadSavedImage() {
		// BackProcess backProcess = new BackProcess(this, this);
		// backProcess.execute(null);

		AlternativeRequestReview alRequest = new AlternativeRequestReview();
		alRequest.setAction((com.photostory.communication.ActionRequest) this);

		alRequest.setFunctionNumber(71);
		alRequest.setParseAfterRequest(false);
		AlternativeBackProcess alBackProcess = new AlternativeBackProcess(this, alRequest, true);

		try {
			Uploader upload = new Uploader(AppConstant.API_URL, bitmapPreview, "png");
			String result = upload.startUpload();
			Log.w(getLocalClassName(), "<<<Need to parse uploaded image path>>>");
//			upload_path = new APIReturnChecker().bruteForceGetValueFromXML(result, "url");
//			Debug.debug(getClass(), "upload path = " + upload_path);
			alBackProcess.execute(createSaveStockAPI(upload_path));
		} catch (Exception ex) {
			Debug.debug(getClass(), "error", ex);
		}

	}

	private AlternativeRequest createSaveStockAPI(String userStatus) {
		AlternativeRequest ggl = new AlternativeRequest(this);
		ggl.setFunctionId("71");
		ggl.addParameter("private_id", private_id);
		ggl.addParameter("picture_uri", upload_path);
		return ggl;
	}

	// private gugulogAPI createStockImageAPI(String upload_path) {
	// Debug.debug(getClass(), "request stock");
	// gugulogAPI api = new gugulogAPI();
	// api.AddParam("private_id", private_id);
	// api.AddParam("picture_uri", upload_path);
	// api.setCallFunName("eGGAPIFuncID_PostImageStock");
	// api.setCallFunName(GGAPIFuncID.eGGAPIFuncID_PostImageStock.name());
	// return api;
	// }
	//
	// public void process(gugulogAPI... api0) {
	//
	// if (!uploaded) {
	// Uploader upload = new Uploader(getString(R.string.upload_image_url),
	// bitmapPreview, "png");
	// String result = upload.startUpload();
	// upload_path = new APIReturnChecker().bruteForceGetValueFromXML(result,
	// "url");
	// Debug.debug(getClass(), "upload path = " + upload_path);
	//
	// if (upload_path.startsWith("http")) {
	//
	// gugulogAPI api = createStockImageAPI(upload_path);
	// api.callGuGuLogAPI(api.getCallFunName());
	// Debug.debug(getClass(), "stock return = " + api.getXML());
	//
	// if (new APIReturnChecker().isFunctionReturnError(api.getXML())) {
	// Toast.makeText(this, "upload fail try again", Toast.LENGTH_LONG).show();
	// }
	//
	// }
	// }
	// uploaded = true;
	//
	// }

	public void onFinishRequest(Object model, int FunNumber, String resultString) {
		
		uploaded = true;
		Log.w(getLocalClassName(), "<<<Need to check uploaded status>>>");
//		if (new APIReturnChecker().isFunctionReturnError(resultString)) {
//			Toast.makeText(this, "upload fail try again", Toast.LENGTH_LONG).show();
//		} else {
//			finish();
//		}
	}
}