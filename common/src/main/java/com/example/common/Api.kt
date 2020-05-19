package com.example.common

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/07/19:14
 *     desc   :
 *     version: 1.0
 * </pre>
 */
interface Api {

    companion object {

        const val baseApi = "http://192.168.97.6:9005/"

        //获取上传凭证信息
        const val getVideoIdAddr = "content/vodInfo/getVideoIdAddr"

        //保存视频上传信息接口
        const val saveUpload = "/content/vodInfo/saveUpload"
    }
}