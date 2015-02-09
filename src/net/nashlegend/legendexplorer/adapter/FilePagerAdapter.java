
package net.nashlegend.legendexplorer.adapter;

import java.util.ArrayList;

import net.nashlegend.legendexplorer.fragment.BaseFragment;


import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

public class FilePagerAdapter extends FragmentPagerAdapter {

    ArrayList<BaseFragment> list = new ArrayList<BaseFragment>();

    public FilePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public BaseFragment getItem(int arg0) {
        return list.get(arg0);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    public ArrayList<BaseFragment> getList() {
        return list;
    }

    public void setList(ArrayList<BaseFragment> list) {
        this.list = list;
    }

}
