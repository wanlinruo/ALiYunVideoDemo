package com.example.player.view.gesture.gesturedialog

import android.app.Activity
import com.aliyun.utils.VcPlayerLog
import com.example.player.R

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势滑动的时的亮度提示框
 */
class BrightnessDialog(activity: Activity?, percent: Int) :
    BaseGestureDialog(activity!!) {
    // 当前亮度。0~100
    private var mCurrentBrightness = 0

    /**
     * 更新对话框上的亮度百分比
     *
     * @param percent 亮度百分比
     */
    fun updateBrightness(percent: Int) {
        mTextView.text = "$percent%"
    }

    /**
     * 计算最终的亮度百分比
     *
     * @param changePercent 变化的百分比
     * @return 最终的亮度百分比
     */
    fun getTargetBrightnessPercent(changePercent: Int): Int {
        VcPlayerLog.d(
            TAG,
            "changePercent = $changePercent , mCurrentBrightness  = $mCurrentBrightness"
        )
        var newBrightness = mCurrentBrightness - changePercent
        if (newBrightness > 100) {
            newBrightness = 100
        } else if (newBrightness < 0) {
            newBrightness = 0
        }
        return newBrightness
    }

    companion object {
        private val TAG = BrightnessDialog::class.java.simpleName

        /**
         * 获取当前亮度百分比
         *
         * @param activity 活动
         * @return 当前亮度百分比
         */
        fun getActivityBrightness(activity: Activity?): Int {
            if (activity != null) {
                val window = activity.window
                val layoutParams = window.attributes
                var screenBrightness = layoutParams.screenBrightness
                if (screenBrightness > 1) {
                    screenBrightness = 1f
                } else if (screenBrightness < 0.1f) {
                    //解决三星某些手机亮度值等于0自动锁屏的bug
                    screenBrightness = 0.1f
                }
                VcPlayerLog.d(
                    TAG,
                    "getActivityBrightness layoutParams.screenBrightness = $screenBrightness"
                )
                return (screenBrightness * 100).toInt()
            }
            return 0
        }
    }

    init {
        mCurrentBrightness = percent

        //设置亮度图片
        mImageView.setImageResource(R.drawable.alivc_brightness)
        updateBrightness(percent)
    }
}