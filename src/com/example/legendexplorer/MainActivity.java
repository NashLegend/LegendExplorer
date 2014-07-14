package com.example.legendexplorer;

import java.util.ArrayList;
import java.util.Timer;

import com.example.legendexplorer.adapter.FilePagerAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.fragment.BaseFragment;
import com.example.legendexplorer.fragment.BookMarksFragment;
import com.example.legendexplorer.fragment.CategoriedFragment;
import com.example.legendexplorer.fragment.FilesFragment;
import com.example.legendexplorer.utils.StorageObserver;
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

	public static final int SearchFileItemFlag = 1;
	public static final int ToggleViewItemFlag = 2;
	public static final int AddFileItemFlag = 4;
	public static final int ToggleHiddleItemFlag = 8;
	public static final int RefreshListItemFlag = 16;
	public static final int AllFileListMask = 31;

	public static final int CopyFileItemFlag = 1;
	public static final int CutFileItemFlag = 2;
	public static final int DeleteFileItemFlag = 4;
	public static final int RenameFileItemFlag = 8;
	public static final int ZipFileItemFlag = 16;
	public static final int PropertyItemFlag = 32;
	public static final int AllOperationMask = 63;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

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
		filter.addAction(FileConst.Action_Set_File_Operation_ActionBar);
		filter.addAction(FileConst.Action_Set_File_View_ActionBar);
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
		case R.id.action_rename:
			renameFile();
			break;
		case R.id.action_zip:
			zipFile();
			break;
		case R.id.action_property:
			propertyFile();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void showFileOperationMenu(int exclu) {
		if (mMenu == null) {
			return;
		}
		searchView.setOnQueryTextListener(null);
		pager.setScrollEnabled(false);
		if (mMenu.findItem(R.id.action_copy) == null) {
			mMenu.clear();
			getMenuInflater().inflate(R.menu.fileop, mMenu);
		}
		if (exclu >= PropertyItemFlag) {
			mMenu.findItem(R.id.action_property).setVisible(false);
			exclu = exclu ^ PropertyItemFlag;
		} else {
			mMenu.findItem(R.id.action_property).setVisible(true);
		}
		if (exclu >= ZipFileItemFlag) {
			mMenu.findItem(R.id.action_zip).setVisible(false);
			exclu = exclu ^ ZipFileItemFlag;
		} else {
			mMenu.findItem(R.id.action_zip).setVisible(true);
		}
		if (exclu >= RenameFileItemFlag) {
			mMenu.findItem(R.id.action_rename).setVisible(false);
			exclu = exclu ^ RenameFileItemFlag;
		} else {
			mMenu.findItem(R.id.action_rename).setVisible(true);
		}
		if (exclu >= DeleteFileItemFlag) {
			mMenu.findItem(R.id.action_delete).setVisible(false);
			exclu = exclu ^ DeleteFileItemFlag;
		} else {
			mMenu.findItem(R.id.action_delete).setVisible(true);
		}
		if (exclu >= CutFileItemFlag) {
			mMenu.findItem(R.id.action_cut).setVisible(false);
			exclu = exclu ^ CutFileItemFlag;
		} else {
			mMenu.findItem(R.id.action_cut).setVisible(true);
		}
		if (exclu >= CopyFileItemFlag) {
			mMenu.findItem(R.id.action_copy).setVisible(false);
		} else {
			mMenu.findItem(R.id.action_copy).setVisible(true);
		}
	}

	public void showFileListMenu(int exclu) {
		if (mMenu == null) {
			return;
		}
		pager.setScrollEnabled(true);
		if (mMenu.findItem(R.id.action_search) == null) {
			mMenu.clear();
			getMenuInflater().inflate(R.menu.filelist, mMenu);
		}

		if (exclu >= RefreshListItemFlag) {
			mMenu.findItem(R.id.action_refresh).setVisible(false);
			exclu = exclu ^ RefreshListItemFlag;
		} else {
			mMenu.findItem(R.id.action_refresh).setVisible(true);
		}
		if (exclu >= ToggleHiddleItemFlag) {
			mMenu.findItem(R.id.action_toggle_hidden).setVisible(false);
			exclu = exclu ^ ToggleHiddleItemFlag;
		} else {
			mMenu.findItem(R.id.action_toggle_hidden).setVisible(true);
		}
		if (exclu >= AddFileItemFlag) {
			mMenu.findItem(R.id.action_new).setVisible(false);
			exclu = exclu ^ AddFileItemFlag;
		} else {
			mMenu.findItem(R.id.action_new).setVisible(true);
		}
		if (exclu >= ToggleViewItemFlag) {
			mMenu.findItem(R.id.action_viewmode).setVisible(false);
			exclu = exclu ^ ToggleViewItemFlag;
		} else {
			mMenu.findItem(R.id.action_viewmode).setVisible(true);
		}
		if (exclu >= SearchFileItemFlag) {
			mMenu.findItem(R.id.action_search).setVisible(false);
		} else {
			MenuItem searchItem = mMenu.findItem(R.id.action_search);
			searchItem.setVisible(true);
			searchView = (SearchView) searchItem.getActionView();
			searchItem.setOnActionExpandListener(onActionExpandListener);
			searchView.setOnQueryTextListener(onQueryTextListener);
		}
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

	private void renameFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Rename_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void zipFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Zip_File);
		adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
	}

	private void propertyFile() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Property_File);
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
			int mask = intent.getIntExtra(FileConst.Extra_Menu_Mask, 0);
			if (FileConst.Action_Set_File_Operation_ActionBar.equals(action)) {
				showFileOperationMenu(mask);
				return;
			} else if (FileConst.Action_Set_File_View_ActionBar.equals(action)) {
				showFileListMenu(mask);
				return;
			} else if (FileConst.Action_Quit_Search.equals(action)) {
				quitSearchFile();
				return;
			}
			adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
		}
	}

}
