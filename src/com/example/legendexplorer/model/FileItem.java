
package com.example.legendexplorer.model;

import java.io.File;
import java.net.URI;

import com.example.legendexplorer.R;
import com.example.legendexplorer.R.drawable;
import com.example.legendexplorer.application.ExplorerApplication;

/**
 * 文件对象，继承自File
 * 
 * @author NashLegend
 */
public class FileItem extends File {

    /**
     * TODO。每次改动后都要修改serialVersionUID……
     */
    private static final long serialVersionUID = 1L;
    /**
     * 普通文件
     */
    public static final int FILE_TYPE_NORMAL = 0;
    /**
     * 文件夹
     */
    public static final int FILE_TYPE_FOLDER = 1;
    /**
     * 声音类型的文件
     */
    public static final int FILE_TYPE_AUDIO = 2;
    /**
     * 图像类型的文件
     */
    public static final int FILE_TYPE_IMAGE = 3;
    /**
     * 视频类型的文件
     */
    public static final int FILE_TYPE_VIDEO = 4;
    /**
     * APK文件
     */
    public static final int FILE_TYPE_APK = 5;
    /**
     * TXT文件
     */
    public static final int FILE_TYPE_TXT = 6;
    /**
     * ZIP文件
     */
    public static final int FILE_TYPE_ZIP = 7;
    /**
     * HTML文件
     */
    public static final int FILE_TYPE_HTML = 8;
    /**
     * WORD文件
     */
    public static final int FILE_TYPE_WORD = 9;
    /**
     * EXCEL文件
     */
    public static final int FILE_TYPE_EXCEL = 10;
    /**
     * PPT文件
     */
    public static final int FILE_TYPE_PPT = 11;
    /**
     * PDF文件
     */
    public static final int FILE_TYPE_PDF = 12;
    /**
     * 电子书文件
     */
    public static final int FILE_TYPE_EBOOK = 13;
    /**
     * 种子文件
     */
    public static final int FILE_TYPE_TORRENT = 14;
    /**
     * CHM文件
     */
    public static final int FILE_TYPE_CHM = 15;

    /**
     * 文件在文件列表中显示的icon
     */
    private int icon = R.drawable.ic_launcher;

    /**
     * 文件是否在列表中被选中
     */
    private boolean selected = false;

    /**
     * 文件类型，默认为FILE_TYPE_NORMAL，即普通文件。
     */
    private int fileType = FILE_TYPE_NORMAL;

    /**
     * 文件后缀
     */
    private String suffix = "";

    /**
     * 是否处于可选状态（可显示选择复）
     */
    private boolean inSelectMode = false;
    /**
     * FileItem类型，普通文件，收藏文件，或者类型文件
     */
    private int itemType = 0;

    public static final int Item_Type_File_Or_Folder = 0;
    public static final int Item_type_Bookmark = 1;

    public FileItem(File file) {
        this(file.getAbsolutePath());
    }

    public FileItem(String path) {
        super(path);
        setFileTypeBySuffix();
    }

    public FileItem(URI uri) {
        super(uri);
        setFileTypeBySuffix();
    }

    public FileItem(File dir, String name) {
        super(dir, name);
        setFileTypeBySuffix();
    }

    public FileItem(String dirPath, String name) {
        super(dirPath, name);
        setFileTypeBySuffix();
    }

