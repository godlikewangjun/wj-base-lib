package com.wj.okhttp.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.lang.Exception
import android.database.sqlite.SQLiteOpenHelper

/**
 * 数据库操作
 * @author Admin
 * @version 1.0
 * @date 2017/7/3
 */
class SQLHelper(context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        /**
         * 创建上传记录表
         */
        db.execSQL("create table if not exists " + TABLE_UPLOAD + " (" +
                "[id] VARCHAR PRIMARY KEY NOT NULL," +
                "[length] VARCHAR NOT NULL," +
                "[totallength] VARCHAR NOT NULL," +
                "[uploadid] VARCHAR NOT NULL," +
                "[uploadurl] VARCHAR NOT NULL)")
        /**
         * 创建下载记录表
         */
        db.execSQL("create table if not exists " + TABLE_DOWMLOAD + " (" +
                "[id] VARCHAR PRIMARY KEY NOT NULL," +
                "[totallength] VARCHAR NOT NULL)")
    }

    /**创建表 */
    private fun execCreatTable(db: SQLiteDatabase, tablename: String) {}

    /**删除表 */
    private fun execDealTable(db: SQLiteDatabase, tablename: String) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            //更改数据库版本的操作
            if (newVersion > oldVersion) {
                upDate(db)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**执行数据库更新 */
    private fun upDate(db: SQLiteDatabase) {}

    companion object {
        const val DB_NAME = "wjsavedb.db" // 数据库名称
        const val VERSION = 1

        //上传记录表
        var TABLE_UPLOAD = "upload"

        //下载记录表
        var TABLE_DOWMLOAD = "dowmload"
    }
}