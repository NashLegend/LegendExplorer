
package net.nashlegend.legendexplorer.db;

import android.provider.BaseColumns;

public class BookmarkColumn implements BaseColumns {

    public BookmarkColumn() {

    }

    // 列名
    public static final String FILE_NAME = "FILE_NAME"; // 文件名
    public static final String FILE_PATH = "FILE_PATH"; // 文件路径

    // 索引值
    public static final int FILE_NAME_COLUMN = 0;
    public static final int FILE_PATH_COLUMN = FILE_NAME_COLUMN + 1;
    // 查询结果集
    public static final String[] PROJECTION =
    {
            FILE_NAME,
            FILE_PATH
    };

}
