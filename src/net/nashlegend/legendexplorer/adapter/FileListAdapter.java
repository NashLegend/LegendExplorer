package net.nashlegend.legendexplorer.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import net.nashlegend.legendexplorer.consts.FileConst;
import net.nashlegend.legendexplorer.db.BookmarkHelper;
import net.nashlegend.legendexplorer.fragment.CategoriedFragment.FileCategory;
import net.nashlegend.legendexplorer.model.FileItem;
import net.nashlegend.legendexplorer.utils.FileCategoryHelper;
import net.nashlegend.legendexplorer.utils.SharePreferencesUtil;
import net.nashlegend.legendexplorer.view.FileGridItemView;
import net.nashlegend.legendexplorer.view.FileItemView;

import net.nashlegend.legendutils.Tools.FileUtil;
import net.nashlegend.legendutils.Tools.SystemUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

@SuppressLint("DefaultLocale")
public class FileListAdapter extends BaseAdapter implements Filterable {
	private ArrayList<FileItem> list = new ArrayList<FileItem>();
	private Context mContext;
	private File currentDirectory;
	private boolean displayModeGrid = false;
	private Cursor mCursor;
	private FileCategory fc;

	public FileListAdapter(Context Context) {
		mContext = Context;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return displayModeGrid ? 1 : 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (displayModeGrid) {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = new FileGridItemView(mContext);
				holder.fileGridItemView = (FileGridItemView) convertView;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.fileGridItemView.setFileItem(list.get(position), this);
		} else {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = new FileItemView(mContext);
				holder.fileItemView = (FileItemView) convertView;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.fileItemView.setFileItem(list.get(position), this);
		}
		return convertView;
	}

	class ViewHolder {
		FileItemView fileItemView;
		FileGridItemView fileGridItemView;
	}

	public ArrayList<FileItem> getList() {
		return list;
	}

	public void setList(ArrayList<FileItem> list) {
		this.list = list;
	}

	/**
	 * 打开文件夹，更新文件列表
	 * 
	 * @param file
	 */
	public void openFolder(File file) {
		OpenFolderTask task = new OpenFolderTask();
		task.execute(file);
	}

	class OpenFolderTask extends AsyncTask<File, Integer, ArrayList<FileItem>> {

