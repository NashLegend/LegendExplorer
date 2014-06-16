
package com.example.legendexplorer;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;

public class MainActivity extends Activity {

    private ViewPager pager;
    private FilePagerAdapter adapter;
    private ArrayList<BaseFragment> list;
    private FilesFragment filesFragment;
    private BookMarksFragment bookMarksFragment;
    private ClassifiedFragment classifiedFragment;

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

        pager = (ViewPager) findViewById(R.id.pager);
        adapter = new FilePagerAdapter(getFragmentManager());
        adapter.setList(list);
        pager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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

}
