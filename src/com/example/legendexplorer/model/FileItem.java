package com.example.legendexplorer.model;

import java.io.File;
import java.net.URI;

import com.example.legendexplorer.R;
import com.example.legendexplorer.R.drawable;

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
	public static final int FILE_TYPE_SOUND = 2;
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

	// 当然不全……
	public static final String[] soundSuffixArray = { "mp3", "wav" };
	public static final String[] imageSuffixArray = { "jpg", "jpeg", "png",
			"bmp", "gif" };
	public static final String[] videoSuffixArray = { "mp4", "avi", "rmvb",
			"flv", "mkv", "wmv", };
	public static final String[] apkSuffixArray = { "apk" };
	public static final String[] txtSuffixArray = { "txt", "xml" };
	public static final String[] zipSuffixArray = { "zip", "rar", "gz", "7z" };

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
	private int ItemTyle = 0;

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
			if (isArrayContains(apkSuffixArray, suffix)) {
				setFileType(FILE_TYPE_APK);
			} else if (isArrayContains(imageSuffixArray, suffix)) {
				setFileType(FILE_TYPE_IMAGE);
			} else if (isArrayContains(soundSuffixArray, suffix)) {
				setFileType(FILE_TYPE_SOUND);
			} else if (isArrayContains(videoSuffixArray, suffix)) {
				setFileType(FILE_TYPE_VIDEO);
			} else if (isArrayContains(txtSuffixArray, suffix)) {
				setFileType(FILE_TYPE_TXT);
			} else if (isArrayContains(zipSuffixArray, suffix)) {
				setFileType(FILE_TYPE_ZIP);
			} else {
				setFileType(FILE_TYPE_NORMAL);
			}
		}
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
		case FILE_TYPE_SOUND:
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
}
