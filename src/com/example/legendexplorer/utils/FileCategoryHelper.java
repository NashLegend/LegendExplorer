package com.example.legendexplorer.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;
import com.example.legendexplorer.fragment.CategoriedFragment.FileCategory;

public class FileCategoryHelper {

	public static final int COLUMN_ID = 0;

	public static final int COLUMN_PATH = 1;

	public static final int COLUMN_SIZE = 2;

	public static final int COLUMN_DATE = 3;

	private static final String LOG_TAG = "cat";

	public static class CategoryInfo {
		public long count;

		public long size;
	}

	private static HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

	public static HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
		return mCategoryInfo;
	}

	public static FileCategory[] sCategories = new FileCategory[] {
			FileCategory.Music, FileCategory.Video, FileCategory.Picture,
			FileCategory.Doc, FileCategory.Zip, FileCategory.Apk };

	public FileCategoryHelper(Context context) {

	}

	public static void delete(FileCategory fc, String path, Context context) {

	}

	public static Cursor query(FileCategory fc, Context context) {
		Uri uri = getContentUriByCategory(fc);
		String selection = buildSelectionByCategory(fc);
		String sortOrder = null;

		if (uri == null) {
			return null;
		}

		String[] columns = new String[] { FileColumns._ID, FileColumns.DATA,
				FileColumns.SIZE, FileColumns.DATE_MODIFIED };
		return context.getContentResolver().query(uri, columns, selection,
				null, sortOrder);
	}

	private static Uri getContentUriByCategory(FileCategory cat) {
		Uri uri;
		String volumeName = "external";
		switch (cat) {
		case Doc:
		case Zip:
		case Apk:
			uri = Files.getContentUri(volumeName);
			break;
		case Music:
			uri = Audio.Media.getContentUri(volumeName);
			break;
		case Video:
			uri = Video.Media.getContentUri(volumeName);
			break;
		case Picture:
			uri = Images.Media.getContentUri(volumeName);
			break;
		default:
			uri = null;
		}
		return uri;
	}

	public static void refreshCategoryInfo(Context mContext) {
		for (FileCategory fc : sCategories) {
			setCategoryInfo(fc, 0, 0);
		}
		String volumeName = "external";
		Uri uri = Audio.Media.getContentUri(volumeName);
		refreshMediaCategory(FileCategory.Music, uri, mContext);
		uri = Video.Media.getContentUri(volumeName);
		refreshMediaCategory(FileCategory.Video, uri, mContext);
		uri = Images.Media.getContentUri(volumeName);
		refreshMediaCategory(FileCategory.Picture, uri, mContext);
		uri = Files.getContentUri(volumeName);
		refreshMediaCategory(FileCategory.Doc, uri, mContext);
		refreshMediaCategory(FileCategory.Zip, uri, mContext);
		refreshMediaCategory(FileCategory.Apk, uri, mContext);
	}

	private static void setCategoryInfo(FileCategory fc, long count, long size) {
		CategoryInfo info = mCategoryInfo.get(fc);
		if (info == null) {
			info = new CategoryInfo();
			mCategoryInfo.put(fc, info);
		}
		info.count = count;
		info.size = size;
	}

	private static boolean refreshMediaCategory(FileCategory fc, Uri uri,
			Context mContext) {
		String[] columns = new String[] { "COUNT(*)", "SUM(_size)" };
		Cursor c = mContext.getContentResolver().query(uri, columns,
				buildSelectionByCategory(fc), null, null);
		if (c == null) {
			Log.e(LOG_TAG, "fail to query uri:" + uri);
			return false;
		}

		if (c.moveToNext()) {
			setCategoryInfo(fc, c.getLong(0), c.getLong(1));
			c.close();
			return true;
		}

		return false;
	}

	public static HashSet<String> sDocMimeTypesSet = new HashSet<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("text/plain");
			add("application/pdf");
			add("application/msword");
			add("application/vnd.ms-excel");
			add("application/vnd.ms-excel");
		}
	};

	private static String buildDocSelection() {
		StringBuilder selection = new StringBuilder();
		Iterator<String> iter = sDocMimeTypesSet.iterator();
		while (iter.hasNext()) {
			selection.append("(" + FileColumns.MIME_TYPE + "=='" + iter.next()
					+ "') OR ");
		}
		return selection.substring(0, selection.lastIndexOf(")") + 1);
	}

	private static String buildSelectionByCategory(FileCategory cat) {
		String selection = null;
		switch (cat) {
		case Doc:
			selection = buildDocSelection();
			break;
		case Zip:
			selection = "(" + FileColumns.MIME_TYPE + " == '"
					+ "application/zip" + "')";
			break;
		case Apk:
			selection = FileColumns.DATA + " LIKE '%.apk'";
			break;
		default:
			selection = null;
		}
		return selection;
	}

}
