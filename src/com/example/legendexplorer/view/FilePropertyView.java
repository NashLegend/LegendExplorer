package com.example.legendexplorer.view;

import java.io.File;
import java.util.Calendar;

import com.example.legendexplorer.R;
import com.example.legendutils.Tools.FileUtil;

import android.content.Context;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FilePropertyView extends FrameLayout {

	private TextView fileNameTextView;
	private TextView fileTypeTextView;
	private TextView filePathTextView;
	private TextView fileVolumeTextView;
	private TextView fileNumberTextView;
	private TextView fileTimeTextView;

	public View fileNamelayout;
	public View fileTypelayout;
	public View filePathlayout;
	public View fileVolumelayout;
	public View fileNumberlayout;
	public View fileTimelayout;

	loadPropertyTask task;

	private File[] files;

	public FilePropertyView(Context context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.dialog_file_property, this);
		fileNameTextView = (TextView) findViewById(R.id.text_filename);
		fileTypeTextView = (TextView) findViewById(R.id.text_filetype);
		filePathTextView = (TextView) findViewById(R.id.text_filepath);
		fileVolumeTextView = (TextView) findViewById(R.id.text_filevolume);
		fileNumberTextView = (TextView) findViewById(R.id.text_fileinvolving);
		fileTimeTextView = (TextView) findViewById(R.id.text_modified_time);
		fileNamelayout = findViewById(R.id.layout_filename);
		fileTypelayout = findViewById(R.id.layout_filetype);
		filePathlayout = findViewById(R.id.layout_filepath);
		fileVolumelayout = findViewById(R.id.layout_filevolume);
		fileNumberlayout = findViewById(R.id.layout_filenumber);
		fileTimelayout = findViewById(R.id.layout_modifiedtime);
	}

	public void setFiles(File[] fs, boolean allInOneFolder) {
		files = fs;
		if (files == null || files.length == 0) {
			return;
		}
		boolean needTask = false;

		if (files.length == 1) {
			File file = files[0];
			fileNameTextView.setText(file.getName());
			fileTypeTextView.setText(FileUtil.getFileSuffix(file));
			filePathTextView.setText(file.getParent());
			if (file.isDirectory()) {
				fileVolumeTextView.setText("Calculating");
				fileNumberTextView.setText("Calculating");
				needTask = true;
			} else {
				fileVolumeTextView.setText(FileUtil.convertStorage(file
						.length()));
				fileNumberlayout.setVisibility(View.GONE);
			}
			fileTimeTextView.setText(file.lastModified() + "");
		} else {
			fileNamelayout.setVisibility(View.GONE);
			fileTypelayout.setVisibility(View.GONE);
			fileTimelayout.setVisibility(View.GONE);
			if (allInOneFolder) {
				filePathTextView.setText(files[0].getParent());
			} else {
				filePathTextView.setText("N/A");
			}
			fileVolumeTextView.setText("Calculating");
			fileNumberTextView.setText("Calculating");
			needTask = true;
		}
		if (needTask) {
			task = new loadPropertyTask();
			task.execute("");
		}
	}

	public void cancel() {
		if (task != null && task.getStatus() == Status.RUNNING) {
			task.cancel(true);
		}
	}

	class loadPropertyTask extends AsyncTask<String, Integer, Integer> {

		long fsize = 0l;
		int fnumber = 0;

		@Override
		protected Integer doInBackground(String... params) {
			fsize = FileUtil.getFileSize(files);
			// 包含文件夹数量
			fnumber = FileUtil.getNumFilesInFolder(files, true, true);
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			fileVolumeTextView.setText(FileUtil.convertStorage(fsize));
			fileNumberTextView.setText(String.valueOf(fnumber));
		}

	}

	public FilePropertyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FilePropertyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
