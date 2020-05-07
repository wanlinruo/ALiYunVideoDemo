package com.example.core.dataSource

import com.example.core.api.ApiService
import com.example.core.getSign
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


    fun requestUserChannelList(callback: RequestCallback<String>) {
//        execute(callback) {
//
//            getService().requestUserChannelList(getSign(null))
//        }
    }
}