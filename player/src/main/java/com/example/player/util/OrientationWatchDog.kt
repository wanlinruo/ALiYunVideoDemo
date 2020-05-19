package com.example.player.util

import android.content.Context
import android.hardware.SensorManager
import android.view.OrientationEventListener
import com.aliyun.utils.VcPlayerLog

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 屏幕方向监听类
 */
class OrientationWatchDog(context: Context) {

    private val mContext: Context = context.applicationContext

    //系统的屏幕方向改变监听
    private var mLandOrientationListener: OrientationEventListener? = null

    //对外的设置的监听
    private var mOrientationListener: OnOrientationListener? = null

    //上次屏幕的方向
    private var mLastOrientation =
        Orientation.Port

    /**
     * 屏幕方向
     */
    private enum class Orientation {
        /**
         * 竖屏
         */
        Port,

        /**
         * 横屏,正向
         */
        Land_Forward,

        /**
         * 横屏,反向
         */
        Land_Reverse
    }

    /**
     * 开始监听
     */
    fun startWatch() {
        VcPlayerLog.e(TAG, "startWatch")
        if (mLandOrientationListener == null) {
            mLandOrientationListener =
                object : OrientationEventListener(mContext, SensorManager.SENSOR_DELAY_NORMAL) {
                    override fun onOrientationChanged(orientation: Int) {
                        if (orientation == -1) {
                            return
                        }
                        //这里的|| 和&& 不能弄错！！
                        //根据手机的方向角度计算。在90和180度上下10度的时候认为横屏了。
                        //竖屏类似。
                        val isLand = (orientation < 100 && orientation > 80
                                || orientation < 280 && orientation > 260)
                        val isPort = (orientation < 10 || orientation > 350
                                || orientation < 190 && orientation > 170)
                        if (isLand) {
                            if (mOrientationListener != null && orientation < 100 && orientation > 80) {
                                VcPlayerLog.d(TAG, "ToLandForward")
                                mOrientationListener!!.changedToLandReverseScape(
                                    mLastOrientation == Orientation.Port
                                            || mLastOrientation == Orientation.Land_Forward
                                )
                                mLastOrientation =
                                    Orientation.Land_Reverse
                            } else if (mOrientationListener != null && orientation < 280 && orientation > 260) {
                                VcPlayerLog.d(TAG, "ToLandReverse")
                                mOrientationListener!!.changedToLandForwardScape(
                                    mLastOrientation == Orientation.Port
                                            || mLastOrientation == Orientation.Land_Reverse
                                )
                                mLastOrientation =
                                    Orientation.Land_Forward
                            }
                        } else if (isPort) {
                            if (mOrientationListener != null) {
                                VcPlayerLog.d(TAG, "ToPort")
                                mOrientationListener!!.changedToPortrait(
                                    mLastOrientation == Orientation.Land_Reverse
                                            || mLastOrientation == Orientation.Land_Forward
                                )
                            }
                            mLastOrientation =
                                Orientation.Port
                        }
                    }
                }
        }
        mLandOrientationListener!!.enable()
    }

    /**
     * 结束监听
     */
    fun stopWatch() {
        VcPlayerLog.e(TAG, "stopWatch")
        if (mLandOrientationListener != null) {
            mLandOrientationListener!!.disable()
        }
    }

    /**
     * 销毁监听
     */
    fun destroy() {
        VcPlayerLog.e(TAG, "onDestroy")
        stopWatch()
        mLandOrientationListener = null
    }

    /**
     * 屏幕方向变化事件
     */
    interface OnOrientationListener {
        /**
         * 变为Land_Forward
         *
         * @param fromPort 是否是从竖屏变过来的
         */
        fun changedToLandForwardScape(fromPort: Boolean)

        /**
         * 变为Land_Reverse
         *
         * @param fromPort 是否是从竖屏变过来的
         */
        fun changedToLandReverseScape(fromPort: Boolean)

        /**
         * 变为Port
         *
         * @param fromLand 是否是从横屏变过来的
         */
        fun changedToPortrait(fromLand: Boolean)
    }

    /**
     * 设置屏幕方向变化事件
     *
     * @param l 事件监听
     */
    fun setOnOrientationListener(l: OnOrientationListener?) {
        mOrientationListener = l
    }

    companion object {
        private val TAG = OrientationWatchDog::class.java.simpleName
    }

}