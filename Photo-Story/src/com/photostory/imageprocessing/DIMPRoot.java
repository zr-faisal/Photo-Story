package com.photostory.imageprocessing;

import android.graphics.Bitmap;

import com.photostory.imagefilter.FilterProcess;

public abstract class DIMPRoot implements FilterProcess {

	protected Bitmap src;
	protected int width;
	protected int height;

	public int safeColorValue(int value) {
		return (value > 255) ? 255 : (value < 0) ? 0 : value;
	}

}
