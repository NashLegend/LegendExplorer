
package com.example.legendexplorer.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import com.example.legendexplorer.R;
import com.example.legendutils.Tools.DisplayUtil;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class DropDownAncestorList extends LinearLayout implements OnClickListener {

    private File baseFile;
    private ArrayList<View> views = new ArrayList<View>();
    private int padStep = DisplayUtil.dip2px(10, getContext());
    private OnAncestorClickListener onAncestorClickListener;

    public DropDownAncestorList(Context context) {
        super(context);
    }

    public DropDownAncestorList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DropDownAncestorList(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public OnAncestorClickListener getOnAncestorClickListener() {
        return onAncestorClickListener;
    }
    
    public void setOnAncestorClickListener(OnAncestorClickListener onAncestorClickListener) {
        this.onAncestorClickListener = onAncestorClickListener;
    }

    public void setupList(File file) {
        if (!baseFile.equals(file)) {
            clearAllViews();
            baseFile = file;
            buildViewList();
        }
    }

    private void buildViewList() {
        File tmpFile = baseFile;

        while (tmpFile.getParentFile() != null) {
            File file = tmpFile.getParentFile();
            View view = LayoutInflater.from(getContext()).inflate(R.layout.view_ancestor_item,
                    null);
            views.add(view);
            tmpFile = file;
        }

        Collections.reverse(views);
        int st = 0;
        for (Iterator<View> iterator = views.iterator(); iterator.hasNext();) {
            View view = (View) iterator.next();
            LinearLayout root = (LinearLayout) view.findViewById(R.id.rootFolderItemView);
            root.setPadding(padStep * st, 0, 0, 0);
            addView(view);
            st++;
        }
    }

    public void clearAllViews() {
        for (Iterator<View> iterator = views.iterator(); iterator.hasNext();) {
            View view = (View) iterator.next();
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }
        views.clear();
    }

    @Override
    public void onClick(View v) {
        String path = v.getTag().toString();
        if (TextUtils.isEmpty(path)) {
            return;
        }
        if (onAncestorClickListener != null) {
            onAncestorClickListener.onClick(path);
        }
    }

    public static interface OnAncestorClickListener {
        void onClick(String path);
    }

}
