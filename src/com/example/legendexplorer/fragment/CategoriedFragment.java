package com.example.legendexplorer.fragment;

import com.example.legendexplorer.R;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 分类视图
 * 
 * @author NashLegend
 */
public class CategoriedFragment extends BaseFragment {
	protected View view;

	public CategoriedFragment() {
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.layout_file_explorer, null);
		} else {
			if (view.getParent() != null) {
				((ViewGroup) view.getParent()).removeView(view);
			}
		}
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public boolean doBackAction() {
		// TODO 自动生成的方法存根
		return false;
	}

	@Override
	public boolean doVeryAction(Intent intent) {
		// TODO 自动生成的方法存根
		return false;
	}

}
