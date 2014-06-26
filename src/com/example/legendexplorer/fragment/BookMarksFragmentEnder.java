
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * 书签视图
 * 
 * @author NashLegend
 */
public class BookMarksFragmentEnder extends BaseFragment implements OnClickListener,
        OnCheckedChangeListener {
    private EditText pathText;
    private ImageButton backButton;
    private CheckBox selectAllButton;
    private boolean inSelectMode = false;
    private String pathPreffix = "/LegendWillNeverExsit";
    private ArrayList<FileListFragment> fakeBackStack = new ArrayList<FileListFragment>();

    public BookMarksFragmentEnder() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_file_bookmark, null);
        pathText = (EditText) view.findViewById(R.id.edittext_file_path);
        backButton = (ImageButton) view
                .findViewById(R.id.imagebutton_file_back);
        selectAllButton = (CheckBox) view.findViewById(R.id.checkbox_file_all);
        backButton.setOnClickListener(this);
        selectAllButton.setOnCheckedChangeListener(this);
        selectAllButton.setVisibility(View.GONE);
        pathText.setKeyListener(null);
        openBookMarks();
        return view;
    }

    public void openFolder(String path) {
        openFolder(new File(path));
    }

    /**
     * 打开目录
     * 
     * @param file 要打开的文件夹
     */
    public void openFolder(File file) {
        
        if (file.equals(new File(pathPreffix))) {
            openBookMarks();
            return;
        }

        if (file.exists() && file.isDirectory()) {
            FileListFragment fragment = new FileListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FileConst.Extra_File_Path, file.getAbsolutePath());
            bundle.putString(FileConst.Extra_Path_Preffix, pathPreffix);
            bundle.putInt(FileConst.Extra_Item_Type, FileItem.Item_type_Bookmark);
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_bookmark, fragment, file.getAbsolutePath());
            transaction.commit();
            fakeBackStack.add(fragment);
            pathText.setText(fragment.getDisplayedFilePath());
        } else {
            throw new NullPointerException("openFolder://file.exists() && file.isDirectory()");
        }
    }

    /**
     * 打开收藏夹目录
     */
    public void openBookMarks() {
        FileListFragment fragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FileConst.Extra_File_Path, FileConst.Value_Bookmark_Path);
        bundle.putString(FileConst.Extra_Path_Preffix, pathPreffix);
        bundle.putInt(FileConst.Extra_Item_Type, FileItem.Item_type_Bookmark);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_bookmark, fragment, FileConst.Value_Bookmark_Path);
        transaction.commit();
        fakeBackStack.add(fragment);
        pathText.setText(fragment.getDisplayedFilePath());
    }

    /**
     * 回退
     */
    private void backStack() {
        if (fakeBackStack.size() > 1) {
            fakeBackStack.remove(fakeBackStack.size() - 1);
            FileListFragment fragment = fakeBackStack.get(fakeBackStack.size() - 1);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_bookmark, fragment, fragment.getDisplayedFilePath());
            transaction.commit();
            pathText.setText(fragment.getDisplayedFilePath());
        } else {
            // do nothing
        }
    }

    /**
     * 选中当前目录所有文件
     */
    private void selectAll() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).selectAll();
        }
    }

    /**
     * 取消选中当前目录所有文件
     */
    private void unselectAll() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).unselectAll();
        }
    }

    public void unselectCheckBox() {
        selectAllButton.setOnCheckedChangeListener(null);
        selectAllButton.setChecked(false);
        selectAllButton.setOnCheckedChangeListener(this);
    }

    /**
     * @return 返回选中的文件列表
     */
    public File[] getSelectedFiles() {
        if (fakeBackStack.size() > 0) {
            return fakeBackStack.get(fakeBackStack.size() - 1).getSelectedFiles();
        } else {
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.imagebutton_file_back) {
            backStack();
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

    private void change2SelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.VISIBLE);
            inSelectMode = true;
            fakeBackStack.get(fakeBackStack.size() - 1).change2SelectMode();
        }
    }

    private void exitSelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.GONE);
            inSelectMode = false;
            fakeBackStack.get(fakeBackStack.size() - 1).exitSelectMode();
        }
    }

    @Override
    public boolean doBackAction() {
        if (inSelectMode) {
            exitSelectMode();
            return true;
        } else {
            if (fakeBackStack.size() > 1) {
                backStack();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doVeryAction(Intent intent) {
        String action = intent.getAction();
        if (FileConst.Action_Open_Folder.equals(action)) {
            int tp = intent.getIntExtra(FileConst.Extra_Item_Type,
                    FileItem.Item_Type_File_Or_Folder);
            String path = intent.getStringExtra(FileConst.Extra_File_Path);
            File file = new File(path);
            if (file.getParentFile() != null) {
                if (tp == FileItem.Item_type_Bookmark) {
                    pathPreffix = new File(path).getParent();
                    if (pathPreffix.lastIndexOf("/") != pathPreffix.length() - 1) {
                        pathPreffix += "/";
                    }
                }
            }
            openFolder(file);
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
