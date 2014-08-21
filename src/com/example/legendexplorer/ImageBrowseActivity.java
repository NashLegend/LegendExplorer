package com.example.legendexplorer;

import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.view.ImageViewer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.view.WindowManager;

public class ImageBrowseActivity extends Activity {
    
    ImageViewer imageViewer;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    requestWindowFeature(Window.FEATURE_NO_TITLE);
	    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_browse);
		final String path=getIntent().getStringExtra(FileConst.Extra_File_Path);
		imageViewer=(ImageViewer) findViewById(R.id.fullscreen_content);
		imageViewer.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            
		    @SuppressLint("NewApi")
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (imageViewer.getWidth()>0) {                    
                    imageViewer.setDataSource(path);
                    if (android.os.Build.VERSION.SDK_INT>=16) {
                        imageViewer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }else {                        
                        imageViewer.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            }
        });
	}
}
