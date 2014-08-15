
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.example.legendexplorer.MainActivity;
import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.view.DropDownAncestorList;
import com.example.legendexplorer.view.DropDownAncestorList.OnAncestorClickListener;
import com.example.legendutils.Tools.FileUtil;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.PopupWindow;

/**
 * 普通视图,事实上if (fakeBackStack.size() > 0) 都是不需要的
 * 
 * @author NashLegend
 */
public class FilesFragment extends BaseFragment implements OnClickListener,
        OnCheckedChangeListener, Explorable {
    protected View view;
    protected EditText pathText;
    protected ImageButton backButton;
    protected CheckBox selectAllButton;
    protected boolean inSelectMode = false;
    protected DropDownAncestorList ancestorList;
    protected ArrayList<FileListFragment> fakeBackStack = new ArrayList<FileListFragment>();
    protected String initialPath = Environment.getExternalStorageDirectory()
            .getPath();
    protected PopupWindow popupWindow;

    public FilesFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.layout_file_explorer, container,
                    false);
            pathText = (EditText) view.findViewById(R.id.edittext_file_path);
            backButton = (ImageButton) view
                    .findViewById(R.id.imagebutton_file_back);
            selectAllButton = (CheckBox) view
                    .findViewById(R.id.checkbox_file_all);

            backButton.setOnClickListener(this);
            selectAllButton.setOnCheckedChangeListener(this);
            selectAllButton.setVisibility(View.GONE);
            pathText.setKeyListener(null);
            pathText.setOnClickListener(this);

            ancestorList = new DropDownAncestorList(getActivity());
            ancestorList
                    .setOnAncestorClickListener(new OnAncestorClickListener() {

                        @Override
                        public void onClick(String path) {
                            openAncestorFolder(new File(path));
                            popupWindow.dismiss();
                        }
                    });

            popupWindow = new PopupWindow(ancestorList,
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(
                    getResources(), (Bitmap) null));

            openFolder();
        } else {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }

        return view;
    }

    protected void invokeAncestorList() {
        String path = pathText.getText().toString();
        if ("/".equals(path)) {
            return;
        } else {
            File file = new File(path);
            ancestorList.setupList(file);
        }
        popupWindow.showAsDropDown(pathText);
    }

    protected void openAncestorFolder(File file) {
        // file.isDirectory()为true则说明文件存在
        if (file != null && file.isDirectory()) {
            for (int i = fakeBackStack.size() - 1; i >= 0; i--) {
                FileListFragment subFragment = fakeBackStack.get(i);
                File file2 = new File(subFragment.getFilePath());
                if (FileUtil.isAncestorOf(file, file2) || file.equals(file2)) {
                    fakeBackStack.remove(i);
                }
            }
        }

        openFolder(file, FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
    }

    /**
     * @param file
     * @param animation
     */
    public void openFolder(File file, int animation) {
        // file.isDirectory()不为true则说明文件要么不存在要么是文件
        if (file == null || !file.isDirectory()) {
            // 若不存在此目录，则打开根文件夹
            return;
        }

        FileListFragment fragment = new FileListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FileConst.Extra_File_Path, file.getAbsolutePath());
        fragment.setArguments(bundle);

        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.setTransition(animation);
        transaction.replace(R.id.content_explorer, fragment,
                file.getAbsolutePath());
        transaction.commit();
        fakeBackStack.add(fragment);
        pathText.setText(fragment.getDisplayedFilePath());
    }

    /**
     * 打开目录
     * 
     * @param file 要打开的文件夹
     */
    public void openFolder(File file) {
        openFolder(file, FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
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
     * 返回一层fragment,不一定是上一级目录
     */
    protected void backStack() {
        if (fakeBackStack.size() > 1) {
            fakeBackStack.remove(fakeBackStack.size() - 1);
            FileListFragment fragment = fakeBackStack
                    .get(fakeBackStack.size() - 1);
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.replace(R.id.content_explorer, fragment,
                    fragment.getDisplayedFilePath());
            transaction.commit();
            pathText.setText(fragment.getDisplayedFilePath());
        } else {
            // do nothing
        }
    }

    /**
     * 返回上一级目录
     */
    protected void back2ParentLevel() {
        // 相当于BackStack，但是当pFile上sd卡的父级或以上时，相当于打开父文件夹并清除其子文件夹
        if (fakeBackStack.size() > 0) {
            FileListFragment fragment = fakeBackStack
                    .get(fakeBackStack.size() - 1);
            File file = new File(fragment.getDisplayedFilePath());
            File pFile = file.getParentFile();
            for (int i = fakeBackStack.size() - 1; i >= 0; i--) {
                FileListFragment subFragment = fakeBackStack.get(i);
                File file2 = new File(subFragment.getFilePath());
                if (FileUtil.isAncestorOf(pFile, file2) || pFile.equals(file2)) {
                    fakeBackStack.remove(i);
                }
            }
            if (pFile != null && pFile.exists() && pFile.isDirectory()) {
                openFolder(pFile, FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            }
        }
    }

    /**
     * 选中当前目录所有文件
     */
    protected void selectAll() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).selectAll();
        }
    }

    /**
     * 取消选中当前目录所有文件
     */
    protected void unselectAll() {
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
            return fakeBackStack.get(fakeBackStack.size() - 1)
                    .getSelectedFiles();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        if (inSelectMode) {
            exitSelectMode();
        }
        int id = v.getId();
        switch (id) {
            case R.id.imagebutton_file_back:
                back2ParentLevel();
                break;
            case R.id.edittext_file_path:
                invokeAncestorList();
                break;
            default:
                break;
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

    protected void change2SelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.VISIBLE);
            inSelectMode = true;
            fakeBackStack.get(fakeBackStack.size() - 1).change2SelectMode();

            Intent intent = new Intent();
            intent.setAction(FileConst.Action_Set_File_Operation_ActionBar);
            getActivity().sendBroadcast(intent);
        }
    }

    protected void exitSelectMode() {
        if (fakeBackStack.size() > 0) {
            selectAllButton.setVisibility(View.GONE);
            selectAllButton.setChecked(false);
            inSelectMode = false;
            fakeBackStack.get(fakeBackStack.size() - 1).exitSelectMode();

            Intent intent = new Intent();
            intent.setAction(FileConst.Action_Set_File_View_ActionBar);
            getActivity().sendBroadcast(intent);
        }
    }

    protected void doOpenFolderAction(Intent intent) {
        openFolder(intent.getStringExtra(FileConst.Extra_File_Path));
    }

    private boolean isInSearchingMode() {
        if (fakeBackStack.size() > 0) {
            return fakeBackStack.get(fakeBackStack.size() - 1)
                    .isInSearchingMode();
        }
        return false;
    }

    private void quitSearchFile() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Quit_Search);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public boolean doBackAction() {
        if (inSelectMode) {
            exitSelectMode();
            return true;
        }
        if (isInSearchingMode()) {
            quitSearchFile();
            return true;
        }
        if (fakeBackStack.size() > 1) {
            backStack();
            return true;
        }
        return false;
    }

    @Override
    public boolean doVeryAction(Intent intent) {
        String action = intent.getAction();
        if (FileConst.Action_Open_Folder.equals(action)) {
            doOpenFolderAction(intent);
        } else if (FileConst.Action_FileItem_Long_Click.equals(action)) {
            change2SelectMode();
            getItemSelect();
        } else if (FileConst.Action_FileItem_Unselect.equals(action)) {
            getItemUnselect();
        } else if (FileConst.Action_FileItem_Select.equals(action)) {
            getItemSelect();
        } else if (FileConst.Action_File_Operation_Done.equals(action)) {
            exitSelectMode();
        } else if (FileConst.Action_Add_New_File.equals(action)) {
            addNewFile();
        } else if (FileConst.Action_Search_File.equals(action)) {
            String query = intent
                    .getStringExtra(FileConst.Key_Search_File_Query);
            searchFile(query);
        } else if (FileConst.Action_Quit_Search.equals(action)) {
            searchFile("");
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
        } else if (FileConst.Action_Toggle_Show_Hidden.equals(action)) {
            toggleShowHidden();
        } else if (FileConst.Action_Zip_File.equals(action)) {
            zipFile();
        } else if (FileConst.Action_Rename_File.equals(action)) {
            renameFile();
        } else if (FileConst.Action_Property_File.equals(action)) {
            propertyFile();
        } else if (FileConst.Action_Unzip_File.equals(action)) {
            unzipFile();
        } else if (FileConst.Action_Favor_File.equals(action)) {
            favorFile();
        }
        return false;
    }

    public void getItemSelect() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).getItemSelect();
        }
    }

    public void getItemUnselect() {

        selectAllButton.setOnCheckedChangeListener(null);
        selectAllButton.setChecked(false);
        selectAllButton.setOnCheckedChangeListener(this);

        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).getItemUnselect();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Intent intent = new Intent();
            int mask = 0;
            intent.putExtra(FileConst.Extra_Menu_Mask, mask);
            intent.setAction(FileConst.Action_Set_File_View_ActionBar);
            getActivity().sendBroadcast(intent);
        }
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
    public void searchFile(String query) {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).searchFile(query);
        }
    }

    @Override
    public void toggleShowHidden() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).toggleShowHidden();
        }
    }

    @Override
    public void zipFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).zipFile();
        }
    }

    @Override
    public void renameFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).renameFile();
        }
    }

    @Override
    public void propertyFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).propertyFile();
        }
    }

    public void unzipFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).unzipFile();
        }
    }

    public void favorFile() {
        if (fakeBackStack.size() > 0) {
            fakeBackStack.get(fakeBackStack.size() - 1).favorFile();
        }
    }
}
