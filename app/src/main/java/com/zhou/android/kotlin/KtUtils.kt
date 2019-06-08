package com.zhou.android.kotlin

import android.text.Editable

/**
 *  @author Administrator_Zhou
 *  created on 2019/6/8
 */

fun string(text:CharSequence):Editable = Editable.Factory.getInstance().newEditable(text)