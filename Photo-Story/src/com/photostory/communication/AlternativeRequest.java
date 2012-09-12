package com.photostory.communication;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.app.Activity;

public class AlternativeRequest {

	 public static final String API_URL = "localhost/img_upload.php";
	 public static final String API_VERSION = "10";
	 
	// "http://nijg.mydns.jp:3000/guuguu/mvc3/";

	// now use
	// public static final String API_URL =
	// "http://stg01.service.gugulog.com/guuguu/mvc3/";

	// public static final String API_URL =
	// "http://176.34.22.139/gugu/mvc3/index.php";

	// private final String urlUploadImage =
	// "http://hpguild.com/img_upload/img_upload.php";
	// private final String urlUploadImage =
	// "http://stg01.service.gugulog.com/guuguu/img_upload/img_upload.php";

	private HttpClient httpclient;
	private HttpPost httppost;
	List<NameValuePair> nameValuePairs;
	private String version;

	public AlternativeRequest(Activity activity) {
		httpclient = new DefaultHttpClient();
		httppost = new HttpPost(API_URL);
		version = API_VERSION;
	}

	public void setPairValue(List<NameValuePair> value) {
		this.nameValuePairs = value;
	}

	public void setFunctionId(String funcid) {
		nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("func_id", funcid));
		nameValuePairs.add(new BasicNameValuePair("version", version));
	}

	// must call set function id before add param
	public void addParameter(String name, String value) {
		nameValuePairs.add(new BasicNameValuePair(name, value));
	}

	public String execute() {
		try {
			/* 20120216 kawamura 文字列変換の際の文字コードにUTF-8を指定 */

//			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//			HttpResponse response = httpclient.execute(httppost);
//			String responseBody = EntityUtils.toString(response.getEntity());

			org.apache.http.client.entity.UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
			httppost.setEntity(formEntity);
			HttpResponse response = httpclient.execute(httppost);
			org.apache.http.HttpEntity httpEntity = response.getEntity();
			String responseBody = EntityUtils.toString(httpEntity, "UTF-8");

			/* 20120216 kawamura 上記修正ここまで */

			// Debug.error(getClass(), "response is = " + responseBody);
			return responseBody;
		} catch (Exception e) {
			Debug.error(getClass(), "error", e);
		}
		return null;

	}

}
