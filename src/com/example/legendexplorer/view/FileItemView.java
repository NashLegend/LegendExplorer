
package com.example.legendexplorer.view;

import java.io.File;

import com.example.legendexplorer.R;
import com.example.legendexplorer.R.id;
import com.example.legendexplorer.R.layout;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
    private TextView title;
    private CheckBox checkBox;
    private ViewGroup rootFileItemView;
    private FileListAdapter adapter;

    private FileItem fileItem;

    public FileItemView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_file_item, this);
        icon = (ImageView) findViewById(R.id.image_file_icon);
        title = (TextView) findViewById(R.id.text_file_title);
        rootFileItemView = (ViewGroup) findViewById(R.id.rootFileItemView);
        checkBox = (CheckBox) findViewById(R.id.checkbox_file_item_select);
        setOnClickListener(this);
        setLongClickable(true);
        setOnLongClickListener(this);
    }

    public FileItem getFileItem() {
        return fileItem;
    }

    public void setFileItem(FileItem fileItem, FileListAdapter adapter) {
        this.fileItem = fileItem;
        this.adapter = adapter;
        icon.setImageResource(fileItem.getIcon());
        title.setText(fileItem.getName());
        toggleSelectState();
        if (fileItem.isInSelectMode()) {
            checkBox.setVisibility(View.VISIBLE);
        } else {
            checkBox.setVisibility(View.GONE);
        }
    }

    /**
     * 切换选中、未选中状态,fileItem.setSelected(boolean)先发生;
     */
    public void toggleSelectState() {
        if (fileItem.isSelected()) {
            rootFileItemView.setBackgroundColor(Color.CYAN);
        } else {
            rootFileItemView.setBackgroundColor(Color.WHITE);

            Intent intent = new Intent();
            intent.setAction(FileConst.Action_FileItem_Unselect);
            getContext().sendBroadcast(intent);
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

    }

    public void selectOne() {
        if (fileItem.isInSelectMode()) {
            fileItem.setSelected(!fileItem.isSelected());
            toggleSelectState();
        }
    }

    /**
     * 打开文件夹
     */
    public void openFolder() {
        adapter.openFolder(fileItem);
    }

    public FileListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        fileItem.setSelected(isChecked);
        toggleSelectState();
    }

    @Override
    public boolean onLongClick(View v) {
        Log.i("file", "long");
        Vibrator vibrator = (Vibrator) getContext().getSystemService(
                Service.VIBRATOR_SERVICE);
        vibrator.vibrate(20);

        fileItem.setSelected(true);

        Intent intent = new Intent();
        intent.setAction(FileConst.Action_FileItem_Long_Click);
        getContext().sendBroadcast(intent);

        setOnLongClickListener(null);

        return false;
    }
}
