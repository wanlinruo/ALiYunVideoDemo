package com.example.core

import android.util.Log
import com.example.core.http.HttpManager
import com.example.core.http.dataSource.CoreDataSource
import com.example.core.http.vo.UploadAuthDTO
import com.example.player.PlayerManager
import com.example.upload.UploadManager
import leavesc.reactivehttp.core.callback.RequestCallback
import leavesc.reactivehttp.core.exception.BaseException
import java.util.logging.Logger

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/23:25
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object FacadeManager {

    private val dataSource = CoreDataSource()

    init {
        HttpManager.initHttp()
        PlayerManager.initPlayer()
        UploadManager.initUpload()
    }


    fun getUploadAuth() {
        dataSource.getUploadAuth(object : RequestCallback<UploadAuthDTO> {
            override fun onSuccess(data: UploadAuthDTO) {
                Log.d("haha", "data$data")

            }

            override fun onFail(exception: BaseException) {
                Log.d("haha", "exception$exception")
            }
        })
    }
}