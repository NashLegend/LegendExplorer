
package net.nashlegend.legendexplorer.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

public class FolderViewPager extends ViewPager {

    private boolean scrollEnabled = true;
    private float preX = 0f;
    private float preY = 0f;

    public FolderViewPager(Context context) {
        super(context);
    }

    public FolderViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        if (scrollEnabled) {
            return super.onTouchEvent(arg0);
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        switch (arg0.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = arg0.getX();
                preY = arg0.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!scrollEnabled) {
                    float currentX = arg0.getX();
                    float currentY = arg0.getY();
                    if (Math.abs((currentX - preX) / (currentY - preY)) > 1) {
                        return true;
                    }
                }
                break;

            default:
                break;
        }
        return super.onInterceptTouchEvent(arg0);
    }

    public boolean isScrollEnabled() {
        return scrollEnabled;
    }

    public void setScrollEnabled(boolean scrollEnabled) {
        // TODO
        this.scrollEnabled = scrollEnabled;
    }

}
