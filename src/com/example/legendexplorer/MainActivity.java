
package com.example.legendexplorer;

import java.io.File;
import java.util.ArrayList;

import com.example.legendexplorer.adapter.FilePagerAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.fragment.BaseFragment;
import com.example.legendexplorer.fragment.BookMarksFragment;
import com.example.legendexplorer.fragment.ClassifiedFragment;
import com.example.legendexplorer.fragment.FilesFragment;
import com.example.legendexplorer.model.FileItem;

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

public class MainActivity extends Activity {

    private ViewPager pager;
    private FilePagerAdapter adapter;
    private ArrayList<BaseFragment> list;
    private FilesFragment filesFragment;
    private BookMarksFragment bookMarksFragment;
    private ClassifiedFragment classifiedFragment;
    private FileBroadcastReceiver fileBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getActionBar().setDisplayShowTitleEnabled(false);

        filesFragment = new FilesFragment();
        bookMarksFragment = new BookMarksFragment();
        classifiedFragment = new ClassifiedFragment();

        list = new ArrayList<BaseFragment>();
        list.add(filesFragment);
        list.add(bookMarksFragment);
        list.add(classifiedFragment);

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new FilePagerAdapter(getFragmentManager());
        adapter.setList(list);
        pager.setAdapter(adapter);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FileConst.Action_Open_Folder);
        filter.addAction(FileConst.Action_FileItem_Long_Click);
        filter.addAction(FileConst.Action_FileItem_Unselect);
        fileBroadcastReceiver = new FileBroadcastReceiver();
        registerReceiver(fileBroadcastReceiver, filter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (doBackAction()) {
            return true;
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
            if (FileConst.Action_Open_Folder.equals(action)) {

            }

            adapter.getItem(pager.getCurrentItem()).doVeryAction(intent);
        }

    }

}
