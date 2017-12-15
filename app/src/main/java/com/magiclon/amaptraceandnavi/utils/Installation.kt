package com.magiclon.amaptraceandnavi.utils

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.RandomAccessFile
import java.util.*


/**
 * Created by MagicLon on 2017/7/18.设备唯一码，不推荐使用deviceId，有的设备不会返回
 */
object Installation {
    private var sID: String? = null
    private val INSTALLATION = "INSTALLATION"

    @Synchronized fun getId(context: Context): String {
        if (sID == null) {
            val installation = File(context.filesDir, INSTALLATION)
            try {
                if (!installation.exists())
                    writeInstallationFile(installation)
                sID = readInstallationFile(installation)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }

        }
        return sID as String
    }

    @Throws(IOException::class)
    private fun readInstallationFile(installation: File): String {
        val f = RandomAccessFile(installation, "r")
        val bytes = ByteArray(f.length().toInt())
        f.readFully(bytes)
        f.close()
        return String(bytes)
    }

    @Throws(IOException::class)
    private fun writeInstallationFile(installation: File) {
        val out = FileOutputStream(installation)
        val id = UUID.randomUUID().toString()
        out.write(id.toByteArray())
        out.close()
    }

     fun getMyUUID(context: Activity): String {

        val tm = context.baseContext.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

        val tmDevice: String
        val tmSerial: String
        val tmPhone: String
        val androidId: String

        tmDevice = "" + tm.deviceId

        tmSerial = "" + tm.simSerialNumber

        androidId = "" + android.provider.Settings.Secure.getString(context.contentResolver, android.provider.Settings.Secure.ANDROID_ID)

        val deviceUuid = UUID(androidId.hashCode().toLong(), tmDevice.hashCode().toLong() shl 32 or tmSerial.hashCode().toLong())

        val uniqueId = deviceUuid.toString()

        Log.e("ard", "uuid=" + uniqueId)

        return uniqueId

    }

    /**
     * 检查手机上是否安装了指定的软件
     * @param context
     * *
     * @param packageName：应用包名
     * *com.autonavi.minimap
     * com.baidu.BaiduMap
     * @return
     */
     fun isAvilible(context: Context, packageName: String): Boolean {
        //获取packagemanager
        val packageManager = context.packageManager
        //获取所有已安装程序的包信息
        val packageInfos = packageManager.getInstalledPackages(0)
        //用于存储所有已安装程序的包名
        val packageNames = ArrayList<String>()
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (i in packageInfos.indices) {
                val packName = packageInfos[i].packageName
                packageNames.add(packName)
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName)
    }

}