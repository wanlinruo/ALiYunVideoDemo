package com.example.core

import com.example.common.extend.ContextExtend
import com.example.player.PlayerManager
import com.example.upload.UploadManager
import leavesc.reactivehttp.core.ReactiveHttp

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/23:25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object FacadeManager {

    init {
        //初始化 Http 相关配置
        ReactiveHttp.Builder(ContextExtend.appContext, "http://192.168.97.6/")
    }

    fun initAuth() {

    }

    fun init() {
        PlayerManager.initPlayer()
        UploadManager.initUpload()
    }
}