package com.example.legendexplorer;

import java.util.ArrayList;
import java.util.Timer;

import com.example.legendexplorer.adapter.FilePagerAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.fragment.BaseFragment;
import com.example.legendexplorer.fragment.BookMarksFragment;
import com.example.legendexplorer.fragment.CategoriedFragment;
import com.example.legendexplorer.fragment.FilesFragment;
import com.example.legendexplorer.view.FolderViewPager;
import com.example.legendutils.Tools.TimerUtil;
import com.example.legendutils.Tools.ToastUtil;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class MainActivity extends Activity {

	private FolderViewPager pager;
	private FilePagerAdapter adapter;
	private ArrayList<BaseFragment> list;
	private FilesFragment filesFragment;
	private BookMarksFragment bookMarksFragment;
	private CategoriedFragment classifiedFragment;
	private FileBroadcastReceiver fileBroadcastReceiver;
	private Menu mMenu;
	private SearchView searchView;
	private StorageObserver observer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		observer = new StorageObserver(Environment
//				.getExternalStorageDirectory().getAbsolutePath(),
//				FileObserver.CREATE | FileObserver.DELETE
//						| FileObserver.MOVED_FROM | FileObserver.MOVED_TO);
//		observer.startWatching();

		filesFragment = new FilesFragment();
		bookMarksFragment = new BookMarksFragment();
		classifiedFragment = new CategoriedFragment();

		list = new ArrayList<BaseFragment>();
		list.add(filesFragment);
		list.add(bookMarksFragment);
		list.add(classifiedFragment);

		pager = (FolderViewPager) findViewById(R.id.pager);
		adapter = new FilePagerAdapter(getFragmentManager());
		adapter.setList(list);
		pager.setAdapter(adapter);
		pager.setScrollEnabled(true);

		IntentFilter filter = new IntentFilter();
		filter.addAction(FileConst.Action_Open_Folder);
		filter.addAction(FileConst.Action_FileItem_Long_Click);
		filter.addAction(FileConst.Action_FileItem_Unselect);
		filter.addAction(FileConst.Action_Switch_2_Select_Mode);
		filter.addAction(FileConst.Action_Exit_Select_Mode);
		filter.addAction(FileConst.Action_File_Operation_Done);
		filter.addAction(FileConst.Action_Quit_Search);
		fileBroadcastReceiver = new FileBroadcastReceiver();
		registerReceiver(fileBroadcastReceiver, filter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		getMenuInflater().inflate(R.menu.filelist, menu);
		MenuItem searchItem = mMenu.findItem(R.id.action_search);
		searchView = (SearchView) searchItem.getActionView();
		searchItem.setOnActionExpandListener(onActionExpandListener);
		searchView.setOnQueryTextListener(onQueryTextListener);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_new:
			addNewFile();
			break;
		case R.id.action_refresh:
			refreshFileList();
			break;
		case R.id.action_viewmode:
			toggleViewMode();
			break;
		case R.id.action_toggle_hidden:
			toggleShowHidden();
			break;
		case R.id.action_copy:
			copyFile();
			break;
		case R.id.action_cut:
			moveFile();
			break;
		case R.id.action_delete:
			deleteFile();
			break;
		case R.id.action_zip:
			zipFile();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showFileOperationMenu() {
		searchView.setOnQueryTextListener(null);
		pager.setScrollEnabled(false);
		mMenu.clear();
		getMenuInflater().inflate(R.menu.fileop, mMenu);
	}

	public void showFileListMenu() {
		pager.setScrollEnabled(true);
		mMenu.clear();
		getMenuInflater().inflate(R.menu.filelist, mMenu);
		MenuItem searchItem = mMenu.findItem(R.id.action_search);
		searchItem.setOnActionExpandListener(onActionExpandListener);
		searchView = (SearchView) searchItem.getActionView();
		searchView.setOnQueryTextListener(onQueryTextListener);
	}

	private void toggleViewMode() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Toggle_View_Mode);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void toggleShowHidden() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Toggle_Show_Hidden);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void copyFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Copy_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void moveFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Move_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void deleteFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Delete_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void zipFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Zip_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void addNewFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Add_New_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void refreshFileList() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Refresh_FileList);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void searchFile(String query) {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Search_File);
		intent.putExtra(FileConst.Key_Search_File_Query, query);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void quitSearchFile() {
		MenuItem searchItem = mMenu.findItem(R.id.action_search);
		if (searchItem != null) {
			searchItem.collapseActionView();
		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (doBackAction()) {
				return true;
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	private boolean doBackAction() {
		return adapter.getItem(pager.getCurrentItem()).doBackAction();
	}
	
	@Override
	protected void onDestroy() {
		unregisterReceiver(fileBroadcastReceiver);
		super.onDestroy();
	}

	private OnActionExpandListener onActionExpandListener = new OnActionExpandListener() {

		@Override
		public boolean onMenuItemActionExpand(MenuItem item) {
			return true;
		}

		@Override
		public boolean onMenuItemActionCollapse(MenuItem item) {
			searchFile("");
			return true;
		}
	};

	private OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {
		private int delayMillis = 300;
		private String query = "";
		private Timer timer;
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				searchFile(query);
			}
		};

		@Override
		public boolean onQueryTextSubmit(String query) {
			ToastUtil.showToast(getApplicationContext(), query);
			return false;
		}

		@Override
		public boolean onQueryTextChange(String newText) {
			TimerUtil.clearTimeOut(timer);
			query = newText;
			timer = TimerUtil.setTimeOut(runnable, delayMillis);
			return false;
		}
	};

	class FileBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (FileConst.Action_Switch_2_Select_Mode.equals(action)) {
				showFileOperationMenu();
				return;
			} else if (FileConst.Action_Exit_Select_Mode.equals(action)) {
				showFileListMenu();
				return;
			} else if (FileConst.Action_Quit_Search.equals(action)) {
				quitSearchFile();
				return;
			}
			adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
		}
	}

}
