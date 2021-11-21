package com.zhou.android.kotlin

import android.graphics.Color
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.ColorRes
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.zhou.android.R

/**
 * Created by mxz on 2019/7/25.
 */
class TextFragment : Fragment() {

    private var text: TextView? = null
    private var str = ""
    private var color = Color.WHITE

    var mark = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        text = inflater.inflate(R.layout.fragment_text, container, false) as TextView
        return text
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        text?.text = str
        text?.setBackgroundColor(color)
    }

    fun setText(_str: String) {
        if (text == null) {
            str = _str
        } else {
            text?.text = _str
        }
    }

    fun bg(@ColorInt c: Int) {
        if (text == null) {
            color = c
        } else {
            text?.setBackgroundColor(c)
        }
    }
}