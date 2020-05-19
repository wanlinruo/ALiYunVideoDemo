package com.example.upload.api

import com.example.common.Api
import com.example.common.http.HttpResBean
import com.example.common.dto.UploadAuthDTO
import retrofit2.http.*

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
    suspend fun getUploadAuth(@HeaderMap signMap: Map<String, Any>): HttpResBean<UploadAuthDTO>


    //获取上传凭证信息
    @JvmSuppressWildcards
    @POST(Api.saveUpload)
    @FormUrlEncoded
    suspend fun saveUpload(
        @HeaderMap signMap: Map<String, Any>,
        @FieldMap paramsMap: Map<String, Any>
    ): HttpResBean<Any>
}