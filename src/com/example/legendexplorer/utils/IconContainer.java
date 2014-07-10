package com.example.legendexplorer.utils;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

public class IconContainer {
	private final static ConcurrentHashMap<String, SoftReference<Bitmap>> mCachedIcons = new ConcurrentHashMap<String, SoftReference<Bitmap>>();

	public static Bitmap get(String path) {
		Bitmap bm = null;
		if (mCachedIcons.containsKey(path)) {
			bm = mCachedIcons.get(path).get();
		}
		return bm;
	}

	public static Bitmap get(File file) {
		return get(file.getAbsolutePath());
	}

	public static void put(String path, Bitmap bm) {
		SoftReference<Bitmap> soft = new SoftReference<Bitmap>(bm);
		mCachedIcons.put(path, soft);
	}

	public static void put(File file, Bitmap bm) {
		put(file.getAbsolutePath(), bm);
	}
}