		@Override
		protected ArrayList<FileItem> doInBackground(File... params) {
			ArrayList<FileItem> flist = new ArrayList<FileItem>();
			boolean showhidden = SharePreferencesUtil.readBoolean(
					FileConst.Key_Show_Hiddle_Files, false);
			File file = params[0];
			currentDirectory = file;
			if (file != null
					&& (file.isDirectory() || file.equals(new File(
							FileConst.Value_File_Path_Never_Existed)))) {
				flist.clear();
				if (file.equals(new File(
						FileConst.Value_File_Path_Never_Existed))) {
					BookmarkHelper helper = new BookmarkHelper(mContext);
					helper.open();
					flist = helper.getBookmarks();
					helper.close();
				} else {
					File[] files = null;
					if (file.canRead()) {
						files = file.listFiles();
					} else {
						if (SystemUtil.isRooted()) {
							files = FileUtil.ListFilesWithRoot(file
									.getAbsolutePath());
						}
					}
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							if (showhidden) {
								flist.add(new FileItem(files[i]));
							} else {
								if (!files[i].isHidden()) {
									flist.add(new FileItem(files[i]));
								}
							}

						}
					}
					files = null;
					sortList(flist);
				}
			}
			return flist;
		}

		@Override
		protected void onPostExecute(ArrayList<FileItem> result) {
			setList(result);
			notifyDataSetChanged();
		}

	}

	public void openCursor(FileCategory f) {
		OpenCursorTask task = new OpenCursorTask();
		this.fc = f;
		task.execute(fc);
	}

	public void closeCursor() {
		if (mCursor != null && !mCursor.isClosed()) {
			mCursor.close();
		}
	}

	class OpenCursorTask extends
			AsyncTask<FileCategory, Integer, ArrayList<FileItem>> {

		@Override
		protected ArrayList<FileItem> doInBackground(FileCategory... params) {
			ArrayList<FileItem> flist = new ArrayList<FileItem>();
			currentDirectory = null;
			fc = params[0];
			Cursor cursor = FileCategoryHelper.query(fc, mContext);
			mCursor = cursor;
			flist.clear();
			ArrayList<File> fileNos = new ArrayList<File>();
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToPosition(-1);
				while (cursor.moveToNext()) {
					FileItem item = new FileItem(cursor.getString(1));
					if (item.exists()) {
						flist.add(item);
					} else {
						fileNos.add(item);
					}
				}
			}
			if (fileNos.size() > 0) {
				String[] files = new String[fileNos.size()];
				for (int i = 0; i < fileNos.size(); i++) {
					files[i] = fileNos.get(i).getAbsolutePath();
				}
				if (mContext != null) {
					MediaScannerConnection
							.scanFile(mContext, files, null, null);
				}
			}
			sortList(flist);
			return flist;
		}

		@Override
		protected void onPostExecute(ArrayList<FileItem> result) {
			setList(result);
			notifyDataSetChanged();
		}

	}

	/**
	 * 选择当前目录下所有文件
	 */
	public void selectAll() {
		for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext();) {
			FileItem fileItem = (FileItem) iterator.next();
			fileItem.setSelected(true);
		}
		notifyDataSetChanged();
	}

	/**
	 * 取消所有文件的选中状态
	 */
	public void unselectAll() {
		for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext();) {
			FileItem fileItem = (FileItem) iterator.next();
			fileItem.setSelected(false);
		}
		notifyDataSetChanged();
	}

	public void change2SelectMode() {
		for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext();) {
			FileItem fileItem = (FileItem) iterator.next();
			fileItem.setInSelectMode(true);
		}
		notifyDataSetChanged();
	}

	public void exitSelectMode() {
		for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext();) {
			FileItem fileItem = (FileItem) iterator.next();
			fileItem.setInSelectMode(false);
			fileItem.setSelected(false);
		}
		notifyDataSetChanged();
	}

	public void sortList() {
		FileItemComparator comparator = new FileItemComparator();
		Collections.sort(list, comparator);
	}

	public void sortList(ArrayList<FileItem> lis) {
		FileItemComparator comparator = new FileItemComparator();
		Collections.sort(lis, comparator);
	}

	/**
	 * @return 选中的文件列表
	 */
	public ArrayList<File> getSelectedFiles() {
		ArrayList<File> selectedFiles = new ArrayList<File>();
		for (Iterator<FileItem> iterator = list.iterator(); iterator.hasNext();) {
			FileItem file = iterator.next();// 强制转换为File
			if (file.isSelected()) {
				selectedFiles.add(file);
			}
		}
		return selectedFiles;
	}

	public class FileItemComparator implements Comparator<FileItem> {

		@Override
		public int compare(FileItem lhs, FileItem rhs) {
			if (lhs.isDirectory() != rhs.isDirectory()) {
				// 如果一个是文件，一个是文件夹，优先按照类型排序
				if (lhs.isDirectory()) {
					return -1;
				} else {
					return 1;
				}
			} else {
				// 如果同是文件夹或者文件，则按名称排序
				return lhs.getName().toLowerCase()
						.compareTo(rhs.getName().toLowerCase());
			}
		}
	}

	public File getCurrentDirectory() {
		return currentDirectory;
	}

	public boolean isDisplayModeGrid() {
		return displayModeGrid;
	}

	public void setDisplayModeGrid(boolean displayModeGrid) {
		this.displayModeGrid = displayModeGrid;
	}

	@Override
	public Filter getFilter() {
		return filter;
	}

	private Filter filter = new Filter() {

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			setList((ArrayList<FileItem>) results.values);
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			boolean showhidden = SharePreferencesUtil.readBoolean(
					FileConst.Key_Show_Hiddle_Files, false);
			String mat = constraint.toString().toLowerCase();
			FilterResults results = new FilterResults();
			ArrayList<FileItem> tmpList = new ArrayList<FileItem>();
			File file = currentDirectory;
			if (file != null) {
				if (currentDirectory.equals(new File(
						FileConst.Value_File_Path_Never_Existed))) {
					BookmarkHelper helper = new BookmarkHelper(mContext);
					helper.open();
					tmpList = helper.getBookmarks();
					helper.close();
				} else if (file.isDirectory()) {
					File[] files = file.listFiles();
					if (files != null) {
						for (int i = 0; i < files.length; i++) {
							File tmpFile = files[i];
							if (tmpFile.getName().toLowerCase().contains(mat)) {
								if (showhidden) {
									tmpList.add(new FileItem(tmpFile));
								} else {
									if (!files[i].isHidden()) {
										tmpList.add(new FileItem(tmpFile));
									}
								}
							}

						}
					}
					files = null;
				}
			} else if (mCursor != null) {
				if (mCursor.getCount() > 0) {
					mCursor.moveToPosition(-1);
					while (mCursor.moveToNext()) {
						FileItem item = new FileItem(mCursor.getString(1));
						if (item.getName().toLowerCase().contains(mat)) {
							tmpList.add(item);
						}
					}
				}
			}
			sortList(tmpList);
			results.values = tmpList;
			results.count = tmpList.size();
			return results;
		}
	};

}
