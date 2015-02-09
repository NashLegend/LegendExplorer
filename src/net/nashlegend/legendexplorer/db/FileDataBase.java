
package net.nashlegend.legendexplorer.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FileDataBase extends SQLiteOpenHelper {

    public static final String DatabaseName = "explorer.db";
    public static final String TableBookmark = "bookmark";
    public static final int DBVersion = 1;
    public static FileDataBase mFileDataBase = null;

    public FileDataBase(Context context) {
        super(context, DatabaseName, null, DBVersion);
    }

    public static FileDataBase getInstance(Context context) {
        if (mFileDataBase == null) {
            mFileDataBase = new FileDataBase(context);
        }
        return mFileDataBase;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createBookmarkDB(db);
    }

    private void createBookmarkDB(SQLiteDatabase db) {
        String section_sql = "create table IF NOT EXISTS  " + TableBookmark + "("
                + BookmarkColumn._ID + " integer primary key AUTOINCREMENT,"
                + BookmarkColumn.FILE_NAME + " varchar default '',"
                + BookmarkColumn.FILE_PATH + " varchar default '' )";
        try {
            db.execSQL(section_sql);
        } catch (SQLException e) {
        } finally {
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
