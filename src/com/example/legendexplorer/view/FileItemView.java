package com.example.legendexplorer.view;

import java.io.IOException;

import com.example.legendexplorer.ImageBrowseActivity;
import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;
import com.example.legendexplorer.utils.IconContainer;
import com.example.legendexplorer.view.FileGridItemView.IconLoadTask;
import com.example.legendutils.Tools.FileUtil;

import android.app.Service;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

/**
 * 文件列表单个item的view
 * 
 * @author NashLegend
 */
public class FileItemView extends FrameLayout implements OnClickListener,
		OnCheckedChangeListener, OnLongClickListener {
	private ImageView icon;
	private ImageView symbolView;
	private TextView title;
	private CheckBox checkBox;
	private ViewGroup rootFileItemView;
	private FileListAdapter adapter;
	private FileItem fileItem;
	private IconLoadTask task;

	public FileItemView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.view_file_item, this);
		icon = (ImageView) findViewById(R.id.image_file_icon);
		symbolView = (ImageView) findViewById(R.id.image_file_sym_icon);
		title = (TextView) findViewById(R.id.text_file_title);
		rootFileItemView = (ViewGroup) findViewById(R.id.rootFileItemView);
		checkBox = (CheckBox) findViewById(R.id.checkbox_file_item_select);
		setOnClickListener(this);
		setLongClickable(true);
	}

	public FileItem getFileItem() {
		return fileItem;
	}

	public void setFileItem(FileItem fileItem, FileListAdapter adapter) {
		if (!fileItem.equals(this.fileItem)) {
			this.fileItem = fileItem;
			showFileIcon();
		}
		this.fileItem = fileItem;
		this.adapter = adapter;
		title.setText(fileItem.getName());
		toggleSelectState();
		if (fileItem.isInSelectMode()) {
			checkBox.setVisibility(View.VISIBLE);
			setOnLongClickListener(null);
		} else {
			checkBox.setVisibility(View.GONE);
			setOnLongClickListener(this);
		}
	}

	/**
	 * 显示文件图标
	 */
	private void showFileIcon() {
		icon.setImageResource(fileItem.getIcon());
		int tp = fileItem.getFileType();
		if (tp == FileItem.FILE_TYPE_APK || tp == FileItem.FILE_TYPE_AUDIO
				|| tp == FileItem.FILE_TYPE_IMAGE
				|| tp == FileItem.FILE_TYPE_VIDEO) {
			if (task != null && task.getStatus() == Status.RUNNING) {
				task.cancel(true);
			}
			Bitmap bmp = IconContainer.get(fileItem);
			if (bmp == null) {
				task = new IconLoadTask();
				task.execute(fileItem);
			} else {
				icon.setImageBitmap(bmp);
			}
		}
		try {
			if (fileItem.isDirectory() && FileUtil.isSymboliclink(fileItem)) {
				symbolView.setVisibility(View.VISIBLE);
			} else {
				symbolView.setVisibility(View.GONE);
			}
		} catch (IOException e) {
			symbolView.setVisibility(View.GONE);
		}
	}

	class IconLoadTask extends AsyncTask<FileItem, Integer, Bitmap> {

		FileItem originalFile;

		@Override
		protected Bitmap doInBackground(FileItem... params) {
			originalFile = params[0];
			Bitmap bmp = FileUtil.extractFileThumbnail(originalFile,
					getContext());
			if (bmp != null) {
				IconContainer.put(originalFile, bmp);
			}
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			if (fileItem.equals(this.originalFile)) {
				if (result != null) {
					icon.setImageBitmap(result);
				} else {
					icon.setImageResource(fileItem.getIcon());
				}
			}
			super.onPostExecute(result);
		}

	}

	/**
	 * 切换选中、未选中状态,fileItem.setSelected(boolean)先发生;
	 */
	public void toggleSelectState() {
		toggleSelectState(false);
	}

	private void toggleSelectState(boolean manual) {
		if (fileItem.isSelected()) {
			rootFileItemView
					.setBackgroundResource(R.drawable.bg_file_item_select);
			if (manual && fileItem.isInSelectMode()) {
				Intent intent = new Intent();
				intent.setAction(FileConst.Action_FileItem_Select);
				getContext().sendBroadcast(intent);
			}
		} else {
			rootFileItemView
					.setBackgroundResource(R.drawable.bg_file_item_normal);
			if (manual && fileItem.isInSelectMode()) {
				Intent intent = new Intent();
				intent.setAction(FileConst.Action_FileItem_Unselect);
				getContext().sendBroadcast(intent);
			}
		}
		checkBox.setOnCheckedChangeListener(null);
		checkBox.setChecked(fileItem.isSelected());
		checkBox.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() != R.id.checkbox_file_item_select) {
			if (fileItem.isInSelectMode()) {
				selectOne();
			} else {
				if (fileItem.isDirectory()) {
					openFolder();
				} else {
					openFile();
				}
			}

		}
	}

	/**
	 * 打开文件
	 */
	private void openFile() {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		Uri data = Uri.fromFile(fileItem);
		intent.setDataAndType(data, "*/*");
		switch (fileItem.getFileType()) {
		case FileItem.FILE_TYPE_APK:
			intent.setDataAndType(data,
					"application/vnd.android.package-archive");
			break;
		case FileItem.FILE_TYPE_IMAGE:
			intent.setDataAndType(data, "image/*");
			Intent intentImage=new Intent();
			intentImage.putExtra(FileConst.Extra_File_Path, fileItem.getAbsolutePath());
			intentImage.setClass(getContext(), ImageBrowseActivity.class);
			getContext().startActivity(intentImage);
			return;
		case FileItem.FILE_TYPE_AUDIO:
			intent.putExtra("oneshot", 0);
			intent.putExtra("configchange", 0);
			intent.setDataAndType(data, "audio/*");
			break;
		case FileItem.FILE_TYPE_TXT:
			intent.setDataAndType(data, "text/plain");
			break;
		case FileItem.FILE_TYPE_VIDEO:
			intent.putExtra("oneshot", 0);
			intent.putExtra("configchange", 0);
			intent.setDataAndType(data, "video/*");
			break;
		case FileItem.FILE_TYPE_ZIP:
			intent.setDataAndType(data, "application/zip");
			break;
		case FileItem.FILE_TYPE_WORD:
			intent.setDataAndType(data, "application/msword");
			break;
		case FileItem.FILE_TYPE_PPT:
			intent.setDataAndType(data, "application/vnd.ms-powerpoint");
			break;
		case FileItem.FILE_TYPE_EXCEL:
			intent.setDataAndType(data, "application/vnd.ms-excel");
			break;
		case FileItem.FILE_TYPE_HTML:
			intent.setDataAndType(data, "text/html");
			break;
		case FileItem.FILE_TYPE_PDF:
			intent.setDataAndType(data, "application/pdf");
			break;
		case FileItem.FILE_TYPE_TORRENT:
			intent.setDataAndType(data, "torrent/*");
			break;
		case FileItem.FILE_TYPE_CHM:
			intent.setDataAndType(data, "application/mshelp");
			break;

		default:
			break;
		}
		try {
			getContext().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(getContext(), "Cannot open this file type",
					Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

	public void selectOne() {
		if (fileItem.isInSelectMode()) {
			fileItem.setSelected(!fileItem.isSelected());
			toggleSelectState(true);
		}
	}

	public void openFolder() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_Open_Folder);
		intent.putExtra(FileConst.Extra_File_Path, fileItem.getAbsolutePath());
		intent.putExtra(FileConst.Extra_Item_Type, fileItem.getItemType());
		getContext().sendBroadcast(intent);
	}

	public FileListAdapter getAdapter() {
		return adapter;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		fileItem.setSelected(isChecked);
		toggleSelectState(true);
	}

	@Override
	public boolean onLongClick(View v) {
		if (FileUtil.isInExternalStorage(fileItem)) {
			Vibrator vibrator = (Vibrator) getContext().getSystemService(
					Service.VIBRATOR_SERVICE);
			vibrator.vibrate(20);

			fileItem.setSelected(true);

			Intent intent = new Intent();
			intent.setAction(FileConst.Action_FileItem_Long_Click);
			getContext().sendBroadcast(intent);
		}
		return false;
	}
}
