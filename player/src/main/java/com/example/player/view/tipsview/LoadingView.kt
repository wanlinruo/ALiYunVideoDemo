package com.example.player.view.tipsview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.player.R
import com.example.player.view.tipsview.LoadingView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 加载提示对话框。加载过程中，缓冲过程中会显示。
 */
class LoadingView : RelativeLayout {
    //加载提示文本框
    private var mLoadPercentView: TextView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        val inflater = context
            .applicationContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val resources = context.resources
        val view = inflater.inflate(R.layout.alivc_dialog_loading, null)
        val viewWidth =
            resources.getDimensionPixelSize(R.dimen.alivc_palyer_dialog_loading_width)
        val viewHeight =
            resources.getDimensionPixelSize(R.dimen.alivc_palyer_dialog_loading_width)
        val params = LayoutParams(viewWidth, viewHeight)
        addView(view, params)
        mLoadPercentView = view.findViewById<View>(R.id.net_speed) as TextView
        mLoadPercentView!!.text = context.getString(R.string.alivc_loading) + " 0%"
    }

    /**
     * 更新加载进度
     *
     * @param percent 百分比
     */
    fun updateLoadingPercent(percent: Int) {
        mLoadPercentView!!.text = context.getString(R.string.alivc_loading) + percent + "%"
    }

    /**
     * 只显示loading，不显示进度提示
     */
    fun setOnlyLoading() {
        findViewById<View>(R.id.loading_layout).visibility = View.GONE
    }

    companion object {
        private val TAG = LoadingView::class.java.simpleName
    }
}