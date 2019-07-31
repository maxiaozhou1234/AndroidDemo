package com.zhou.android.kotlin

import android.os.Handler
import android.os.SystemClock
import android.util.Log
import com.zhou.android.R
import com.zhou.android.common.BaseActivity
import kotlinx.android.synthetic.main.activity_ntp_time.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.and

/**
 * android.net.SntpClient 获取网络时间的部分代码<Br/>
 * 经测试，系统同步时间很慢，不是网络不通问题，在时间迟迟不更新调用这部分代码是可以获取到时间
 *
 * 部分源码地址<Br/>
 *
 * @see <a href="https://www.androidos.net.cn/android/6.0.1_r16/xref/frameworks/base/services/core/java/com/android/server/NetworkTimeUpdateService.java">NetworkTimeUpdateService</a>
 * @see <a href="https://www.androidos.net.cn/android/6.0.1_r16/xref/frameworks/base/core/java/android/util/NtpTrustedTime.java">NtpTrustedTime</a>
 * 所以应该是恰好触发 NetworkTimeUpdateService 判断逻辑bug，导致更新时间事件一直被往后延
 *
 * Created by mxz on 2019/7/31.
 */
class NtpTimeActivity : BaseActivity() {

    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.getDefault())

    private val handler = Handler {
        text.append("${it.obj}\n")
        true
    }

    override fun setContentView() {
        setContentView(R.layout.activity_ntp_time)
    }

    override fun init() {
    }

    override fun addListener() {
        btnRequest.setOnClickListener {
            text.append("start request \n")
            Thread(Runnable { requestTime() }).start()
        }
    }

    private val REFERENCE_TIME_OFFSET = 16
    private val ORIGINATE_TIME_OFFSET = 24
    private val RECEIVE_TIME_OFFSET = 32
    private val TRANSMIT_TIME_OFFSET = 40
    private val NTP_PACKET_SIZE = 48

    private val NTP_PORT = 123
    private val NTP_MODE_CLIENT = 3
    private val NTP_MODE_SERVER = 4
    private val NTP_MODE_BROADCAST = 5
    private val NTP_VERSION = 3

    private val NTP_LEAP_NOSYNC = 3
    private val NTP_STRATUM_DEATH = 0
    private val NTP_STRATUM_MAX = 15
    private val OFFSET_1900_TO_1970 = (365L * 70L + 17L) * 24L * 60L * 60L

    // system time computed from NTP server response
    private var mNtpTime: Long = 0

    // value of SystemClock.elapsedRealtime() corresponding to mNtpTime
    private var mNtpTimeReference: Long = 0

    // round trip time in milliseconds
    private var mRoundTripTime: Long = 0

    private val DBG = true
    private val TAG = "zhou"

    private fun requestTime() {
        var address: InetAddress? = null
        try {
            address = InetAddress.getByName("time.gionee.com")
        } catch (e: Exception) {
            Log.d(TAG, "request time failed: $e")
            sendMsg("request time failed: $e")
            return
        }

        requestTime(address, 123, 5000)
    }

    private fun requestTime(address: InetAddress, port: Int, timeout: Int): Boolean {
        Log.d(TAG, "requestTime running")
        var socket: DatagramSocket? = null
//        val oldTag = TrafficStats.getAndSetThreadStatsTag(TrafficStats.TAG_SYSTEM_NTP)
        try {
            socket = DatagramSocket()
            socket.soTimeout = timeout
            val buffer = ByteArray(NTP_PACKET_SIZE)
            val request = DatagramPacket(buffer, buffer.size, address, port)

            // set mode = 3 (client) and version = 3
            // mode is in low 3 bits of first byte
            // version is in bits 3-5 of first byte
            buffer[0] = (NTP_MODE_CLIENT or (NTP_VERSION shl 3)).toByte()

            // get current time and write it to the request packet
            val requestTime = System.currentTimeMillis()
            val requestTicks = SystemClock.elapsedRealtime()
            writeTimeStamp(buffer, TRANSMIT_TIME_OFFSET, requestTime)

            socket.send(request)

            // read the response
            val response = DatagramPacket(buffer, buffer.size)
            socket.receive(response)
            val responseTicks = SystemClock.elapsedRealtime()
            val responseTime = requestTime + (responseTicks - requestTicks)

            // extract the results
            val leap = (buffer[0].toInt() shr 6 and 0x3).toByte()
            val mode = (buffer[0] and 0x7)
            val stratum = (buffer[1].toInt() and 0xff)
            val originateTime = readTimeStamp(buffer, ORIGINATE_TIME_OFFSET)
            val receiveTime = readTimeStamp(buffer, RECEIVE_TIME_OFFSET)
            val transmitTime = readTimeStamp(buffer, TRANSMIT_TIME_OFFSET)

            /* do sanity check according to RFC */
            checkValidServerReply(leap, mode, stratum, transmitTime)

            val roundTripTime = responseTicks - requestTicks - (transmitTime - receiveTime)

            val clockOffset = (receiveTime - originateTime + (transmitTime - responseTime)) / 2
            Log.d(TAG, "round trip: " + roundTripTime + "ms, " +
                    "clock offset: " + clockOffset + "ms")

            // save our results - use the times on this side of the network latency
            // (response rather than request time)
            mNtpTime = responseTime + clockOffset
            mNtpTimeReference = responseTicks
            mRoundTripTime = roundTripTime
            sendMsg("success >> mNtpTime: $mNtpTime, mRoundTripTime: $mRoundTripTime \n格式化 >> ${formatter.format(mNtpTime)}")
            Log.d("zhou", "success >> mNtpTime: $mNtpTime, mRoundTripTime: $mRoundTripTime, mNtpTimeReference: $mNtpTimeReference")
        } catch (e: Exception) {
            if (DBG) Log.d(TAG, "request time failed: $e")
            sendMsg("request time failed: $e")
            return false
        } finally {
            socket?.close()
        }

        return true
    }

    private fun writeTimeStamp(buffer: ByteArray, offset: Int, time: Long) {
        var offset = offset
        // Special case: zero means zero.
        if (time == 0L) {
            Arrays.fill(buffer, offset, offset + 8, 0x00.toByte())
            return
        }

        var seconds = time / 1000L
        val milliseconds = time - seconds * 1000L
        seconds += OFFSET_1900_TO_1970

        // write seconds in big endian format
        buffer[offset++] = (seconds shr 24).toByte()
        buffer[offset++] = (seconds shr 16).toByte()
        buffer[offset++] = (seconds shr 8).toByte()
        buffer[offset++] = (seconds shr 0).toByte()

        val fraction = milliseconds * 0x100000000L / 1000L
        // write fraction in big endian format
        buffer[offset++] = (fraction shr 24).toByte()
        buffer[offset++] = (fraction shr 16).toByte()
        buffer[offset++] = (fraction shr 8).toByte()
        // low order bits should be random data
        buffer[offset++] = (Math.random() * 255.0).toByte()
    }

    private fun readTimeStamp(buffer: ByteArray, offset: Int): Long {
        val seconds = read32(buffer, offset)
        val fraction = read32(buffer, offset + 4)
        // Special case: zero means zero.
        return if (seconds == 0L && fraction == 0L) {
            0
        } else (seconds - OFFSET_1900_TO_1970) * 1000 + fraction * 1000L / 0x100000000L
    }

    private fun read32(buffer: ByteArray, offset: Int): Long {
        val b0 = buffer[offset].toInt()
        val b1 = buffer[offset + 1].toInt()
        val b2 = buffer[offset + 2].toInt()
        val b3 = buffer[offset + 3].toInt()

        // convert signed bytes to unsigned values
        val i0 = if (b0 and 0x80 == 0x80) (b0 and 0x7F) + 0x80 else b0
        val i1 = if (b1 and 0x80 == 0x80) (b1 and 0x7F) + 0x80 else b1
        val i2 = if (b2 and 0x80 == 0x80) (b2 and 0x7F) + 0x80 else b2
        val i3 = if (b3 and 0x80 == 0x80) (b3 and 0x7F) + 0x80 else b3

        return (i0.toLong() shl 24) + (i1.toLong() shl 16) + (i2.toLong() shl 8) + i3.toLong()
    }

    @Throws(Exception::class)
    private fun checkValidServerReply(
            leap: Byte, mode: Byte, stratum: Int, transmitTime: Long) {
        if (leap.toInt() == NTP_LEAP_NOSYNC) {
            throw Exception("unsynchronized server")
        }
        if (mode.toInt() != NTP_MODE_SERVER && mode.toInt() != NTP_MODE_BROADCAST) {
            throw Exception("untrusted mode: $mode")
        }
        if (stratum == NTP_STRATUM_DEATH || stratum > NTP_STRATUM_MAX) {
            throw Exception("untrusted stratum: $stratum")
        }
        if (transmitTime == 0L) {
            throw Exception("zero transmitTime")
        }
    }

    private fun sendMsg(data: String) {
        val msg = handler.obtainMessage()
        msg.obj = data
        msg.sendToTarget()
    }
}