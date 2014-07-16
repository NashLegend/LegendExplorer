package com.example.legendexplorer.db;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class BookmarkHelper {

	private final Context mContext;
	private FileDataBase mDbHelper;
	private SQLiteDatabase mSqlDB;

	public BookmarkHelper(Context context) {
		this.mContext = context;
	}

	public BookmarkHelper open() {
		mDbHelper = FileDataBase.getInstance(mContext);
		mSqlDB = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		if (mDbHelper != null) {
			mDbHelper.close();
		}
	}

	public boolean deleteBookmark(ArrayList<File> list) {
		try {
			mSqlDB.beginTransaction();
			for (File item : list) {
				deleteBookmark(item);
			}
			mSqlDB.setTransactionSuccessful();
		} finally {
			mSqlDB.endTransaction();
		}
		return true;
	}

	public boolean deleteBookmark(File item) {
		String whereClause = BookmarkColumn.FILE_PATH + "='"
				+ item.getAbsolutePath() + "'";
		Log.i("where", whereClause);
		int num = mSqlDB.delete(FileDataBase.TableBookmark, whereClause, null);
		return num > 0;
	}

	public boolean insertBookmark(ArrayList<FileItem> list) {
		try {
			mSqlDB.beginTransaction();
			for (FileItem item : list) {
				insert(item);
			}
			mSqlDB.setTransactionSuccessful();
		} finally {
			mSqlDB.endTransaction();
		}
		return true;
	}

	public boolean insert(FileItem item) {
		long ret = -1;
		ContentValues values = new ContentValues();
		values.put(BookmarkColumn.FILE_NAME, item.getName());
		values.put(BookmarkColumn.FILE_PATH, item.getAbsolutePath());
		ret = mSqlDB.insert(FileDataBase.TableBookmark, null, values);
		return ret != -1;
	}

	public ArrayList<FileItem> getBookmarks() {

		ArrayList<FileItem> list = new ArrayList<FileItem>();

		Cursor cursor = mSqlDB.query(FileDataBase.TableBookmark,
				BookmarkColumn.PROJECTION, null, null, null, null, null,
				String.valueOf(10));

		if (cursor.getCount() > 0) {
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				String filePath = cursor
						.getString(BookmarkColumn.FILE_PATH_COLUMN);
				FileItem item = new FileItem(filePath);
				item.setItemType(FileConst.Value_Item_Type_Bookmark);
				list.add(item);
			}
		}

		cursor.close();
		cursor = null;

		return list;

	}

	public boolean truncate() {
		int ret = mSqlDB.delete(FileDataBase.TableBookmark, null, null);
		return ret > 0 ? true : false;
	}

	public void initBookmarks() {
		// 当文件不存在时isDirectory和isFile都返回false
		ArrayList<FileItem> fileItems = new ArrayList<FileItem>();
		FileItem item_download = new FileItem(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
		if (item_download.exists() && item_download.isDirectory()) {
			fileItems.add(item_download);
		}
		FileItem item_camera = new FileItem(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
		if (item_camera.exists() && item_camera.isDirectory()) {
			fileItems.add(item_camera);
		}
		FileItem item_movie = new FileItem(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));
		if (item_movie.exists() && item_movie.isDirectory()) {
			fileItems.add(item_movie);
		}
		FileItem item_music = new FileItem(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
		if (item_music.exists() && item_music.isDirectory()) {
			fileItems.add(item_music);
		}
		FileItem item_picture = new FileItem(
				Environment
						.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
		if (item_picture.exists() && item_picture.isDirectory()) {
			fileItems.add(item_picture);
		}

		open();
		truncate();
		insertBookmark(fileItems);
		close();
	}
}
