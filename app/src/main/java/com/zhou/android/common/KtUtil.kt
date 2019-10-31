package com.zhou.android.common

import android.app.Activity
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by mxz on 2019/10/31.
 */

fun Disposable.addToComposite(composite: CompositeDisposable) = composite.add(this)

fun Activity.toast(text: CharSequence, duration: Int = Toast.LENGTH_LONG) = Toast.makeText(this, text, duration).show()