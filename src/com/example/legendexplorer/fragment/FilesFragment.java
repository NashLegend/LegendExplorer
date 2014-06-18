
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 普通视图
 * 
 * @author NashLegend
 */
public class FilesFragment extends BaseFragment implements OnClickListener,
        OnCheckedChangeListener {
    private FileListAdapter adapter;
    private ListView listView;
    private EditText pathText;
    private ImageButton backButton;
    private CheckBox selectAllButton;
    private boolean inSelectMode = false;
    private ArrayList<FileListAdapter> fakeBackStack = new ArrayList<FileListAdapter>();

    private String initialPath = "/";

    public FilesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_file_explorer, null);
        listView = (ListView) view.findViewById(R.id.listview_files);
        pathText = (EditText) view.findViewById(R.id.edittext_file_path);
        backButton = (ImageButton) view
                .findViewById(R.id.imagebutton_file_back);
        selectAllButton = (CheckBox) view
                .findViewById(R.id.checkbox_file_all);

        backButton.setOnClickListener(this);
        selectAllButton.setOnCheckedChangeListener(this);
        selectAllButton.setVisibility(View.GONE);
        pathText.setKeyListener(null);

        adapter = new FileListAdapter(getActivity());
        listView.setAdapter(adapter);
        listView.setLongClickable(true);

        openFolder();

        return view;
    }

    /**
     * 打开目录
     * 
     * @param file 要打开的文件夹
     */
    public void openFolder(File file) {
        if (!file.exists() || !file.isDirectory()) {
            // 若不存在此目录，则打开根文件夹
            file = Environment.getExternalStorageDirectory();
        }
        if (adapter.openFolder(file)) {
            pathText.setText(file.getAbsolutePath());
        }
    }

    /**
     * 打开目录
     * 
     * @param path 要打开的文件夹路径
     */
    public void openFolder(String path) {
        openFolder(new File(path));
    }

    /**
     * 打开初始目录
     */
    public void openFolder() {
        openFolder(initialPath);
    }

    /**
     * 返回上级目录
     */
    private void back2ParentLevel() {
        File file = adapter.getCurrentDirectory();
        if (file != null && file.getParentFile() != null) {
            openFolder(file.getParentFile());
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

    public String getInitialPath() {
        return initialPath;
    }

    public void setInitialPath(String initialPath) {
        this.initialPath = initialPath;
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
            if (!adapter.getCurrentDirectory().getAbsolutePath()
                    .equals(initialPath)) {
                back2ParentLevel();
                return true;
            }
        }

        return false;
    }

    private void change2SelectMode() {
        selectAllButton.setVisibility(View.VISIBLE);
        inSelectMode = true;
        adapter.change2SelectMode();
    }

    private void exitSelectMode() {
        selectAllButton.setVisibility(View.GONE);
        inSelectMode = false;
        adapter.exitSelectMode();
    }

    @Override
    public boolean doVeryAction(Intent intent) {
        String action = intent.getAction();
        if (FileConst.Action_Open_Folder.equals(action)) {
            openFolder(intent.getStringExtra(FileConst.Extra_File_Path));
        } else if (FileConst.Action_FileItem_Long_Click.equals(action)) {
            change2SelectMode();
        } else if (FileConst.Action_FileItem_Unselect.equals(action)) {
            selectAllButton.setOnCheckedChangeListener(null);
            selectAllButton.setChecked(false);
            selectAllButton.setOnCheckedChangeListener(this);
        }
        return false;
    }
}
