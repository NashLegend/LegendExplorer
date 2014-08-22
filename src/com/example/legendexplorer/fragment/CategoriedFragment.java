
package com.example.legendexplorer.fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.example.legendexplorer.MainActivity;
import com.example.legendexplorer.R;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.utils.FileCategoryHelper;
import com.example.legendutils.Tools.FileUtil;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 分类视图
 * 
 * @author NashLegend
 */
public class CategoriedFragment extends BaseFragment implements Explorable {
    protected View view;
    private static ScannerReceiver receiver;
    private FileListFragment listFragment;
    private View usedView;
    private View totalView;
    private TextView usageText;
    private RelativeLayout spaceLayout;

    private static HashMap<Integer, FileCategory> button2Category = new HashMap<Integer, FileCategory>();
    static {
        button2Category.put(R.id.category_music, FileCategory.Music);
        button2Category.put(R.id.category_video, FileCategory.Video);
        button2Category.put(R.id.category_picture, FileCategory.Picture);
        button2Category.put(R.id.category_document, FileCategory.Doc);
        button2Category.put(R.id.category_zip, FileCategory.Zip);
        button2Category.put(R.id.category_apk, FileCategory.Apk);
    }

    public enum FileCategory {
        All, Music, Video, Picture, Doc, Zip, Apk, Other
    }

