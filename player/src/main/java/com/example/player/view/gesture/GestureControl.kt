package com.example.player.view.gesture

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.OnDoubleTapListener
import android.view.MotionEvent
import android.view.View
import com.example.player.util.ScreenUtils
import com.example.player.view.gesture.GestureView.GestureListener

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 播放控手势控制。通过对view的GestureDetector事件做监听，判断水平滑动还是垂直滑动。
 * 最后的结果通过[GestureView.GestureListener]返回出去。
 * 主要在[GestureView]中使用到此类。
 */
class GestureControl(
    var mContext: Context,
    /**
     * 播放控制层
     */
    private val mGesturebleView: View
) {

    /**
     * 是否允许触摸 //TODO 可以删掉
     */
    private var isGestureEnable = true

    //是否水平
    private var isInHorizenalGesture = false

    //是否右边垂直
    private var isInRightGesture = false

    //是否左边垂直
    private var isInLeftGesture = false

    //手势决定器
    private var mGestureDetector: GestureDetector? = null

    //手势监听
    private var mGestureListener: GestureListener? = null
    private fun init() {
        mGestureDetector = GestureDetector(mContext, mOnGestureListener)
        mGesturebleView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    //对结束事件的监听
                    if (mGestureListener != null) {
                        mGestureListener!!.onGestureEnd()
                    }
                    isInLeftGesture = false
                    isInRightGesture = false
                    isInHorizenalGesture = false
                }
                else -> {
                }
            }
            //其他的事件交给GestureDetector。
            mGestureDetector!!.onTouchEvent(event)
        }

        //GestureDetector增加双击事件的监听。。里面包含了单击事件
        mGestureDetector!!.setOnDoubleTapListener(object : OnDoubleTapListener {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                //			处理点击事件
                if (mGestureListener != null) {
                    mGestureListener!!.onSingleTap()
                }
                return false
            }

            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (mGestureListener != null) {
                    mGestureListener!!.onDoubleTap()
                }
                return false
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                return false
            }
        })
    }

    /**
     * 开启关闭手势控制。
     *
     * @param enable 开启
     */
    fun enableGesture(enable: Boolean) {
        isGestureEnable = enable
    }

    /**
     * 设置手势监听事件
     *
     * @param mGestureListener 手势监听事件
     */
    fun setOnGestureControlListener(mGestureListener: GestureListener?) {
        this.mGestureListener = mGestureListener
    }

    /**
     * 绑定到GestureDetector的。
     */
    private val mOnGestureListener: GestureDetector.OnGestureListener =
        object : GestureDetector.OnGestureListener {
            private var mXDown = 0f
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                return false
            }

            override fun onShowPress(e: MotionEvent) {}
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                //如果关闭了手势。则不处理。
                if (!isGestureEnable || e1 == null || e2 == null) {
                    return false
                }
                if (Math.abs(distanceX) > Math.abs(distanceY)) {
                    //水平滑动
                    if (isInLeftGesture || isInRightGesture) {
                        //此前已经是竖直滑动了，不管
                    } else {
                        isInHorizenalGesture = true
                    }
                } else {
                    //垂直滑动
                    if (isInHorizenalGesture) {
                    } else {
                    }
                }
                if (isInHorizenalGesture) {
                    if (mGestureListener != null) {
                        mGestureListener!!.onHorizontalDistance(e1.x, e2.x)
                    }
                } else {
                    if (ScreenUtils.isInLeft(mContext, mXDown.toInt())) {
                        isInLeftGesture = true
                        if (mGestureListener != null) {
                            mGestureListener!!.onLeftVerticalDistance(e1.y, e2.y)
                        }
                    } else if (ScreenUtils.isInRight(mContext, mXDown.toInt())) {
                        isInRightGesture = true
                        if (mGestureListener != null) {
                            mGestureListener!!.onRightVerticalDistance(e1.y, e2.y)
                        }
                    }
                }
                return true
            }

            override fun onLongPress(e: MotionEvent) {}
            override fun onFling(
                e1: MotionEvent,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return false
            }

            override fun onDown(e: MotionEvent): Boolean {
                mXDown = e.x
                return true
            }
        }

    companion object {
        private val TAG = GestureControl::class.java.simpleName
    }

    /**
     * @param mContext
     * @param gestureView 播放控制层
     */
    init {
        init()
    }
}