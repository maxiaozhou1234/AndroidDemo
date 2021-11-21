package com.zhou.android.livedata

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.annotation.MainThread
import android.support.annotation.NonNull
import android.util.Log


/**
 * 通过对 version 的拦截，让新订阅的 observer 不触发最后一次的值
 * 参考文章：https://tech.meituan.com/2018/07/26/android-livedatabus.html
 *
 * 一个完整的 LiveEventBus 地址：https://github.com/JeremyLiao/LiveEventBus
 * Created by mxz on 2020/5/25.
 */
class LiveEvent<T> {

    private val liveData = MutableLiveData<T>()
    private val map = HashMap<Observer<T>, ObserverWrapper<T>>()

    private fun getVersion(): Int {
        var version = -1
        try {
            val fieldVersion = LiveData::class.java.getDeclaredField("mVersion")
            fieldVersion.isAccessible = true
            version = fieldVersion.get(liveData) as Int
            Log.d("liveBus", "get version $version")
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
            Log.e("liveBus", "error:${e.message}")
        }
        return version
    }

    @MainThread
    fun observe(@NonNull owner: LifecycleOwner, @NonNull observer: Observer<T>) {

        val wrapper = ObserverWrapper(observer)
        wrapper.ignore = getVersion() > -1
        map[observer] = wrapper
        liveData.observe(owner, wrapper)
    }

    @MainThread
    fun observeSticky(@NonNull owner: LifecycleOwner, @NonNull observer: Observer<T>) {

        val wrapper = ObserverWrapper(observer)
        wrapper.ignore = false
        map[observer] = wrapper
        liveData.observe(owner, wrapper)
    }

    @MainThread
    fun observeForeverSticky(@NonNull observer: Observer<T>) {
        val wrapper = ObserverWrapper(observer)
        wrapper.ignore = false
        map[observer] = wrapper
        liveData.observeForever(observer)
    }

    @MainThread
    fun removeObserver(@NonNull observer: Observer<T>) {
        val observerWrapper = map.remove(observer)
        if (observerWrapper != null) {
            liveData.removeObserver(observerWrapper)
        }
    }

    @MainThread
    fun removeObservers(owner: LifecycleOwner) {
        map.clear()
        liveData.removeObservers(owner)
    }

    @MainThread
    fun postValue(value: T) {
        liveData.postValue(value)
    }

    @MainThread
    fun setValue(value: T) {
        liveData.value = value
    }

    class ObserverWrapper<T>(private val observer: Observer<T>) : Observer<T> {

        var ignore = true

        override fun onChanged(t: T?) {
            Log.i("liveBus", "onchange == ignore: $ignore")
            if (ignore) {
                ignore = false
                return
            }
            observer.onChanged(t)
        }
    }
}