package com.zhou.android.kotlin

import android.annotation.TargetApi
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.IPackageDataObserver
import android.content.pm.IPackageStatsObserver
import android.content.pm.PackageManager
import android.content.pm.PackageStats
import android.os.*
import android.os.storage.StorageManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.util.Log
import com.zhou.android.R
import java.io.File
import java.io.IOException
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import kotlin.collections.HashMap


object StorageUtil {

    const val key_total = "total"
    const val key_used = "used"
    const val key_available = "available"
    const val key_system = "system"

    const val WITHOUT_PERMISSION = 0
    const val NO_SUCH_METHOD = 1
    const val OTHER_ERROR = 2

    fun queryMyPhone(context: Context): HashMap<String, Long> {

        val map = HashMap<String, Long>()
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE)
        val version = Build.VERSION.SDK_INT

        if (version > Build.VERSION_CODES.M) {

            val getVolumes = storageManager.javaClass.getDeclaredMethod("getVolumes")
            val getVolumeInfo = getVolumes.invoke(storageManager) as List<Object>
            var getType: Field? = null
            var used = 0L
            var total = 0L
            var systemSize = 0L
            for (obj in getVolumeInfo) {
                if (getType == null) {
                    getType = obj.`class`.getField("type")
                }
                val type = getType?.getInt(obj)
                when (type) {
                    1 -> {//TYPE_PRIVATE
                        var totalSize = 0L
                        if (version >= Build.VERSION_CODES.O) {//8.0
                            val getFsUuid = obj.javaClass.getDeclaredMethod("getFsUuid")
                            val fsUuid = getFsUuid.invoke(obj)
                            totalSize = getTotalSize(context, fsUuid)//8.0 以后使用
                        } else if (version >= Build.VERSION_CODES.N_MR1) {//7.1.1
                            val getPrimaryStorageSize = StorageManager::class.java.getMethod("getPrimaryStorageSize")//5.0 6.0 7.0没有
                            totalSize = getPrimaryStorageSize.invoke(storageManager) as Long
                        }

                        val isMountedReadable = obj.javaClass.getDeclaredMethod("isMountedReadable")
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File

                            if (totalSize == 0L) {
                                totalSize = f.totalSpace
                            }
                            systemSize = totalSize - f.totalSpace
                            used += totalSize - f.freeSpace
                            total += totalSize
                        }
                    }
                    0 -> {//TYPE_PUBLIC
                        val isMountedReadable = obj.javaClass.getDeclaredMethod("isMountedReadable")
                        val readable = isMountedReadable.invoke(obj) as Boolean
                        if (readable) {
                            val file = obj.javaClass.getDeclaredMethod("getPath")
                            val f = file.invoke(obj) as File
                            used += f.totalSpace - f.freeSpace
                            total += f.totalSpace
                        }
                    }
                }
            }

