package com.magiclon.amaptraceandnavi

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.amap.api.maps.AMap
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.Poi
import com.amap.api.navi.AmapNaviPage
import com.amap.api.navi.AmapNaviParams
import com.amap.api.navi.AmapNaviType
import com.amap.api.navi.INaviInfoCallback
import com.amap.api.navi.model.AMapNaviLocation
import com.amap.api.trace.TraceOverlay
import com.magiclon.amaptraceandnavi.db.MyDb
import com.magiclon.amaptraceandnavi.offlinemap.OfflineMapActivity
import com.magiclon.amaptraceandnavi.utils.AmapTTSController
import com.magiclon.amaptraceandnavi.utils.Installation
import com.magiclon.individuationtoast.ToastUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_navitype.view.*
import pub.devrel.easypermissions.EasyPermissions
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.lang.Exception


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, AMap.OnMyLocationChangeListener, INaviInfoCallback {
    var perms = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION)
    private var aMap: AMap? = null
    private var myLocationStyle: MyLocationStyle? = null
    private var traceOverlay: TraceOverlay? = null
    private var lat = 0.0
    private var lng = 0.0
    private val REQUESTCODE_CALLAPPNAVI = 1
    var mdb: MyDb? = null
    var isAppNaving = false
    var isNativeNaving = false
    var isStop=true
    var uid = "asdqwe123"
    var type = ""
    var subscription: Subscription? = null
    var mProgress: ProgressDialog? = null
    var isShowDistanse = false
    private var amapTTSController: AmapTTSController? = null
    private var p2 = LatLng(40.062, 113.31 )//故宫博物院
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mapView.onCreate(savedInstanceState)// 此方法必须重写
        mdb = MyDb(this)
        if (!EasyPermissions.hasPermissions(this, *perms)) {
            EasyPermissions.requestPermissions(this, "申请权限",
                    1, *perms)
        } else {
            initMap()
        }
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        amapTTSController?.destroy()
    }

    /**
     * 初始化地图
     */
    fun initMap() {
        aMap = mapView.map
        myLocationStyle = MyLocationStyle()
        myLocationStyle?.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
        myLocationStyle?.interval(10000)
        aMap?.myLocationStyle = myLocationStyle
        aMap?.uiSettings?.isMyLocationButtonEnabled = true// 设置默认定位按钮是否显示
        aMap?.setOnMyLocationChangeListener(this)
        tv_offlinedown.setOnClickListener { startActivity(Intent(this@MainActivity, OfflineMapActivity::class.java)) }
        tv_navi.setOnClickListener {
            if(csl_notify.isShown){
                traceOverlay?.remove()
                isShowDistanse = false
                csl_notify.visibility=View.GONE
            }
            selectNaviType()
        }
        aMap?.moveCamera(CameraUpdateFactory.zoomTo(19f))
        aMap?.isMyLocationEnabled = true// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false

        btn_iknow.setOnClickListener {
            traceOverlay?.remove()
            isShowDistanse = false
            csl_notify.visibility = View.GONE
        }
        amapTTSController = AmapTTSController.getInstance(applicationContext)
        amapTTSController?.init()
    }

    /**
     * 去导航
     */
    private fun callAppNavi() {
        if (Installation.isAvilible(this, "com.autonavi.minimap")) {
            try {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                val uri = Uri.parse("amapuri://route/plan/?sourceApplication=amap&dlat=${p2.latitude}&dlon=${p2.longitude}&dev=0&t=0")
                intent.data = uri
                startActivityForResult(intent, REQUESTCODE_CALLAPPNAVI)
                isAppNaving = true
                type = "${System.currentTimeMillis()}"
            } catch (e: Exception) {
                ToastUtil.showerror(this, "请安装最新版高德地图")
            }
        } else {
            ToastUtil.showerror(this, "请安装最新版高德地图")
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, "请确认所有权限", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (perms.size == 5) {
            initMap()
        }
    }

    /**
     * 定位回调监听
     */
    override fun onMyLocationChange(location: Location?) {
        if (location != null) {
//            Log.e("location", "${location.latitude}**${location.longitude}**$type")
            if (location.latitude != 0.0 && location.longitude != 0.0) {
                lat = location.latitude
                lng = location.longitude
                var latlng = LatLng(lat, lng)
                if (!isShowDistanse) {
                    aMap?.moveCamera(CameraUpdateFactory.newLatLng(latlng))
                }
                if (isAppNaving) {
                    mdb?.insertLatLon(uid, type, "$lat", "$lng")
                }
            }
        } else {
//            Log.e("amap", "定位失败")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUESTCODE_CALLAPPNAVI) {
            isShowDistanse = true
            isAppNaving = false
            showDialog("计算距离中...")
            subscription = Observable.create(Observable.OnSubscribe<Float> { action ->
                var latlngs = mdb?.getSomeonsLatlngs(uid, type)
                traceOverlay = TraceOverlay(aMap)
                traceOverlay?.add(latlngs)
                traceOverlay?.setProperCamera(latlngs)
                var distanse = sum(latlngs!!)
                action.onNext(distanse)
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { action ->
                        showNotify(action)
                        mProgress?.dismiss()
                        subscription?.unsubscribe()
                    }
        }
    }

    /**
     * 距离信息提示
     */
    private fun showNotify(distanse: Float) {
        csl_notify.visibility = View.VISIBLE
        tv_distanse.text = "您刚才走了${distanse}米"
    }

    //导航方式选择
    private fun selectNaviType() {
        var bottomdialog: BottomSheetDialog? = null
        if (bottomdialog == null) {
            bottomdialog = BottomSheetDialog(this)
            var view = LayoutInflater.from(this).inflate(R.layout.layout_navitype, null)
            bottomdialog.setContentView(view)
            var appnavi = view.tv_amapapp
            appnavi.setOnClickListener {
                bottomdialog.dismiss()
                callAppNavi()
            }
            var nativenavi = view.tv_nativenavi
            nativenavi.setOnClickListener {
                bottomdialog.dismiss()
                AmapNaviPage.getInstance().showRouteActivity(applicationContext, AmapNaviParams(null, null, Poi("", p2, ""), AmapNaviType.DRIVER), this@MainActivity)
            }
        }
        bottomdialog.show()
    }

    /**
     * 显示进度条
     */
    fun showDialog(msg: String) {
        if (null == mProgress) {
            mProgress = ProgressDialog(this)
            mProgress?.setCanceledOnTouchOutside(false)
        }
        mProgress?.setMessage(msg)
        mProgress?.show()
    }

    /**
     * 距离求和
     */
    fun sum(latlngs: List<LatLng>): Float {
        var sum = 0.0f
        if (latlngs.size > 1) {
            latlngs.forEachIndexed { index, latLng ->
                if (index != latlngs.size - 1) {
                    sum += AMapUtils.calculateLineDistance(latLng, latlngs[index + 1])
                }
            }
        }
        return sum
    }

    /**
     * 导航集成回调
     */
    override fun onGetNavigationText(p0: String?) {
        amapTTSController?.onGetNavigationText(p0)
    }

    /**
     * 导航集成回调
     */
    override fun onLocationChange(p0: AMapNaviLocation?) {
//        Log.e("-----","${p0?.coord?.latitude}---${p0?.coord?.longitude}")
        if(isNativeNaving){
            mdb?.insertLatLon(uid, type, "${p0?.coord?.latitude}", "${p0?.coord?.longitude}")
        }

    }

    /**
     * 导航集成回调
     */
    override fun onCalculateRouteSuccess(p0: IntArray?) {
//        Log.e("****","onCalculateRouteSuccess")
    }

    /**
     * 导航集成回调
     */
    override fun onInitNaviFailure() {
//        Log.e("****","onInitNaviFailure")
    }

    /**
     * 导航集成回调
     */
    override fun onArriveDestination(p0: Boolean) {

    }

    /**
     * 导航集成回调
     */
    override fun onStartNavi(p0: Int) {
        type = "${System.currentTimeMillis()}"
        isNativeNaving=true
//        Log.e("****","onStartNavi")
    }

    /**
     * 导航集成回调
     */
    override fun onStopSpeaking() {
//        Log.e("---","onStopSpeaking")
        amapTTSController?.stopSpeaking()
        isStop=!isStop
        if(isStop){
            isNativeNaving=false
            isShowDistanse = true
            showDialog("计算距离中...")
            subscription = Observable.create(Observable.OnSubscribe<Float> { action ->
                var latlngs = mdb?.getSomeonsLatlngs(uid, type)
                traceOverlay = TraceOverlay(aMap)
                traceOverlay?.add(latlngs)
                traceOverlay?.setProperCamera(latlngs)
                var distanse = sum(latlngs!!)
                action.onNext(distanse)
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { action ->
                        showNotify(action)
                        mProgress?.dismiss()
                        subscription?.unsubscribe()
                    }
        }

    }

    /**
     * 导航集成回调
     */
    override fun onCalculateRouteFailure(p0: Int) {
//        Log.e("****","onCalculateRouteFailure")
    }


    /**
     * 导航页面白屏的问题
     */
    override fun getResources(): Resources {
        return baseContext.resources
    }

}
