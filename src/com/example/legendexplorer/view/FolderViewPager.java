
package com.example.legendexplorer.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class FolderViewPager extends ViewPager {

    private boolean scrollEnabled = true;

    public FolderViewPager(Context context) {
        super(context);
    }

    public FolderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        this.scrollEnabled = scrollEnabled;
    }

}
