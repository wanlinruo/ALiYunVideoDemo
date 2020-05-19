package com.example.player.view.tipsview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.player.R
import com.example.player.theme.ITheme
import com.example.player.widget.AliyunVodPlayerView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 错误提示对话框。出错的时候会显示。
 */
class ErrorView : RelativeLayout, ITheme {
    //错误信息
    private var mMsgView: TextView? = null

    //错误码
    private var mCodeView: TextView? = null

    //重试的图片
    private var mRetryView: View? = null

    //重试的按钮
    private var mRetryBtn: TextView? = null
    private var mOnRetryClickListener: OnRetryClickListener? = null //重试点击事件

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
        val view = inflater.inflate(R.layout.alivc_dialog_error, null)
        val viewWidth = resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_err_width)
        val viewHeight =
            resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_err_height)
        val params = LayoutParams(viewWidth, viewHeight)
        addView(view, params)
        mRetryBtn = view.findViewById<View>(R.id.retry_btn) as TextView
        mMsgView = view.findViewById<View>(R.id.msg) as TextView
        mCodeView = view.findViewById<View>(R.id.code) as TextView
        mRetryView = view.findViewById(R.id.retry)
        //重试的点击监听
        mRetryView?.setOnClickListener(OnClickListener {
            if (mOnRetryClickListener != null) {
                mOnRetryClickListener!!.onRetryClick()
            }
        })
    }

    /**
     * 更新提示文字
     *
     * @param errorCode  错误码
     * @param errorEvent 错误事件
     * @param errMsg     错误码
     */
    fun updateTips(errorCode: Int, errorEvent: String, errMsg: String?) {
        mMsgView!!.text = errMsg
        mCodeView!!.text =
            context.getString(R.string.alivc_error_code) + errorCode + " - " + errorEvent
    }

    /**
     * 更新提示文字,不包含错误码
     */
    fun updateTipsWithoutCode(errMsg: String?) {
        mMsgView!!.text = errMsg
        mCodeView!!.visibility = View.GONE
    }

    /**
     * 更新主题
     *
     * @param theme 支持的主题
     */
    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            mRetryView!!.setBackgroundResource(R.drawable.alivc_rr_bg_blue)
            mRetryBtn!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.alivc_refresh_blue,
                0,
                0,
                0
            )
            mRetryBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_blue
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            mRetryView!!.setBackgroundResource(R.drawable.alivc_rr_bg_green)
            mRetryBtn!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.alivc_refresh_green,
                0,
                0,
                0
            )
            mRetryBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_green
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            mRetryView!!.setBackgroundResource(R.drawable.alivc_rr_bg_orange)
            mRetryBtn!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.alivc_refresh_orange,
                0,
                0,
                0
            )
            mRetryBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_orange
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            mRetryView!!.setBackgroundResource(R.drawable.alivc_rr_bg_red)
            //这个重试图片是白色。。很尴尬
            mRetryBtn!!.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.alivc_refresh_red,
                0,
                0,
                0
            )
            mRetryBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_red
                )
            )
        }
    }

    /**
     * 重试的点击事件
     */
    interface OnRetryClickListener {
        /**
         * 重试按钮点击
         */
        fun onRetryClick()
    }

    /**
     * 设置重试点击事件
     *
     * @param l 重试的点击事件
     */
    fun setOnRetryClickListener(l: OnRetryClickListener?) {
        mOnRetryClickListener = l
    }

    companion object {
        private val TAG = ErrorView::class.java.simpleName
    }
}