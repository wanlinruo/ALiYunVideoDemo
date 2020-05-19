package com.example.player.view.tipsview

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.aliyun.player.bean.ErrorCode
import com.aliyun.utils.VcPlayerLog
import com.example.player.theme.ITheme
import com.example.player.view.tipsview.ErrorView.OnRetryClickListener
import com.example.player.view.tipsview.NetChangeView.OnNetChangeClickListener
import com.example.player.view.tipsview.ReplayView.OnReplayClickListener
import com.example.player.widget.AliyunVodPlayerView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 提示对话框的管理器。
 * 用于管理[ErrorView] ，[LoadingView] ，[NetChangeView] , [ReplayView]等view的显示/隐藏等。
 */
class TipsView : RelativeLayout, ITheme {
    //错误码
    private var mErrorCode = 0

    //错误提示
    private var mErrorView: ErrorView? = null

    //重试提示
    private var mReplayView: ReplayView? = null

    //缓冲加载提示
    private var mNetLoadingView: LoadingView? = null

    //网络变化提示
    private var mNetChangeView: NetChangeView? = null

    //网络请求加载提示
    private var mBufferLoadingView: LoadingView? = null

    //提示点击事件
    private var mOnTipClickListener: OnTipClickListener? = null

    //当前的主题
    private var mCurrentTheme: AliyunVodPlayerView.Theme? = null

    //网络变化监听事件。
    private val onNetChangeClickListener: OnNetChangeClickListener =
        object : OnNetChangeClickListener {
            override fun onContinuePlay() {
                if (mOnTipClickListener != null) {
                    mOnTipClickListener!!.onContinuePlay()
                }
            }

            override fun onStopPlay() {
                if (mOnTipClickListener != null) {
                    mOnTipClickListener!!.onStopPlay()
                }
            }
        }

    //错误提示的重试点击事件
    private val onRetryClickListener: OnRetryClickListener = object : OnRetryClickListener {
        override fun onRetryClick() {
            if (mOnTipClickListener != null) {
                //鉴权过期
                if (mErrorCode == ErrorCode.ERROR_SERVER_POP_UNKNOWN.value) {
                    mOnTipClickListener!!.onRefreshSts()
                } else {
                    mOnTipClickListener!!.onRetryPlay()
                }
            }
        }
    }

