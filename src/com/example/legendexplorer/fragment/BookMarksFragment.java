
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.model.FileItem;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
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
public class BookMarksFragment extends BaseFragment implements OnClickListener,
        OnCheckedChangeListener {
    private EditText pathText;
    private ImageButton backButton;
    private CheckBox selectAllButton;
    private boolean inSelectMode = false;
    private FileItem currentFileItem = null;
    private ArrayList<FileListFragment> fakeBackStack = new ArrayList<FileListFragment>();

    public BookMarksFragment() {

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
        if (file.exists() && file.isDirectory()) {
            FileListFragment fragment = new FileListFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FileConst.Extra_File_Path, file.getAbsolutePath());
            bundle.putInt(FileConst.Extra_Item_Type, FileItem.Item_type_Bookmark);
            fragment.setArguments(bundle);

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_bookmark, fragment, file.getAbsolutePath());
            transaction.commit();
            fakeBackStack.add(fragment);
            pathText.setText(fragment.getFilePath());
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
        bundle.putInt(FileConst.Extra_Item_Type, FileItem.Item_type_Bookmark);
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_bookmark, fragment, FileConst.Value_Bookmark_Path);
        transaction.commit();
        fakeBackStack.add(fragment);
        pathText.setText(fragment.getFilePath());
        currentFileItem = null;
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
        if (fakeBackStack.size() > 1) {
            fakeBackStack.remove(fakeBackStack.size() - 1);
            FileListFragment fragment = fakeBackStack.get(fakeBackStack.size() - 1);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_bookmark, fragment, fragment.getFilePath());
            transaction.commit();
            pathText.setText(fragment.getFilePath());
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
    public ArrayList<File> getSelectedFiles() {
        if (fakeBackStack.size() > 0) {
            return fakeBackStack.get(fakeBackStack.size() - 1).getSelectedFiles();
        } else {
            return new ArrayList<File>();
        }
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
                back2ParentLevel();
                return true;
            }
        }
        return false;
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
