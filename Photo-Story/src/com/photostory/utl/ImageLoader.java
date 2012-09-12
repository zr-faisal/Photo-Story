package com.photostory.utl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
	public static final int REQUIRED_SIZE_HEIGHT = 215;
	public static final int REQUIRED_SIZE_LOW = 70;
	public static final int REQUIRED_SIZE = 180;
	public static final int REQUIRED_SIZE_SMALL = 30;
	
	private static ImageLoader instance; 

	private Activity activity;
	private FileCache fileCache;
	private MemoryCache memoryCache;
	private Bitmap loadingBitmap;
	private Bitmap notfoundBitmap;
	private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

	private ImageLoader(Context context) {
		photoLoaderThread.setPriority(Thread.MIN_PRIORITY);

		try {
			InputStream st = context.getAssets().open("image/loading/loadingimage.png");
			loadingBitmap = decodeStream(st, REQUIRED_SIZE);
			st = context.getAssets().open("image/loading/noimage.png");
			notfoundBitmap = decodeStream(st, REQUIRED_SIZE);

		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
		}
		memoryCache = new MemoryCache(loadingBitmap, notfoundBitmap);
		fileCache = new FileCache(context);

	}

	public static ImageLoader getInstance(Context context) {
		if (instance == null) {
			instance = new ImageLoader(context);
		}
		return instance;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	// public Bitmap loadBitmap(ImageView url) {
	// url.setImageBitmap(null);
	// return memoryCache.get(url);
	// }

	public void DisplayImage(String url, Activity activity, ImageView imageView) {
		DisplayImage(url, activity, imageView, REQUIRED_SIZE_LOW);
	}

	public void DisplayImage(String url, Activity activity, ImageView imageView, int requairedSize) {
		imageViews.put(imageView, url);
		imageView.setImageBitmap(loadingBitmap);
		memoryCache.get(imageView);
		imageView.setImageBitmap(loadingBitmap);
		try {
			queuePhoto(url, activity, imageView, requairedSize);
		} catch (Exception ex) {
			Log.e("AndroidRuntime", "error", ex);
		}
	}

	public void DisplayImage(String url, Activity activity, ImageView imageView, int width, int height) {
		imageViews.put(imageView, url);
		imageView.setImageBitmap(loadingBitmap);
		memoryCache.get(imageView);
		imageView.setImageBitmap(loadingBitmap);
		try {
			queuePhoto(url, activity, imageView, REQUIRED_SIZE_LOW);
		} catch (Exception ex) {
			Log.e("AndroidRuntime", "error", ex);
		}
	}

	private void queuePhoto(String url, Activity activity, ImageView imageView, int requairedSize) {
		// This ImageView may be used for other images before. So there may be
		// some old tasks in the queue. We need to discard them.
		photosQueue.Clean(imageView);
		PhotoToLoad p = new PhotoToLoad(url, imageView, requairedSize);
		synchronized (photosQueue.photosToLoad) {
			photosQueue.photosToLoad.push(p);
			photosQueue.photosToLoad.notifyAll();
		}

		// start thread if it's not started yet
		if (photoLoaderThread.getState() == Thread.State.NEW)
			photoLoaderThread.start();
	}

	/*
	public Bitmap getBitmapTask(String url, int requiredSize) {

		if (url == null) {
			return null;
		}
		url = url.trim();
		if (!(url.startsWith("http") || url.startsWith("/"))) {
			return null;
		}

		try {
			// from web
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			// Debug.debug(getClass(), "load image url ----> = " + url);
			HttpURLConnection conn = null;
			if (imageUrl.getProtocol().toLowerCase().equals("https")) {
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) imageUrl.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				conn = https;
			} else {
				conn = (HttpURLConnection) imageUrl.openConnection();
			}
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			InputStream is = conn.getInputStream();
			File f = new File(android.os.Environment.getExternalStorageDirectory(), ".gugulog/cachemap.png");
			if (!f.exists()) {
				f.createNewFile();
			}
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, requiredSize);
			return bitmap;
		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
			return null;
		}
	}
	*/

	public Bitmap getBitmap(String url, int requiredSize) {
		if (url == null) {
			// Debug.debug(getClass(), "hahaha not valid url");
			return null;
		}
		url = url.trim();
		if (!(url.startsWith("http") || url.startsWith("/"))) {
			// Debug.debug(getClass(), "hahaha not valid url");
			return null;
		}

		Bitmap b = null;
		File f = fileCache.getFile(url);

		// from SD cache
		if (f != null) {
			b = decodeFile(f, requiredSize);
		}

		if (b == null) {
			if (!url.startsWith("http")) {
				f = new File(url);
				b = decodeFile(f, requiredSize);
			} else {
				b = loadFromInternet(url, f, requiredSize);
			}
		}

		return b;
	}

	private Bitmap loadFromInternet(String url, File f, int requiredSize) {
		try {
			// from web
			Bitmap bitmap = null;
			URL imageUrl = new URL(url);
			// Debug.debug(getClass(), "load image url ----> = " + url);
			HttpURLConnection conn = null;
			if (imageUrl.getProtocol().toLowerCase().equals("https")) {
				trustAllHosts();
				HttpsURLConnection https = (HttpsURLConnection) imageUrl.openConnection();
				https.setHostnameVerifier(DO_NOT_VERIFY);
				conn = https;
			} else {
				conn = (HttpURLConnection) imageUrl.openConnection();
			}
			conn.setConnectTimeout(60000);
			conn.setReadTimeout(60000);
			InputStream is = conn.getInputStream();
			if (!f.exists()) {
				f.createNewFile();
			}
			OutputStream os = new FileOutputStream(f);
			Utils.CopyStream(is, os);
			os.close();
			bitmap = decodeFile(f, requiredSize);
			return bitmap;
		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
			return null;
		}
	}

	// private int getREQUIREDSIZE(boolean decodeSmall) {
	// return (decodeSmall) ? REQUIRED_SIZE_SMALL : REQUIRED_SIZE;
	// }

	// always verify the host - dont check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	/**
	 * Trust every server - dont check for any certificate
	 */
	private static void trustAllHosts() {
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}

			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}

			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				// TODO Auto-generated method stub

			}
		} };

		// Install the all-trusting trust manager
		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// decodes image and scales it to reduce memory consumption
	public Bitmap decodeStream(InputStream st, int REQUIRED_SIZE) {
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(st, null, o);

			// Find the correct scale value. It should be the power of 2.
			// final int REQUIRED_SIZE = 70;
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			try {
				return BitmapFactory.decodeStream(st, null, o2);
			} catch (Throwable ex) {
				Debug.debug(getClass(), "error", ex);
			}
		} catch (Throwable e) {
			Debug.debug(getClass(), "ohhh no exceed memory !!!!!!!!!!!!!!!!!!!", e);
		}
		return null;
	}

	public Bitmap decodeFile(byte[] data, int REQUIRED_SIZE) {
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, o);

			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			try {
				return BitmapFactory.decodeByteArray(data, 0, data.length, o2);
			} catch (Throwable ex) {
				Debug.debug(getClass(), "error", ex);
			}
		} catch (Throwable e) {
			Debug.debug(getClass(), "ohhh no exceed memory !!!!!!!!!!!!!!!!!!!", e);
		}
		return null;
	}

	public Bitmap decodeFile(File f, int REQUIRED_SIZE) {
		f.setReadOnly();
		try {
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			try {
				return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
			} catch (Throwable ex) {
				Debug.debug(getClass(), "error", ex);
			}
		} catch (Throwable e) {
			Debug.debug(getClass(), "ohhh no exceed memory !!!!!!!!!!!!!!!!!!!", e);
		}
		return null;
	}

	// Task for the queue
	private class PhotoToLoad {
		public String url;
		public ImageView imageView;
		int requiredSize;

		public PhotoToLoad(String u, ImageView i, int requiredSize) {
			url = u;
			imageView = i;
			this.requiredSize = requiredSize;
		}
	}

	PhotosQueue photosQueue = new PhotosQueue();

	public void stopThread() {
		photoLoaderThread.interrupt();
	}

	// stores list of photos to download
	class PhotosQueue {
		private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();

		// removes all instances of this ImageView
		public void Clean(ImageView image) {
			for (int j = 0; j < photosToLoad.size();) {
				if (photosToLoad.get(j).imageView == image)
					photosToLoad.remove(j);
				else
					++j;
			}
		}
	}

	class PhotosLoader extends Thread {
		public void run() {
			try {
				while (true) {
					// thread waits until there are any images to load in the
					// queue
					if (photosQueue.photosToLoad.size() == 0)
						synchronized (photosQueue.photosToLoad) {
							photosQueue.photosToLoad.wait();
						}
					if (photosQueue.photosToLoad.size() != 0) {
						PhotoToLoad photoToLoad;
						synchronized (photosQueue.photosToLoad) {
							photoToLoad = photosQueue.photosToLoad.pop();
						}
						Bitmap bmp = getBitmap(photoToLoad.url, photoToLoad.requiredSize);

						// String tag = imageViews.get(photoToLoad.imageView);
						// if (tag != null && tag.equals(photoToLoad.url)) {
						BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad.imageView);
						try {
							Activity a = (Activity) photoToLoad.imageView.getContext();
							a.runOnUiThread(bd);
						} catch (Exception ex) {
							activity.runOnUiThread(bd);
							// Debug.debug(getClass(), "error", ex);
						}
						// }

						if (bmp != null) {
							memoryCache.put(photoToLoad.imageView, bmp);
						}
					}
					if (Thread.interrupted())
						break;
				}
			} catch (InterruptedException e) {
				// allow thread to exit
			}
		}
	}

	PhotosLoader photoLoaderThread = new PhotosLoader();

	// Used to display bitmap in the UI thread
	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		ImageView imageView;

		public BitmapDisplayer(Bitmap b, ImageView i) {
			bitmap = b;
			imageView = i;
		}

		public void run() {

			if (bitmap != null && !bitmap.isRecycled()) {
				try {
					imageView.setImageBitmap(bitmap);
				} catch (Exception ex) {
					Debug.debug(getClass(), "error ", ex);
					imageView.setImageBitmap(notfoundBitmap);
				}
			} else {
				imageView.setImageBitmap(notfoundBitmap);
			}
		}
	}

	public void clearCache() {
		memoryCache.clear();
		fileCache.clear();
	}

}
