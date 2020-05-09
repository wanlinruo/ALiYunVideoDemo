package com.example.common.http

import com.example.common.Api
import com.example.common.extend.ContextExtend
import leavesc.reactivehttp.core.ReactiveHttp

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/22:57
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object HttpManager {

    fun initHttp() {
        //初始化 Http 相关配置
        ReactiveHttp.Builder(ContextExtend.appContext, Api.baseApi).build().init()
    }
}