
package com.example.legendexplorer;

import java.io.File;

import android.content.Context;
import android.graphics.Color;
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

/**
 * 文件列表单个item的view
 * 
 * @author NashLegend
 */
public class FileItemView extends FrameLayout implements OnClickListener,
        OnCheckedChangeListener {

    private ImageView icon;
    private TextView title;
    private CheckBox checkBox;
    private ViewGroup rootFileItemView;
    private FileListAdapter adapter;
    private boolean selectable = true;

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

        if (!fileItem.isDirectory()) {
            checkBox.setEnabled(false);
            selectable = false;
            checkBox.setOnCheckedChangeListener(null);
            return;
        }

        if (fileItem.isDirectory()) {
            checkBox.setEnabled(false);
            selectable = false;
            checkBox.setOnCheckedChangeListener(null);
            return;
        }
        selectable = true;
        checkBox.setEnabled(true);
        checkBox.setOnCheckedChangeListener(this);
    }

    public void setFileItem(File file) {
        setFileItem(new FileItem(file));
    }

    public void setFileItem(String path) {
        setFileItem(new FileItem(path));
    }

    /**
     * 切换选中、未选中状态,fileItem.setSelected(boolean)先发生;
     */
    public void toggleSelectState() {
        if (fileItem.isSelected()) {
            rootFileItemView.setBackgroundColor(Color.CYAN);
        } else {
            rootFileItemView.setBackgroundColor(Color.WHITE);
        }
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(fileItem.isSelected());
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.checkbox_file_item_select) {
            if (fileItem.isDirectory()) {
                openFolder();
            } else {
                // 选中一个
                selectOne();
            }
        }
    }

    public void selectOne() {
        if (selectable) {
            if (fileItem.isSelected()) {
                // 取消选中状态，只在FileItemView就可以
                fileItem.setSelected(!fileItem.isSelected());
                toggleSelectState();
                adapter.unselectOne();
            } else {
                // 如果要选中某个FileItem，则必须要在adapter里面进行，因为如果是单选的话，还要取消其他的选中状态
                adapter.selectOne(fileItem);
            }
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
        if (isChecked) {
            adapter.selectOne(fileItem);
        } else {
            rootFileItemView.setBackgroundColor(Color.WHITE);
            adapter.unselectOne();
        }
    }
}
