package com.zhou.android.kotlin

import android.os.Bundle
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

    var text: TextView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        text = inflater.inflate(R.layout.fragment_text, container, false) as TextView
        return text
    }

    fun setText(str: String) {
        text?.text = str
    }
}