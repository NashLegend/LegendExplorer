package com.example.legendexplorer.fragment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Files.FileColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 分类视图
 * 
 * @author NashLegend
 */
public class CategoriedFragment extends BaseFragment {
	protected View view;
	private static ScannerReceiver receiver;
	private FileCategoryHelper mFileCagetoryHelper;
	private FileListFragment listFragment;
	private FrameLayout frameLayout;

	private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();
	static {
		button2Category.put(R.id.category_music, FileCategory.Music);
		button2Category.put(R.id.category_video, FileCategory.Video);
		button2Category.put(R.id.category_picture, FileCategory.Picture);
		button2Category.put(R.id.category_document, FileCategory.Doc);
		button2Category.put(R.id.category_zip, FileCategory.Zip);
		button2Category.put(R.id.category_apk, FileCategory.Apk);
	}

	public enum FileCategory {
		All, Music, Video, Picture, Theme, Doc, Zip, Apk, Custom, Other, Favorite
	}

	public CategoriedFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {

			view = inflater.inflate(R.layout.layout_file_category, container,
					false);
			frameLayout = (FrameLayout) view
					.findViewById(R.id.content_category);
			mFileCagetoryHelper = new FileCategoryHelper(getActivity());
			setupClick();
			updateUI();
			registerReceiver();
		} else {
			if (view.getParent() != null) {
				((ViewGroup) view.getParent()).removeView(view);
			}
		}
		return view;
	}

	public void updateUI() {
		refreshData();
	}

	public void refreshData() {
		mFileCagetoryHelper.refreshCategoryInfo();
		long size = 0;
		for (FileCategory fc : FileCategoryHelper.sCategories) {
			FileCategoryHelper.CategoryInfo categoryInfo = mFileCagetoryHelper
					.getCategoryInfos().get(fc);
			setCategoryCount(fc, categoryInfo.count);
			if (fc == FileCategory.Other)
				continue;

			// setCategorySize(fc, categoryInfo.size);
			// setCategoryBarValue(fc, categoryInfo.size);
			size += categoryInfo.size;
		}

	}

	public void setupClick() {
		setupClick(R.id.category_music);
		setupClick(R.id.category_video);
		setupClick(R.id.category_picture);
		setupClick(R.id.category_document);
		setupClick(R.id.category_zip);
		setupClick(R.id.category_apk);
	}

	private void setupClick(int id) {
		View button = view.findViewById(id);
		button.setOnClickListener(onClickListener);
	}

	private static int getCategoryCountId(FileCategory fc) {
		switch (fc) {
		case Music:
			return R.id.category_music_count;
		case Video:
			return R.id.category_video_count;
		case Picture:
			return R.id.category_picture_count;
		case Doc:
			return R.id.category_document_count;
		case Zip:
			return R.id.category_zip_count;
		case Apk:
			return R.id.category_apk_count;
		default:
			break;
		}
		return 0;
	}

	// private void setCategorySize(FileCategory fc, long size) {
	// int txtId = 0;
	// int resId = 0;
	// switch (fc) {
	// case Music:
	// txtId = R.id.category_legend_music;
	// resId = R.string.category_music;
	// break;
	// case Video:
	// txtId = R.id.category_legend_video;
	// resId = R.string.category_video;
	// break;
	// case Picture:
	// txtId = R.id.category_legend_picture;
	// resId = R.string.category_picture;
	// break;
	// break;
	// case Doc:
	// txtId = R.id.category_legend_document;
	// resId = R.string.category_document;
	// break;
	// case Zip:
	// txtId = R.id.category_legend_zip;
	// resId = R.string.category_zip;
	// break;
	// case Apk:
	// txtId = R.id.category_legend_apk;
	// resId = R.string.category_apk;
	// break;
	// case Other:
	// txtId = R.id.category_legend_other;
	// resId = R.string.category_other;
	// break;
	// default:
	// break;
	// }
	//
	// if (txtId == 0 || resId == 0)
	// return;
	//
	// setTextView(txtId,
	// getString(resId) + ":" + FileUtil.convertStorage(size));
	// }

	private void setCategoryCount(FileCategory fc, long count) {
		int id = getCategoryCountId(fc);
		if (id == 0)
			return;
		setTextView(id, "(" + count + ")");
	}

	private void setTextView(int id, String t) {
		TextView text = (TextView) view.findViewById(id);
		text.setText(t);
	}

	public void registerReceiver() {
		receiver = new ScannerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addDataScheme("file");
		getActivity().registerReceiver(receiver, filter);
	}

	View.OnClickListener onClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			FileCategory f = button2Category.get(v.getId());
			if (f != null) {
				showCategoryList(f);
			}
		}
	};

	public void showCategoryList(FileCategory f) {
		frameLayout.setVisibility(View.VISIBLE);
		Cursor cursor = mFileCagetoryHelper.query(f);
		listFragment = new FileListFragment();
		listFragment.setCursor(cursor);
		Bundle bundle = new Bundle();
		bundle.putInt(FileConst.Extra_Explore_Type,
				FileConst.Value_Explore_Type_Categories);
		listFragment.setArguments(bundle);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.content_category, listFragment);
		transaction.commit();
	}

	public boolean hideCategoryList() {
		if (listFragment == null) {
			return false;
		} else {
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.remove(listFragment);
			transaction.commit();
			listFragment = null;
			frameLayout.setVisibility(View.GONE);
			return true;
		}
	}

	private static final int MSG_FILE_CHANGED_TIMER = 100;
	private Timer timer;
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_FILE_CHANGED_TIMER:
				updateUI();
				break;
			}
			super.handleMessage(msg);
		}

	};

	synchronized public void notifyFileChanged() {
		if (timer != null) {
			timer.cancel();
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				timer = null;
				Message message = new Message();
				message.what = MSG_FILE_CHANGED_TIMER;
				handler.sendMessage(message);
			}

		}, 1000);
	}

	@Override
	public void onDestroy() {
		Log.i("cat", "destroy");
		getActivity().unregisterReceiver(receiver);
		super.onDestroy();
	}

	@Override
	public boolean doBackAction() {
		if (listFragment != null) {
			hideCategoryList();
			return true;
		}
		return false;
	}

	@Override
	public boolean doVeryAction(Intent intent) {
		// TODO 自动生成的方法存根
		return false;
	}

	public class ScannerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i("cat", action);
			if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)
					|| action.equals(Intent.ACTION_MEDIA_MOUNTED)
					|| action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				notifyFileChanged();
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {

			}
		}
	}

	public static class FileCategoryHelper {

		public static final int COLUMN_ID = 0;

		public static final int COLUMN_PATH = 1;

		public static final int COLUMN_SIZE = 2;

		public static final int COLUMN_DATE = 3;

		private static final String LOG_TAG = "cat";

		public class CategoryInfo {
			public long count;

			public long size;
		}

		private HashMap<FileCategory, CategoryInfo> mCategoryInfo = new HashMap<FileCategory, CategoryInfo>();

		public HashMap<FileCategory, CategoryInfo> getCategoryInfos() {
			return mCategoryInfo;
		}

		public static FileCategory[] sCategories = new FileCategory[] {
				FileCategory.Music, FileCategory.Video, FileCategory.Picture,
				FileCategory.Theme, FileCategory.Doc, FileCategory.Zip,
				FileCategory.Apk, FileCategory.Other };

		private FileCategory mCategory;

		private Context mContext;

		public FileCategoryHelper(Context context) {
			mContext = context;
			mCategory = FileCategory.All;
		}

		public Cursor query(FileCategory fc) {
			Uri uri = getContentUriByCategory(fc);
			String selection = buildSelectionByCategory(fc);
			String sortOrder = null;

			if (uri == null) {
				return null;
			}

			String[] columns = new String[] { FileColumns._ID,
					FileColumns.DATA, FileColumns.SIZE,
					FileColumns.DATE_MODIFIED };

			return mContext.getContentResolver().query(uri, columns, selection,
					null, sortOrder);
		}

		private Uri getContentUriByCategory(FileCategory cat) {
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

		public void refreshCategoryInfo() {
			for (FileCategory fc : sCategories) {
				setCategoryInfo(fc, 0, 0);
			}
			String volumeName = "external";
			Uri uri = Audio.Media.getContentUri(volumeName);
			refreshMediaCategory(FileCategory.Music, uri);
			uri = Video.Media.getContentUri(volumeName);
			refreshMediaCategory(FileCategory.Video, uri);
			uri = Images.Media.getContentUri(volumeName);
			refreshMediaCategory(FileCategory.Picture, uri);
			uri = Files.getContentUri(volumeName);
			refreshMediaCategory(FileCategory.Theme, uri);
			refreshMediaCategory(FileCategory.Doc, uri);
			refreshMediaCategory(FileCategory.Zip, uri);
			refreshMediaCategory(FileCategory.Apk, uri);
		}

		private void setCategoryInfo(FileCategory fc, long count, long size) {
			CategoryInfo info = mCategoryInfo.get(fc);
			if (info == null) {
				info = new CategoryInfo();
				mCategoryInfo.put(fc, info);
			}
			info.count = count;
			info.size = size;
		}

		private boolean refreshMediaCategory(FileCategory fc, Uri uri) {
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
			{
				add("text/plain");
				add("text/plain");
				add("application/pdf");
				add("application/msword");
				add("application/vnd.ms-excel");
				add("application/vnd.ms-excel");
			}
		};

		private String buildDocSelection() {
			StringBuilder selection = new StringBuilder();
			Iterator<String> iter = sDocMimeTypesSet.iterator();
			while (iter.hasNext()) {
				selection.append("(" + FileColumns.MIME_TYPE + "=='"
						+ iter.next() + "') OR ");
			}
			return selection.substring(0, selection.lastIndexOf(")") + 1);
		}

		private String buildSelectionByCategory(FileCategory cat) {
			String selection = null;
			switch (cat) {
			case Theme:
				selection = FileColumns.DATA + " LIKE '%.mtz'";
				break;
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

}
