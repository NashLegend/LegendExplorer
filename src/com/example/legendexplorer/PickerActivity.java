package com.example.legendexplorer;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R.color;
import com.example.legendutils.BuildIn.FileDialogView;
import com.example.legendutils.Dialogs.FileDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

/**
 * 全局文件选择器，Intent.ACTION_GET_CONTENT用来发出要选择项的Action。setType决定要选择的类型
 * 
 * Intent intent = new Intent(Intent.ACTION_GET_CONTENT);_______________________
 * intent.setType("*\/*"); _____________________________________________________
 * Intent wrapperIntent = Intent.createChooser(intent, null);___________________
 * startActivityForResult(wrapperIntent, 1001);_________________________________
 * 
 * @author NashLegend
 *
 */
public class PickerActivity extends Activity {

	private FileDialogView pickerView;
	private Button cancelButton;
	private Button okButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picker);
		setTitle("Pick A File");
		Intent intent = getIntent();
		if (intent != null
				&& Intent.ACTION_GET_CONTENT.equals(intent.getAction())) {
			pickerView = (FileDialogView) findViewById(R.id.picker);
			pickerView.setFileMode(FileDialog.FILE_MODE_OPEN_FILE_SINGLE);
			pickerView.setInitialPath(Environment.getExternalStorageDirectory()
					.getAbsolutePath());
			pickerView.openFolder();
			cancelButton = (Button) pickerView
					.findViewById(com.example.legendutils.R.id.button_dialog_file_cancel);
			okButton = (Button) pickerView
					.findViewById(com.example.legendutils.R.id.button_dialog_file_ok);
			cancelButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setResult(RESULT_CANCELED);
					finish();
				}
			});
			okButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					ArrayList<File> files = pickerView.getSelectedFiles();
					if (files != null && files.size() > 0) {
						File file = files.get(0);
						Intent intent = new Intent();
						Uri uri = Uri.fromFile(file);
						intent.setData(uri);
						setResult(RESULT_OK, intent);
						finish();
					}
				}
			});
		}

	}
}
