package com.example.aliyunvideodemo

import android.app.Application
import com.example.common.extend.ContextExtend
import com.example.common.kv.FinalKV
import com.example.core.FacadeManager

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/22:31
 *     desc   :
 *     version: 1.0
 * </pre>
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //全局使用
        ContextExtend.appContext = this
//        KVHolderKt.init()
    }
}