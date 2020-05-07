package com.example.core.http.dataSource

import com.example.common.extend.getSign
import com.example.core.http.api.ApiService
import com.example.core.http.vo.UploadAuthDTO
import leavesc.reactivehttp.core.callback.RequestCallback
import leavesc.reactivehttp.core.datasource.RemoteDataSource
import leavesc.reactivehttp.core.viewmodel.IUIActionEvent

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/19:12
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class CoreDataSource(baseViewModelEvent: IUIActionEvent? = null) :
    RemoteDataSource<ApiService>(baseViewModelEvent, ApiService::class.java) {


    fun getUploadAuth(callback: RequestCallback<UploadAuthDTO>) {
        execute(callback) {
            getService().getUploadAuth(getSign(null))
        }
    }
}