package net.nashlegend.legendexplorer.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.os.FileObserver;
import android.util.Log;

public class StorageObserver extends FileObserver {

	public static int CHANGES_ONLY = CLOSE_WRITE | MOVE_SELF | MOVED_FROM;

	List<SingleFileObserver> mObservers;
	String mPath;
	int mMask;

	public StorageObserver(String path) {
		this(path, ALL_EVENTS);
	}

	public StorageObserver(String path, int mask) {
		super(path, mask);
		mPath = path;
		mMask = mask;
	}

	@Override
	public void startWatching() {
		if (mObservers != null)
			return;
		mObservers = new ArrayList<SingleFileObserver>();
		Stack<String> stack = new Stack<String>();
		stack.push(mPath);

		while (!stack.empty()) {
			String parent = stack.pop();
			mObservers.add(new SingleFileObserver(parent, mMask));
			File path = new File(parent);
			File[] files = path.listFiles();
			if (files == null)
				continue;
			for (int i = 0; i < files.length; ++i) {
				if (files[i].isDirectory() && !files[i].getName().equals(".")
						&& !files[i].getName().equals("..")) {
					stack.push(files[i].getPath());
				}
			}
		}
		for (int i = 0; i < mObservers.size(); i++)
			mObservers.get(i).startWatching();
	}

	@Override
	public void stopWatching() {
		if (mObservers == null)
			return;

		for (int i = 0; i < mObservers.size(); ++i)
			mObservers.get(i).stopWatching();

		mObservers.clear();
		mObservers = null;
	}

	@Override
	public void onEvent(int event, String path) {
		event &= FileObserver.ALL_EVENTS;
		switch (event) {
		case FileObserver.DELETE:
			Log.i("obs", "DELETE" + "__" + path);
			break;
		case FileObserver.CREATE:
			Log.i("obs", "CREATE" + "__" + path);
			File createdFile = new File(path);
			if (createdFile.isDirectory()) {
				SingleFileObserver ob = new SingleFileObserver(path, mMask);
				ob.startWatching();
				mObservers.add(ob);
			}
			break;
		case FileObserver.MOVED_TO:
			Log.i("obs", "MOVED_TO" + "__" + path);
			break;
		case FileObserver.MOVED_FROM:
			Log.i("obs", "MOVED_FROM" + "__" + path);
			break;
		default:
			break;
		}
	}

	private class SingleFileObserver extends FileObserver {
		private String mPath;

		public SingleFileObserver(String path, int mask) {
			super(path, mask);
			mPath = path;
		}

		@Override
		public void onEvent(int event, String path) {
			String newPath = mPath + "/" + path;
			StorageObserver.this.onEvent(event, newPath);
		}

	}
}