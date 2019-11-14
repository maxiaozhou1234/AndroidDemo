package com.zhou.android.h264

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

/**
 *
 * android 硬编码
 *
 * 软编码 x264 编码器 https://github.com/sszhangpengfei/android_x264_encoder.git
 *
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
class AvcEncoder(private val width: Int, private val height: Int, private val frameRate: Int) {

    companion object {
        const val TAG = "AvcEncoder"
        const val TIMEOUT_US = 1000//12000
        const val CACHE_SIZE = 1024
    }

    private var mediaCodec: MediaCodec? = null

    private var configByte: ByteArray? = null
    private val bufferInfo = MediaCodec.BufferInfo()
    private var thread: Thread? = null

    private val cache = ArrayBlockingQueue<ByteArray>(CACHE_SIZE)
    var frameListener: ((data: ByteArray, isFrame: Boolean) -> Unit)? = null

    init {

        val mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height)

        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar)// yuv420sp  yyyy uv uv
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5)
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        try {
            mediaCodec = MediaCodec.createEncoderByType("video/avc")
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mediaCodec?.apply {
            configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            start()
        }

    }

    fun releaseEncoder() {
        clearCache()
        try {
            mediaCodec = mediaCodec?.run {
                stop()
                release()
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun clearCache() {
        cache.clear()
    }

    fun startEncoderThread() {

        thread = Thread(Runnable {
            var input: ByteArray
            var pts: Long
            var generateIndex: Long = 0
            val size = width * height * 3 / 2
//            val yuv420sp = ByteArray(size)

            val buf = ByteBuffer.allocate(size)
            var time: Long

            while (true) {//isRunning
                try {
                    input = cache.take()
                    time = System.currentTimeMillis()

//                    val yuv420sp = ByteArray(size)
//                    NV21ToNV12(input, yuv420sp, width, height)//不调用颜色值不对
//                    input = yuv420sp

//                    YV12ToNV12_2(input, buf, width, height)//不调用颜色值不对
//                    buf.flip()
//                    input = buf.array()

                    val inputBuffers = mediaCodec!!.inputBuffers
                    val outputBuffers = mediaCodec!!.outputBuffers
                    val inputBufferIndex = mediaCodec!!.dequeueInputBuffer(-1)
                    if (inputBufferIndex >= 0) {
                        pts = computePresentationTime(generateIndex)
                        val inputBuffer = inputBuffers[inputBufferIndex]
                        inputBuffer.clear()
                        inputBuffer.put(input)
                        mediaCodec!!.queueInputBuffer(inputBufferIndex, 0, input.size, pts, 0)
                        generateIndex += 1
                    }

                    var outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, TIMEOUT_US.toLong())
                    while (outputBufferIndex >= 0) {

                        val outputBuffer = outputBuffers[outputBufferIndex]
                        val outData = ByteArray(bufferInfo.size)
                        outputBuffer.get(outData)
                        if (bufferInfo.flags == 2) {
                            configByte = ByteArray(bufferInfo.size)
                            configByte = outData
                            Log.d("video", "configByte >> ${Arrays.toString(outData)}")
                        } else if (bufferInfo.flags == 1) {
                            val keyframe = ByteArray(bufferInfo.size + configByte!!.size)
                            System.arraycopy(configByte!!, 0, keyframe, 0, configByte!!.size)
                            System.arraycopy(outData, 0, keyframe, configByte!!.size, outData.size)

                            Log.d("video", "IFrame >> ${Arrays.toString(outData)}")
                            frameListener?.invoke(keyframe, true)

                        } else {
                            Log.d("video", "normal >> ${Arrays.toString(outData)}")
                            frameListener?.invoke(outData, false)
                        }
                        mediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
                        outputBufferIndex = mediaCodec!!.dequeueOutputBuffer(bufferInfo, TIMEOUT_US.toLong())
                    }
                    Log.i("video", "time >> ${System.currentTimeMillis() - time}")
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        })
        thread?.start()
    }

    fun addFrame(data: ByteArray) {
        cache.offer(data)
        Log.i("video", "addFrame ---------------------->")
    }

    fun release() {
        clearCache()
        if (thread?.isInterrupted == true) {
            thread = thread!!.run {
                interrupt()
                join(800)
                null
            }
        }
    }

    private fun NV21ToNV12(nv21: ByteArray?, nv12: ByteArray?, width: Int, height: Int) {
        if (nv21 == null || nv12 == null) return
        val frameSize = width * height
        System.arraycopy(nv21, 0, nv12, 0, frameSize)
        var i = 0
        while (i < frameSize / 2) {
            nv12[frameSize + i - 1] = nv21[i + frameSize]
            nv12[frameSize + i] = nv21[i + frameSize - 1]
            i += 2
        }
    }

    private fun YV12ToNV12(yv12: ByteArray?, nv12: ByteArray?, width: Int, height: Int) {
        if (yv12 == null || nv12 == null) return
        val frameSize = width * height
        System.arraycopy(yv12, 0, nv12, 0, frameSize)
        val half = frameSize / 4
        var i = 0
        var j = 0
        while (i < half) {
            nv12[frameSize + j] = yv12[frameSize + half + i]//u
            nv12[frameSize + j + 1] = yv12[frameSize + i]//v
            i++
            j += 2
        }
    }

    private fun YV12ToNV12_2(yv12: ByteArray?, nv12: ByteBuffer, width: Int, height: Int) {
        if (yv12 == null) return
        nv12.clear()
        val frameSize = width * height
//        System.arraycopy(yv12, 0, nv12, 0, frameSize)
        nv12.put(yv12, 0, frameSize)
        val half = frameSize / 4
        var i = 0
        var j = 0
        while (i < half) {
            nv12.put(frameSize + j, yv12[frameSize + half + i])//u
            nv12.put(frameSize + j + 1, yv12[frameSize + i])//v
            i++
            j += 2
        }
    }

    // Generates the presentation time for frame N, in microseconds.
    private fun computePresentationTime(frameIndex: Long): Long {
        return 132 + frameIndex * 1000000 / frameRate
    }

}
