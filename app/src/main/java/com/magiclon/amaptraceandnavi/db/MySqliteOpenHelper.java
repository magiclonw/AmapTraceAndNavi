package com.magiclon.amaptraceandnavi.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 作者：MagicLon
 * 时间：2017/12/14 028
 * 邮箱：1348149485@qq.com
 * 描述：
 */

public class MySqliteOpenHelper extends SQLiteOpenHelper {
    private String tablename="traceandnavi";
    private static final String DATABASE_NAME="traceandnavi.db";
    private static final int DATABASE_VERSION = 1;
    public MySqliteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION,null);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+tablename+" (uid text,type text,lat text,lon text);");
//        sqLiteDatabase.execSQL("create table "+tablenamestr+" (type text,typename text,lat text,lon text);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
