package com.photostory.imagefilter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public class FilterBackprocess extends AsyncTask<FilterProcess, Integer, Bitmap> {

	private FilterCallback callback;
	private ProgressDialog loadingDialog;
	private Context context;

	public FilterBackprocess(Context context, FilterCallback callback) {
		
		this.context = context;
		this.callback = callback;
	}

	@Override
	protected Bitmap doInBackground(FilterProcess... p) {
		return (p[0] == null) ? null : p[0].doProcess();
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		
		loadingDialog.dismiss();
		if (callback != null) {
			callback.onFinish(result);
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		
		loadingDialog = new ProgressDialog(context, android.R.style.Theme_Translucent_NoTitleBar);
		loadingDialog.show();
		super.onPreExecute();
	}

}
