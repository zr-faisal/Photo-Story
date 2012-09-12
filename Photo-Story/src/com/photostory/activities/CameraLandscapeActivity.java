package com.photostory.activities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.photostory.utl.AppConstant;
import com.photostory.utl.ImageLoader;

public class CameraLandscapeActivity extends Activity {
	// OnClickListener {

	private int sensorHeight;
	private Preview mPreview;
	Camera mCamera;
	int numberOfCameras = 1;
	int cameraCurrentlyLocked;

	int defaultCameraId;

	private boolean flashOn = false;
	private boolean frontCamera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_layout_land);

		initView();

		mPreview = new Preview(this);
		LinearLayout holder = (LinearLayout) findViewById(R.id.camera_holder);
		holder.addView(mPreview);
		// holder.setOnClickListener(this);
		frontCamera = false;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			// Find the total number of cameras available
			numberOfCameras = Camera.getNumberOfCameras();
			//
			// // Find the ID of the default camera
			CameraInfo cameraInfo = new CameraInfo();
			for (int i = 0; i < numberOfCameras; i++) {
				Camera.getCameraInfo(i, cameraInfo);
				if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
					defaultCameraId = i;
					Debug.debug(getClass(), "num of camera = " + numberOfCameras);
				}
			}
		}
	}

	public void initView() {
		
		View top = findViewById(R.id.top_sensor);
		View bottom = findViewById(R.id.bottom_sensor);

		int consoleBarHeight = (int) (190 * AppConstant.ratio);
		sensorHeight = (AppConstant.RUNTIME_SCREEN_HEIGHT - AppConstant.RUNTIME_SCREEN_WIDTH - consoleBarHeight) / 2;
		// Debug.debug(getClass(), "ratio = " + AppConstant.ratio);
		// Debug.debug(getClass(), "sensor size = " + sensorHeight);

		LayoutParams paramTop = new LayoutParams(sensorHeight, AppConstant.RUNTIME_SCREEN_HEIGHT);
		LayoutParams paramBottom = new LayoutParams(AppConstant.RUNTIME_SCREEN_HEIGHT, AppConstant.RUNTIME_SCREEN_HEIGHT);
		int sensorHeightx = (AppConstant.RUNTIME_SCREEN_WIDTH - AppConstant.RUNTIME_SCREEN_HEIGHT - consoleBarHeight) / 2;
		paramBottom.setMargins(sensorHeightx + AppConstant.RUNTIME_SCREEN_HEIGHT, 0, 0, 0);

		bottom.setLayoutParams(paramBottom);
		top.setLayoutParams(paramTop);

		// Debug.debug(getClass(), "num of camera = " + numberOfCameras);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
			View v = findViewById(R.id.switch_camera);
			v.setVisibility(View.GONE);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			mCamera = Camera.open();
			cameraCurrentlyLocked = defaultCameraId;
			mPreview.setCamera(mCamera);

		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
//			startActivity(new Intent(this, NonCameraActivity.class));
			finish();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	// -----------------------------------------------------------------
	// inner class
	// -----------------------------------------------------------------

	public void clickShoot(View view) {
		try {
			mPreview.setFlash(flashOn);
			mCamera.takePicture(null, null, jpegCallback);
		} catch (Throwable ex) {
			Debug.debug(getClass(), "ex", ex);
			finish();
		}

	}

	public void clickPhotoLibrary(View view) {
//		Intent n = new Intent(this, CameraRollListActivity.class);
//		startActivity(n);
//		finish();
		Toast.makeText(this, "Go to photo library...", 1000).show();
	}

	public void clickSwitch(View view) {
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mPreview.setCamera(null);
				mCamera.release();
				mCamera = null;
			}

			// frontCamera = ((cameraCurrentlyLocked + 1) % numberOfCameras ==
			// 1) ;
			frontCamera = ((cameraCurrentlyLocked + 1) % numberOfCameras == 1);
			mCamera = Camera.open((cameraCurrentlyLocked + 1) % numberOfCameras);
			cameraCurrentlyLocked = (cameraCurrentlyLocked + 1) % numberOfCameras;
			mPreview.switchCamera(mCamera);

			mCamera.startPreview();
		} catch (Exception ex) {
			Debug.debug(getClass(), "error", ex);
			finish();
		}
	}

	public void clickFlash(View view) {
		flashOn = (flashOn) ? false : true;
	}

	public void clickBack(View view) {
		finish();
	}

	// -----------------------------------------------------------------
	// take picture method
	// -----------------------------------------------------------------
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			if (data == null) {
				Toast.makeText(getApplicationContext(), "写真を撮ることができません!", Toast.LENGTH_SHORT).show();
				Debug.debug(getClass(), "error data==null");
			} else {
				writePictureWithRotate(data);
			}
		}
	};

	private Bitmap rotating(Bitmap bitmap) {
		Matrix matrix = new Matrix();
		matrix.postRotate((frontCamera) ? -90 : 90);
		Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
		bitmap.recycle();
		return rotated;
	}

	private Bitmap cropping(Bitmap bitmap) {

		int imgWidth = bitmap.getWidth();
		float ratio = AppConstant.RUNTIME_SCREEN_WIDTH * 1.0f / imgWidth;
		int scaledSensorHeight = (int) (sensorHeight / ratio);

		Bitmap croped = Bitmap.createBitmap(bitmap, 0, scaledSensorHeight, imgWidth, imgWidth);
		bitmap.recycle();
		return croped;
	}

	private Bitmap scalling(Bitmap bitmap, int outputSize) {
		Bitmap scaling = Bitmap.createScaledBitmap(bitmap, outputSize, outputSize, true);
		bitmap.recycle();
		return scaling;
	}

	private Bitmap flibBitmap(Bitmap ori) {
		if (!frontCamera)
			return ori;

		int width = ori.getWidth();
		int height = ori.getHeight();
		Bitmap out = Bitmap.createBitmap(ori.getWidth(), ori.getHeight(), ori.getConfig());
		int A, R, G, B;
		int pixel;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = ori.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				out.setPixel(width - 1 - x, y, Color.argb(A, R, G, B));
			}
		}
		ori.recycle();
		return out;

	}

	private byte[] bitmapToBytes(Bitmap bitmap) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] data = baos.toByteArray();
		bitmap.recycle();
		return data;
	}

	private void writePictureWithRotate(byte[] data) {
		try {
			int outputSize = 300;
			ImageLoader imgloader = ImageLoader.getInstance(this);
			Bitmap bitmap = imgloader.decodeFile(data, outputSize);
			bitmap = rotating(bitmap);
			bitmap = cropping(bitmap);
			bitmap = scalling(bitmap, 480);
			bitmap = flibBitmap(bitmap);
			data = bitmapToBytes(bitmap);
			writePicture(data);
		} catch (Throwable e) {
			Log.d("AndroidRuntime", "write fail ", e);
			Toast.makeText(this, "Problem. your camera service might be down. restart phone", Toast.LENGTH_LONG).show();
			finish();
		}
	}

	private void writePicture(byte[] data) {
		FileOutputStream outStream = null;
		try {
			File f = new File(Environment.getExternalStorageDirectory(), ".photo-story/lastpic.jpg");
			if (!f.exists())
				f.createNewFile();
			outStream = new FileOutputStream(f);
			outStream.write(data);  
			outStream.close();
			Log.d("AndroidRuntime", "onPictureTaken - wrote bytes: " + data.length);

			// ----------------------------
			Intent n = new Intent(CameraLandscapeActivity.this, FilterImageActivity.class);
			n.putExtra("image_url", f.getPath());
			n.putExtra("rotate", false);
			startActivity(n);
			finish();

		} catch (Throwable e) {
			Log.d("AndroidRuntime", "write fail ", e);
			finish();
		} finally {
		}
		Log.d("AndroidRuntime", "onPictureTaken - jpeg");
	}

	// -----------------------------------------------------------------
	// inner class
	// -----------------------------------------------------------------

	class Preview extends ViewGroup implements SurfaceHolder.Callback {
		private SurfaceView mSurfaceView;
		private SurfaceHolder mHolder;
		private Size mPreviewSize;
		private List<Size> mSupportedPreviewSizes;
		private Camera mCamera;

		Preview(Context context) {
			super(context);

			mSurfaceView = new SurfaceView(context);
			addView(mSurfaceView);

			mHolder = mSurfaceView.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		public void setCamera(Camera camera) {
			mCamera = camera;
			if (mCamera != null) {
				mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
				requestLayout();
			}
		}

		public void setFlash(boolean on) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			if (on) {
				parameters.setFlashMode(Parameters.FLASH_MODE_ON);
			} else {
				parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
			}
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			mCamera.setParameters(parameters);
		}

		public void switchCamera(Camera camera) {
			setCamera(camera);
			try {
				camera.setPreviewDisplay(mHolder);
			} catch (IOException exception) {
				Debug.debug(getClass(), "IOException caused by setPreviewDisplay()", exception);
			}
			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			camera.setParameters(parameters);
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			if (mSupportedPreviewSizes != null) {
				mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, AppConstant.RUNTIME_SCREEN_WIDTH, AppConstant.RUNTIME_SCREEN_HEIGHT);
			}
			Debug.debug(getClass(), "preview size width = " + mPreviewSize.width + " height = " + mPreviewSize.height);
			setMeasuredDimension(mPreviewSize.width, mPreviewSize.height);
		} 

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			if (changed && getChildCount() > 0) {
				View child = getChildAt(0);
				child.layout(0, 0, mPreviewSize.width, mPreviewSize.height);
			}
		}

		public void surfaceCreated(SurfaceHolder holder) {
			try {
				if (mCamera != null) {
					mCamera.setPreviewDisplay(holder);
				}
			} catch (IOException exception) {
				Debug.debug(getClass(), "IOException caused by setPreviewDisplay()", exception);
			}
		}

		public void surfaceDestroyed(SurfaceHolder holder) {
			if (mCamera != null) {
				mCamera.stopPreview();
			}
		}

		private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
			final double ASPECT_TOLERANCE = 0.1;
			double targetRatio = (double) w / h;
			if (sizes == null)
				return null;

			Size optimalSize = null;
			double minDiff = Double.MAX_VALUE;

			int targetHeight = h;

			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}

			if (optimalSize == null) {
				minDiff = Double.MAX_VALUE;
				for (Size size : sizes) {
					if (Math.abs(size.height - targetHeight) < minDiff) {
						optimalSize = size;
						minDiff = Math.abs(size.height - targetHeight);
					}
				}
			}

			for (Size s : sizes) {
				// Debug.debug(getClass(), "size = " + s.width + " " +
				// s.height);
				// Debug.debug(getClass(), "size = " + (s.width*1.0f/160) + " "
				// + (s.height*1.0f/160));

				// prefer 4:3 screen
				if (((int) (s.width * 1.0f / 160)) == 4 && ((int) (s.height * 1.0f / 160)) == 3) {
					optimalSize = s;
				}
			}
			return optimalSize;
		}

		public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
			requestLayout();

			try {
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			} catch (Throwable ex) {
//				Intent n = new Intent(CameraLandscapeActivity.this, NonCameraActivity.class);
//				startActivity(n);
				Log.e(getLocalClassName(), "<<<Exception in changing surface>>>");
				finish();
			}
		}
	}

	// @Override
	// public void onClick(View v) {
	// try {
	// mCamera.takePicture(null, null, jpegCallback);
	// } catch (Throwable ex) {
	// Debug.debug(getClass(), "ex", ex);
	// }
	// }
}