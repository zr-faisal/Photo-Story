package com.photostory.imageprocessing;

import x.br.com.dina.ui.custom.activity.util.Debug;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.photostory.imagefilter.FilterProcess;

public class Saturation extends DIMPRoot implements FilterProcess {

	private double value;

	public void setUp(Bitmap src, int width, int height, double value) {
		this.src = src;
		this.value = value;
		this.width = width;
		this.height = height;
	}

	public Bitmap doProcess() {
		if (src == null) {
			return null;
		}

		// cal val for satulation
		value = (value + 50) / 50;

		
		// scale 0.5 > 1 > 1.5
		// value = (value+1)/2;

		// scale 0.2 > 1 > 1.2
		//value = (value + 4) / 5;
		
		value = (value + 8) / 9; 

		// value = 1;//(value + 100) / 100;
		 Debug.debug(getClass(), "seek value " + value);

		int[] pixels = new int[width * height];
		float[] HSV = new float[3];
		// get pixel array from source
		src.getPixels(pixels, 0, width, 0, 0, width, height);
		int index = 0;
		// iteration through pixels
		for (int y = 0; y < height; ++y) {
			for (int x = 0; x < width; ++x) {
				// get current index in 2D-matrix
				index = y * width + x;
				// convert to HSV
				Color.colorToHSV(pixels[index], HSV);
				// increase Saturation level
				HSV[1] *= value;
				HSV[1] = (float) Math.max(0.0, Math.min(HSV[1], 1.0));
				// take color back
				pixels[index] |= Color.HSVToColor(HSV);
			}
		}
		Bitmap bmOut = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
		return bmOut;
	}

}
