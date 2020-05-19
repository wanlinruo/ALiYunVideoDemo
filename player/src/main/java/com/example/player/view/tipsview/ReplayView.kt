package com.example.player.view.tipsview

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
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
 * 重播提示对话框。播放结束的时候会显示这个界面
 */
class ReplayView : RelativeLayout, ITheme {
    //重播按钮
    private var mReplayBtn: TextView? = null

    //重播事件监听
    private var mOnReplayClickListener: OnReplayClickListener? = null

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
        val view = inflater.inflate(R.layout.alivc_dialog_replay, null)
        val viewWidth = resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_err_width)
        val viewHeight =
            resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_err_height)
        val params = LayoutParams(viewWidth, viewHeight)
        addView(view, params)

        //设置监听
        mReplayBtn = view.findViewById<View>(R.id.replay) as TextView
        mReplayBtn!!.setOnClickListener {
            if (mOnReplayClickListener != null) {
                mOnReplayClickListener!!.onReplay()
            }
        }
    }

    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        //更新主题
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            mReplayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_blue)
            mReplayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_blue
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            mReplayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_green)
            mReplayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_green
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            mReplayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_orange)
            mReplayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_orange
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            mReplayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_red)
            mReplayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_red
                )
            )
        }
    }

    /**
     * 重播点击事件
     */
    interface OnReplayClickListener {
        /**
         * 重播事件
         */
        fun onReplay()
    }

    /**
     * 设置重播事件监听
     *
     * @param l 重播事件
     */
    fun setOnReplayClickListener(l: OnReplayClickListener?) {
        mOnReplayClickListener = l
    }
}