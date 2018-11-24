package com.abase.okhttp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 数据库操作
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/3
 */

public class SQLTools {
    private SQLiteDatabase database;
    public static SQLTools sqlTools;

    /**
     * 获取单例
     *
     * @param context
     * @return
     */
    public static SQLTools init(Context context) {
        if (sqlTools == null) {
            sqlTools = new SQLTools(context);
        }
        return sqlTools;
    }

    /**
     * 初始化
     */
    private SQLTools(Context context) {
        if (database == null && context != null) {
            database = helper(context).getWritableDatabase();
        }
    }

    /**
     * 初始化数据库
     */
    private SQLHelper helper(Context context) {
        return new SQLHelper(context);
    }

    /**
     * 保存上传的数据
     *
     * @param id          任务ID
     * @param length      目前上传的长度
     * @param totallength 总长度
     * @param uploadid    上传的sourceid
     * @param uploadurl   上传的 url
     */
    public void saveUploadInfo(String id, String length, String totallength, String uploadid, String uploadurl) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("length", length);
        contentValues.put("totallength", totallength);
        contentValues.put("uploadid", uploadid);
        contentValues.put("uploadurl", uploadurl);

        Cursor cursor = database.rawQuery("select * from " + SQLHelper.TABLE_UPLOAD + " where id='" + id + "'", null);
        if (cursor.getCount() > 0) {
            cursor.close();
            database.update(SQLHelper.TABLE_UPLOAD, contentValues, "id='" + id + "'", null);
        } else {
            database.insert(SQLHelper.TABLE_UPLOAD, null, contentValues);
        }
        cursor.close();
    }

    /**
     * 查询上传的记录
     *
     * @param id
     * @return
     */
    public JSONObject selectUploadInfo(String id) {
        JSONObject jsonObject = new JSONObject();
        Cursor cursor = database.rawQuery("select * from " + SQLHelper.TABLE_UPLOAD + " where id='" + id + "'", null);
        cursor.isFirst();
        while (cursor.moveToNext()) {
            try {
                jsonObject.put("id", cursor.getString(cursor.getColumnIndex("id")));
                jsonObject.put("length", cursor.getString(cursor.getColumnIndex("length")));
                jsonObject.put("totallength", cursor.getString(cursor.getColumnIndex("totallength")));
                jsonObject.put("uploadid", cursor.getString(cursor.getColumnIndex("uploadid")));
                jsonObject.put("uploadurl", cursor.getString(cursor.getColumnIndex("uploadurl")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return jsonObject;
    }

    /**
     * 按照id删除上传记录
     *
     * @param id
     */
    public void delUpload(String id) {
        database.delete(SQLHelper.TABLE_UPLOAD, "id=" + id, null);
    }

    /**
     * 清理上传记录
     *
     * @param id
     */
    public void clearUploadTab(String id) {
        database.delete(SQLHelper.TABLE_UPLOAD, "", null);
    }

    /**
     * 保存下载的数据
     *
     * @param id          任务ID
     * @param totallength 总长度
     */
    public void saveDowmloadInfo(String id, String totallength) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("totallength", totallength);

        Cursor cursor = database.rawQuery("select * from " + SQLHelper.TABLE_DOWMLOAD + " where id='" + id + "'", null);
        if (cursor.getCount() > 0) {
            cursor.close();
            database.update(SQLHelper.TABLE_UPLOAD,contentValues,"id='"+id+"'",null);
        } else {
            contentValues.put("id", id);
            database.insert(SQLHelper.TABLE_DOWMLOAD, null, contentValues);
        }
        cursor.close();
    }

    /**
     * 查询下载任务信息
     *
     * @param id
     */
    public JSONObject selectDownLoad(String id) {
        JSONObject jsonObject = new JSONObject();
        try {
            if (database == null) {
                return jsonObject;
            }
            Cursor cursor = database.rawQuery("select * from " + SQLHelper.TABLE_DOWMLOAD + " where id='" + id + "'", null);
            cursor.isFirst();
            while (cursor.moveToNext()) {
                try {
                    jsonObject.put("id", cursor.getString(cursor.getColumnIndex("id")));
                    jsonObject.put("totallength", cursor.getString(cursor.getColumnIndex("totallength")));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    /**
     * 按照id删除下载记录
     *
     * @param id
     */
    public void delDownLoad(String id) {
        database.delete(SQLHelper.TABLE_DOWMLOAD, "id='" + id + "'", null);
    }

    /**
     * 清理下载任务的记录
     *
     * @param id
     */
    public void clearDownLoadTab(String id) {
        database.delete(SQLHelper.TABLE_DOWMLOAD, "", null);
    }

    /**
     * 在应用退出的时候销毁数据库连接
     */
    public void onDestory() {
        if (database != null) {
            database.close();
            database = null;
            sqlTools = null;
        }
    }
}