    /**
     * 根据后缀取得文件类型
     */
    private void setFileTypeBySuffix() {
        if (isDirectory()) {
            setFileType(FILE_TYPE_FOLDER);
        } else {
            String suffix = getSuffixFromName();
            // 在此处设置后缀
            setSuffix(suffix);
            // 根据后缀获取文件类型
            if (isArrayContains(getStringArraySourceByName(R.array.TypePackage), suffix)) {
                setFileType(FILE_TYPE_APK);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeImage), suffix)) {
                setFileType(FILE_TYPE_IMAGE);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeAudio), suffix)) {
                setFileType(FILE_TYPE_AUDIO);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeVideo), suffix)) {
                setFileType(FILE_TYPE_VIDEO);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeText), suffix)) {
                setFileType(FILE_TYPE_TXT);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeZip), suffix)) {
                setFileType(FILE_TYPE_ZIP);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeExcel), suffix)) {
                setFileType(FILE_TYPE_EXCEL);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeHTML), suffix)) {
                setFileType(FILE_TYPE_HTML);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypePDF), suffix)) {
                setFileType(FILE_TYPE_PDF);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypePPT), suffix)) {
                setFileType(FILE_TYPE_PPT);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeWord), suffix)) {
                setFileType(FILE_TYPE_WORD);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeChm), suffix)) {
                setFileType(FILE_TYPE_CHM);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeTorrent), suffix)) {
                setFileType(FILE_TYPE_TORRENT);
            } else if (isArrayContains(getStringArraySourceByName(R.array.TypeEBook), suffix)) {
                setFileType(FILE_TYPE_EBOOK);
            } else {
                setFileType(FILE_TYPE_NORMAL);
            }
        }
    }

    private String[] getStringArraySourceByName(int id) {
        return ExplorerApplication.getGlobalContext().getResources().getStringArray(id);
    }

    private boolean isArrayContains(String[] strs, String suffix) {
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

    private String getSuffixFromName() {
        String fileName = getName();
        String suffix = "";
        int offset = fileName.lastIndexOf(".");
        // -1则没有后缀。0,则表示是一个隐藏文件而没有后缀，offset == fileName.length() -
        // 1，表示"."是最后一个字符，没有后缀
        if (offset > 0 && offset < fileName.length() - 1) {
            suffix = fileName.substring(offset + 1);
        }
        return suffix;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getFileType() {
        return fileType;
    }

    /**
     * 设置fileTyle,同时修改icon,目前没有icon...TODO
     * 
     * @param fileType
     */
    public void setFileType(int fileType) {
        this.fileType = fileType;
        switch (fileType) {
            case FILE_TYPE_APK:
                setIcon(R.drawable.format_apk);
                break;
            case FILE_TYPE_FOLDER:
                setIcon(R.drawable.format_folder);
                break;
            case FILE_TYPE_IMAGE:
                setIcon(R.drawable.format_picture);
                break;
            case FILE_TYPE_NORMAL:
                setIcon(R.drawable.format_unkown);
                break;
            case FILE_TYPE_AUDIO:
                setIcon(R.drawable.format_music);
                break;
            case FILE_TYPE_TXT:
                setIcon(R.drawable.format_text);
                break;
            case FILE_TYPE_VIDEO:
                setIcon(R.drawable.format_media);
                break;
            case FILE_TYPE_ZIP:
                setIcon(R.drawable.format_zip);
                break;
            case FILE_TYPE_HTML:
                setIcon(R.drawable.format_html);
                break;
            case FILE_TYPE_PDF:
                setIcon(R.drawable.format_pdf);
                break;
            case FILE_TYPE_WORD:
                setIcon(R.drawable.format_word);
                break;
            case FILE_TYPE_EXCEL:
                setIcon(R.drawable.format_excel);
                break;
            case FILE_TYPE_PPT:
                setIcon(R.drawable.format_ppt);
                break;
            case FILE_TYPE_TORRENT:
                setIcon(R.drawable.format_torrent);
                break;
            case FILE_TYPE_EBOOK:
                setIcon(R.drawable.format_ebook);
                break;
            case FILE_TYPE_CHM:
                setIcon(R.drawable.format_chm);
                break;
            default:
                setIcon(R.drawable.format_unkown);
                break;
        }
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public boolean isInSelectMode() {
        return inSelectMode;
    }

    public void setInSelectMode(boolean inSelectMode) {
        this.inSelectMode = inSelectMode;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
