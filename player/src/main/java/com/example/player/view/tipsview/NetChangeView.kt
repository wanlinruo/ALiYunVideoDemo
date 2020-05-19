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
 * 网络变化提示对话框。当网络由wifi变为4g的时候会显示。
 */
class NetChangeView : RelativeLayout, ITheme {
    //结束播放的按钮
    private var mStopPlayBtn: TextView? = null

    //界面上的操作按钮事件监听
    private var mOnNetChangeClickListener: OnNetChangeClickListener? = null

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
        val view = inflater.inflate(R.layout.alivc_dialog_netchange, null)
        val viewWidth =
            resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_netchange_width)
        val viewHeight =
            resources.getDimensionPixelSize(R.dimen.alivc_player_dialog_netchange_height)
        val params = LayoutParams(viewWidth, viewHeight)
        addView(view, params)

        //继续播放的点击事件
        view.findViewById<View>(R.id.continue_play)
            .setOnClickListener {
                if (mOnNetChangeClickListener != null) {
                    mOnNetChangeClickListener!!.onContinuePlay()
                }
            }

        //停止播放的点击事件
        mStopPlayBtn = view.findViewById<View>(R.id.stop_play) as TextView
        mStopPlayBtn!!.setOnClickListener {
            if (mOnNetChangeClickListener != null) {
                mOnNetChangeClickListener!!.onStopPlay()
            }
        }
    }

    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        //更新停止播放按钮的主题
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            mStopPlayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_blue)
            mStopPlayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_blue
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            mStopPlayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_green)
            mStopPlayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_green
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            mStopPlayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_orange)
            mStopPlayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_orange
                )
            )
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            mStopPlayBtn!!.setBackgroundResource(R.drawable.alivc_rr_bg_red)
            mStopPlayBtn!!.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_player_theme_red
                )
            )
        }
    }

    /**
     * 界面中的点击事件
     */
    interface OnNetChangeClickListener {
        /**
         * 继续播放
         */
        fun onContinuePlay()

        /**
         * 停止播放
         */
        fun onStopPlay()
    }

    /**
     * 设置界面的点击监听
     *
     * @param l 点击监听
     */
    fun setOnNetChangeClickListener(l: OnNetChangeClickListener?) {
        mOnNetChangeClickListener = l
    }
}