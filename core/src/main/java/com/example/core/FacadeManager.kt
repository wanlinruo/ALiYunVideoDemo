package com.example.core

import com.example.common.bo.VodInfoBO
import com.example.common.http.HttpManager
import com.example.upload.UploadManager

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
        HttpManager.initHttp()
    }

    fun getUploadAuth(vodInfoBO: VodInfoBO) {
        UploadManager.getUploadAuth(vodInfoBO)
    }

    fun startUpload() {
        UploadManager.startUpload()
    }
}