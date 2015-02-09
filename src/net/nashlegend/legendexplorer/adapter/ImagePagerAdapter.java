
package net.nashlegend.legendexplorer.adapter;

import java.io.File;
import java.util.ArrayList;

import net.nashlegend.legendexplorer.view.ZoomImageView;

import net.nashlegend.legendutils.Tools.FileUtil;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

public class ImagePagerAdapter extends PagerAdapter {
    String mPath;
    Context mContext;
    ArrayList<File> files = new ArrayList<File>();
    private int imageIndex;

    public ImagePagerAdapter(Context context, String path) {
        mPath = path;
        mContext = context;

        File FirstImage = new File(path);

        if (isImageFile(FirstImage)) {
            File parentFile = FirstImage.getParentFile();
            File[] listFiles = parentFile.listFiles();
            if (listFiles != null) {
                for (int i = 0; i < parentFile.listFiles().length; i++) {
                    File file = listFiles[i];
                    if (isImageFile(file)) {
                        files.add(file);
                        if (file.equals(FirstImage)) {
                            imageIndex = files.size() - 1;
                        }
                    }
                }
            }
        }
    }

    public int getImageIndex() {
        return imageIndex;
    }

    public boolean isImageFile(File file) {
        return FileUtil.getFileType(file) == FileUtil.FILE_TYPE_IMAGE;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return files.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // TODO
        ZoomImageView imageView = new ZoomImageView(mContext);
        imageView.setImageFile(files.get(position).getAbsolutePath());
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        try {
            container.removeView((View) object);
        } catch (Exception e) {

        }
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

}
