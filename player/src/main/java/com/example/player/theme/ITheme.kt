package com.example.player.theme

import com.example.player.view.control.ControlView
import com.example.player.view.guide.GuideView
import com.example.player.quality.QualityView
import com.example.player.view.speed.SpeedView
import com.example.player.view.tipsview.ErrorView
import com.example.player.view.tipsview.NetChangeView
import com.example.player.view.tipsview.ReplayView
import com.example.player.view.tipsview.TipsView
import com.example.player.widget.AliyunVodPlayerView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 主题的接口。用于变换UI的主题。
 * 实现类有
 * [ErrorView]，
 * [NetChangeView] ,
 * [ReplayView] ,
 * [ControlView],
 * [GuideView] ,
 * [QualityView],
 * [SpeedView] ,
 * [TipsView],
 * [AliyunVodPlayerView]
 */
interface ITheme {
    /**
     * 设置主题
     *
     * @param theme 支持的主题
     */
    fun setTheme(theme: AliyunVodPlayerView.Theme)
}