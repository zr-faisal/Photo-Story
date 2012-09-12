package com.photostory.utl;

import java.lang.ref.SoftReference;
import java.util.HashMap;

import x.br.com.dina.ui.custom.activity.util.BitmapManager;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class MemoryCache {

	private HashMap<ImageView, SoftReference<Bitmap>> cache = new HashMap<ImageView, SoftReference<Bitmap>>();

	private Bitmap load, not;

	public MemoryCache(Bitmap load, Bitmap not) {
		this.load = load;
		this.not = not;
	}

	public Bitmap get(ImageView id) {
		if (!cache.containsKey(id))
			return null;
		SoftReference<Bitmap> ref = cache.get(id);
		Bitmap b = ref.get();
		if (b != load && b != not) {
			BitmapManager.safeRecycle(b);
		}
		return null;
	}

	public Bitmap get(ImageView id, Bitmap bit) {
		if (!cache.containsKey(id))
			return null;
		SoftReference<Bitmap> ref = cache.get(id);
		Bitmap b = ref.get();
		if (b != load && b != not && b != bit) {
			BitmapManager.safeRecycle(b);
		}
		return null;
	}

	public void put(ImageView id, Bitmap bitmap) {
		get(id, bitmap);
		cache.put(id, new SoftReference<Bitmap>(bitmap));
	}

	public void clear() {
		cache.clear();
	}
}