    public CategoriedFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.layout_file_category, container,
                    false);
            spaceLayout = (RelativeLayout) view.findViewById(R.id.spaceLayout);
            usedView = view.findViewById(R.id.view_used);
            totalView = view.findViewById(R.id.view_total);
            usageText = (TextView) view.findViewById(R.id.text_usage);
            setupClick();
            updateUI();
            registerReceiver();
        } else {
            if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
            }
        }

        return view;
    }

    public void updateUI() {
        refreshData();
    }

    public void refreshData() {
        spaceLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                new OnGlobalLayoutListener() {

                    @SuppressWarnings("deprecation")
                    @SuppressLint("NewApi")
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT > 15) {
                            spaceLayout.getViewTreeObserver()
                                    .removeOnGlobalLayoutListener(this);
                        } else {
                            spaceLayout.getViewTreeObserver()
                                    .removeGlobalOnLayoutListener(this);
                        }
                        if (Environment.getExternalStorageState().equals(
                                Environment.MEDIA_MOUNTED)) {
                            try {
                                StatFs statfs = new StatFs(Environment
                                        .getExternalStorageDirectory()
                                        .getAbsolutePath());

                                // 获取SDCard上BLOCK总数
                                long nTotalBlocks = statfs.getBlockCount();

                                // 获取SDCard上每个block的SIZE
                                long nBlocSize = statfs.getBlockSize();

                                // 获取可供程序使用的Block的数量
                                long nAvailaBlock = statfs.getAvailableBlocks();

                                // 计算SDCard 总容量大小MB
                                long total = nTotalBlocks * nBlocSize;

                                // 计算 SDCard 剩余大小MB
                                long free = nAvailaBlock * nBlocSize;

                                // 计算 SDCard 剩余大小MB
                                long used = total - free;

                                ViewGroup.LayoutParams params = usedView
                                        .getLayoutParams();
                                params.width = (int) (totalView.getWidth()
                                        * used / total);
                                usedView.setLayoutParams(params);

                                usageText.setText(FileUtil.convertStorage(used)
                                        + "/" + FileUtil.convertStorage(total));
                            } catch (IllegalArgumentException e) {

                            }
                        }
                    }
                });
        QueryTask task = new QueryTask();
        task.execute("");
    }

    class QueryTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... params) {
            FileCategoryHelper.refreshCategoryInfo(getActivity());
            return null;
        }

        @Override
        protected void onPostExecute(Integer result) {
            for (FileCategory fc : FileCategoryHelper.sCategories) {
                FileCategoryHelper.CategoryInfo categoryInfo = FileCategoryHelper
                        .getCategoryInfos().get(fc);
                setCategoryCount(fc, categoryInfo.count);
            }
        }

    }

    public void setupClick() {
        setupClick(R.id.category_music);
        setupClick(R.id.category_video);
        setupClick(R.id.category_picture);
        setupClick(R.id.category_document);
        setupClick(R.id.category_zip);
        setupClick(R.id.category_apk);
    }

    private void setupClick(int id) {
        View button = view.findViewById(id);
        button.setOnClickListener(onClickListener);
    }

    private static int getCategoryCountId(FileCategory fc) {
        switch (fc) {
            case Music:
                return R.id.category_music_count;
            case Video:
                return R.id.category_video_count;
            case Picture:
                return R.id.category_picture_count;
            case Doc:
                return R.id.category_document_count;
            case Zip:
                return R.id.category_zip_count;
            case Apk:
                return R.id.category_apk_count;
            default:
                break;
        }
        return 0;
    }

    private void setCategoryCount(FileCategory fc, long count) {
        int id = getCategoryCountId(fc);
        if (id == 0)
            return;
        setTextView(id, "(" + count + ")");
    }

    private void setTextView(int id, String t) {
        TextView text = (TextView) view.findViewById(id);
        text.setText(t);
    }

    public void registerReceiver() {
        receiver = new ScannerReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
        getActivity().registerReceiver(receiver, filter);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FileCategory f = button2Category.get(v.getId());
            if (f != null) {
                showCategoryList(f);
            }
        }
    };

    public void showCategoryList(FileCategory f) {
        listFragment = new FileListFragment();
        listFragment.setCategory(f);
        Bundle bundle = new Bundle();
        bundle.putInt(FileConst.Extra_Explore_Type,
                FileConst.Value_Explore_Type_Categories);
        listFragment.setArguments(bundle);
        FragmentTransaction transaction = getFragmentManager()
                .beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.replace(R.id.content_category, listFragment);
        transaction.commit();

        Intent intent = new Intent();
        int mask = 0;
        mask = MainActivity.FlagRefreshListItem
                | MainActivity.FlagToggleHiddleItem
                | MainActivity.FlagAddFileItem;
        intent.putExtra(FileConst.Extra_Menu_Mask, mask);
        intent.setAction(FileConst.Action_Set_File_View_ActionBar);
        getActivity().sendBroadcast(intent);

    }

    public boolean hideCategoryList() {
        if (listFragment == null) {
            return false;
        } else {
            FragmentTransaction transaction = getFragmentManager()
                    .beginTransaction();
            transaction
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            transaction.remove(listFragment);
            transaction.commit();
            listFragment = null;
            // frameLayout.setVisibility(View.GONE);
            Intent intent = new Intent();
            int mask = MainActivity.FlagSearchFileItem
                    | MainActivity.FlagToggleViewItem
                    | MainActivity.FlagAddFileItem
                    | MainActivity.FlagToggleHiddleItem;
            intent.putExtra(FileConst.Extra_Menu_Mask, mask);
            intent.setAction(FileConst.Action_Set_File_View_ActionBar);
            getActivity().sendBroadcast(intent);
            return true;
        }
    }

    private static final int MSG_FILE_CHANGED_TIMER = 100;
    private Timer timer;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FILE_CHANGED_TIMER:
                    updateUI();
                    break;
            }
            super.handleMessage(msg);
        }

    };
    private boolean inSelectMode;

    synchronized public void notifyFileChanged() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                timer = null;
                Message message = new Message();
                message.what = MSG_FILE_CHANGED_TIMER;
                handler.sendMessage(message);
            }
        }, 1000);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {

        }
        super.onDestroy();
    }

    @Override
    public boolean doBackAction() {
        if (inSelectMode) {
            exitSelectMode();
            return true;
        }

        if (listFragment != null) {
            hideCategoryList();
            return true;
        }
        return false;
    }

    @Override
    public boolean doVeryAction(Intent intent) {
        String action = intent.getAction();
        if (FileConst.Action_FileItem_Long_Click.equals(action)) {
            change2SelectMode();
        } else if (FileConst.Action_FileItem_Unselect.equals(action)) {
            // selectAllButton.setOnCheckedChangeListener(null);
            // selectAllButton.setChecked(false);
            // selectAllButton.setOnCheckedChangeListener(this);
        } else if (FileConst.Action_File_Operation_Done.equals(action)) {
            exitSelectMode();
        } else if (FileConst.Action_Search_File.equals(action)) {
            String query = intent
                    .getStringExtra(FileConst.Key_Search_File_Query);
            searchFile(query);
        } else if (FileConst.Action_Quit_Search.equals(action)) {
            searchFile("");
        } else if (FileConst.Action_Toggle_View_Mode.equals(action)) {
            toggleViewMode();
        } else if (FileConst.Action_Refresh_FileList.equals(action)) {
            new Handler().post(new Runnable() {

                @Override
                public void run() {
                    refreshFileList();
                }
            });
        } else if (FileConst.Action_Copy_File.equals(action)) {
            copyFile();
        } else if (FileConst.Action_Move_File.equals(action)) {
            moveFile();
        } else if (FileConst.Action_Delete_File.equals(action)) {
            deleteFile();
        } else if (FileConst.Action_Rename_File.equals(action)) {
            renameFile();
        } else if (FileConst.Action_Zip_File.equals(action)) {
            zipFile();
        } else if (FileConst.Action_Property_File.equals(action)) {
            propertyFile();
        }
        return false;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Intent intent = new Intent();
            int mask = 0;
            if (listFragment == null) {
                mask = MainActivity.FlagSearchFileItem
                        | MainActivity.FlagToggleViewItem
                        | MainActivity.FlagAddFileItem
                        | MainActivity.FlagToggleHiddleItem;
            } else {
                mask = MainActivity.FlagRefreshListItem
                        | MainActivity.FlagToggleHiddleItem
                        | MainActivity.FlagAddFileItem;
            }
            intent.putExtra(FileConst.Extra_Menu_Mask, mask);
            intent.setAction(FileConst.Action_Set_File_View_ActionBar);
            getActivity().sendBroadcast(intent);
        }
    }

    public void deleteFile() {
        if (listFragment != null) {
            listFragment.deleteFile();
        }
    }

    public void moveFile() {
        if (listFragment != null) {
            listFragment.moveFile();
        }
    }

    public void copyFile() {
        if (listFragment != null) {
            listFragment.copyFile();
        }
    }

    /**
     * 调用系统扫描方法起不到什么作用，真的。除非发生如下情况： 1.所有app在修改、删除、添加、移动文件时都更新content.
     * 2.手动扫描全盘，但是慢
     */
    public void refreshFileList() {
        // Intent.ACTION_MEDIA_MOUNTED not allow after API19
        // getActivity().sendBroadcast(new Intent(
        // Intent.ACTION_MEDIA_MOUNTED,
        // Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        // getActivity().sendBroadcast(new
        // Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, mediaMountUri));
        // no use,it won't scan all files
        // MediaScannerConnection.scanFile(getActivity(),
        // new String[] { Environment.getExternalStorageDirectory()
        // .getAbsolutePath() }, null,
        // new MediaScannerConnection.OnScanCompletedListener() {
        //
        // @Override
        // public void onScanCompleted(String path, Uri uri) {
        //
        // }
        // });

        refreshData();
        if (listFragment != null) {
            listFragment.refreshFileList();
        }
    }

    public void toggleViewMode() {
        if (listFragment != null) {
            listFragment.toggleViewMode();
        }
    }

    public void searchFile(String query) {
        if (listFragment != null) {
            listFragment.searchFile(query);
        }
    }

    private void exitSelectMode() {
        if (listFragment != null) {
            // selectAllButton.setVisibility(View.GONE);
            inSelectMode = false;
            listFragment.exitSelectMode();

            Intent intent = new Intent();
            int mask = 0;
            mask = MainActivity.FlagRefreshListItem
                    | MainActivity.FlagToggleHiddleItem
                    | MainActivity.FlagAddFileItem;
            intent.putExtra(FileConst.Extra_Menu_Mask, mask);
            intent.setAction(FileConst.Action_Set_File_View_ActionBar);
            getActivity().sendBroadcast(intent);
        }
    }

    private void change2SelectMode() {
        if (listFragment != null) {
            // selectAllButton.setVisibility(View.VISIBLE);
            inSelectMode = true;
            listFragment.change2SelectMode();

            Intent intent = new Intent();
            int mask = 0;
            mask = MainActivity.FlagFavorItem | MainActivity.FlagUnzipFileItem
                    | MainActivity.FlagZipFileItem
                    | MainActivity.FlagRenameFileItem;
            intent.putExtra(FileConst.Extra_Menu_Mask, mask);
            intent.setAction(FileConst.Action_Set_File_Operation_ActionBar);
            getActivity().sendBroadcast(intent);
        }
    }

    public class ScannerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)
                    || action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                notifyFileChanged();
            }
        }
    }

    /**
     * 彻底地扫描全盘
     * 
     * @author NashLegend
     */
    class ScanTask extends AsyncTask<String, Integer, Boolean> {

        ArrayList<String> paths = new ArrayList<String>();

        public void scanFile(File file) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (int i = 0; i < files.length; i++) {
                        File file2 = files[i];
                        scanFile(file2);
                    }
                }
            } else {
                String suffix = FileUtil.getFileSuffix(file);
                if (suffix.length() > 0 && isArrayContains(suffix)) {
                    paths.add(file.getAbsolutePath());
                }
            }
        }

        private boolean isArrayContains(String suffix) {
            // 事实上无需扫描媒体文件，如果后缀是媒体文件但是却没有被MediaScanner扫描到，说明这可能是一个私有的文件
            // 比如在Android/data/com.xx.xxx/image里面有文件是不会想要被放进媒体库的，所以不使用下列数组
            // String[] strs = { "mp3", "jpg", "jpeg", "bmp", "gif", "png",
            // "mp4","avi", "rmvk", "mkv", "wmv", "apk", "txt", "doc", "docx",
            // "xls", "xlsx", "ppt", "pptx", "pdf", "zip", "rar", "7z" };

            // 有良心的app在使用doc类型的文件时，也会将其扫描的，没有加入库的可能也是私有的，所以不使用下列数组
            // String[] strs = { "apk", "txt", "doc", "docx", "xls", "xlsx",
            // "ppt", "pptx", "pdf", "zip", "rar", "7z" };

            // 事实上需要扫描的只有下面几个，但是如果所有app厂商足够细心的话，下面几个也不需要的
            // 可以分别以mime取得下面的文件，分别为：
            // application/vnd.android.package-archive、text/plain、application/zip
            // 当然，不能信任他们
            String[] strs = {
                    "apk", "txt", "zip"
            };

            if (strs == null || suffix == null) {
                return false;
            }
            for (int i = 0; i < strs.length; i++) {
                if (suffix.equals(strs[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            scanFile(Environment.getExternalStorageDirectory());
            String[] pathStrings = new String[paths.size()];
            for (int i = 0; i < paths.size(); i++) {
                pathStrings[i] = paths.get(i);
            }

            MediaScannerConnection.scanFile(getActivity(), pathStrings, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("scan", path);
                        }
                    });
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
        }
    }

    @Override
    public void toggleShowHidden() {
        // do nothing
    }

    @Override
    public void renameFile() {
        // TODO
    }

    @Override
    public void zipFile() {
        // do nothing
    }

    @Override
    public void addNewFile() {
        // do nothing
        if (listFragment != null) {
            listFragment.addNewFile();
        }
    }

    @Override
    public void propertyFile() {
        if (listFragment != null) {
            listFragment.propertyFile();
        }
    }
}
