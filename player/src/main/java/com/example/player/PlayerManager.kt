package com.example.player

import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.utils.VcPlayerLog
import com.example.common.extend.ContextExtend

/**
 * <pre>
 *     author : wanlinruo
 *     time   : 2020/05/06/23:27
 *     desc   :
 *     version: 1.0
 * </pre>
 */
object PlayerManager {

    const val TAG = "PlayerManager"

    private val aLiPlayer = AliPlayerFactory.createAliPlayer(ContextExtend.appContext)

    init {
        initListener()
    }

    private fun initListener() {
        //播放完成事件
        aLiPlayer.setOnCompletionListener {
            VcPlayerLog.v(TAG, "播放完成事件")
        }

        //出错事件
        aLiPlayer.setOnErrorListener {
            VcPlayerLog.v(TAG, "出错事件")
        }

        //准备成功事件
        aLiPlayer.setOnPreparedListener {
            VcPlayerLog.v(TAG, "准备成功事件")
        }

        //视频分辨率变化回调
        aLiPlayer.setOnVideoSizeChangedListener { width, height ->
            VcPlayerLog.v(TAG, "视频分辨率变化回调-width=$width,height=$height")
        }

        //首帧渲染显示事件
        aLiPlayer.setOnRenderingStartListener {
            VcPlayerLog.v(TAG, "首帧渲染显示事件")
        }

        //其他信息的事件，type包括了：循环播放开始，缓冲位置，当前播放位置，自动播放开始等
        aLiPlayer.setOnInfoListener {
            VcPlayerLog.v(TAG, "其他信息的事件-InfoBean=$it")
        }

        //缓存事件
        aLiPlayer.setOnLoadingStatusListener(object : IPlayer.OnLoadingStatusListener {

            //缓冲开始
            override fun onLoadingBegin() {
                VcPlayerLog.v(TAG, "缓存事件-缓冲开始")
            }

            //缓冲进度
            override fun onLoadingProgress(percent: Int, kbps: Float) {
                VcPlayerLog.v(TAG, "缓存事件-缓冲进度-percent=$percent,kbps=$kbps")
            }

            //缓冲结束
            override fun onLoadingEnd() {
                VcPlayerLog.v(TAG, "缓存事件-缓冲结束")
            }
        })

        //拖动结束
        aLiPlayer.setOnSeekCompleteListener {
            VcPlayerLog.v(TAG, "拖动结束")
        }

        //字幕事件
        aLiPlayer.setOnSubtitleDisplayListener(object : IPlayer.OnSubtitleDisplayListener {

            //显示字幕
            override fun onSubtitleShow(id: Long, data: String?) {
                VcPlayerLog.v(TAG, "显示字幕-id=$id,data=$data")
            }

            //隐藏字幕
            override fun onSubtitleHide(id: Long) {
                VcPlayerLog.v(TAG, "隐藏字幕-id=$id")
            }
        })

        //切换音视频流或者清晰度
        aLiPlayer.setOnTrackChangedListener(object : IPlayer.OnTrackChangedListener {

            //切换音视频流或者清晰度成功
            override fun onChangedSuccess(trackInfo: TrackInfo?) {
                VcPlayerLog.v(TAG, "切换音视频流或者清晰度成功-trackInfo=$trackInfo")
            }

            //切换音视频流或者清晰度失败
            override fun onChangedFail(trackInfo: TrackInfo?, errorInfo: ErrorInfo?) {
                VcPlayerLog.v(TAG, "切换音视频流或者清晰度失败-trackInfo=$trackInfo，errorInfo=$errorInfo")
            }
        })

        //播放器状态改变事件
        aLiPlayer.setOnStateChangedListener {
            VcPlayerLog.v(TAG, "播放器状态改变事件-newState=$it")
        }

        //截图事件
        aLiPlayer.setOnSnapShotListener { bitmap, with, height ->
            VcPlayerLog.v(TAG, "截图事件-bitmap=$bitmap,with=$with,height=$height")
        }
    }

    private fun initConfig() {

    }

}