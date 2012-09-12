package com.photostory.imageprocessing;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.photostory.imagefilter.FilterProcess;

public class Sharpen extends DIMPRoot implements FilterProcess {

	private double value;
	private boolean sharpened;

	public Sharpen() {
		sharpened = false;
	}
	public void reset(){
		sharpened = false;
	}

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

		if (sharpened) {
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

					R += 0;
					G += 0;
					B += 0;
					R = safeColorValue(R);
					G = safeColorValue(G);
					B = safeColorValue(B);
					bmOut.setPixel(x, y, Color.argb(A, R, G, B));
				}
			}
			return bmOut;
		} else {
			sharpened = true;
			double[][] SharpConfig = new double[][] { { 0, -2, 0 }, { -2, value, -2 }, { 0, -2, 0 } };
			ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
			convMatrix.applyConfig(SharpConfig);
			convMatrix.Factor = value - 8;
			return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
		}
	}

}
