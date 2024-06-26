package com.bihe0832.android.lib.download.core.dabase;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.bihe0832.android.lib.file.FileUtils;
import com.bihe0832.android.lib.log.ZLog;
import com.bihe0832.android.lib.sqlite.BaseDBHelper;
import com.bihe0832.android.lib.sqlite.BaseTableModel;

/**
 * DownloadPartInfoTableModel
 *
 * @author zixie code@bihe0832.com Created on 2020/6/12.
 */
public class DownloadPartInfoTableModel extends BaseTableModel {

    public static final String col_id = "id";
    public static final String col_download_part_id = "download_part_id";
    public static final String col_part_id = "part_id";
    public static final String col_download_id = "download_id";
    public static final String col_range_start = "range_start";
    public static final String col_local_start = "local_start";
    public static final String col_length = "length";
    public static final String col_finished = "finished";
    static final String TABLE_NAME = "download_part_info";
    static final String TABLE_CREATE_SQL = "CREATE TABLE IF NOT EXISTS ["
            + TABLE_NAME
            + "] ("
            + "[" + col_id + "] INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "[" + col_download_part_id + "] NVARCHAR(128) NOT NULL,"
            + "[" + col_part_id + "] INT  NULL,"
            + "[" + col_download_id + "] NVARCHAR(128)  NULL,"
            + "[" + col_range_start + "] VARCHAR(256)  NULL,"
            + "[" + col_local_start + "] VARCHAR(256)  NULL,"
            + "[" + col_length + "] VARCHAR(256)  NULL,"
            + "[" + col_finished + "] VARCHAR(256) NULL"
            + ")";
    static final String TABLE_DROP_SQL = "DROP TABLE IF EXISTS " + TABLE_NAME;

    static int deleteAll(BaseDBHelper helper) {
        return deleteAll(helper, TABLE_NAME);
    }

    private static ContentValues data2CV(String download_part_id, int partID, long download_id, long rangeStart,
            long localStart, long length, long finished) {
        ContentValues cv = new ContentValues();
        putValues(cv, col_download_part_id, download_part_id);
        putValues(cv, col_part_id, partID);
        putValues(cv, col_download_id, download_id);
        putValues(cv, col_range_start, rangeStart);
        putValues(cv, col_local_start, localStart);
        putValues(cv, col_length, length);
        putValues(cv, col_finished, finished);
        return cv;
    }

    static boolean insertData(BaseDBHelper helper, String download_part_id, int partID, long download_id,
            long rangeStart, long localStart, long length, long finished) {
        ContentValues values = data2CV(download_part_id, partID, download_id, rangeStart, localStart, length, finished);
        long id = helper.insert(TABLE_NAME, null, values);
        return (id != -1);
    }

    static boolean updateData(BaseDBHelper helper, String download_part_id, int partID, long download_id,
            long rangeStart, long localStart, long length, long finished) {
        ContentValues values = data2CV(download_part_id, partID, download_id, rangeStart, localStart, length, finished);
        String whereClause = " `" + col_download_part_id + "` = ? ";
        String[] whereArgs = new String[]{download_part_id};
        int rows = helper.update(TABLE_NAME, values, whereClause, whereArgs);
        return (rows != 0);
    }

    static boolean hasData(BaseDBHelper helper, long download_id, boolean showDetail) {
        boolean hasData = hasData(helper, download_id);
        ZLog.d("数据库信息：hasData of :" + download_id + " is :" + hasData);
        if (showDetail) {
            Cursor cursor = helper.queryInfo(TABLE_NAME, null, null, null, null, null, null, null);
            try {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    long id = getLongByName(cursor, col_id);
                    long finished = getLongByName(cursor, col_finished);
                    long downloadINfo = getLongByName(cursor, col_download_id);
                    String partID = getStringByName(cursor, col_download_part_id);
                    ZLog.d("数据库信息：id:" + id + "，partID :" + partID + "，download_id :" + downloadINfo + "finished :"
                            + finished);
                    cursor.moveToNext();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return hasData;
    }

    static boolean hasData(BaseDBHelper helper, String download_part_id) {
        String[] columns = null;
        String selection = " " + col_download_part_id + " = ? ";
        String[] selectionArgs = {download_part_id};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        boolean find = false;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns,
                selection, selectionArgs, groupBy, having, orderBy, limit);
        try {
            find = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return find;
    }

    static boolean hasData(BaseDBHelper helper, long download_id) {
        boolean find = false;
        Cursor cursor = getDownloadPartInfo(helper, download_id);
        try {
            find = (cursor != null && cursor.getCount() > 0);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return find;
    }

    static boolean saveData(BaseDBHelper helper, String download_part_id, int partID, long download_id, long rangeStart,
            long localStart, long length, long finished) {
        if (TextUtils.isEmpty(download_part_id)) {
            return false;
        }

        boolean success;
        if (hasData(helper, download_part_id)) {
            success = updateData(helper, download_part_id, partID, download_id, rangeStart, localStart, length,
                    finished);
        } else {
            success = insertData(helper, download_part_id, partID, download_id, rangeStart, localStart, length,
                    finished);
        }

        return success;
    }

    static boolean updateDownloadFinished(BaseDBHelper helper, String downloadPartID, long finished) {
        ContentValues cv = new ContentValues();
        putValues(cv, col_download_part_id, downloadPartID);
        putValues(cv, col_finished, finished);
        ZLog.d(DownloadInfoDBManager.TAG,
                "分片下载数据 - " + downloadPartID + " DB 数据更新，累积下载:" + finished + ", " + FileUtils.INSTANCE
                        .getFileLength(finished));

        String whereClause = " `" + col_download_part_id + "` = ? ";
        String[] whereArgs = new String[]{downloadPartID};
        int rows = helper.update(TABLE_NAME, cv, whereClause, whereArgs);
        return (rows != 0);
    }

    static Cursor getDownloadPartInfo(BaseDBHelper helper, long download_id) {
        String[] columns = null;
        String selection = " " + col_download_id + " = ? ";
        String[] selectionArgs = {String.valueOf(download_id)};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns,
                selection, selectionArgs, groupBy, having, orderBy, limit);
        return cursor;
    }


    static long getFinished(BaseDBHelper helper, long download_id) {
        String[] columns = null;
        String selection = " " + col_download_id + " = ? ";
        String[] selectionArgs = {String.valueOf(download_id)};
        String groupBy = null;
        String having = null;
        String orderBy = null;
        String limit = null;
        Cursor cursor = helper.queryInfo(TABLE_NAME, columns,
                selection, selectionArgs, groupBy, having, orderBy, limit);
        long finished = 0;
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                long partINfo = getLongByName(cursor, col_finished);
                String partID = getStringByName(cursor, col_download_part_id);
                ZLog.d("getFinished:" + partINfo + "of id :" + partID);
                finished += partINfo;
                ZLog.d(DownloadInfoDBManager.TAG, "分片下载数据 - " + partID + " DB数据读取，累积下载:" + partINfo);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return finished;
    }


    static boolean clearData(BaseDBHelper helper, long download_id) {
        int rows = 0;
        String whereClause = " " + col_download_id + " = ? ";
        String[] whereArgs = {String.valueOf(download_id)};
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            rows = db.delete(TABLE_NAME, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
            rows = 0;
        }
        return rows != 0;
    }
}
