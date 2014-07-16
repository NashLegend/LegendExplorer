package com.example.legendexplorer.fragment;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.example.legendexplorer.MainActivity;
import com.example.legendexplorer.R;
import com.example.legendexplorer.adapter.FileListAdapter;
import com.example.legendexplorer.consts.FileConst;
import com.example.legendexplorer.db.BookmarkHelper;
import com.example.legendexplorer.dialog.FilePropertyDialog;
import com.example.legendexplorer.fragment.CategoriedFragment.FileCategory;
import com.example.legendexplorer.model.FileItem;
import com.example.legendexplorer.utils.SharePreferencesUtil;
import com.example.legendutils.Dialogs.FileDialog;
import com.example.legendutils.Dialogs.InputDialog;
import com.example.legendutils.Dialogs.ListDialog;
import com.example.legendutils.Dialogs.Win8ProgressDialog;
import com.example.legendutils.Dialogs.FileDialog.FileDialogListener;
import com.example.legendutils.Dialogs.ListDialog.OnItemSelectedListener;
import com.example.legendutils.Tools.FileUtil;
import com.example.legendutils.Tools.ToastUtil;
import com.example.legendutils.Tools.FileUtil.FileOperationListener;
import com.example.legendutils.Tools.ZipUtil;
import com.example.legendutils.Tools.ZipUtil.ZipOperationListener;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

public class FileListFragment extends Fragment {
	private FileListAdapter adapter;
	private ListView listView;
	private String filePath;
	private int itemType = FileConst.Value_Item_Type_File_Or_Folder;
	private int exploreType = FileConst.Value_Explore_Type_Files;
	private String pathPreffix = "/////////////";
	private GridView gridView;
	private View rootView;
	private String searchQuery = "";
	private FileCategory fc;

	public FileListFragment() {
	}

	@Override
	public void setArguments(Bundle args) {
		super.setArguments(args);
		filePath = args.getString(FileConst.Extra_File_Path);
		itemType = args.getInt(FileConst.Extra_Item_Type,
				FileConst.Value_Item_Type_File_Or_Folder);
		exploreType = args.getInt(FileConst.Extra_Explore_Type,
				FileConst.Value_Explore_Type_Files);
		pathPreffix = args.getString(FileConst.Extra_Path_Preffix,
				"/////////////");
	}

	public void setCategory(FileCategory f) {
		this.fc = f;
	}

