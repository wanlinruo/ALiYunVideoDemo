package com.example.common.bo

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/09/11:20
 *     desc   : > 注意：支持的文件大小<=4G。
 *     version: 1.0
 * </pre>
 */
data class VodInfoBO(
    //文件地址
    val filePath: String,
    //文件名称
    val title: String,
    //文件描述
    val desc: String
) {
    //上传凭证
    var uploadAuth: String = ""

    //上传地址
    var uploadAddress: String = ""
}