
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;

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
 * 普通视图
 * 
 * @author NashLegend
 */
public class FilesFragment extends BaseFragment implements OnClickListener,
        OnCheckedChangeListener, Explorable {
    private EditText pathText;
    private ImageButton backButton;
    private CheckBox selectAllButton;
    private boolean inSelectMode = false;
    private ArrayList<FileListFragment> fakeBackStack = new ArrayList<FileListFragment>();

    private String initialPath = "/";

    public FilesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_file_explorer, null);
        // listView = (ListView) view.findViewById(R.id.listview_files);
        pathText = (EditText) view.findViewById(R.id.edittext_file_path);
        backButton = (ImageButton) view
                .findViewById(R.id.imagebutton_file_back);
        selectAllButton = (CheckBox) view
                .findViewById(R.id.checkbox_file_all);

        backButton.setOnClickListener(this);
        selectAllButton.setOnCheckedChangeListener(this);
        selectAllButton.setVisibility(View.GONE);
        pathText.setKeyListener(null);
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
            return;
        }

        FileListFragment fragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FileConst.Extra_File_Path, file.getAbsolutePath());
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.content_explorer, fragment, file.getAbsolutePath());
        transaction.commit();
        fakeBackStack.add(fragment);
        pathText.setText(fragment.getFilePath());
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
        if (fakeBackStack.size() > 1) {
            fakeBackStack.remove(fakeBackStack.size() - 1);
            FileListFragment fragment = fakeBackStack.get(fakeBackStack.size() - 1);
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.content_explorer, fragment, fragment.getFilePath());
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
    public File[] getSelectedFiles() {
        if (fakeBackStack.size() > 0) {
            return fakeBackStack.get(fakeBackStack.size() - 1).getSelectedFiles();
        }
        return null;
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
            if (fakeBackStack.size() > 1) {
                back2ParentLevel();
                return true;
            }
        }

        return false;
    }

    private void change2SelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.VISIBLE);
            inSelectMode = true;
            fakeBackStack.get(fakeBackStack.size() - 1).change2SelectMode();

            Intent intent = new Intent();
            intent.setAction(FileConst.Action_Switch_2_Select_Mode);
            getActivity().sendBroadcast(intent);
        }
    }

    private void exitSelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.GONE);
            inSelectMode = false;
            fakeBackStack.get(fakeBackStack.size() - 1).exitSelectMode();

            Intent intent = new Intent();
            intent.setAction(FileConst.Action_Exit_Select_Mode);
            getActivity().sendBroadcast(intent);
        }
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
        } else if (FileConst.Action_Add_New_File.equals(action)) {
            addNewFile();
        } else if (FileConst.Action_Search_File.equals(action)) {
            searchFile();
        } else if (FileConst.Action_Toggle_View_Mode.equals(action)) {
            toggleViewMode();
        } else if (FileConst.Action_Refresh_FileList.equals(action)) {
            refreshFileList();
        } else if (FileConst.Action_Copy_File.equals(action)) {
            copyFile();
        } else if (FileConst.Action_Move_File.equals(action)) {
            moveFile();
        } else if (FileConst.Action_Delete_File.equals(action)) {
            deleteFile();
        } else if (FileConst.Action_File_Opration_Done.equals(action)) {
            exitSelectMode();
        }
        return false;
    }

    @Override
    public void toggleViewMode() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).toggleViewMode();
        }
    }

    @Override
    public void copyFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).copyFile();
        }
    }

    @Override
    public void moveFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).moveFile();
        }
    }

    @Override
    public void deleteFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).deleteFile();
        }
    }

    @Override
    public void addNewFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).addNewFile();
        }
    }

    @Override
    public void refreshFileList() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).refreshFileList();
        }
    }

    @Override
    public void searchFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).searchFile();
        }
    }
}
