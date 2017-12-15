package com.magiclon.amaptraceandnavi.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者：MagicLon
 * 时间：2017/11/28 028
 * 邮箱：1348149485@qq.com
 * 描述：
 */

public class MyDb {
    private MySqliteOpenHelper mysqliteopenhelper;

    public MyDb(Context context) {
        if (mysqliteopenhelper == null) {
            mysqliteopenhelper = new MySqliteOpenHelper(context);
        }
    }

    public void insertLatLon(String uid, String type, String lat, String lon) {
        SQLiteDatabase db = mysqliteopenhelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("insert into traceandnavi values (?,?,?,?);".toString(), new String[]{uid, type, lat, lon});
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
            db.close();
        }
    }


    public List<LatLng> getSomeonsLatlngs(String uid) {
        SQLiteDatabase db = mysqliteopenhelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("select lat,lon from traceandnavi where uid='" + uid+"'", null);
        List<LatLng> latLngs = new ArrayList<>();
        while (cursor.moveToNext()) {
            double lat = cursor.getDouble(0);
            double lon = cursor.getDouble(1);
            LatLng latlng = new LatLng(lat, lon);
            latLngs.add(latlng);
        }
        cursor.close();
        db.endTransaction();
        db.close();
        return latLngs;
    }
    public List<LatLng> getSomeonsLatlngs(String uid,String type) {
        SQLiteDatabase db = mysqliteopenhelper.getReadableDatabase();
        db.beginTransaction();
        Cursor cursor = db.rawQuery("select lat,lon from traceandnavi where uid='" + uid +"' and type='"+type+"'", null);
        List<LatLng> latLngs = new ArrayList<>();
        while (cursor.moveToNext()) {
            double lat = cursor.getDouble(0);
            double lon = cursor.getDouble(1);
//            Log.e("***",lat+"*"+lon);
            LatLng latlng = new LatLng(lat, lon);
            latLngs.add(latlng);
        }
        db.setTransactionSuccessful();
        cursor.close();
        db.endTransaction();
        db.close();
        return latLngs;
    }
}
