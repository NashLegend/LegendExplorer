package net.nashlegend.legendexplorer.dialog;

import java.io.File;

import net.nashlegend.legendexplorer.view.FilePropertyView;

import net.nashlegend.legendutils.BuildIn.FileDialogView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup.LayoutParams;

public class FilePropertyDialog extends Dialog {

	public FilePropertyDialog(Context context) {
		super(context);
	}

	public FilePropertyDialog(Context context, int theme) {
		super(context, theme);
	}

	public FilePropertyDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public static class Builder {
		private boolean allInOneFolder = true;
		private String title = "Property";
		private File[] files;
		private Context mContext;

		public Builder(Context context) {
			mContext = context;
		}

		public Builder setAllInOneFolder(boolean in) {
			allInOneFolder = in;
			return this;
		}

		public Builder setTitle(String tit) {
			title = tit;
			return this;
		}

		public Builder setFiles(File[] fs) {
			files = fs;
			return this;
		}

		public FilePropertyDialog create() {
			FilePropertyDialog dialog = new FilePropertyDialog(mContext);
			final FilePropertyView view = new FilePropertyView(mContext);
			view.setFiles(files, allInOneFolder);

			LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			dialog.setTitle(title);
			dialog.addContentView(view, params);
			dialog.setOnCancelListener(new OnCancelListener() {

				@Override
				public void onCancel(DialogInterface dialog) {
					view.cancel();
				}
			});
			return dialog;
		}
	}

}