            with(map) {
                put(key_total, total)
                put(key_available, total - used)
                put(key_used, used)
                put(key_system, systemSize)
                map
            }

//            map.put(key_total, total)
//            map.put(key_available, total - used)
//            map.put(key_used, used)
//            map.put(key_system, systemSize)

        } else {
            val statFs = StatFs(Environment.getExternalStorageDirectory().path)
            val size = statFs.blockSizeLong

            with(map) {
                put(key_total, size * statFs.blockCountLong)
                put(key_used, size * (statFs.blockCountLong - statFs.availableBlocksLong))
                put(key_available, size * statFs.availableBlocksLong)
                put(key_system, -1)
                map
            }

//            map.put(key_total, (size * statFs.blockCount).toLong())
//            map.put(key_used, (size * (statFs.blockCount - statFs.availableBlocks)).toLong())
//            map.put(key_available, (size * statFs.availableBlocks).toLong())
//            map.put(key_system, -1)
        }

        return map
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getTotalSize(context: Context, fsUuid: Any?): Long {
        try {
            val id: UUID = fsUuid?.run { UUID.fromString(fsUuid as String) }
                    ?: StorageManager.UUID_DEFAULT
            val stats = context.getSystemService(StorageStatsManager::class.java)
            return stats?.getTotalBytes(id) ?: -1
        } catch (e: NoSuchFieldError) {
            e.printStackTrace()
            return -1
        } catch (e: NoClassDefFoundError) {
            e.printStackTrace()
            return -1
        } catch (e: NullPointerException) {
            e.printStackTrace()
            return -1
        } catch (e: IOException) {
            e.printStackTrace()
            return -1
        }
    }

    private val units = arrayOf("B", "KB", "MB", "GB", "TB")

    /**
     * 进制转换
     */
    fun getUnit(_size: Long?): String {

        var base = 1024
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            base = 1000
        }

        var size = _size?.toFloat() ?: 0F
        var index = 0
        while (size > base && index < 4) {
            size = size.div(base)
            index++
        }
        return String.format(Locale.getDefault(), " %.2f %s ", size, units[index])
    }

    fun queryMyApps(context: Context, callback: Callback) {
        Thread(({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                queryAppsAfter26(context, callback)
            } else {
                queryAppsBefore26(context, callback)
            }
        })).start()
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun queryAppsAfter26(context: Context, callback: Callback) {

        val result = mutableListOf<AppDetail>()

        val statsManger: StorageStatsManager = context.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager
        val userHandler = UserHandle.getUserHandleForUid(-2)
        val pm = context.packageManager
        val list = pm.getInstalledPackages(0)

        val default = context.resources.getDrawable(R.mipmap.ic_launcher)

        var hasPermission = true
        var error = -1

        var item: AppDetail?
        for (info in list) {
            val packageName = info.packageName
            val icon = pm.getApplicationIcon(packageName) ?: default

            var code = 0L
            var data = 0L
            var cache = 0L

            if (!hasPermission && context.packageName != packageName) {
                item = AppDetail(packageName, code, data, cache, icon)
                result.add(item)
                continue
            }

            try {
                val storageStats = statsManger.queryStatsForPackage(StorageManager.UUID_DEFAULT, packageName, userHandler)
                storageStats?.apply {
                    code = storageStats.appBytes
                    data = storageStats.dataBytes
                    cache = storageStats.cacheBytes
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
                hasPermission = false
                error = WITHOUT_PERMISSION
            }

            item = AppDetail(packageName, code, data, cache, icon)
            result.add(item)
        }

        val e = error
        Handler(Looper.getMainLooper()).post {
            if (e == -1) {
                callback.onSuccess(result)
            } else {
                callback.onError(result, e, "without permission")
            }
        }
    }

    private fun queryAppsBefore26(context: Context, callback: Callback) {

        val map = HashMap<String, AppDetail>()
        var observer: StorageUtil.StatsObserver? = null

        val pm = context.packageManager
        val list = pm.getInstalledPackages(0)
        var count = list.size
        val default = context.resources.getDrawable(R.mipmap.ic_launcher)
        var error = -1
        var method: Method? = null
        var reason: String? = null
        try {
            method = PackageManager::class.java.getMethod("getPackageSizeInfo", String::class.java, IPackageStatsObserver::class.java)
        } catch (e: NoSuchMethodException) {
            error = NO_SUCH_METHOD
            reason = "NoSuchMethodException"
        }

        for (info in list) {
            val packageName = info.packageName
            val icon = pm.getApplicationIcon(packageName) ?: default

            if (observer == null) {
                observer = StorageUtil.StatsObserver(map, count, callback)
            }

            method?.invoke(pm, packageName, observer) ?: run {
                error = OTHER_ERROR
                reason = "other error"
            }

            val app = AppDetail(packageName, 0, 0, 0, icon)
            map.apply {
                put(packageName, app)
            }
        }

        if (error != -1) {
            Handler(Looper.getMainLooper()).post {
                val data = mutableListOf<AppDetail>()
                data.addAll(map.values)
                callback.onError(data, error, reason ?: "error")
            }
        }


    }

    fun requestPermission(context: Context) {
        val msg = "缺少权限：permission.PACKAGE_USAGE_STATS\n需要在\"设置>安全\"中给应用提供权限"
        AlertDialog.Builder(context)
                .setTitle("警告")
                .setMessage(msg)
                .setPositiveButton("设置", DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    context.startActivity(intent)
                    dialog.dismiss()
                }).setNegativeButton("拒绝", ({ dialog, which ->
                    dialog.dismiss()
                })).create()
                .show()
    }

    interface Callback {
        fun onSuccess(data: List<AppDetail>)
        fun onError(data: List<AppDetail>, error: Int, reason: String)
    }

    //has android.permission.INTERNAL_DELETE_CACHE_FILES.
    //不可用，权限限制
    fun cleanAppCache(context: Context, apps: Array<String>, callback: CleanCallback) {
        Log.d("clean", "clean package ${apps.reduce { acc, s -> "$acc $s" }}")
        Thread {
            val pm: PackageManager = context.packageManager
            val handler = Handler(Looper.getMainLooper())
            for (app in apps) {
                try {
                    val method = pm::class.java.getMethod("deleteApplicationCacheFiles", String::class.java, IPackageDataObserver::class.java)
                    method.invoke(pm, app, DataObserver(handler, callback))
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                    handler.post { callback.callback(app, false) }
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                    handler.post { callback.callback(app, false) }
                }
            }
        }.start()
    }

    interface CleanCallback {
        fun callback(packageName: String, bool: Boolean)
    }

    private class StatsObserver(val map: HashMap<String, AppDetail>, val size: Int, val callback: Callback) : IPackageStatsObserver.Stub() {

        val data = mutableListOf<AppDetail>()
        var count = 0

        override fun onGetStatsCompleted(pStats: PackageStats?, succeeded: Boolean) {
            pStats?.also {
                val app = map[it.packageName]
                app?.apply {
                    code = it.codeSize
                    data = it.dataSize
                    cache = it.cacheSize
                    this@StatsObserver.data.add(app)
                }
            }
            count++
            if (count == size) {
                Handler(Looper.getMainLooper()).post(({
                    callback.onSuccess(data)
                }))
            }
        }
    }

    private class DataObserver(val handler: Handler, val callback: CleanCallback) : IPackageDataObserver.Stub() {
        override fun onRemoveCompleted(packageName: String?, successed: Boolean) {
            handler.post {
                callback.callback(packageName ?: "", successed)
            }
        }
    }
}