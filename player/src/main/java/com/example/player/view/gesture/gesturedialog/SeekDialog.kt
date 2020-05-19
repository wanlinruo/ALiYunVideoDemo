package com.example.player.view.gesture.gesturedialog

import android.app.Activity
import com.example.player.R
import com.example.player.util.TimeFormater.formatMs

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势滑动的seek提示框。
 */
class SeekDialog(activity: Activity, position: Int) : BaseGestureDialog(activity) {
    private var mInitPosition = 0

    /**
     * 获取最终的位置
     *
     * @return
     */
    var finalPosition = 0
        private set

    fun updatePosition(position: Int) {
        //这里更新了网签和往后seek的图片
        if (position >= mInitPosition) {
            mImageView.setImageResource(R.drawable.alivc_seek_forward)
        } else {
            mImageView.setImageResource(R.drawable.alivc_seek_rewind)
        }
        mTextView.text = formatMs(position.toLong())
    }

    /**
     * 目标位置计算算法
     *
     * @param duration        视频总时长
     * @param currentPosition 当前播放位置
     * @param deltaPosition   与当前位置相差的时长
     * @return
     */
    fun getTargetPosition(
        duration: Long,
        currentPosition: Long,
        deltaPosition: Long
    ): Int {
        // seek步长
        val finalDeltaPosition: Long
        // 根据视频时长，决定seek步长
        val totalMinutes = duration / 1000 / 60
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()

        // 视频时长为1小时以上，小屏和全屏的手势滑动最长为视频时长的十分之一
        finalDeltaPosition = if (hours >= 1) {
            deltaPosition / 10
        } // 视频时长为31分钟－60分钟时，小屏和全屏的手势滑动最长为视频时长五分之一
        else if (minutes > 30) {
            deltaPosition / 5
        } // 视频时长为11分钟－30分钟时，小屏和全屏的手势滑动最长为视频时长三分之一
        else if (minutes > 10) {
            deltaPosition / 3
        } // 视频时长为4-10分钟时，小屏和全屏的手势滑动最长为视频时长二分之一
        else if (minutes > 3) {
            deltaPosition / 2
        } // 视频时长为1秒钟至3分钟时，小屏和全屏的手势滑动最长为视频结束
        else {
            deltaPosition
        }
        var targetPosition = finalDeltaPosition + currentPosition
        if (targetPosition < 0) {
            targetPosition = 0
        }
        if (targetPosition > duration) {
            targetPosition = duration
        }
        finalPosition = targetPosition.toInt()
        return finalPosition
    }

    init {
        mInitPosition = position
        updatePosition(mInitPosition)
    }
}