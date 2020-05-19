package com.example.player.view.gesture.gesturedialog

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.example.player.R

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 手势滑动的手势提示框。
 * 其子类有：[BrightnessDialog] , [SeekDialog] , [VolumeDialog]
 */
open class BaseGestureDialog(context: Context) : PopupWindow() {
    //手势文字
    @JvmField
    var mTextView: TextView

    //手势图片
    @JvmField
    var mImageView: ImageView

    //对话框的宽高
    private val mDialogWidthAndHeight: Int

    /**
     * 居中显示对话框
     *
     * @param parent 所属的父界面
     */
    fun show(parent: View) {
        val location = IntArray(2)
        parent.getLocationOnScreen(location)
        //保证显示居中
        val x =
            location[0] + (parent.right - parent.left - mDialogWidthAndHeight) / 2
        val y =
            location[1] + (parent.bottom - parent.top - mDialogWidthAndHeight) / 2
        showAtLocation(parent, Gravity.TOP or Gravity.LEFT, x, y)
    }

    init {
        //使用同一个布局
        val mInflater = context.applicationContext
            .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = mInflater.inflate(R.layout.alivc_dialog_gesture, null)
        view.measure(0, 0)
        contentView = view

        //找出view
        mTextView = view.findViewById<View>(R.id.gesture_text) as TextView
        mImageView =
            view.findViewById<View>(R.id.gesture_image) as ImageView

        //设置对话框宽高
        mDialogWidthAndHeight =
            context.resources.getDimensionPixelSize(R.dimen.alivc_player_gesture_dialog_size)
        width = mDialogWidthAndHeight
        height = mDialogWidthAndHeight
    }
}