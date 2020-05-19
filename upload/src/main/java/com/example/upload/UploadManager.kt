package com.example.upload

import android.util.Log
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.vod.upload.VODUploadCallback
import com.alibaba.sdk.android.vod.upload.VODUploadClientImpl
import com.alibaba.sdk.android.vod.upload.model.UploadFileInfo
import com.alibaba.sdk.android.vod.upload.model.VodInfo
import com.example.common.bo.VodInfoBO
import com.example.common.dto.UploadAuthDTO
import com.example.common.extend.ContextExtend
import com.example.upload.api.UploadDataSource
import leavesc.reactivehttp.core.callback.RequestCallback
import leavesc.reactivehttp.core.exception.BaseException


/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/23:28
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object UploadManager {

    //绑定
    private val dataSource = UploadDataSource()

    private var mVodInfoBO: VodInfoBO? = null

    private val uploader = VODUploadClientImpl(ContextExtend.appContext)

    init {
        //开启日志
        if (BuildConfig.DEBUG)
            OSSLog.enableLog()

        //初始化
        uploader.init(object : VODUploadCallback() {
            /**
             * 上传完成回调
             *
             * @param info 上传文件信息
             */
            override fun onUploadSucceed(info: UploadFileInfo) {
//                dataSource.saveUpload()
                OSSLog.logDebug("onsucceed ------------------" + info.filePath)
            }

            /**
             * 上传失败回调
             *
             * @param info 上传文件信息
             * @param code 错误码
             * @param message 错误描述
             */
            override fun onUploadFailed(info: UploadFileInfo, code: String, message: String) {
                OSSLog.logError("onfailed ------------------ " + info.filePath + " " + code + " " + message)
            }

            /**
             * 上传进度回调
             *
             * @param info 上传文件信息
             * @param uploadedSize 已上传大小
             * @param totalSize 总大小
             */
            override fun onUploadProgress(
                info: UploadFileInfo,
                uploadedSize: Long,
                totalSize: Long
            ) {
                OSSLog.logDebug("onProgress ------------------ " + info.filePath + " " + uploadedSize + " " + totalSize)
            }

            /**
             * token过期回调
             * 上传地址和凭证方式上传需要调用resumeWithAuth方法继续上传
             * STS方式上传需要调用resumeWithToken方法继续上传
             */
            override fun onUploadTokenExpired() {
                OSSLog.logError("onExpired ------------- ")
                // 重新刷新上传凭证:RefreshUploadVideo
//            uploadAuth = "此处需要设置重新刷新凭证之后的值";
//            uploader.resumeWithAuth(uploadAuth);
            }

            /**
             * 上传开始重试回调
             *
             * @param code 错误码
             * @param message 错误描述
             */
            override fun onUploadRetry(code: String?, message: String?) {
                OSSLog.logError("onUploadRetry ------------- ")
            }

            /**
             * 上传结束重试，继续上传回调
             */
            override fun onUploadRetryResume() {
                OSSLog.logError("onUploadRetryResume ------------- ")
            }

            /**
             * 开始上传回调
             * 上传地址和凭证方式上传需要调用setUploadAuthAndAddress:uploadAuth:uploadAddress:方法设置上传地址和凭证
             *
             * @param uploadFileInfo 上传文件信息
             */
            override fun onUploadStarted(uploadFileInfo: UploadFileInfo?) {
                //本地获取上传凭证
                mVodInfoBO?.let {
                    uploader.setUploadAuthAndAddress(
                        uploadFileInfo,
                        it.uploadAuth,
                        it.uploadAddress
                    )
                }
            }
        })
    }

    /**
     * 获取上传凭证
     */
    fun getUploadAuth(vodInfoBO: VodInfoBO) {
        dataSource.getUploadAuth(object : RequestCallback<UploadAuthDTO> {
            override fun onSuccess(data: UploadAuthDTO) {
                Log.d("haha", "data$data")
                //更新变量
                vodInfoBO.uploadAuth = data.uploadAuth
                vodInfoBO.uploadAddress = data.uploadAddress
                //存储到本地
                mVodInfoBO = vodInfoBO
                //添加到上传列表
                addFile()
            }

            override fun onFail(exception: BaseException) {
                Log.d("haha", "exception$exception")
            }
        })
    }

    /**
     * 获取上传列表
     */
    fun getUploadList() {
        uploader.listFiles()
    }


    /**
     * 添加文件
     */
    fun addFile() {
        mVodInfoBO?.let {
            val vodInfo = VodInfo()
            vodInfo.title = it.title
            vodInfo.desc = it.desc
            uploader.addFile(it.filePath, vodInfo)
        }
    }

    /**
     * 上传控制-开始上传
     */
    fun startUpload() {
        uploader.start()
    }

    /**
     * 上传控制-暂停上传
     */
    fun stopUpload() {
        uploader.stop()
    }

    /**
     * 上传控制-恢复上传
     */
    fun resumeUpload() {
        uploader.resume()
    }
}