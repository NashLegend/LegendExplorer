
package com.example.legendexplorer;

import com.example.legendexplorer.adapter.ImagePagerAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.view.ImageViewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.view.WindowManager;

public class ImageBrowseActivity extends Activity {

    ImageViewer imageViewer;
    ViewPager viewPager;
    ImagePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_browse);
        final String path = getIntent().getStringExtra(FileConst.Extra_File_Path);
        viewPager = (ViewPager) findViewById(R.id.fullscreen_content);
        adapter = new ImagePagerAdapter(this, path);
        if (adapter.getImageIndex() >= 0) {
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(adapter.getImageIndex());
        }
    }
}
