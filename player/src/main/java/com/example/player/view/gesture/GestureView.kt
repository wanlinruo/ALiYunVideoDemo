package com.example.player.view.gesture

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.aliyun.utils.VcPlayerLog
import com.example.player.view.gesture.GestureView
import com.example.player.view.interfaces.ViewAction
import com.example.player.view.interfaces.ViewAction.HideType
import com.example.player.widget.AliyunScreenMode

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势滑动的view。用于UI中处理手势的滑动事件，从而去实现手势改变亮度，音量，seek等操作。
 * 此view主要被[AliyunVodPlayerView] 使用。
 */
class GestureView : View, ViewAction {
    //手势控制
    protected var mGestureControl: GestureControl? = null

    //监听器
    private var mOutGestureListener: GestureListener? = null

    //隐藏原因
    private var mHideType: HideType? = null

    //是否锁定屏幕
    private var mIsFullScreenLocked = false

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
        //创建手势控制
        mGestureControl = GestureControl(context, this)
        //设置监听
        mGestureControl!!.setOnGestureControlListener(object : GestureListener {
            override fun onHorizontalDistance(
                downX: Float,
                nowX: Float
            ) {
                //其他手势如果锁住了就不回调了。
                if (mIsFullScreenLocked) {
                    return
                }
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onHorizontalDistance(downX, nowX)
                }
            }

            override fun onLeftVerticalDistance(
                downY: Float,
                nowY: Float
            ) {
                //其他手势如果锁住了就不回调了。
                if (mIsFullScreenLocked) {
                    return
                }
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onLeftVerticalDistance(downY, nowY)
                }
            }

            override fun onRightVerticalDistance(
                downY: Float,
                nowY: Float
            ) {
                //其他手势如果锁住了就不回调了。
                if (mIsFullScreenLocked) {
                    return
                }
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onRightVerticalDistance(downY, nowY)
                }
            }

            override fun onGestureEnd() {
                //其他手势如果锁住了就不回调了。
                if (mIsFullScreenLocked) {
                    return
                }
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onGestureEnd()
                }
            }

            override fun onSingleTap() {
                //锁屏的时候，单击还是有用的。。不然没法显示锁的按钮了
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onSingleTap()
                }
            }

            override fun onDoubleTap() {
                //其他手势如果锁住了就不回调了。
                if (mIsFullScreenLocked) {
                    return
                }
                if (mOutGestureListener != null) {
                    mOutGestureListener!!.onDoubleTap()
                }
            }
        })
    }

    /**
     * 设置是否锁定全屏了。锁定全屏的话，除了单击手势有响应，其他都不会有响应。
     *
     * @param locked true：锁定。
     */
    fun setScreenLockStatus(locked: Boolean) {
        mIsFullScreenLocked = locked
    }

    fun setHideType(hideType: HideType?) {
        mHideType = hideType
    }

    interface GestureListener {
        /**
         * 水平滑动距离
         *
         * @param downX 按下位置
         * @param nowX  当前位置
         */
        fun onHorizontalDistance(downX: Float, nowX: Float)

        /**
         * 左边垂直滑动距离
         *
         * @param downY 按下位置
         * @param nowY  当前位置
         */
        fun onLeftVerticalDistance(downY: Float, nowY: Float)

        /**
         * 右边垂直滑动距离
         *
         * @param downY 按下位置
         * @param nowY  当前位置
         */
        fun onRightVerticalDistance(downY: Float, nowY: Float)

        /**
         * 手势结束
         */
        fun onGestureEnd()

        /**
         * 单击事件
         */
        fun onSingleTap()

        /**
         * 双击事件
         */
        fun onDoubleTap()
    }

    /**
     * 设置手势监听事件
     *
     * @param gestureListener 手势监听事件
     */
    fun setOnGestureListener(gestureListener: GestureListener?) {
        mOutGestureListener = gestureListener
    }

    override fun reset() {
        mHideType = null
    }

    override fun show() {
        if (mHideType === HideType.End) {
            //如果是由于错误引起的隐藏，那就不能再展现了
            VcPlayerLog.d(TAG, "show END")
        } else {
            VcPlayerLog.d(TAG, "show ")
            visibility = VISIBLE
        }
    }

    override fun hide(hideType: HideType?) {
        if (mHideType !== HideType.End) {
            mHideType = hideType
        }
        visibility = GONE
    }

    override fun setScreenModeStatus(mode: AliyunScreenMode) {}

    companion object {
        private val TAG = GestureView::class.java.simpleName
    }
}