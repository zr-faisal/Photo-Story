package com.photostory.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.photostory.imagefilter.FilterProcess;

public class ColorFirst extends DIMPRoot implements FilterProcess {

	private int value;

	public void setUp(Bitmap src, int width, int height, int value) {
		this.src = src;
		this.value = value;
		this.width = width;
		this.height = height;
	}

	public Bitmap doProcess() {
		if (src == null) {
			return null;
		}

		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		int A, R, G, B;
		int pixel;
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				pixel = src.getPixel(x, y);
				A = Color.alpha(pixel);
				R = Color.red(pixel);
				G = Color.green(pixel);
				B = Color.blue(pixel);

				R += value;
				R = safeColorValue(R);
				bmOut.setPixel(x, y, Color.argb(A, R, G, B));
			}
		}
		return bmOut;
	}

}
