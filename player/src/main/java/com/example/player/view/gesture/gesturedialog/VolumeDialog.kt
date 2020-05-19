package com.example.player.view.gesture.gesturedialog

import android.app.Activity
import com.aliyun.utils.VcPlayerLog
import com.example.player.R

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势滑动的音量提示框。
 */
class VolumeDialog(context: Activity?, percent: Float) :
    BaseGestureDialog(context!!) {
    private var initVolume = 0f

    /**
     * 更新音量值
     *
     * @param percent 音量百分比
     */
    fun updateVolume(percent: Float) {
        mTextView.text = "$percent%"
        mImageView.setImageLevel(percent.toInt())
    }

    /**
     * 获取最后的音量
     *
     * @param changePercent 变化的百分比
     * @return 最后的音量
     */
    fun getTargetVolume(changePercent: Int): Float {
        VcPlayerLog.d(
            TAG,
            "changePercent = $changePercent , initVolume  = $initVolume"
        )
        var newVolume = initVolume - changePercent
        if (newVolume > 100) {
            newVolume = 100f
        } else if (newVolume < 0) {
            newVolume = 0f
        }
        return newVolume
    }

    companion object {
        private val TAG = VolumeDialog::class.java.simpleName
    }

    init {
        initVolume = percent
        mImageView.setImageResource(R.drawable.alivc_volume_img)
        updateVolume(percent)
    }
}