	@Override
	public void onDestroy() {
		adapter.closeCursor();
		super.onDestroy();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.layout_file_list, container,
					false);
			listView = (ListView) rootView
					.findViewById(R.id.fragment_listview_files);
			listView.setTextFilterEnabled(true);
			adapter = new FileListAdapter(getActivity());
			gridView = (GridView) rootView
					.findViewById(R.id.fragment_gridview_files);
			gridView.setTextFilterEnabled(true);
			gridView.setLongClickable(true);
			listView.setLongClickable(true);
			initViews();
		} else {
			if (rootView.getParent() != null) {
				((ViewGroup) rootView.getParent()).removeView(rootView);
			}
		}
		return rootView;
	}

	private void initViews() {
		int mode = SharePreferencesUtil.readInt(
				FileConst.Key_Files_Display_Mode,
				FileConst.Value_Files_Display_List);
		if (mode == FileConst.Value_Files_Display_List) {
			gridView.setVisibility(View.GONE);
			gridView.setAdapter(null);
			adapter.setDisplayModeGrid(false);
			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);
		} else {
			listView.setVisibility(View.GONE);
			listView.setAdapter(null);
			adapter.setDisplayModeGrid(true);
			gridView.setVisibility(View.VISIBLE);
			gridView.setAdapter(adapter);
		}
		loadData();
	}

	public void loadData() {
		if (exploreType == FileConst.Value_Explore_Type_Categories) {
			adapter.openCursor(this.fc);
		} else {
			if (filePath != null) {
				if (filePath.equals(FileConst.Value_Bookmark_Path)) {
					adapter.openFolder(new File(
							FileConst.Value_File_Path_Never_Existed));
				} else {
					adapter.openFolder(new File(filePath));
				}
			}
		}
	}

	/**
	 * 选中当前目录所有文件
	 */
	public void selectAll() {
		adapter.selectAll();
		getItemSelect();
	}

	/**
	 * 取消选中当前目录所有文件
	 */
	public void unselectAll() {
		adapter.unselectAll();
		getItemUnselect();
	}

	/**
	 * @return 返回选中的文件列表
	 */
	public File[] getSelectedFiles() {
		ArrayList<File> files = adapter.getSelectedFiles();
		File[] files2 = new File[files.size()];
		for (int i = 0; i < files2.length; i++) {
			files2[i] = files.get(i);
		}
		return files2;

	}

	public void change2SelectMode() {
		adapter.change2SelectMode();
	}

	public void exitSelectMode() {
		adapter.exitSelectMode();
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDisplayedFilePath() {
		switch (exploreType) {
		case FileConst.Value_Explore_Type_Files:
			return filePath;
		case FileConst.Value_Explore_Type_Bookmarks:
			if (pathPreffix.equals("") || pathPreffix.equals("/")) {
				return FileConst.Value_Bookmark_Path.replace("//", "/")
						+ filePath;
			}
			return filePath.replace(pathPreffix, FileConst.Value_Bookmark_Path);
		default:
			return filePath;
		}
	}

	public void toggleViewMode() {
		if (gridView.getVisibility() == View.GONE) {

			listView.setVisibility(View.GONE);
			listView.setAdapter(null);

			adapter.setDisplayModeGrid(true);

			gridView.setVisibility(View.VISIBLE);
			gridView.setAdapter(adapter);

			SharePreferencesUtil.saveInt(FileConst.Key_Files_Display_Mode,
					FileConst.Value_Files_Display_Grid);

		} else {

			gridView.setVisibility(View.GONE);
			gridView.setAdapter(null);

			adapter.setDisplayModeGrid(false);

			listView.setVisibility(View.VISIBLE);
			listView.setAdapter(adapter);

			SharePreferencesUtil.saveInt(FileConst.Key_Files_Display_Mode,
					FileConst.Value_Files_Display_List);

		}

	}

	public void propertyFile() {
		File[] files = getSelectedFiles();
		boolean in = exploreType != FileConst.Value_Explore_Type_Categories
				&& !FileConst.Value_Bookmark_Path.equals(filePath);
		if (files.length > 0) {
			new FilePropertyDialog.Builder(getActivity()).setAllInOneFolder(in)
					.setFiles(files).setTitle("Property").create().show();
		}
	}

	public void addNewFile() {
		if (filePath.equals(FileConst.Value_Bookmark_Path)) {
			// add new book mark
		} else {
			String[] values = { "File", "Folder" };
			ListDialog dialog = new ListDialog.Builder(getActivity())
					.setTitle("choose").setMultiSelect(false)
					.setDisplayedValues(values)
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void OnItemSelected(int[] items) {
							int selected = items[0];
							if (selected == 0) {
								addOneNewFile();
							} else {
								addOneNewFolder();
							}
						}

						@Override
						public void OnCalcelSelect() {

						}
					}).create();
			dialog.show();
		}
	}

	private void addOneNewFile() {
		new InputDialog.Builder(getActivity()).setTitle("Input File Name")
				.setButtonText("Okay", "Nay").setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (arg1 == DialogInterface.BUTTON_POSITIVE) {
							InputDialog dialog = (InputDialog) arg0;
							if (!TextUtils.isEmpty(dialog.InputString)) {
								File file = new File(getFilePath(),
										dialog.InputString);
								if (file.exists()) {
									ToastUtil.showToast(getActivity(),
											"File already Exsited");
									return;
								}
								try {
									if (file.createNewFile()) {
										MediaScannerConnection.scanFile(
												getActivity(),
												new String[] { file
														.getAbsolutePath() },
												null, null);
										refreshFileList();
									} else {
										ToastUtil.showToast(getActivity(),
												"Creating File Failed");
									}
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}
					}
				}).create().show();
	}

	private void addOneNewFolder() {
		new InputDialog.Builder(getActivity()).setTitle("Input Folder Name")
				.setButtonText("Okay", "Nay").setCancelable(true)
				.setCanceledOnTouchOutside(true)
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if (arg1 == DialogInterface.BUTTON_POSITIVE) {
							InputDialog dialog = (InputDialog) arg0;
							if (!TextUtils.isEmpty(dialog.InputString)) {
								File file = new File(getFilePath(),
										dialog.InputString);
								if (file.exists()) {
									ToastUtil.showToast(getActivity(),
											"Folder already Exsited");
									return;
								}
								if (file.mkdir()) {
									refreshFileList();
								} else {
									ToastUtil.showToast(getActivity(),
											"Creating Folder Failed");
								}
							}
						}
					}
				}).create().show();
	}

	public void refreshFileList() {
		loadData();
	}

	public void searchFile(String query) {
		searchQuery = query;
		adapter.getFilter().filter(query);
	}

	public void copyFile() {
		if (adapter.getSelectedFiles().size() == 0) {
			return;
		}
		FileDialog dialog = new FileDialog.Builder(getActivity())
				.setFileMode(FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)
				.setCancelable(true).setCanceledOnTouchOutside(false)
				.setTitle("selectFolder")
				.setFileSelectListener(new FileDialogListener() {

					@Override
					public void onFileSelected(ArrayList<File> files) {
						if (files.size() > 0) {
							copy2Folder(getSelectedFiles(), files.get(0));
						}
					}

					@Override
					public void onFileCanceled() {
						ToastUtil.showToast(getActivity(), "Copy Cancelled!");
					}
				}).create(getActivity());
		dialog.show();
	}

	private void copy2Folder(File[] files, File destFile) {
		Log.i("copy", "cops");
		final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(
				getActivity()).setCancelable(false)
				.setCanceledOnTouchOutside(false).create();
		dialog.show();
		FileUtil.copy2DirectoryAsync(files, destFile, getActivity(),
				new FileOperationListener() {

					@Override
					public void onProgress(int progress) {

					}

					@Override
					public void onError(String e) {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Copy Error!");
					}

					@Override
					public void onComplete() {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Copy OK!");
					}
				});
	}

	public void moveFile() {
		if (adapter.getSelectedFiles().size() == 0) {
			return;
		}
		FileDialog dialog = new FileDialog.Builder(getActivity())
				.setFileMode(FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)
				.setCancelable(false).setCanceledOnTouchOutside(false)
				.setTitle("selectFolder")
				.setFileSelectListener(new FileDialogListener() {

					@Override
					public void onFileSelected(ArrayList<File> files) {
						move2Folder(getSelectedFiles(), files.get(0));
					}

					@Override
					public void onFileCanceled() {

					}
				}).create(getActivity());
		dialog.show();
	}

	private void move2Folder(File[] files, File destFile) {
		final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(
				getActivity()).setCancelable(false)
				.setCanceledOnTouchOutside(false).create();
		dialog.show();
		FileUtil.move2DirectoryAsync(files, destFile, getActivity(),
				new FileOperationListener() {

					@Override
					public void onProgress(int progress) {

					}

					@Override
					public void onError(String e) {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Move Error!");
					}

					@Override
					public void onComplete() {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Move OK!");
					}
				});
	}

	public void deleteFile() {
		if (adapter.getSelectedFiles().size() == 0) {
			return;
		}
		new AlertDialog.Builder(getActivity()).setMessage("Confirm to delete?")
				.setTitle("Message")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						deleteFiles();
					}
				})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).create().show();
	}

	private void deleteFiles() {
		// 删除数据库或者文件
		if (itemType == FileConst.Value_Item_Type_Bookmark) {
			// 删除数据库
			File file = adapter.getSelectedFiles().get(0);
			BookmarkHelper helper = new BookmarkHelper(getActivity());
			helper.open();
			boolean isok = helper.deleteBookmark(adapter.getSelectedFiles());
			helper.close();
			operationDone();
			ToastUtil.showToast(getActivity(), isok ? "Delete OK!"
					: "Delete Error!");
		} else {
			final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(
					getActivity()).setCancelable(false)
					.setCanceledOnTouchOutside(false).create();
			dialog.show();
			final File file = getSelectedFiles()[0];
			FileUtil.deleteAsync(getSelectedFiles(), getActivity(),
					new FileOperationListener() {

						@Override
						public void onProgress(int progress) {

						}

						@Override
						public void onError(String e) {
							dialog.dismiss();
							operationDone();
							ToastUtil.showToast(getActivity(), "Delete Error!");
						}

						@Override
						public void onComplete() {
							dialog.dismiss();
							operationDone();

							MediaScannerConnection.scanFile(getActivity(),
									new String[] { file.getAbsolutePath() },
									null, null);
							ToastUtil.showToast(getActivity(), "Delete OK!");
						}
					});
		}
	}

	public boolean isInSearchingMode() {
		return !TextUtils.isEmpty(searchQuery);
	}

	public void toggleShowHidden() {
		boolean pre = SharePreferencesUtil.readBoolean(
				FileConst.Key_Show_Hiddle_Files, false);
		SharePreferencesUtil.saveBoolean(FileConst.Key_Show_Hiddle_Files, !pre);
		refreshFileList();
	}

	public void renameFile() {
		final File[] files = getSelectedFiles();
		String title = "";
		String input = "";
		if (files.length > 0) {
			if (files.length == 1) {
				title = "Rename file";
				input = files[0].getName();
			} else {
				title = "Rename multi-files";
			}

			new InputDialog.Builder(getActivity()).setTitle(title)
					.setInputText(input)
					.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(DialogInterface dia, int which) {
							if (which == DialogInterface.BUTTON_POSITIVE) {
								InputDialog dialog = (InputDialog) dia;
								if (!TextUtils.isEmpty(dialog.InputString)) {
									String fname = dialog.InputString.trim();
									FileUtil.rename(files, fname, getActivity());
								}
								operationDone();
							}
						}
					}).create().show();
		}

	}

	public void zipFile() {
		final File[] files = getSelectedFiles();
		if (files.length > 0) {
			if (files.length == 1) {
				File sourceFile = files[0];
				String path = "";
				String suffix = FileUtil.getFileSuffix(sourceFile);
				if (suffix.length() > 0) {
					path = sourceFile.getAbsolutePath().replaceAll(
							suffix + "$", "zip");
				} else {
					path = sourceFile.getAbsolutePath() + ".zip";
				}
				File destFile = new File(path);
				zipWithDialog(files, destFile);
			} else {
				new InputDialog.Builder(getActivity())
						.setTitle("Input File Name")
						.setButtonText("Okay", "Nay").setCancelable(true)
						.setCanceledOnTouchOutside(true)
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (arg1 == DialogInterface.BUTTON_POSITIVE) {
									InputDialog dialog = (InputDialog) arg0;
									if (!TextUtils.isEmpty(dialog.InputString)) {
										String fname = dialog.InputString;
										if (!fname.endsWith(".zip")) {
											if (fname.endsWith(".")) {
												fname += "zip";
											} else {
												fname += ".zip";
											}
										}
										File destFile = new File(getFilePath(),
												fname);
										zipWithDialog(files, destFile);
									}
								}
							}
						}).create().show();
			}
		} else {
			// do nothing
		}
	}

	private void zipWithDialog(File[] sourceFile, File destFile) {

		final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(
				getActivity()).setCancelable(false)
				.setCanceledOnTouchOutside(false).create();
		dialog.show();

		ZipUtil.zipAsync(sourceFile, destFile, "", new ZipOperationListener() {

			@Override
			public void onProgress(int progress) {
				// TODO
			}

			@Override
			public void onError(String e) {
				dialog.dismiss();
				operationDone();
				ToastUtil.showToast(getActivity(), "Zip Error!");
			}

			@Override
			public void onComplete() {
				dialog.dismiss();
				operationDone();
				ToastUtil.showToast(getActivity(), "Zip OK!");
			}

			@Override
			public void onCancelled() {
				dialog.dismiss();
				operationDone();
				ToastUtil.showToast(getActivity(), "Zip Cancelled!");
			}
		});
	}

	public void unzipFile() {
		final File[] files = getSelectedFiles();
		if (files.length == 1) {
			final File sourceFile = files[0];
			if ("zip".equals(FileUtil.getFileSuffix(sourceFile))) {
				try {
					if (ZipUtil.isZipfileValid(sourceFile)) {

					} else {
						ToastUtil.showToast(getActivity(),
								"Not a valid zip file!");
					}
				} catch (IOException e1) {
					ToastUtil.showToast(getActivity(),
							"Maybe not a valid zip file!");
				}
				FileDialog dialog = new FileDialog.Builder(getActivity())
						.setFileMode(FileDialog.FILE_MODE_OPEN_FOLDER_SINGLE)
						.setCancelable(true).setCanceledOnTouchOutside(false)
						.setTitle("selectFolder")
						.setFileSelectListener(new FileDialogListener() {

							@Override
							public void onFileSelected(final ArrayList<File> fs) {
								if (fs.size() > 0) {
									final File file = fs.get(0);
									try {
										if (ZipUtil
												.isZipfileEncrypted(sourceFile)) {
											new InputDialog.Builder(
													getActivity())
													.setTitle("Input Password")
													.setButtonText("Okay",
															"Nay")
													.setCancelable(true)
													.setCanceledOnTouchOutside(
															false)
													.setOnClickListener(
															new OnClickListener() {

																@Override
																public void onClick(
																		DialogInterface arg0,
																		int arg1) {
																	if (arg1 == DialogInterface.BUTTON_POSITIVE) {
																		InputDialog dialog = (InputDialog) arg0;
																		unzipWithDialog(
																				sourceFile,
																				file.getAbsolutePath(),
																				dialog.InputString);
																	} else {
																		ToastUtil
																				.showToast(
																						getActivity(),
																						"Unzip cancelled!");
																	}
																}
															}).create().show();
										} else {
											unzipWithDialog(sourceFile,
													file.getAbsolutePath(), "");
										}
									} catch (IOException e) {
										ToastUtil.showToast(getActivity(),
												"Something error Happened");
									}

								}
							}

							@Override
							public void onFileCanceled() {
								ToastUtil.showToast(getActivity(),
										"Unzip Cancelled!");
							}
						}).create(getActivity());
				dialog.show();
			}
		}
	}

	private void unzipWithDialog(File sourceFile, String destFile, String pwd) {

		final Win8ProgressDialog dialog = new Win8ProgressDialog.Builder(
				getActivity()).setCancelable(false)
				.setCanceledOnTouchOutside(false).create();
		dialog.show();

		ZipUtil.unZipAsync(sourceFile, destFile, pwd,
				new ZipOperationListener() {

					@Override
					public void onProgress(int progress) {
						// TODO
					}

					@Override
					public void onError(String e) {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Unzip Error!");
					}

					@Override
					public void onComplete() {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Unzip OK!");
					}

					@Override
					public void onCancelled() {
						dialog.dismiss();
						operationDone();
						ToastUtil.showToast(getActivity(), "Unzip Cancelled!");
					}
				});
	}

	public void favorFile() {
		// TODO
		final File[] files = getSelectedFiles();
		if (files.length == 1) {
			File sourceFile = files[0];
			if (sourceFile.isDirectory()) {
				FileItem item = new FileItem(sourceFile);
				BookmarkHelper helper = new BookmarkHelper(getActivity());
				helper.open();
				boolean isok = helper.insert(item);
				helper.close();

				operationDone();
				ToastUtil.showToast(getActivity(), isok ? "Favor OK!"
						: "Favor Error!");
			} else {
				ToastUtil
						.showToast(getActivity(), "Only Folder Can Be Favored");
			}
		}
	}

	public void getItemSelect() {
		ArrayList<File> files = adapter.getSelectedFiles();
		if (isNormalList()) {
			int mask = MainActivity.MaskNormalListNormal;
			if (files.size() == 1) {
				File file = files.get(0);
				if (file.isDirectory()) {
					mask = MainActivity.MaskNormalListFavor;
				} else if ("zip".equals(FileUtil.getFileSuffix(file))) {
					mask = MainActivity.MaskNormalListUnzip;
				}
			}

			Log.i("mask", mask + "");

			Intent intent = new Intent();
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_Operation_ActionBar);
			getActivity().sendBroadcast(intent);
		}
		Log.i("mask", "masksss");

	}

	public void getItemUnselect() {
		ArrayList<File> files = adapter.getSelectedFiles();
		if (isNormalList()) {
			int mask = MainActivity.MaskNormalListNormal;
			if (files.size() > 0) {
				if (files.size() == 1) {
					File file = files.get(0);
					if (file.isDirectory()) {
						mask = MainActivity.MaskNormalListFavor;
					} else if ("zip".equals(FileUtil.getFileSuffix(file))) {
						mask = MainActivity.MaskNormalListUnzip;
					}
				}
			} else {
				// TODO donot show ActionBarMenu
			}
			Intent intent = new Intent();
			intent.putExtra(FileConst.Extra_Menu_Mask, mask);
			intent.setAction(FileConst.Action_Set_File_Operation_ActionBar);
			getActivity().sendBroadcast(intent);
		}

	}

	public boolean isNormalList() {
		return exploreType != FileConst.Value_Explore_Type_Categories
				&& !FileConst.Value_Bookmark_Path.equals(filePath);
	}

	private void operationDone() {
		Intent intent = new Intent();
		intent.setAction(FileConst.Action_File_Operation_Done);
		getActivity().sendBroadcast(intent);
		refreshFileList();
	}

}
