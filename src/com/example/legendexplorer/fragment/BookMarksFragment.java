package com.example.legendexplorer.fragment;

import java.io.File;

import com.example.legendexplorer.MainActivity;
import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;
import com.example.legendexplorer.view.DropDownAncestorList;
import com.example.legendexplorer.view.DropDownAncestorList.OnAncestorClickListener;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;

/**
 * 书签视图
 * 
 * @author NashLegend
 */
public class BookMarksFragment extends FilesFragment {
	private String pathPreffix = FileConst.Value_File_Path_Never_Existed;

	public BookMarksFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.layout_file_bookmark, container,
					false);
			pathText = (EditText) view.findViewById(R.id.edittext_file_path);

			backButton = (ImageButton) view
					.findViewById(R.id.imagebutton_file_back);
			selectAllButton = (CheckBox) view
					.findViewById(R.id.checkbox_file_all);
			backButton.setOnClickListener(this);
			selectAllButton.setOnCheckedChangeListener(this);
			selectAllButton.setVisibility(View.GONE);
			pathText.setKeyListener(null);
			pathText.setOnClickListener(this);

			ancestorList = new DropDownAncestorList(getActivity());
			ancestorList
					.setOnAncestorClickListener(new OnAncestorClickListener() {

						@Override
						public void onClick(String path) {
							openAncestorFolder(new File(path));
							popupWindow.dismiss();
						}
					});

			popupWindow = new PopupWindow(ancestorList,
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
			popupWindow.setTouchable(true);
			popupWindow.setOutsideTouchable(true);
			popupWindow.setBackgroundDrawable(new BitmapDrawable(
					getResources(), (Bitmap) null));

			openBookMarks();
		} else {
			if (view.getParent() != null) {
				((ViewGroup) view.getParent()).removeView(view);
			}
		}

		return view;
	}

	@Override
	protected void change2SelectMode() {
		if (fakeBackStack.size() > 0) {
			selectAllButton.setVisibility(View.VISIBLE);
			inSelectMode = true;
			fakeBackStack.get(fakeBackStack.size() - 1).change2SelectMode();

			Intent intent = new Intent();
			int mask = 0;
			if (fakeBackStack.size() > 0) {
				FileListFragment fragment = fakeBackStack.get(fakeBackStack
						.size() - 1);
				if (fragment.getFilePath()
						.equals(FileConst.Value_Bookmark_Path)) {
					mask = MainActivity.ZipFileItemFlag
							| MainActivity.RenameFileItemFlag
							| MainActivity.CutFileItemFlag;
				} else {
					mask = 0;
				}
			}
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_Operation_ActionBar);
			getActivity().sendBroadcast(intent);
		}
	}

	@Override
	protected void exitSelectMode() {
		if (fakeBackStack.size() > 0) {
			selectAllButton.setVisibility(View.GONE);
			selectAllButton.setChecked(false);
			inSelectMode = false;
			fakeBackStack.get(fakeBackStack.size() - 1).exitSelectMode();

			Intent intent = new Intent();
			int mask = 0;
			if (fakeBackStack.size() > 0) {
				FileListFragment fragment = fakeBackStack.get(fakeBackStack
						.size() - 1);
				if (fragment.getFilePath()
						.equals(FileConst.Value_Bookmark_Path)) {
					mask = MainActivity.AddFileItemFlag;
				} else {
					mask = 0;
				}
			}
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_View_ActionBar);
			getActivity().sendBroadcast(intent);
		}
	}

	/*
	 * @see
	 * com.example.legendexplorer.fragment.FilesFragment#openFolder(java.io.
	 * File, int)
	 */
	public void openFolder(File file, int animation) {

		if (file == null || !file.isDirectory()) {
			throw new NullPointerException(
					"openFolder://file.exists() && file.isDirectory()");
		}

		if (file.equals(new File(pathPreffix))) {
			openBookMarks();
			return;
		}

		FileListFragment fragment = new FileListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(FileConst.Extra_File_Path, file.getAbsolutePath());
		bundle.putString(FileConst.Extra_Path_Preffix, pathPreffix);
		bundle.putInt(FileConst.Extra_Item_Type,
				FileConst.Value_Item_Type_File_Or_Folder);
		bundle.putInt(FileConst.Extra_Explore_Type,
				FileConst.Value_Explore_Type_Bookmarks);
		fragment.setArguments(bundle);

		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.setTransition(animation);
		transaction.replace(R.id.content_bookmark, fragment,
				file.getAbsolutePath());
		transaction.commit();
		fakeBackStack.add(fragment);
		pathText.setText(fragment.getDisplayedFilePath());

		Intent intent = new Intent();
		int mask = 0;
		intent.putExtra(FileConst.Extra_Menu_Mask, mask);
		intent.setAction(FileConst.Action_Set_File_View_ActionBar);
		getActivity().sendBroadcast(intent);
	}

	/**
	 * 打开目录
	 * 
	 * @param file
	 *            要打开的文件夹
	 */
	public void openFolder(File file) {
		openFolder(file, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
	}

	/**
	 * 打开收藏夹目录
	 */
	public void openBookMarks() {
		pathPreffix = FileConst.Value_File_Path_Never_Existed;
		fakeBackStack.clear();
		FileListFragment fragment = new FileListFragment();
		Bundle bundle = new Bundle();
		bundle.putString(FileConst.Extra_File_Path,
				FileConst.Value_Bookmark_Path);
		bundle.putString(FileConst.Extra_Path_Preffix, pathPreffix);
		bundle.putInt(FileConst.Extra_Item_Type,
				FileConst.Value_Item_Type_Bookmark);
		bundle.putInt(FileConst.Extra_Explore_Type,
				FileConst.Value_Explore_Type_Bookmarks);
		fragment.setArguments(bundle);
		FragmentTransaction transaction = getFragmentManager()
				.beginTransaction();
		transaction.replace(R.id.content_bookmark, fragment,
				FileConst.Value_Bookmark_Path);
		transaction.commit();
		fakeBackStack.add(fragment);
		pathText.setText(fragment.getDisplayedFilePath());

		Intent intent = new Intent();
		int mask = MainActivity.AddFileItemFlag;
		intent.putExtra(FileConst.Extra_Menu_Mask, mask);
		intent.setAction(FileConst.Action_Set_File_View_ActionBar);
		getActivity().sendBroadcast(intent);
	}

	/**
	 * 回退
	 */
	protected void backStack() {
		if (fakeBackStack.size() > 1) {
			fakeBackStack.remove(fakeBackStack.size() - 1);
			FileListFragment fragment = fakeBackStack
					.get(fakeBackStack.size() - 1);
			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction
					.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
			transaction.replace(R.id.content_bookmark, fragment,
					fragment.getDisplayedFilePath());
			transaction.commit();
			pathText.setText(fragment.getDisplayedFilePath());

			Intent intent = new Intent();
			int mask = 0;
			if (fragment.getFilePath().equals(FileConst.Value_Bookmark_Path)) {
				mask = MainActivity.AddFileItemFlag;
			} else {
				mask = 0;
			}
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_View_ActionBar);
			getActivity().sendBroadcast(intent);

		} else {
			// do nothing
		}
	}

	@Override
	protected void back2ParentLevel() {
		backStack();
	}

	@Override
	protected void invokeAncestorList() {
		String path = fakeBackStack.get(fakeBackStack.size() - 1).getFilePath();
		if (FileConst.Value_Bookmark_Path.equals(path)) {
			return;
		}
		File file = new File(path);
		ancestorList
				.setupList(file, pathPreffix, FileConst.Value_Bookmark_Path);
		popupWindow.showAsDropDown(pathText);
	}

	@Override
	protected void doOpenFolderAction(Intent intent) {
		int tp = intent.getIntExtra(FileConst.Extra_Item_Type,
				FileConst.Value_Item_Type_File_Or_Folder);
		String path = intent.getStringExtra(FileConst.Extra_File_Path);
		File file = new File(path);
		if (file.getParentFile() != null) {
			if (tp == FileConst.Value_Item_Type_Bookmark) {
				if (pathPreffix == FileConst.Value_File_Path_Never_Existed) {
					pathPreffix = new File(path).getParent();
					if (pathPreffix.lastIndexOf("/") != pathPreffix.length() - 1) {
						pathPreffix += "/";
					}
				}
			}
		}
		openFolder(file);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		if (isVisibleToUser) {
			Intent intent = new Intent();
			int mask = MainActivity.AddFileItemFlag;
			if (fakeBackStack.size() > 0) {
				FileListFragment fragment = fakeBackStack.get(fakeBackStack
						.size() - 1);
				if (fragment.getFilePath()
						.equals(FileConst.Value_Bookmark_Path)) {
					mask = MainActivity.AddFileItemFlag;
				} else {
					mask = 0;
				}
			}
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_View_ActionBar);
			getActivity().sendBroadcast(intent);
		}
	}

}
