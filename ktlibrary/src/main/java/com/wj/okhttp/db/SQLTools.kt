package com.wj.okhttp.db

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.content.ContentValues
import org.json.JSONObject
import org.json.JSONException
import java.lang.Exception

/**
 * 数据库操作
 *
 * @author Admin
 * @version 1.0
 * @date 2017/7/3
 */
class SQLTools private constructor(context: Context?) {
    private var database: SQLiteDatabase? = null

    /**
     * 初始化
     */
    init {
        if (database == null && context != null) {
            database = helper(context).writableDatabase
        }
    }

    /**
     * 初始化数据库
     */
    private fun helper(context: Context): SQLHelper {
        return SQLHelper(context)
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
    fun saveUploadInfo(
        id: String,
        length: String?,
        totallength: String?,
        uploadid: String?,
        uploadurl: String?
    ) {
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("length", length)
        contentValues.put("totallength", totallength)
        contentValues.put("uploadid", uploadid)
        contentValues.put("uploadurl", uploadurl)
        val cursor =
            database!!.rawQuery("select * from " + SQLHelper.Companion.TABLE_UPLOAD + " where id='" + id + "'",
                null)
        if (cursor.count > 0) {
            cursor.close()
            database!!.update(SQLHelper.Companion.TABLE_UPLOAD, contentValues, "id='$id'", null)
        } else {
            database!!.insert(SQLHelper.Companion.TABLE_UPLOAD, null, contentValues)
        }
        cursor.close()
    }

    /**
     * 查询上传的记录
     *
     * @param id
     * @return
     */
    @SuppressLint("Range")
    fun selectUploadInfo(id: String): JSONObject {
        val jsonObject = JSONObject()
        val cursor =
            database!!.rawQuery("select * from " + SQLHelper.Companion.TABLE_UPLOAD + " where id='" + id + "'",
                null)
        cursor.isFirst
        while (cursor.moveToNext()) {
            try {
                jsonObject.put("id", cursor.getString(cursor.getColumnIndex("id")))
                jsonObject.put("length", cursor.getString(cursor.getColumnIndex("length")))
                jsonObject.put("totallength",
                    cursor.getString(cursor.getColumnIndex("totallength")))
                jsonObject.put("uploadid", cursor.getString(cursor.getColumnIndex("uploadid")))
                jsonObject.put("uploadurl", cursor.getString(cursor.getColumnIndex("uploadurl")))
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        cursor.close()
        return jsonObject
    }

    /**
     * 按照id删除上传记录
     *
     * @param id
     */
    fun delUpload(id: String) {
        database!!.delete(SQLHelper.Companion.TABLE_UPLOAD, "id=$id", null)
    }

    /**
     * 清理上传记录
     *
     * @param id
     */
    fun clearUploadTab() {
        database!!.delete(SQLHelper.Companion.TABLE_UPLOAD, "", null)
    }

    /**
     * 保存下载的数据
     *
     * @param id          任务ID
     * @param totallength 总长度
     */
    fun saveDownloadInfo(id: String?, totallength: String?) {
        val contentValues = ContentValues()
        contentValues.put("totallength", totallength)
        val cursor =
            database!!.rawQuery("select * from " + SQLHelper.Companion.TABLE_DOWMLOAD + " where id='" + id + "'",
                null)
        if (cursor.count > 0) {
            cursor.close()
            database!!.update(SQLHelper.Companion.TABLE_UPLOAD, contentValues, "id='$id'", null)
        } else {
            contentValues.put("id", id)
            database!!.insert(SQLHelper.Companion.TABLE_DOWMLOAD, null, contentValues)
        }
        cursor.close()
    }

    /**
     * 查询下载任务信息
     *
     * @param id
     */
    @SuppressLint("Range")
    fun selectDownLoad(id: String?): JSONObject {
        val jsonObject = JSONObject()
        try {
            if (database == null) {
                return jsonObject
            }
            val cursor =
                database!!.rawQuery("select * from " + SQLHelper.Companion.TABLE_DOWMLOAD + " where id='" + id + "'",
                    null)
            cursor.isFirst
            while (cursor.moveToNext()) {
                try {
                    jsonObject.put("id", cursor.getString(cursor.getColumnIndex("id")))
                    jsonObject.put("totallength",
                        cursor.getString(cursor.getColumnIndex("totallength")))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
            cursor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonObject
    }

    /**
     * 按照id删除下载记录
     *
     * @param id
     */
    fun delDownLoad(id: String?) {
        database!!.delete(SQLHelper.Companion.TABLE_DOWMLOAD, "id='$id'", null)
    }

    /**
     * 清理下载任务的记录
     *
     * @param id
     */
    fun clearDownLoadTab() {
        database!!.delete(SQLHelper.Companion.TABLE_DOWMLOAD, "", null)
    }

    /**
     * 在应用退出的时候销毁数据库连接
     */
    fun onDestroy() {
        if (database != null) {
            database!!.close()
            database = null
            sqlTools = null
        }
    }

    companion object {
        var sqlTools: SQLTools? = null

        /**
         * 获取单例
         *
         * @param context
         * @return
         */
        fun init(context: Context?): SQLTools? {
            if (sqlTools == null) {
                sqlTools = SQLTools(context)
            }
            return sqlTools
        }
    }
}