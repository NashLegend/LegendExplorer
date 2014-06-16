package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.model.FileItem;
import com.example.legendexplorer.view.FileItemView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 书签视图
 * 
 * @author NashLegend
 */
public class BookMarksFragment extends BaseFragment implements OnClickListener,
		OnCheckedChangeListener {
	private FileListAdapter adapter;
	private ListView listView;
	private EditText pathText;
	private ImageButton backButton;
	private CheckBox selectAllButton;
	private boolean inSelectMode = false;

	public BookMarksFragment() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_file_explorer, null);
		listView = (ListView) view.findViewById(R.id.listview_files);
		pathText = (EditText) view.findViewById(R.id.edittext_file_path);
		backButton = (ImageButton) view
				.findViewById(R.id.imagebutton_file_back);
		selectAllButton = (CheckBox) view.findViewById(R.id.checkbox_file_all);

		backButton.setOnClickListener(this);
		selectAllButton.setOnCheckedChangeListener(this);
		selectAllButton.setVisibility(View.GONE);
		pathText.setKeyListener(null);

		adapter = new FileListAdapter(getActivity());
		listView.setAdapter(adapter);
		listView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// 长按进入选择模式
				change2SelectMode(view, position, id);
				return true;
			}
		});

		openBookMarks();

		return view;
	}

	/**
	 * 打开目录
	 * 
	 * @param file
	 *            要打开的文件夹
	 */
	public void openFolder(File file) {
		if (file.exists() && file.isDirectory()) {
			adapter.openFolder(file);
		}
	}

	/**
	 * 打开收藏夹目录
	 */
	public void openBookMarks() {
		ArrayList<FileItem> fileItems = getBookMarks();
		if (fileItems != null && fileItems.size() > 0) {
			adapter.setList(fileItems);
			adapter.notifyDataSetChanged();
		} else {
			// 没有收藏
		}
	}

	/**
	 * 读取收藏夹文件
	 */
	private ArrayList<FileItem> getBookMarks() {
		return null;
	}

	/**
	 * 返回上级目录
	 */
	private void back2ParentLevel() {
		File file = adapter.getCurrentDirectory();
		if (file != null && file.getParentFile() != null) {
			if (isInRootPlace(file.getParentFile())) {
				openBookMarks();
			} else {
				openFolder(file.getParentFile());
			}
		}
	}

	/**
	 * 选中当前目录所有文件
	 */
	private void selectAll() {
		adapter.selectAll();
	}

	/**
	 * 取消选中当前目录所有文件
	 */
	private void unselectAll() {
		adapter.unselectAll();
	}

	public void unselectCheckBox() {
		selectAllButton.setOnCheckedChangeListener(null);
		selectAllButton.setChecked(false);
		selectAllButton.setOnCheckedChangeListener(this);
	}

	/**
	 * @return 返回选中的文件列表
	 */
	public ArrayList<File> getSelectedFiles() {
		return adapter.getSelectedFiles();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.imagebutton_file_back) {
			back2ParentLevel();
		}
	}

	public EditText getPathText() {
		return pathText;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (selectAllButton.isChecked()) {
			selectAll();
		} else {
			unselectAll();
		}
	}

	public CheckBox getSelectAllButton() {
		return selectAllButton;
	}

	@Override
	public boolean doBackAction() {
		if (inSelectMode) {
			exitSelectMode();
			return true;
		} else {
			if (isInRootPlace()) {
				back2ParentLevel();
				return true;
			}
		}
		return false;
	}

	/**
	 * 是否在收藏夹根目录下
	 */
	private boolean isInRootPlace() {
		return true;
	}

	private boolean isInRootPlace(File file) {
		return true;
	}

	private void change2SelectMode(View view, int position, long id) {
		if (view instanceof FileItemView) {
			selectAllButton.setVisibility(View.VISIBLE);
			inSelectMode = true;
			FileItemView itemView = (FileItemView) view;
			itemView.getFileItem().setSelected(true);
			adapter.change2SelectMode();
			// TODO 修改ActionBar
		} else {

		}
	}

	private void exitSelectMode() {
		selectAllButton.setVisibility(View.GONE);
		inSelectMode = false;
		adapter.exitSelectMode();
		// TODO修改ActionBar
	}

	@Override
	public boolean doVeryAction(Intent intent) {
		// TODO 自动生成的方法存根
		return false;
	}

}
