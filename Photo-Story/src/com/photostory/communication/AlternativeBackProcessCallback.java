package com.photostory.communication;

public interface AlternativeBackProcessCallback {
	public void process(AlternativeRequest... api);

	public void onFinish();
}
