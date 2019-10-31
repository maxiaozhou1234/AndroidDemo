package com.zhou.android.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.ImageReader
import android.os.*
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions
import com.zhou.android.R
import com.zhou.android.camera2.CameraUtil
import com.zhou.android.camera2.OnImageAvailableListener
import com.zhou.android.common.Utils
import com.zhou.android.common.addToComposite
import com.zhou.android.common.toast
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_camera_h264.*
import okhttp3.internal.Util
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by mxz on 2019/10/31.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CameraH264Activity : AppCompatActivity() {

    private val composite = CompositeDisposable()
    private var cameraUtil: CameraUtil? = null

    private var hasCameraPermission = false

    private val path = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path}/android/"
    @Volatile
    private var takePic = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_h264)
        init()
        addListener()
    }

    private fun init() {

        if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
            try {
                if (cameraUtil == null) {
                    cameraUtil = CameraUtil(this, null, texture).apply {
                        setDefaultCamera(true)
                        setPreviewImageFormat(ImageFormat.YUV_420_888)
                        setOnImageAvailableListener(onImageAvailableListener)
                    }
                }

            } catch (e: IllegalArgumentException) {
                toast("不支持的编码格式")
            }
        }

    }

    private fun addListener() {
        ibAction.setOnClickListener {
            val state = !ibAction.isSelected
            if (state) {
            } else {
            }

            ibAction.isSelected = state
        }

        ibVideo.setOnClickListener {
            if (PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                takePic = true
            } else {
                val context = this@CameraH264Activity
                val rxPermissions = RxPermissions(context)
                rxPermissions.shouldShowRequestPermissionRationale(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .flatMap { b ->
                            if (b) {
                                rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            } else {
                                Observable.just(false)
                            }
                        }.subscribe { result ->
                            if (!result) {
                                Utils.showPermissionRequestAlertDialog(context, "存储")
                            }
                        }.addToComposite(composite)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        val rxPermissions = RxPermissions(this)
        hasCameraPermission = rxPermissions.isGranted(Manifest.permission.CAMERA)
        if (!hasCameraPermission) {
            rxPermissions.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                    .flatMap { r ->
                        if (r) {
                            rxPermissions.request(Manifest.permission.CAMERA)
                        } else {
                            Observable.just(false)
                        }
                    }.subscribe { result ->
                        if (!result) {
                            Utils.showPermissionRequestAlertDialog(this@CameraH264Activity, "相机")
                        } else {
                            if (cameraUtil == null) {
                                cameraUtil = CameraUtil(this, null, texture).apply {
                                    setDefaultCamera(true)
                                    setPreviewImageFormat(ImageFormat.YV12)
                                    setOnImageAvailableListener(onImageAvailableListener)
                                }
                            }
                        }
                    }.addToComposite(composite)
        }

        cameraUtil?.startPreview(this)
    }

    override fun onPause() {
        cameraUtil?.stopPreview()
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        composite.dispose()
    }

    private val sb = StringBuilder()
    private val smallBuf = ByteArray(10)
    private val onImageAvailableListener = object : OnImageAvailableListener() {

        override fun onImageAvailable(reader: ImageReader) {

            val image = reader.acquireLatestImage()

            if (takePic) {
                //这里是 YUV420SemiPlanar yyyyuvuv 通道二三数据一致，只是二通道是 uv 间隔，三通道是 vu 间隔
                sb.setLength(0)
                sb.append("width:${image.width}, height:${image.height} ,format:${image.format} \n")
                val data = ByteArray(image.width * image.height * 3 / 2)
                var count = 0
                for (i in 0 until image.planes.size - 1) {
                    val plane = image.planes[i]

                    val buf = plane.buffer
                    val remaining = buf.remaining()
                    val b = ByteArray(remaining)
                    buf.get(b)

                    System.arraycopy(b, 0, data, count, b.size)

                    count += remaining - 1

//                    System.arraycopy(b, 0, smallBuf, 0, 10)
//                    sb.append("plane[$i], size:$remaining ,stride:${plane.pixelStride} ,10 byte >> ")
//                            .append(Arrays.toString(smallBuf)).append("\n")
                }
                Log.d("zhou1234", sb.append("=========================\n").toString())
                val msg = handler.obtainMessage()
                val bundle = Bundle().apply {
                    putInt("width", image.width)
                    putInt("height", image.height)
                    putByteArray("data", data)
                }
                msg.data = bundle
                msg.sendToTarget()

                takePic = false
            }
            image.close()
        }
    }

    private fun createBitmap(yuv: ByteArray, width: Int, height: Int) {

        Single.fromCallable {
            val image = YuvImage(yuv, ImageFormat.NV21, width, height, null)
            val bos = ByteArrayOutputStream()
            image.compressToJpeg(Rect(0, 0, width, height), 100, bos)
            val array = bos.toByteArray()
            val bitmap = BitmapFactory.decodeByteArray(array, 0, array.size)
            var fos: FileOutputStream? = null
            try {
                val file = File("$path${System.currentTimeMillis()}.jpg")
                if (!file.parentFile.exists()) {
                    file.parentFile.mkdirs()
                }
                file.createNewFile()
                fos = FileOutputStream(file)
                fos.write(array, 0, array.size)
                fos.flush()
                fos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                Util.closeQuietly(fos)
            }
            bos.close()
            bitmap
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { bitmap ->
                    image.setImageBitmap(bitmap)
                }.addToComposite(composite)

    }

    private val handler = @SuppressLint("HandlerLeak")
    object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            val bundle = msg.data
            val buf = bundle.getByteArray("data")
            val width = bundle.getInt("width")
            val height = bundle.getInt("height")
            createBitmap(buf, width, height)
        }
    }
}