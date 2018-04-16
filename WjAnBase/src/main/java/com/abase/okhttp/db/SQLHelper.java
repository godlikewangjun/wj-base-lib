package com.abase.okhttp.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库操作
 * @author Admin
 * @version 1.0
 * @date 2017/7/3
 */
public class SQLHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "wjsavedb.db";// 数据库名称
    public static final int VERSION = 1;
    //上传记录表
    public static String TABLE_UPLOAD="upload";
    //下载记录表
    public static String TABLE_DOWMLOAD="dowmload";


    public SQLHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        /**
         * 创建上传记录表
         */
        db.execSQL( "create table if not exists "+TABLE_UPLOAD+" (" +
                "[id] VARCHAR PRIMARY KEY NOT NULL," +
                "[length] VARCHAR NOT NULL," +
                "[totallength] VARCHAR NOT NULL," +
                "[uploadid] VARCHAR NOT NULL," +
                "[uploadurl] VARCHAR NOT NULL)");
        /**
         * 创建下载记录表
         */
        db.execSQL( "create table if not exists "+TABLE_DOWMLOAD+" (" +
                "[id] VARCHAR PRIMARY KEY NOT NULL," +
                "[totallength] VARCHAR NOT NULL)");

    }
    /**创建表*/
    private void execCreatTable(SQLiteDatabase db,String tablename){
    }
    /**删除表*/
    private void execDealTable(SQLiteDatabase db,String tablename){
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
           //更改数据库版本的操作
            if (newVersion > oldVersion) {
                upDate(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**执行数据库更新*/
    private void upDate(SQLiteDatabase db){
    }
}
