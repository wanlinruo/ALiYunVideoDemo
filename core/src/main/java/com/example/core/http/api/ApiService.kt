package com.example.core.http.api

import com.example.common.Api
import com.example.core.http.HttpResBean
import com.example.core.http.vo.UploadAuthDTO
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/19:12
 *     desc   :
 *     version: 1.0
 * </pre>
 */
interface ApiService {

    //获取上传凭证信息
    @JvmSuppressWildcards
    @GET(Api.getVideoIdAddr)
    suspend fun getUploadAuth(@HeaderMap signMap: Map<String, Any>, @QueryMap map: Map<String, Any>): HttpResBean<UploadAuthDTO>
}