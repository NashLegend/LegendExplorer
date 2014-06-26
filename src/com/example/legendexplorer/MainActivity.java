
package com.example.legendexplorer;

import java.util.ArrayList;

import com.example.legendexplorer.adapter.FilePagerAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.fragment.BaseFragment;
import com.example.legendexplorer.fragment.BookMarksFragment;
import com.example.legendexplorer.fragment.ClassifiedFragment;
import com.example.legendexplorer.fragment.FilesFragment;
import com.example.legendexplorer.view.FolderViewPager;

import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    private FolderViewPager pager;
    private FilePagerAdapter adapter;
    private ArrayList<BaseFragment> list;
    private FilesFragment filesFragment;
    private BookMarksFragment bookMarksFragment;
    private ClassifiedFragment classifiedFragment;
    private FileBroadcastReceiver fileBroadcastReceiver;
    private Menu mMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        filesFragment = new FilesFragment();
        bookMarksFragment = new BookMarksFragment();
        classifiedFragment = new ClassifiedFragment();

        list = new ArrayList<BaseFragment>();
        list.add(filesFragment);
        list.add(bookMarksFragment);
        list.add(classifiedFragment);

        pager = (FolderViewPager) findViewById(R.id.pager);
        adapter = new FilePagerAdapter(getFragmentManager());
        adapter.setList(list);
        pager.setAdapter(adapter);
        pager.setScrollEnabled(true);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FileConst.Action_Open_Folder);
        filter.addAction(FileConst.Action_FileItem_Long_Click);
        filter.addAction(FileConst.Action_FileItem_Unselect);
        filter.addAction(FileConst.Action_Switch_2_Select_Mode);
        filter.addAction(FileConst.Action_Exit_Select_Mode);
        filter.addAction(FileConst.Action_File_Opration_Done);
        filter.addAction(FileConst.Action_Enable_Pager_Scroll);
        filter.addAction(FileConst.Action_Disable_Pager_Scroll);
        fileBroadcastReceiver = new FileBroadcastReceiver();
        registerReceiver(fileBroadcastReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.filelist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                addNewFile();
                break;
            case R.id.action_refresh:
                refreshFileList();
                break;
            case R.id.action_search:
                searchFile();
                break;
            case R.id.action_viewmode:
                toggleViewMode();
                break;
            case R.id.action_toggle_hidden:
                toggleShowHidden();
                break;
            case R.id.action_copy:
                copyFile();
                break;
            case R.id.action_cut:
                moveFile();
                break;
            case R.id.action_delete:
                deleteFile();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showFileOperationMenu() {
        pager.setScrollEnabled(false);
        mMenu.clear();
        getMenuInflater().inflate(R.menu.fileop, mMenu);
    }

    public void showFileListMenu() {
        pager.setScrollEnabled(true);
        mMenu.clear();
        getMenuInflater().inflate(R.menu.filelist, mMenu);
    }

    private void toggleViewMode() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Toggle_View_Mode);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void toggleShowHidden() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Toggle_Show_Hidden);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void copyFile() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Copy_File);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void moveFile() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Move_File);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void deleteFile() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Delete_File);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void addNewFile() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Add_New_File);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void refreshFileList() {
        Intent intent = new Intent();
        intent.setAction(FileConst.Action_Refresh_FileList);
        adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
    }

    private void searchFile() {

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (doBackAction()) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    private boolean doBackAction() {
        return adapter.getItem(pager.getCurrentItem()).doBackAction();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(fileBroadcastReceiver);
        super.onDestroy();
    }

    class FileBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (FileConst.Action_Switch_2_Select_Mode.equals(action)) {
                showFileOperationMenu();
            } else if (FileConst.Action_Exit_Select_Mode.equals(action)) {
                showFileListMenu();
            } else if (FileConst.Action_Disable_Pager_Scroll.equals(action)) {
                pager.setScrollEnabled(false);
            }
            else if (FileConst.Action_Enable_Pager_Scroll.equals(action)) {
                pager.setScrollEnabled(true);
            }
            adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
        }
    }

}
