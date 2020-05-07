package com.example.core.api

import leavesc.reactivehttp.core.bean.IHttpResBean

/**
 * 作者：CZY
 * 时间：2020/4/29 18:13
 * 描述：
 */
data class HttpResBean<T>(val status: Int, val msg: String, val data: T) : IHttpResBean<T> {

    override val httpCode: Int
        get() = status

    override val httpMsg: String
        get() = msg

    override val httpData: T
        get() = data

    override val httpIsSuccess: Boolean
        get() = status == 200

}