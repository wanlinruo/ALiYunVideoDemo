package com.example.player.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 网络连接状态的监听器。通过注册broadcast实现的
 */
class NetWatchdog(context: Context) {

    private val mContext: Context = context.applicationContext

    //网络变化监听
    private var mNetChangeListener: NetChangeListener? = null
    private var mNetConnectedListener: NetConnectedListener? =
        null

    //广播过滤器，监听网络变化
    private val mNetIntentFilter = IntentFilter()

    /**
     * 网络变化监听事件
     */
    interface NetChangeListener {
        /**
         * wifi变为4G
         */
        fun onWifiTo4G()

        /**
         * 4G变为wifi
         */
        fun on4GToWifi()

        /**
         * 网络断开
         */
        fun onNetDisconnected()
    }

    private var isReconnect = false

    /**
     * 判断是否有网络的监听
     */
    interface NetConnectedListener {
        /**
         * 网络已连接
         */
        fun onReNetConnected(isReconnect: Boolean)

        /**
         * 网络未连接
         */
        fun onNetUnConnected()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            //获取手机的连接服务管理器，这里是连接管理器类
            val cm =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            val activeNetworkInfo = cm.activeNetworkInfo
            var wifiState = NetworkInfo.State.UNKNOWN
            var mobileState = NetworkInfo.State.UNKNOWN
            if (wifiNetworkInfo != null) {
                wifiState = wifiNetworkInfo.state
            }
            if (mobileNetworkInfo != null) {
                mobileState = mobileNetworkInfo.state
            }

//            if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
            if (activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting) {
                if (mNetConnectedListener != null) {
                    mNetConnectedListener!!.onReNetConnected(isReconnect)
                    isReconnect = false
                }
            } else if (activeNetworkInfo == null) {
                if (mNetConnectedListener != null) {
                    isReconnect = true
                    mNetConnectedListener!!.onNetUnConnected()
                }
            }
            if (NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED == mobileState) {
                Log.d(
                    TAG,
                    "onWifiTo4G()"
                )
                if (mNetChangeListener != null) {
                    mNetChangeListener!!.onWifiTo4G()
                }
            } else if (NetworkInfo.State.CONNECTED == wifiState && NetworkInfo.State.CONNECTED != mobileState) {
                if (mNetChangeListener != null) {
                    mNetChangeListener!!.on4GToWifi()
                }
            } else if (NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
                if (mNetChangeListener != null) {
                    mNetChangeListener!!.onNetDisconnected()
                }
            }
        }
    }

    /**
     * 设置网络变化监听
     *
     * @param l 监听事件
     */
    fun setNetChangeListener(l: NetChangeListener) {
        mNetChangeListener = l
    }

    fun setNetConnectedListener(mNetConnectedListener: NetConnectedListener) {
        this.mNetConnectedListener = mNetConnectedListener
    }

    /**
     * 开始监听
     */
    fun startWatch() {
        try {
            mContext.registerReceiver(mReceiver, mNetIntentFilter)
        } catch (e: Exception) {
        }
    }

    /**
     * 结束监听
     */
    fun stopWatch() {
        try {
            mContext.unregisterReceiver(mReceiver)
        } catch (e: Exception) {
        }
    }

    companion object {
        private val TAG =
            NetWatchdog::class.java.simpleName

        /**
         * 静态方法获取是否有网络连接
         *
         * @param context 上下文
         * @return 是否连接
         */
        fun hasNet(context: Context): Boolean {
            //获取手机的连接服务管理器，这里是连接管理器类
            val cm = context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            val activeNetworkInfo = cm.activeNetworkInfo
            var wifiState = NetworkInfo.State.UNKNOWN
            var mobileState = NetworkInfo.State.UNKNOWN
            if (wifiNetworkInfo != null) {
                wifiState = wifiNetworkInfo.state
            }
            if (mobileNetworkInfo != null) {
                mobileState = mobileNetworkInfo.state
            }
            if (NetworkInfo.State.CONNECTED != wifiState && NetworkInfo.State.CONNECTED != mobileState) {
                return false
            }
            return if (activeNetworkInfo == null || !activeNetworkInfo.isConnectedOrConnecting) {
                false
            } else true
        }

        /**
         * 静态判断是不是4G网络
         *
         * @param context 上下文
         * @return 是否是4G
         */
        fun is4GConnected(context: Context): Boolean {
            //获取手机的连接服务管理器，这里是连接管理器类
            val cm = context.applicationContext
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobileNetworkInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            var mobileState = NetworkInfo.State.UNKNOWN
            if (mobileNetworkInfo != null) {
                mobileState = mobileNetworkInfo.state
            }
            return NetworkInfo.State.CONNECTED == mobileState
        }
    }

    init {
        mNetIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION)
    }
}