
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.db.BookmarkHelper;
import com.example.legendexplorer.model.FileItem;
import com.example.legendexplorer.utils.SharePreferencesUtil;
import com.example.legendutils.Dialogs.FileDialog;
import com.example.legendutils.Dialogs.ListDialog;
import com.example.legendutils.Dialogs.Win8ProgressDialog;
import com.example.legendutils.Dialogs.FileDialog.FileDialogListener;
import com.example.legendutils.Dialogs.ListDialog.OnItemSelectedListener;
import com.example.legendutils.Tools.FileUtil;
import com.example.legendutils.Tools.ToastUtil;
import com.example.legendutils.Tools.FileUtil.FileOperationListener;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

public class FileListFragment extends Fragment {
    private FileListAdapter adapter;
    private ListView listView;
    private String filePath;
    private int itemType = FileItem.Item_Type_File_Or_Folder;
    private String pathPreffix = "/////////////";
    private GridView gridView;
    private View rootView;

    public FileListFragment() {

    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        filePath = args.getString(FileConst.Extra_File_Path);
        itemType = args.getInt(FileConst.Extra_Item_Type, FileItem.Item_Type_File_Or_Folder);
        pathPreffix = args.getString(FileConst.Extra_Path_Preffix, "/////////////");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.layout_file_list, null);
            listView = (ListView) rootView.findViewById(R.id.fragment_listview_files);
            adapter = new FileListAdapter(getActivity());
            gridView = (GridView) rootView.findViewById(R.id.fragment_gridview_files);
            gridView.setLongClickable(true);
            listView.setLongClickable(true);
            initViews();
        } else {
            if (rootView.getParent() != null) {
                ((ViewGroup) rootView.getParent()).removeView(rootView);
            }
        }
        return rootView;
    }

    private void initViews() {
        int mode = SharePreferencesUtil.readInt(FileConst.Key_Files_Display_Mode,
                FileConst.Value_Files_Display_List);
        if (mode == FileConst.Value_Files_Display_List) {
            gridView.setVisibility(View.GONE);
            gridView.setAdapter(null);
            adapter.setDisplayModeGrid(false);
            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(adapter);
        } else {
            listView.setVisibility(View.GONE);
            listView.setAdapter(null);
            adapter.setDisplayModeGrid(true);
            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(adapter);
        }
        loadData();
    }

    public void loadData() {
        if (filePath != null) {
            if (filePath.equals(FileConst.Value_Bookmark_Path)) {
                ArrayList<FileItem> fileItems;
                BookmarkHelper helper = new BookmarkHelper(getActivity());
                helper.open();
                fileItems = helper.getBookmarks();
                helper.close();
                adapter.setList(fileItems);
            } else {
                adapter.openFolder(new File(filePath));
            }
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 选中当前目录所有文件
     */
    public void selectAll() {
        adapter.selectAll();
    }

    /**
     * 取消选中当前目录所有文件
     */
    public void unselectAll() {
        adapter.unselectAll();
    }

    /**
     * @return 返回选中的文件列表
     */
    public File[] getSelectedFiles() {
        ArrayList<File> files = adapter.getSelectedFiles();
        File[] files2 = new File[files.size()];
        for (int i = 0; i < files2.length; i++) {
            files2[i] = files.get(i);
        }
        return files2;

    }

    public void change2SelectMode() {
        adapter.change2SelectMode();
    }

    public void exitSelectMode() {
        adapter.exitSelectMode();
    }
    
    /**
     * TODO
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * TODO
     */
    public String getDisplayedFilePath() {
        switch (itemType) {
            case FileItem.Item_Type_File_Or_Folder:
                return filePath;
            case FileItem.Item_type_Bookmark:
                if (pathPreffix.equals("") || pathPreffix.equals("/")) {
                    return FileConst.Value_Bookmark_Path.replace("//", "/") + filePath;
                }
                return filePath.replace(pathPreffix, FileConst.Value_Bookmark_Path);
            default:
                return filePath;
        }
    }

    public void toggleViewMode() {
        if (gridView.getVisibility() == View.GONE) {

            listView.setVisibility(View.GONE);
            listView.setAdapter(null);

            adapter.setDisplayModeGrid(true);

            gridView.setVisibility(View.VISIBLE);
            gridView.setAdapter(adapter);

            SharePreferencesUtil.saveInt(FileConst.Key_Files_Display_Mode,
                    FileConst.Value_Files_Display_Grid);

        } else {

            gridView.setVisibility(View.GONE);
            gridView.setAdapter(null);

            adapter.setDisplayModeGrid(false);

            listView.setVisibility(View.VISIBLE);
            listView.setAdapter(adapter);

            SharePreferencesUtil.saveInt(FileConst.Key_Files_Display_Mode,
                    FileConst.Value_Files_Display_List);

        }

    }

    public void addNewFile() {
        String[] values = {
                "File", "Folder"
        };
        ListDialog dialog = new ListDialog.Builder(getActivity()).setTitle("choose")
                .setMultiSelect(false)
                .setDisplayedValues(values)
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void OnItemSelected(int[] items) {
                        int selected = items[0];
                        if (selected == 0) {
                            // File
                        } else {
                            // Folder
                        }
                    }

                    @Override
                    public void OnCalcelSelect() {

                    }
                }).create();
        dialog.show();
    }

    public void refreshFileList() {
        loadData();
    }

    public void searchFile() {

    }

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
                operationDone();
                ToastUtil.showToast(getActivity(), "Copy Error!");
            }

            @Override
            public void onComplete() {
                dialog.dismiss();
                operationDone();
                ToastUtil.showToast(getActivity(), "Copy OK!");
            }
        });
    }

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
                operationDone();
                ToastUtil.showToast(getActivity(), "Move Error!");
            }

            @Override
            public void onComplete() {
                dialog.dismiss();
                operationDone();
                ToastUtil.showToast(getActivity(), "Move OK!");
            }
        });
    }

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
        // TODO
        // 删除数据库或者文件
        if (itemType == FileItem.Item_type_Bookmark) {
            // 删除数据库
        } else {
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
                    operationDone();
                    ToastUtil.showToast(getActivity(), "Delete Error!");
                }

                @Override
                public void onComplete() {
                    dialog.dismiss();
                    operationDone();
                    ToastUtil.showToast(getActivity(), "Delete OK!");
                }
            });
        }
    }

    public void toggleShowHidden() {
        boolean pre = SharePreferencesUtil.readBoolean(FileConst.Key_Show_Hiddle_Files, false);
        SharePreferencesUtil.saveBoolean(FileConst.Key_Show_Hiddle_Files, !pre);
        refreshFileList();
    }

    private void operationDone() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_File_Opration_Done);
        getActivity().sendBroadcast(intent);
        refreshFileList();
    }
}
