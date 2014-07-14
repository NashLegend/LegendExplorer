package com.example.legendexplorer.application;

import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.db.BookmarkHelper;
import com.example.legendexplorer.utils.SharePreferencesUtil;

import android.app.Application;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.widget.ImageView;

public class ExplorerApplication extends Application {

	public static Context GlobalContext;

	public ExplorerApplication() {

	}

	@Override
	public void onCreate() {
		super.onCreate();
		GlobalContext = this;
//		MediaScannerConnection.scanFile(getApplicationContext(),
//				new String[] { Environment.getExternalStorageDirectory()
//						.getAbsolutePath() }, null, null);
		if (SharePreferencesUtil.readBoolean(FileConst.Key_Is_First_Open, true)) {
			SharePreferencesUtil
					.saveBoolean(FileConst.Key_Is_First_Open, false);
			BookmarkHelper helper = new BookmarkHelper(this);
			helper.initBookmarks();
		}
	}

	public static Context getGlobalContext() {
		return GlobalContext;
	}
}
