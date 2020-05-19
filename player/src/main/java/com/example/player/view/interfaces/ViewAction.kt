package com.example.player.view.interfaces

import com.example.player.widget.AliyunScreenMode

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 定义UI界面通用的操作。
 * 主要实现类有
 * [com.aliyun.vodplayerview.view.control.ControlView] ,
 * [com.aliyun.vodplayerview.view.gesture.GestureView]
 */
interface ViewAction {
    /**
     * 隐藏类型
     */
    enum class HideType {
        /**
         * 正常情况下的隐藏
         */
        Normal,

        /**
         * 播放结束的隐藏，比如出错了
         */
        End
    }

    /**
     * 重置
     */
    fun reset()

    /**
     * 显示
     */
    fun show()

    /**
     * 隐藏
     *
     * @param hideType 隐藏类型
     */
    fun hide(hideType: HideType?)

    /**
     * 设置屏幕全屏情况
     *
     * @param mode [AliyunScreenMode.Small]：小屏. [AliyunScreenMode.Full]:全屏
     */
    fun setScreenModeStatus(mode: AliyunScreenMode)
}