    //重播点击事件
    private val onReplayClickListener: OnReplayClickListener = object : OnReplayClickListener {
        override fun onReplay() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener!!.onReplay()
            }
        }
    }

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    /**
     * 显示网络变化提示
     */
    fun showNetChangeTipView() {
        if (mNetChangeView == null) {
            mNetChangeView = NetChangeView(context)
            mNetChangeView!!.setOnNetChangeClickListener(onNetChangeClickListener)
            addSubView(mNetChangeView)
        }
        if (mErrorView != null && mErrorView!!.visibility == View.VISIBLE) {
            //显示错误对话框了，那么网络切换的对话框就不显示了。
            //都出错了，还显示网络切换，没有意义
        } else {
            mNetChangeView!!.visibility = View.VISIBLE
        }
    }

    /**
     * 显示错误提示
     *
     * @param errorCode  错误码
     * @param errorEvent 错误事件
     * @param errorMsg   错误消息
     */
    fun showErrorTipView(
        errorCode: Int,
        errorEvent: String?,
        errorMsg: String?
    ) {
        if (mErrorView == null) {
            mErrorView = ErrorView(context)
            mErrorView!!.setOnRetryClickListener(onRetryClickListener)
            addSubView(mErrorView)
        }

        //出现错误了，先把网络的对话框关闭掉。防止同时显示多个对话框。
        //都出错了，还显示网络切换，没有意义
        hideNetChangeTipView()
        mErrorCode = errorCode
        mErrorView!!.updateTips(errorCode, errorEvent!!, errorMsg)
        mErrorView!!.visibility = View.VISIBLE
        Log.d(TAG, " errorCode = $mErrorCode")
    }

    /**
     * 显示错误提示,不显示错误码
     *
     * @param msg 错误信息
     */
    fun showErrorTipViewWithoutCode(msg: String?) {
        if (mErrorView == null) {
            mErrorView = ErrorView(context)
            mErrorView!!.updateTipsWithoutCode(msg)
            mErrorView!!.setOnRetryClickListener(onRetryClickListener)
            addSubView(mErrorView)
        }
        if (mErrorView!!.visibility != View.VISIBLE) {
            mErrorView!!.visibility = View.VISIBLE
        }
    }

    /**
     * 显示重播view
     */
    fun showReplayTipView() {
        if (mReplayView == null) {
            mReplayView = ReplayView(context)
            mReplayView!!.setOnReplayClickListener(onReplayClickListener)
            addSubView(mReplayView)
        }
        if (mReplayView!!.visibility != View.VISIBLE) {
            mReplayView!!.visibility = View.VISIBLE
        }
    }

    /**
     * 显示缓冲加载view
     */
    fun showBufferLoadingTipView() {
        if (mBufferLoadingView == null) {
            mBufferLoadingView = LoadingView(context)
            addSubView(mBufferLoadingView)
        }
        if (mBufferLoadingView!!.visibility != View.VISIBLE) {
            mBufferLoadingView!!.visibility = View.VISIBLE
        }
    }

    /**
     * 更新缓冲加载的进度
     *
     * @param percent 进度百分比
     */
    fun updateLoadingPercent(percent: Int) {
        showBufferLoadingTipView()
        mBufferLoadingView!!.updateLoadingPercent(percent)
    }

    /**
     * 显示网络加载view
     */
    fun showNetLoadingTipView() {
        if (mNetLoadingView == null) {
            mNetLoadingView = LoadingView(context)
            mNetLoadingView!!.setOnlyLoading()
            addSubView(mNetLoadingView)
        }
        if (mNetLoadingView!!.visibility != View.VISIBLE) {
            mNetLoadingView!!.visibility = View.VISIBLE
        }
    }

    /**
     * 把新增的view添加进来，居中添加
     *
     * @param subView 子view
     */
    fun addSubView(subView: View?) {
        val params = LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT)
        addView(subView, params)

        //同时需要更新新加的view的主题
        if (subView is ITheme) {
            (subView as ITheme).setTheme(mCurrentTheme!!)
        }
    }

    /**
     * 隐藏所有的tip
     */
    fun hideAll() {
        hideNetChangeTipView()
        hideErrorTipView()
        hideReplayTipView()
        hideBufferLoadingTipView()
        hideNetLoadingTipView()
    }

    /**
     * 隐藏缓冲加载的tip
     */
    fun hideBufferLoadingTipView() {
        if (mBufferLoadingView != null && mBufferLoadingView!!.visibility == View.VISIBLE) {
            /*
                隐藏loading时,重置百分比,避免loading再次展示时,loading进度不是从0开始
             */
            mBufferLoadingView!!.updateLoadingPercent(0)
            mBufferLoadingView!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 隐藏网络加载的tip
     */
    fun hideNetLoadingTipView() {
        if (mNetLoadingView != null && mNetLoadingView!!.visibility == View.VISIBLE) {
            mNetLoadingView!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 隐藏重播的tip
     */
    fun hideReplayTipView() {
        if (mReplayView != null && mReplayView!!.visibility == View.VISIBLE) {
            mReplayView!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 隐藏网络变化的tip
     */
    fun hideNetChangeTipView() {
        if (mNetChangeView != null && mNetChangeView!!.visibility == View.VISIBLE) {
            mNetChangeView!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 隐藏错误的tip
     */
    fun hideErrorTipView() {
        if (mErrorView != null && mErrorView!!.visibility == View.VISIBLE) {
            mErrorView!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 错误的tip是否在显示，如果在显示的话，其他的tip就不提示了。
     *
     * @return true：是
     */
    val isErrorShow: Boolean
        get() = if (mErrorView != null) {
            mErrorView!!.visibility == View.VISIBLE
        } else {
            false
        }

    /**
     * 隐藏网络错误tip
     */
    fun hideNetErrorTipView() {
        VcPlayerLog.d(TAG, " hideNetErrorTipView errorCode = $mErrorCode")
        //TODO
//        if (mErrorView != null && mErrorView.getVisibility() == VISIBLE
//                && mErrorCode == AliyunErrorCode.ALIVC_ERROR_LOADING_TIMEOUT.getCode()) {
//            mErrorView.setVisibility(INVISIBLE);
//        }
    }

    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        mCurrentTheme = theme
        //判断子view是不是实现了ITheme的接口，从而达到更新主题的目的
        val childCount = childCount
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is ITheme) {
                (child as ITheme).setTheme(theme)
            }
        }
    }

    /**
     * 提示view中的点击操作
     */
    interface OnTipClickListener {
        /**
         * 继续播放
         */
        fun onContinuePlay()

        /**
         * 停止播放
         */
        fun onStopPlay()

        /**
         * 重试播放
         */
        fun onRetryPlay()

        /**
         * 重播
         */
        fun onReplay()

        /**
         * 刷新sts
         */
        fun onRefreshSts()
    }

    /**
     * 设置提示view中的点击操作 监听
     *
     * @param l 监听事件
     */
    fun setOnTipClickListener(l: OnTipClickListener?) {
        mOnTipClickListener = l
    }

    companion object {
        private val TAG = TipsView::class.java.simpleName
    }
}