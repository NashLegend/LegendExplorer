
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.db.BookmarkHelper;
import com.example.legendexplorer.model.FileItem;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class FileListFragment extends Fragment {
    private FileListAdapter adapter;
    private ListView listView;
    private String filePath;
    private int itemType = FileItem.Item_Type_File_Or_Folder;

    public FileListFragment() {

    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        filePath = args.getString(FileConst.Extra_File_Path);
        itemType = args.getInt(FileConst.Extra_Item_Type, FileItem.Item_Type_File_Or_Folder);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_file_list, null);
        listView = (ListView) view.findViewById(R.id.fragment_listview_files);
        adapter = new FileListAdapter(getActivity());
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
        }
        listView.setAdapter(adapter);
        listView.setLongClickable(true);
        return view;
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
    public ArrayList<File> getSelectedFiles() {
        return adapter.getSelectedFiles();
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
        switch (itemType) {
            case FileItem.Item_Type_File_Or_Folder:
                return filePath;
            case FileItem.Item_type_Bookmark:
                return filePath;
            default:
                return filePath;
        }

    }

}
