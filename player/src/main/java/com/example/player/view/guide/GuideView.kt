package com.example.player.view.guide

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.player.R
import com.example.player.theme.ITheme
import com.example.player.widget.AliyunScreenMode
import com.example.player.widget.AliyunVodPlayerView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势指导页面。
 * 主要在[AliyunVodPlayerView] 使用。
 */
class GuideView : LinearLayout, ITheme {
    //三个文字显示
    private var mBrightText: TextView? = null
    private var mProgressText: TextView? = null
    private var mVolumeText: TextView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        //设置页面布局
        setBackgroundColor(Color.parseColor("#88000000"))
        gravity = Gravity.CENTER
        orientation = VERTICAL
        LayoutInflater.from(context).inflate(R.layout.alivc_view_guide, this, true)

        //这几个文字有颜色的变化
        mBrightText = findViewById<View>(R.id.bright_text) as TextView
        mProgressText = findViewById<View>(R.id.progress_text) as TextView
        mVolumeText = findViewById<View>(R.id.volume_text) as TextView

        //默认是隐藏的
        hide()
    }

    /**
     * 设置当前屏幕的模式
     *
     * @param mode 全屏，小屏
     */
    fun setScreenMode(mode: AliyunScreenMode) {
        if (mode === AliyunScreenMode.Small) {
            //小屏时隐藏
            hide()
            return
        }
        //只有第一次进入全屏的时候显示。通过SharedPreferences记录这个值。
        val spf = context.getSharedPreferences(
            "alivc_guide_record",
            Context.MODE_PRIVATE
        )
        val hasShown = spf.getBoolean("has_shown", false)
        //如果已经显示过了，就不接着走了
        visibility = if (hasShown) {
            return
        } else {
            View.VISIBLE
        }
        //记录下来
        val editor = spf.edit()
        editor.putBoolean("has_shown", true)
        editor.apply()
    }

    /**
     * 隐藏不显示
     */
    fun hide() {
        visibility = View.GONE
    }

    /**
     * 手势点击到了就隐藏
     *
     * @param event 触摸事件
     * @return
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        hide()
        return true
    }

    /**
     * 设置主题色
     *
     * @param theme 支持的主题
     */
    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        var colorRes = R.color.alivc_player_theme_blue
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            colorRes = R.color.alivc_player_theme_blue
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            colorRes = R.color.alivc_player_theme_green
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            colorRes = R.color.alivc_player_theme_orange
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            colorRes = R.color.alivc_player_theme_red
        }
        val color = ContextCompat.getColor(context, colorRes)
        //这三个text的颜色会改变
        mBrightText!!.setTextColor(color)
        mProgressText!!.setTextColor(color)
        mVolumeText!!.setTextColor(color)
    }
}