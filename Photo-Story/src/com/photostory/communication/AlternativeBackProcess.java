package com.photostory.communication;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

public class AlternativeBackProcess extends AsyncTask<AlternativeRequest, Integer, Long> {
	private Context context;
	private ProgressDialog loadingDialog;
	private AlternativeBackProcessCallback callback;
	private boolean showLoading;
	private View loadingView;
	private ListView listView;

	public AlternativeBackProcess(Context context, AlternativeBackProcessCallback callback, boolean shoLoading) {
		this.context = context;
		this.callback = callback;
		this.showLoading = shoLoading;
	}

	@Override
	protected Long doInBackground(AlternativeRequest... api) {
		Debug.debug(getClass(), "doInBackground");
		callback.process(api);
		return null;
	}

	public void setLoadingView(View loading) {
		this.loadingView = loading;
	}

	public void setLoadingView(View loading, ListView v) {
		this.loadingView = loading;
		this.listView = v;
	}

	@Override
	protected void onPostExecute(Long result) {
		Debug.debug(getClass(), "onPostExecute");
		try {
			if (showLoading) {
				loadingDialog.dismiss();
			} else if (loadingView != null && listView != null) {
				try {
					listView.removeFooterView(loadingView);
				} catch (Exception ex) {
					Debug.debug(getClass(), "err", ex);
				}
			} else if (loadingView != null) {
				loadingView.setVisibility(View.GONE);
			}
		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
		}
		callback.onFinish();
		super.onPostExecute(result);
	}

	@Override
	protected void onPreExecute() {
		Debug.debug(getClass(), "onPreExecute");
		try {
			if (showLoading) {
				loadingDialog = new ProgressDialog(context, android.R.style.Theme_Translucent_NoTitleBar);
				loadingDialog.setCancelable(false);
				try {
					loadingDialog.show();
				} catch (Exception e) {
					Log.e("AlternativeBackProcess", "error", e);
				}
			} else if (loadingView != null && listView != null) {
				try {
					listView.addFooterView(loadingView);
				} catch (Exception ex) {
					Debug.debug(getClass(), "err", ex);
				}
			} else if (loadingView != null) {
				loadingView.setVisibility(View.KEEP_SCREEN_ON);
			}
		} catch (Exception ex) {
			Debug.debug(getClass(), "ex", ex);
		}
		super.onPreExecute();
	}

	@Override
	protected void onCancelled() {
		Debug.debug(getClass(), "onCancelled");
		super.onCancelled();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		Debug.debug(getClass(), "onProgressUpdate");
		super.onProgressUpdate(values);
	}

}
