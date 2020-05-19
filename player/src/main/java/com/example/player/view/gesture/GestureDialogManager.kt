package com.example.player.view.gesture

import android.app.Activity
import android.view.View
import com.example.player.view.gesture.gesturedialog.BrightnessDialog
import com.example.player.view.gesture.gesturedialog.SeekDialog
import com.example.player.view.gesture.gesturedialog.VolumeDialog

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势对话框的管理器。
 * 用于管理亮度[BrightnessDialog] ，seek[SeekDialog] ，音量[VolumeDialog]等dialog的显示/隐藏等。
 */
class GestureDialogManager(//用于构建手势用的dialog
    private var mActivity: Activity
) {

    //seek手势对话框
    private var mSeekDialog: SeekDialog? = null

    //亮度对话框
    private var mBrightnessDialog: BrightnessDialog? = null

    //音量对话框
    private var mVolumeDialog: VolumeDialog? = null

    /**
     * 显示seek对话框
     *
     * @param parent         显示在哪个view的中间
     * @param targetPosition seek的位置
     */
    fun showSeekDialog(parent: View, targetPosition: Int) {
        if (mSeekDialog == null) {
            mSeekDialog = SeekDialog(mActivity, targetPosition)
        }
        if (!mSeekDialog!!.isShowing) {
            mSeekDialog!!.show(parent)
            mSeekDialog!!.updatePosition(targetPosition)
        }
    }

    /**
     * 更新seek进度。 在手势滑动的过程中调用。
     *
     * @param duration        时长
     * @param currentPosition 当前位置
     * @param deltaPosition   滑动位置
     */
    fun updateSeekDialog(
        duration: Long,
        currentPosition: Long,
        deltaPosition: Long
    ) {
        val targetPosition =
            mSeekDialog!!.getTargetPosition(duration, currentPosition, deltaPosition)
        mSeekDialog!!.updatePosition(targetPosition)
    }

    /**
     * 隐藏seek对话框
     *
     * @return 最终的seek位置，用于实际的seek操作
     */
    fun dismissSeekDialog(): Int {
        var seekPosition = -1
        if (mSeekDialog != null && mSeekDialog!!.isShowing) {
            seekPosition = mSeekDialog!!.finalPosition
            mSeekDialog!!.dismiss()
        }
        mSeekDialog = null
        //返回最终的seek位置，用于实际的seek操作
        return seekPosition
    }

    /**
     * 显示亮度对话框
     *
     * @param parent 显示在哪个view中间
     */
    fun showBrightnessDialog(parent: View, currentBrightness: Int) {
        if (mBrightnessDialog == null) {
            mBrightnessDialog = BrightnessDialog(mActivity, currentBrightness)
        }
        if (!mBrightnessDialog!!.isShowing) {
            mBrightnessDialog!!.show(parent)
            mBrightnessDialog!!.updateBrightness(currentBrightness)
        }
    }

    /**
     * 更新亮度值
     *
     * @param changePercent 亮度变化百分比
     * @return 最终的亮度百分比
     */
    fun updateBrightnessDialog(changePercent: Int): Int {
        val targetBrightnessPercent =
            mBrightnessDialog!!.getTargetBrightnessPercent(changePercent)
        mBrightnessDialog!!.updateBrightness(targetBrightnessPercent)
        return targetBrightnessPercent
    }

    /**
     * 隐藏亮度对话框
     */
    fun dismissBrightnessDialog() {
        if (mBrightnessDialog != null && mBrightnessDialog!!.isShowing) {
            mBrightnessDialog!!.dismiss()
        }
        mBrightnessDialog = null
    }

    fun initDialog(activity: Activity, currentPercent: Float) {
        mActivity = activity
        if (mVolumeDialog == null) {
            mVolumeDialog = VolumeDialog(activity, currentPercent)
        }
    }

    /**
     * 显示音量对话框
     *
     * @param parent         显示在哪个view中间
     * @param currentPercent 当前音量百分比
     */
    fun showVolumeDialog(parent: View, currentPercent: Float) {
        if (mVolumeDialog == null) {
            mVolumeDialog = VolumeDialog(mActivity, currentPercent)
        }
        if (!mVolumeDialog!!.isShowing) {
            mVolumeDialog!!.show(parent)
            mVolumeDialog!!.updateVolume(currentPercent)
        }
    }

    /**
     * 更新音量
     *
     * @param changePercent 变化的百分比
     * @return 最终的音量百分比
     */
    fun updateVolumeDialog(changePercent: Int): Float {
        val targetVolume = mVolumeDialog!!.getTargetVolume(changePercent)
        mVolumeDialog!!.updateVolume(targetVolume)
        return targetVolume
    }

    /**
     * 关闭音量对话框
     */
    fun dismissVolumeDialog() {
        if (mVolumeDialog != null && mVolumeDialog!!.isShowing) {
            mVolumeDialog!!.dismiss()
        }
        mVolumeDialog = null
    }

}