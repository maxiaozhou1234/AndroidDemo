package com.zhou.android.livedata

/**
 * Created by mxz on 2020/5/25.
 */
object LiveEventBus {

    private val map = HashMap<String, LiveEvent<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> getChannel(target: String, clz: Class<T>): LiveEvent<T> {
        if (!map.containsKey(target)) {
            map[target] = LiveEvent()
        }
        return map[target]!! as LiveEvent<T>
    }

    @Suppress("UNCHECKED_CAST")
    fun getChannel(target: String): LiveEvent<Any> {
        if (!map.containsKey(target)) {
            map[target] = LiveEvent()
        }
        return map[target]!!
    }
}