package com.magiclon.amaptraceandnavi.db;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;

/**
 * 作者：MagicLon
 * 时间：2017/12/14 014
 * 邮箱：1348149485@qq.com
 * 描述：
 */

public class TraceAndNaviBean {
    String uid;
    String type;
    double lat;
    double lon;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public TraceAndNaviBean(String uid, String type, double lat, double lon) {
        this.uid = uid;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
    }

    public TraceAndNaviBean(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString() {
        return "TraceAndNaviBean{" +
                "uid='" + uid + '\'' +
                ", type='" + type + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
