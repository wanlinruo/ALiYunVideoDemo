package com.example.upload.api

import com.example.common.dto.UploadAuthDTO
import com.example.common.extend.getSign
import leavesc.reactivehttp.core.callback.RequestCallback
import leavesc.reactivehttp.core.datasource.RemoteDataSource
import leavesc.reactivehttp.core.viewmodel.IUIActionEvent

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/09/18:16
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class UploadDataSource(baseViewModelEvent: IUIActionEvent? = null) :
    RemoteDataSource<ApiService>(baseViewModelEvent, ApiService::class.java) {

    fun getUploadAuth(callback: RequestCallback<UploadAuthDTO>) {
        execute(callback) {
            getService().getUploadAuth(getSign(null))
        }
    }

    fun saveUpload(videoId: String, callback: RequestCallback<Any>) {
        execute(callback) {
            val map = mapOf("videoId" to videoId)
            getService().saveUpload(getSign(map), map)
        }
    }
}