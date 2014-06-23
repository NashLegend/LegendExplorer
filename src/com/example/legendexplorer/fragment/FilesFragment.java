
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendutils.Dialogs.FileDialog;
import com.example.legendutils.Dialogs.FileDialog.FileDialogListener;
import com.example.legendutils.Dialogs.Win8ProgressDialog;
import com.example.legendutils.Tools.FileUtil;
import com.example.legendutils.Tools.FileUtil.FileOperationListener;
import com.example.legendutils.Tools.ToastUtil;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
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
        ArrayList<File> files = new ArrayList<File>();
        if (fakeBackStack.size() > 0) {
            files = fakeBackStack.get(fakeBackStack.size() - 1).getSelectedFiles();
        }
        File[] files2 = new File[files.size()];
        for (int i = 0; i < files2.length; i++) {
            files2[i] = files.get(i);
        }
        return files2;

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
        FileDialog dialog = new FileDialog.Builder(getActivity())
                .setFileMode(FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE).setCancelable(false)
                .setCanceledOnTouchOutside(false).setTitle("selectFolder")
                .setFileSelectListener(new FileDialogListener() {

                    @Override
                    public void onFileSelected(ArrayList<File> files) {
                        copy2Folder(getSelectedFiles(), files.get(0));
                    }

                    @Override
                    public void onFileCanceled() {

                    }
                }).create(getActivity());
        dialog.show();
    }

    private void copy2Folder(File[] files, File destFile) {
        final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(getActivity())
                .setCancelable(false).setCanceledOnTouchOutside(false).create();
        dialog.show();
        FileUtil.copy2DirectoryAsync(files, destFile, new FileOperationListener() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onError() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Copy Error!");
            }

            @Override
            public void onComplete() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Copy OK!");
            }
        });
    }

    @Override
    public void moveFile() {
        FileDialog dialog = new FileDialog.Builder(getActivity())
                .setFileMode(FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE).setCancelable(false)
                .setCanceledOnTouchOutside(false).setTitle("selectFolder")
                .setFileSelectListener(new FileDialogListener() {

                    @Override
                    public void onFileSelected(ArrayList<File> files) {
                        move2Folder(getSelectedFiles(), files.get(0));
                    }

                    @Override
                    public void onFileCanceled() {

                    }
                }).create(getActivity());
        dialog.show();
    }

    private void move2Folder(File[] files, File destFile) {
        final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(getActivity())
                .setCancelable(false).setCanceledOnTouchOutside(false).create();
        dialog.show();
        FileUtil.move2DirectoryAsync(files, destFile, new FileOperationListener() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onError() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Move Error!");
            }

            @Override
            public void onComplete() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Move OK!");
            }
        });
    }

    @Override
    public void deleteFile() {
        new AlertDialog.Builder(getActivity()).setMessage("Confirm to delete?").setTitle("Message")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteFiles();
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
    }

    private void deleteFiles() {
        final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(getActivity())
                .setCancelable(false).setCanceledOnTouchOutside(false).create();
        dialog.show();
        FileUtil.deleteAsync(getSelectedFiles(), new FileOperationListener() {

            @Override
            public void onProgress() {

            }

            @Override
            public void onError() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Delete Error!");
            }

            @Override
            public void onComplete() {
                dialog.dismiss();
                exitSelectMode();
                refreshFileList();
                ToastUtil.showToast(getActivity(), "Delete OK!");
            }
        });
    }

    @Override
    public void addNewFile() {

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
