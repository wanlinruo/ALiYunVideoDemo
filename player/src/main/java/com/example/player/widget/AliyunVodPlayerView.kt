package com.example.player.widget

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import com.aliyun.player.AliPlayer
import com.aliyun.player.AliPlayerFactory
import com.aliyun.player.IPlayer
import com.aliyun.player.IPlayer.*
import com.aliyun.player.bean.ErrorInfo
import com.aliyun.player.bean.InfoBean
import com.aliyun.player.bean.InfoCode
import com.aliyun.player.nativeclass.MediaInfo
import com.aliyun.player.nativeclass.PlayerConfig
import com.aliyun.player.nativeclass.Thumbnail
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.player.source.UrlSource
import com.aliyun.player.source.VidAuth
import com.aliyun.player.source.VidSts
import com.aliyun.thumbnail.ThumbnailBitmapInfo
import com.aliyun.thumbnail.ThumbnailHelper
import com.aliyun.thumbnail.ThumbnailHelper.OnThumbnailGetListener
import com.aliyun.utils.VcPlayerLog
import com.example.player.R
import com.example.player.constants.PlayParameter
import com.example.player.listener.*
import com.example.player.quality.QualityView
import com.example.player.theme.ITheme
import com.example.player.util.*
import com.example.player.view.control.ControlView
import com.example.player.view.gesture.GestureDialogManager
import com.example.player.view.gesture.GestureView
import com.example.player.view.guide.GuideView
import com.example.player.view.interfaces.ViewAction
import com.example.player.view.speed.SpeedView
import com.example.player.view.thumbnail.ThumbnailView
import com.example.player.view.tipsview.TipsView
import java.lang.ref.WeakReference
import java.util.*

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * UI播放器的主要实现类。 通过ITheme控制各个界面的主题色。 通过各种view的组合实现UI的界面。这些view包括： 用户手势操作的[GestureView] 控制播放，显示信息的[ ] 显示清晰度列表的[QualityView] 倍速选择界面[SpeedView] 用户使用引导页面[GuideView] 用户提示页面[TipsView]
 * 以及封面等。 view 的初始化是在[.initVideoView]方法中实现的。 然后是对各个view添加监听方法，处理对应的操作，从而实现与播放器的共同操作
 */
class AliyunVodPlayerView : RelativeLayout, ITheme {
    /**
     * 判断VodePlayer 是否加载完成
     */
    private val hasLoadEnd: MutableMap<MediaInfo?, Boolean> = HashMap()

    /**
     * 获取播放surfaceView
     *
     * @return 播放surfaceView
     */
    //视频画面
    private var playerView: SurfaceView = SurfaceView(context.applicationContext)

    //手势操作view
    private var mGestureView: GestureView = GestureView(context)

    //皮肤view
    private var mControlView: ControlView = ControlView(context)

    //清晰度view
    private var mQualityView: QualityView = QualityView(context)

    //倍速选择view
    private var mSpeedView: SpeedView = SpeedView(context)

    //引导页view
    private var mGuideView: GuideView = GuideView(context)

    //封面view
    private var mCoverView: ImageView = ImageView(context)

    /**
     * 缩略图View
     */
    private var mThumbnailView: ThumbnailView = ThumbnailView(context)

    //播放器
    private var mAliyunVodPlayer: AliPlayer =
        AliPlayerFactory.createAliPlayer(context.applicationContext)

    //手势对话框控制
    private var mGestureDialogManager: GestureDialogManager? =
        if (context is Activity)
            GestureDialogManager(context as Activity)
        else
            null

    //网络状态监听
    private var mNetWatchdog: NetWatchdog = NetWatchdog(context)

    //屏幕方向监听
    private var mOrientationWatchDog: OrientationWatchDog = OrientationWatchDog(context)

    //Tips view
    private var mTipsView: TipsView = TipsView(context)

    //锁定竖屏
    private var mLockPortraitListener: LockPortraitListener? = null

    //是否锁定全屏
    private var mIsFullScreenLocked = false

    //当前屏幕模式
    private var mCurrentScreenMode: AliyunScreenMode = AliyunScreenMode.Small

    //是不是在seek中
    private var inSeek = false

    //播放是否完成
    private var isCompleted = false

    /**
     * 获取当前播放器正在播放的媒体信息
     */
    //媒体信息
    private var currentMediaInfo: MediaInfo? = null

    //解决bug,进入播放界面快速切换到其他界面,播放器仍然播放视频问题
    private val vodPlayerLoadEndHandler = VodPlayerLoadEndHandler(this)

    //原视频的buffered
    private var mVideoBufferedPosition: Long = 0

    //原视频的currentPosition
    private var mCurrentPosition: Long = 0

    /**
     * 获取播放器状态
     *
     * @return 播放器状态
     */
    //当前播放器的状态 默认为idle状态
    var playerState = idle

    //原视频时长
    private var mSourceDuration: Long = 0

    //目前支持的几种播放方式
    private var mAliyunPlayAuth: VidAuth? = null
    private var mAliyunLocalSource: UrlSource? = null
    private var mAliyunVidSts: VidSts? = null

    //对外的各种事件监听
    private var mOutInfoListener: OnInfoListener? = null
    private var mOutErrorListener: OnErrorListener? = null

    //    private IPlayer.OnRePlayListener mOutRePlayListener = null;
    private var mOutAutoPlayListener: OnAutoPlayListener? = null
    private var mOutPreparedListener: OnPreparedListener? = null
    private var mOutCompletionListener: OnCompletionListener? = null
    private var mOuterSeekCompleteListener: OnSeekCompleteListener? = null
    private var mOutChangeQualityListener: OnChangeQualityListener? = null
    private var mOutFirstFrameStartListener: OnRenderingStartListener? = null
    private val mOnScreenCostingSingleTagListener: OnScreenCostingSingleTagListener? = null
    private var mOnScreenBrightnessListener: OnScreenBrightnessListener? = null
    private var mOutTimeExpiredErrorListener: OnTimeExpiredErrorListener? = null

    //    private IPlayer.OnUrlTimeExpiredListener mOutUrlTimeExpiredListener = null;
    //对外view点击事件监听
    private var mOnPlayerViewClickListener: OnPlayerViewClickListener? = null

    // 连网断网监听
    private var mNetConnectedListener: NetConnectedListener? = null

    // 横屏状态点击更多
    private var mOutOnShowMoreClickListener: ControlView.OnShowMoreClickListener? = null

    //播放按钮点击监听
    private var onPlayStateBtnClickListener: OnPlayStateBtnClickListener? = null

    //停止按钮监听
    private var mOnStoppedListener: OnStoppedListener? = null

    /**
     * 对外SEI消息通知
     */
    private var mOutSeiDataListener: IPlayer.OnSeiDataListener? = null

    /**
     * 设置当前屏幕亮度
     */
    //当前屏幕亮度
    var screenBrightness = 0

    /**
     * 缩略图帮助类
     */
    private var mThumbnailHelper: ThumbnailHelper? = null

    //获取缩略图是否成功
    private var mThumbnailPrepareSuccess = false
    var currentSpeed = 0f
    private var currentVolume = 0f
    private val currentScreenBrigtness = 0

    constructor(context: Context?) : super(context) {
        initVideoView()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs

    ) {
        initVideoView()

    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        initVideoView()
    }

    /**
     * 初始化view
     */
    private fun initVideoView() {
        //初始化播放用的surfaceView
        initSurfaceView()
        //初始化播放器
        initAliVcPlayer()
        //初始化封面
        initCoverView()
        //初始化手势view
        initGestureView()
        //初始化控制栏
        initControlView()
        //初始化清晰度view
        initQualityView()
        //初始化缩略图
        initThumbnailView()
        //初始化倍速view
        initSpeedView()
        //初始化指引view
        initGuideView()
        //初始化提示view
        initTipsView()
        //初始化网络监听器
        initNetWatchdog()
        //初始化屏幕方向监听
        initOrientationWatchdog()
        //初始化手势对话框控制
//        initGestureDialogManager()
        //默认为蓝色主题
        setTheme(Theme.Blue)
        //先隐藏手势和控制栏，防止在没有prepare的时候做操作。
        hideGestureAndControlViews()
    }

    /**
     * 更新UI播放器的主题
     *
     * @param theme 支持的主题
     */
    override fun setTheme(theme: Theme) {
        //通过判断子View是否实现了ITheme的接口，去更新主题
        val childCounts = childCount
        for (i in 0 until childCounts) {
            val view = getChildAt(i)
            if (view is ITheme) {
                (view as ITheme).setTheme(theme)
            }
        }
    }

    /**
     * 切换播放速度
     *
     * @param speedValue 播放速度
     */
    fun changeSpeed(speedValue: SpeedView.SpeedValue) {
        if (speedValue === SpeedView.SpeedValue.Normal) {
            currentSpeed = 1.0f
        } else if (speedValue === SpeedView.SpeedValue.OneQuartern) {
            currentSpeed = 1.25f
        } else if (speedValue === SpeedView.SpeedValue.OneHalf) {
            currentSpeed = 1.5f
        } else if (speedValue === SpeedView.SpeedValue.Twice) {
            currentSpeed = 2.0f
        }
        mAliyunVodPlayer.speed = currentSpeed
    }

    fun setCurrentVolume(progress: Float) {
        currentVolume = progress
        mAliyunVodPlayer.volume = progress
    }

    fun getCurrentVolume(): Float {
        return mAliyunVodPlayer.volume
    }

    fun updateScreenShow() {
        mControlView.updateDownloadBtn()
    }

    /**
     * UI播放器支持的主题
     */
    enum class Theme {
        /**
         * 蓝色主题
         */
        Blue,

        /**
         * 绿色主题
         */
        Green,

        /**
         * 橙色主题
         */
        Orange,

        /**
         * 红色主题
         */
        Red
    }

    /**
     * 隐藏手势和控制栏
     */
    private fun hideGestureAndControlViews() {
        mGestureView.hide(ViewAction.HideType.Normal)
        mControlView.hide(ViewAction.HideType.Normal)
    }

    /**
     * 初始化网络监听
     */
    private fun initNetWatchdog() {
        val context = context
        mNetWatchdog.setNetChangeListener(MyNetChangeListener(this))
        mNetWatchdog.setNetConnectedListener(MyNetConnectedListener(this))
    }

    private fun onWifiTo4G() {
        VcPlayerLog.d(TAG, "onWifiTo4G")

        //如果已经显示错误了，那么就不用显示网络变化的提示了。
        if (mTipsView.isErrorShow) {
            return
        }

        //wifi变成4G，先暂停播放
        if (!isLocalSource) {
            pause()
        }


        //隐藏其他的动作,防止点击界面去进行其他操作
        mGestureView.hide(ViewAction.HideType.Normal)
        mControlView.hide(ViewAction.HideType.Normal)

        //显示网络变化的提示
        if (!isLocalSource) {
            mTipsView.showNetChangeTipView()
        }
    }

    private fun on4GToWifi() {
        VcPlayerLog.d(TAG, "on4GToWifi")
        //如果已经显示错误了，那么就不用显示网络变化的提示了。
        if (mTipsView.isErrorShow) {
            return
        }

        //隐藏网络变化的提示
        mTipsView.hideNetErrorTipView()
    }

    private fun onNetDisconnected() {
        VcPlayerLog.d(TAG, "onNetDisconnected")
        //网络断开。
        // NOTE： 由于安卓这块网络切换的时候，有时候也会先报断开。所以这个回调是不准确的。
    }

    private class MyNetChangeListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        NetWatchdog.NetChangeListener {

        private val viewWeakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onWifiTo4G() {
            val aliyunVodPlayerView = viewWeakReference.get()
            aliyunVodPlayerView?.onWifiTo4G()
        }

        override fun on4GToWifi() {
            val aliyunVodPlayerView = viewWeakReference.get()
            aliyunVodPlayerView?.on4GToWifi()
        }

        override fun onNetDisconnected() {
            val aliyunVodPlayerView = viewWeakReference.get()
            aliyunVodPlayerView?.onNetDisconnected()
        }

    }

    /**
     * 初始化缩略图
     */
    private fun initThumbnailView() {
        mThumbnailView.visibility = View.GONE
        addSubViewByCenter(mThumbnailView)
    }

    /**
     * 初始化屏幕方向旋转。用来监听屏幕方向。结果通过OrientationListener回调出去。
     */
    private fun initOrientationWatchdog() {
        val context = context
        mOrientationWatchDog.setOnOrientationListener(InnerOrientationListener(this))
    }

    private class InnerOrientationListener(playerView: AliyunVodPlayerView) :
        OrientationWatchDog.OnOrientationListener {

        private val playerViewWeakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(playerView)

        override fun changedToLandForwardScape(fromPort: Boolean) {
            val playerView = playerViewWeakReference.get()
            playerView?.changedToLandForwardScape(fromPort)
        }

        override fun changedToLandReverseScape(fromPort: Boolean) {
            val playerView = playerViewWeakReference.get()
            playerView?.changedToLandReverseScape(fromPort)
        }

        override fun changedToPortrait(fromLand: Boolean) {
            val playerView = playerViewWeakReference.get()
            playerView?.changedToPortrait(fromLand)
        }

    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private fun changedToLandForwardScape(fromPort: Boolean) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return
        }
        changeScreenMode(AliyunScreenMode.Full, false)
        if (orientationChangeListener != null) {
            orientationChangeListener!!.orientationChange(fromPort, mCurrentScreenMode)
        }
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private fun changedToLandReverseScape(fromPort: Boolean) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return
        }
        changeScreenMode(AliyunScreenMode.Full, true)
        if (orientationChangeListener != null) {
            orientationChangeListener!!.orientationChange(fromPort, mCurrentScreenMode)
        }
    }

    /**
     * 屏幕方向变为竖屏
     *
     * @param fromLand 是否从横屏转过来
     */
    private fun changedToPortrait(fromLand: Boolean) {
        //屏幕转为竖屏
        if (mIsFullScreenLocked) {
            return
        }
        if (mCurrentScreenMode === AliyunScreenMode.Full) {
            //全屏情况转到了竖屏
            if (lockPortraitMode == null) {
                //没有固定竖屏，就变化mode
                if (fromLand) {
                    changeScreenMode(AliyunScreenMode.Small, false)
                } else {
                    //如果没有转到过横屏，就不让他转了。防止竖屏的时候点横屏之后，又立即转回来的现象
                }
            } else {
                //固定竖屏了，竖屏还是竖屏，不用动
            }
        } else if (mCurrentScreenMode === AliyunScreenMode.Small) {
            //竖屏的情况转到了竖屏
        }
        if (orientationChangeListener != null) {
            orientationChangeListener!!.orientationChange(fromLand, mCurrentScreenMode)
        }
    }

    /**
     * 初始化提示view
     */
    private fun initTipsView() {
        //设置tip中的点击监听事件
        mTipsView.setOnTipClickListener(object : TipsView.OnTipClickListener {
            override fun onContinuePlay() {
                VcPlayerLog.d(TAG, "playerState = " + playerState)
                //继续播放。如果没有prepare或者stop了，需要重新prepare
                mTipsView.hideAll()
                if (playerState == paused || playerState == prepared) {
                    start()
                } else {
                    if (mAliyunPlayAuth != null) {
                        prepareAuth(mAliyunPlayAuth!!)
                    } else if (mAliyunVidSts != null) {
                        prepareVidsts(mAliyunVidSts)
                    } else if (mAliyunLocalSource != null) {
                        prepareLocalSource(mAliyunLocalSource!!)
                    }
                }
            }

            override fun onStopPlay() {
                // 结束播放
                mTipsView.hideAll()
                stop()
                val context = context
                if (context is Activity) {
                    context.finish()
                }
            }

            override fun onRetryPlay() {
                //重试
                reTry()
            }

            override fun onReplay() {
                //重播
                rePlay()
            }

            override fun onRefreshSts() {
                if (mOutTimeExpiredErrorListener != null) {
                    mOutTimeExpiredErrorListener!!.onTimeExpiredError()
                }
            }
        })
        addSubView(mTipsView)
    }

    /**
     * 重试播放，会从当前位置开始播放
     */
    fun reTry() {
        isCompleted = false
        inSeek = false
        val currentPosition: Int = mControlView.videoPosition
        VcPlayerLog.d(TAG, " currentPosition = $currentPosition")
        mTipsView.hideAll()

        mControlView.reset()
        //防止被reset掉，下次还可以获取到这些值
        mControlView.videoPosition = currentPosition

        mGestureView.reset()

        //显示网络加载的loading。。
        mTipsView.showNetLoadingTipView()

        /*
        isLocalSource()判断不够,有可能是sts播放,也有可能是url播放,还有可能是sd卡的视频播放,
        如果是后两者,需要走if,否则走else
        */
        if (isLocalSource || isUrlSource) {
            mAliyunVodPlayer.setDataSource(mAliyunLocalSource)
            mAliyunVodPlayer.prepare()
        } else {
            mAliyunVodPlayer.setDataSource(mAliyunVidSts)
            mAliyunVodPlayer.prepare()
        }
        isAutoAccurate(currentPosition)
    }

    /**
     * 重播，将会从头开始播放
     */
    fun rePlay() {
        isCompleted = false
        inSeek = false
        mTipsView.hideAll()
        mControlView.reset()
        mGestureView.reset()
        //显示网络加载的loading。。
        mTipsView.showNetLoadingTipView()
        //重播是从头开始播
        mAliyunVodPlayer.prepare()
    }

    /**
     * 重置。包括一些状态值，view的状态等
     */
    private fun reset() {
        isCompleted = false
        inSeek = false
        mCurrentPosition = 0
        mVideoBufferedPosition = 0
        mTipsView.hideAll()
        mControlView.reset()
        mGestureView.reset()
        stop()
    }

    /**
     * 初始化封面
     */
    private fun initCoverView() {
        //这个是为了给自动化测试用的id
        mCoverView.id = R.id.custom_id_min
        addSubView(mCoverView)
    }

    /**
     * 初始化控制栏view
     */
    private fun initControlView() {
        addSubView(mControlView)
        //设置播放按钮点击
        mControlView.setOnPlayStateClickListener(object : ControlView.OnPlayStateClickListener {
            override fun onPlayStateClick() {
                switchPlayerState()
            }
        })
        //设置进度条的seek监听
        mControlView.setOnSeekListener(object : ControlView.OnSeekListener {
            override fun onSeekEnd(position: Int) {
                mControlView.videoPosition = position
                if (isCompleted) {
                    //播放完成了，不能seek了
                    inSeek = false
                } else {
                    //拖动结束后，开始seek
                    seekTo(position)
                    if (onSeekStartListener != null) {
                        onSeekStartListener!!.onSeekStart(position)
                    }
                    hideThumbnailView()
                }
            }

            override fun onSeekStart(position: Int) {
                //拖动开始
                inSeek = true
                if (mThumbnailPrepareSuccess) {
                    showThumbnailView()
                }
            }

            override fun onProgressChanged(progress: Int) {
                requestBitmapByPosition(progress)
            }
        })
        //菜单按钮点击
        mControlView.setOnMenuClickListener(object : ControlView.OnMenuClickListener {
            override fun onMenuClick() {
                //点击之后显示倍速界面
                //根据屏幕模式，显示倍速界面
                mSpeedView.show(mCurrentScreenMode)
            }
        })
        mControlView.setOnDownloadClickListener(object : ControlView.OnDownloadClickListener {
            override fun onDownloadClick() {
                //点击下载之后弹出不同清晰度选择下载dialog
                // 如果当前播放视频时url类型, 不允许下载
                if ("localSource" == PlayParameter.PLAY_PARAM_TYPE) {
                    FixedToastUtils.show(
                        context,
                        resources.getString(R.string.alivc_video_not_support_download)
                    )
                    return
                }
                if (mOnPlayerViewClickListener != null) {
                    mOnPlayerViewClickListener!!.onClick(mCurrentScreenMode, PlayViewType.Download)
                }
            }
        })
        //清晰度按钮点击
        mControlView.setOnQualityBtnClickListener(object : ControlView.OnQualityBtnClickListener {
            override fun onQualityBtnClick(
                v: View,
                qualities: List<TrackInfo>,
                currentQuality: String
            ) {
                //显示清晰度列表
                mQualityView.setQuality(qualities, currentQuality)
                mQualityView.showAtTop(v)
            }

            override fun onHideQualityView() {
                mQualityView.hide()
            }
        })
        //点击锁屏的按钮
        mControlView.setOnScreenLockClickListener(object : ControlView.OnScreenLockClickListener {
            override fun onClick() {
                lockScreen(!mIsFullScreenLocked)
            }
        })
        //点击全屏/小屏按钮
        mControlView.setOnScreenModeClickListener(object : ControlView.OnScreenModeClickListener {
            override fun onClick() {
                val targetMode: AliyunScreenMode =
                    if (mCurrentScreenMode === AliyunScreenMode.Small) {
                        AliyunScreenMode.Full
                    } else {
                        AliyunScreenMode.Small
                    }
                changeScreenMode(targetMode, false)
                if (mCurrentScreenMode === AliyunScreenMode.Full) {
                    mControlView.showMoreButton()
                } else if (mCurrentScreenMode === AliyunScreenMode.Small) {
                    mControlView.hideMoreButton()
                }
            }
        })
        //点击了标题栏的返回按钮
        mControlView.setOnBackClickListener(object : ControlView.OnBackClickListener {
            override fun onClick() {
                if (mCurrentScreenMode === AliyunScreenMode.Full) {
                    //设置为小屏状态
                    changeScreenMode(AliyunScreenMode.Small, false)
                } else if (mCurrentScreenMode === AliyunScreenMode.Small) {
                    //小屏状态下，就结束活动
                    val context = context
                    if (context is Activity) {
                        context.finish()
                    }
                }
                if (mCurrentScreenMode === AliyunScreenMode.Small) {
                    mControlView.hideMoreButton()
                }
            }
        })

        // 横屏下显示更多
        mControlView.setOnShowMoreClickListener(object : ControlView.OnShowMoreClickListener {
            override fun showMore() {
                if (mOutOnShowMoreClickListener != null) {
                    mOutOnShowMoreClickListener!!.showMore()
                }
            }
        })

        // 截屏
        mControlView.setOnScreenShotClickListener(object : ControlView.OnScreenShotClickListener {
            override fun onScreenShotClick() {
                if (!mIsFullScreenLocked) {
                    FixedToastUtils.show(context, "功能正在开发中, 敬请期待....")
                }
            }
        })

        // 录制
        mControlView.setOnScreenRecoderClickListener(object :
            ControlView.OnScreenRecoderClickListener {
            override fun onScreenRecoderClick() {
                if (!mIsFullScreenLocked) {
                    FixedToastUtils.show(context, "功能正在开发中, 敬请期待....")
                }
            }
        })
    }

    /**
     * 根据位置请求缩略图
     */
    private fun requestBitmapByPosition(targetPosition: Int) {
        mThumbnailHelper?.let {
            if (mThumbnailPrepareSuccess)
                it.requestBitmapAtPosition(targetPosition.toLong())
        }
    }

    /**
     * 隐藏缩略图
     */
    private fun hideThumbnailView() {
        mThumbnailView.hideThumbnailView()
    }

    /**
     * 显示缩略图
     */
    private fun showThumbnailView() {
        if (mThumbnailPrepareSuccess) {
            mThumbnailView.showThumbnailView()
            //根据屏幕大小调整缩略图的大小
            val thumbnailImageView: ImageView = mThumbnailView.getThumbnailImageView()
            val layoutParams = thumbnailImageView.layoutParams
            layoutParams.width = (ScreenUtils.getWidth(context) / 3)
            layoutParams.height = layoutParams.width / 2 - DensityUtils.px2dip(context, 10F)
            thumbnailImageView.layoutParams = layoutParams
        }
    }

    /**
     * 目标位置计算算法
     *
     * @param duration        视频总时长
     * @param currentPosition 当前播放位置
     * @param deltaPosition   与当前位置相差的时长
     * @return
     */
    fun getTargetPosition(
        duration: Long,
        currentPosition: Long,
        deltaPosition: Long
    ): Int {
        // seek步长
        val finalDeltaPosition: Long
        // 根据视频时长，决定seek步长
        val totalMinutes = duration / 1000 / 60
        val hours = (totalMinutes / 60).toInt()
        val minutes = (totalMinutes % 60).toInt()

        // 视频时长为1小时以上，小屏和全屏的手势滑动最长为视频时长的十分之一
        finalDeltaPosition = if (hours >= 1) {
            deltaPosition / 10
        } // 视频时长为31分钟－60分钟时，小屏和全屏的手势滑动最长为视频时长五分之一
        else if (minutes > 30) {
            deltaPosition / 5
        } // 视频时长为11分钟－30分钟时，小屏和全屏的手势滑动最长为视频时长三分之一
        else if (minutes > 10) {
            deltaPosition / 3
        } // 视频时长为4-10分钟时，小屏和全屏的手势滑动最长为视频时长二分之一
        else if (minutes > 3) {
            deltaPosition / 2
        } // 视频时长为1秒钟至3分钟时，小屏和全屏的手势滑动最长为视频结束
        else {
            deltaPosition
        }
        var targetPosition = finalDeltaPosition + currentPosition
        if (targetPosition < 0) {
            targetPosition = 0
        }
        if (targetPosition > duration) {
            targetPosition = duration
        }
        return targetPosition.toInt()
    }

    interface createSuccessListener {
        fun createSuccess()
    }

    private var mListener: createSuccessListener? = null
    fun setCreateSuccessListener(listener: createSuccessListener?) {
        mListener = listener
    }

    /**
     * 锁定屏幕。锁定屏幕后，只有锁会显示，其他都不会显示。手势也不可用
     *
     * @param lockScreen 是否锁住
     */
    fun lockScreen(lockScreen: Boolean) {
        mIsFullScreenLocked = lockScreen
        mControlView.setScreenLockStatus(mIsFullScreenLocked)
        mGestureView.setScreenLockStatus(mIsFullScreenLocked)
    }

    /**
     * 初始化清晰度列表
     */
    private fun initQualityView() {
        addSubView(mQualityView)
        //清晰度点击事件
        mQualityView.setOnQualityClickListener(object : QualityView.OnQualityClickListener {
            override fun onQualityClick(qualityTrackInfo: TrackInfo) {
                //进行清晰度的切换
                mAliyunVodPlayer.selectTrack(qualityTrackInfo.index)
            }
        })
    }

    /**
     * 初始化倍速view
     */
    private fun initSpeedView() {
        addSubView(mSpeedView)

        //倍速点击事件
        mSpeedView.setOnSpeedClickListener(object : SpeedView.OnSpeedClickListener {
            override fun onSpeedClick(value: SpeedView.SpeedValue?) {
                var speed = 1.0f
                if (value === SpeedView.SpeedValue.Normal) {
                    speed = 1.0f
                } else if (value === SpeedView.SpeedValue.OneQuartern) {
                    speed = 1.25f
                } else if (value === SpeedView.SpeedValue.OneHalf) {
                    speed = 1.5f
                } else if (value === SpeedView.SpeedValue.Twice) {
                    speed = 2.0f
                }

                //改变倍速
                mAliyunVodPlayer.speed = speed
                mSpeedView.setSpeed(value)
            }

            override fun onHide() {
                //当倍速界面隐藏之后，显示菜单按钮
            }
        })
    }

    /**
     * 初始化引导view
     */
    private fun initGuideView() {
        addSubView(mGuideView)
    }

    /**
     * 切换播放状态。点播播放按钮之后的操作
     */
    private fun switchPlayerState() {
        if (playerState == started) {
            pause()
        } else if (playerState == paused || playerState == prepared) {
            start()
        }
        if (onPlayStateBtnClickListener != null) {
            onPlayStateBtnClickListener!!.onPlayBtnClick(playerState)
        }
    }

    /**
     * 初始化手势view
     */
    private fun initGestureView() {
        addSubView(mGestureView)

        //设置手势监听
        mGestureView.setOnGestureListener(object : GestureView.GestureListener {
            override fun onHorizontalDistance(downX: Float, nowX: Float) {
                //水平滑动调节seek。
                // seek需要在手势结束时操作。
                val duration = mAliyunVodPlayer.duration
                val position = mCurrentPosition
                var deltaPosition: Long = 0
                var targetPosition = 0
                if (playerState == prepared || playerState == paused || playerState == started
                ) {
                    //在播放时才能调整大小
                    deltaPosition = (nowX - downX).toLong() * duration / width
                    targetPosition = getTargetPosition(duration, position, deltaPosition)
                }
                if (mGestureDialogManager != null) {
                    inSeek = true
                    mControlView.videoPosition = targetPosition
                    requestBitmapByPosition(targetPosition)
                    showThumbnailView()
                }
            }

            override fun onLeftVerticalDistance(downY: Float, nowY: Float) {
                //左侧上下滑动调节亮度
                val changePercent = ((nowY - downY) * 100 / height).toInt()
                mGestureDialogManager?.let {
                    it.showBrightnessDialog(
                        this@AliyunVodPlayerView,
                        screenBrightness
                    )
                    val brightness: Int = it.updateBrightnessDialog(changePercent)
                    if (mOnScreenBrightnessListener != null) {
                        mOnScreenBrightnessListener!!.onScreenBrightness(brightness)
                    }
                    screenBrightness = brightness
                }
            }

            override fun onRightVerticalDistance(downY: Float, nowY: Float) {
                //右侧上下滑动调节音量
                val volume = mAliyunVodPlayer.volume
                val changePercent = ((nowY - downY) * 100 / height).toInt()

                mGestureDialogManager?.let {
                    it.showVolumeDialog(this@AliyunVodPlayerView, volume * 100)
                    val targetVolume: Float = it.updateVolumeDialog(changePercent)
                    currentVolume = targetVolume
                    //通过返回值改变音量
                    mAliyunVodPlayer.volume = targetVolume / 100.00f
                }
            }

            override fun onGestureEnd() {
                //手势结束。
                //seek需要在结束时操作。
                mGestureDialogManager?.let {
                    it.dismissBrightnessDialog()
                    it.dismissVolumeDialog()
                    if (inSeek) {
                        var seekPosition: Int = mControlView.videoPosition
                        if (seekPosition >= mAliyunVodPlayer.duration) {
                            seekPosition = (mAliyunVodPlayer.duration - 1000).toInt()
                        }
                        if (seekPosition >= 0) {
                            seekTo(seekPosition)
                            hideThumbnailView()
                        } else {
                            inSeek = false
                        }
                    }
                }
            }

            override fun onSingleTap() {
                //单击事件，显示控制栏
                if (mControlView.visibility != View.VISIBLE) {
                    mControlView.show()
                } else {
                    mControlView.hide(ViewAction.HideType.Normal)
                }
            }

            override fun onDoubleTap() {
                switchPlayerState()
            }
        })
    }

    /**
     * 初始化播放器显示view
     */
    private fun initSurfaceView() {
        addSubView(playerView)
        val holder = playerView.holder
        //增加surfaceView的监听
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
                VcPlayerLog.d(
                    TAG,
                    " surfaceCreated = surfaceHolder = $surfaceHolder"
                )
                mAliyunVodPlayer.setDisplay(surfaceHolder)
                //防止黑屏
                mAliyunVodPlayer.redraw()
            }

            override fun surfaceChanged(
                surfaceHolder: SurfaceHolder, format: Int, width: Int,
                height: Int
            ) {
                VcPlayerLog.d(
                    TAG,
                    " surfaceChanged surfaceHolder = " + surfaceHolder + " ,  width = " + width + " , height = "
                            + height
                )
                mAliyunVodPlayer.redraw()
            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                VcPlayerLog.d(
                    TAG,
                    " surfaceDestroyed = surfaceHolder = $surfaceHolder"
                )
                mAliyunVodPlayer.setDisplay(null)
            }
        })
    }

    /**
     * 初始化播放器
     */
    private fun initAliVcPlayer() {
        mAliyunVodPlayer = AliPlayerFactory.createAliPlayer(context.applicationContext)
        mAliyunVodPlayer.enableLog(true)
        //设置准备回调
        mAliyunVodPlayer.setOnPreparedListener(VideoPlayerPreparedListener(this))
        //播放器出错监听
        mAliyunVodPlayer.setOnErrorListener(VideoPlayerErrorListener(this))
        //播放器加载回调
        mAliyunVodPlayer.setOnLoadingStatusListener(VideoPlayerLoadingStatusListener(this))
        //播放器状态
        mAliyunVodPlayer.setOnStateChangedListener(VideoPlayerStateChangedListener(this))
        //播放结束
        mAliyunVodPlayer.setOnCompletionListener(VideoPlayerCompletionListener(this))
        //播放信息监听
        mAliyunVodPlayer.setOnInfoListener(VideoPlayerInfoListener(this))
        //第一帧显示
        mAliyunVodPlayer.setOnRenderingStartListener(VideoPlayerRenderingStartListener(this))
        //trackChange监听
        mAliyunVodPlayer.setOnTrackChangedListener(VideoPlayerTrackChangedListener(this))
        //seek结束事件
        mAliyunVodPlayer.setOnSeekCompleteListener(VideoPlayerOnSeekCompleteListener(this))
        mAliyunVodPlayer.setOnSeiDataListener(VideoPlayerOnSeiDataListener(this))
    }

    /**
     * 获取从源中设置的标题 。 如果用户设置了标题，优先使用用户设置的标题。 如果没有，就使用服务器返回的标题
     *
     * @param title 服务器返回的标题
     * @return 最后的标题
     */
    private fun getTitle(title: String): String? {
        var finalTitle: String? = title
        if (mAliyunLocalSource != null) {
            finalTitle = mAliyunLocalSource!!.title
        } else if (mAliyunPlayAuth != null) {
            finalTitle = mAliyunPlayAuth!!.title
        } else if (mAliyunVidSts != null) {
            finalTitle = mAliyunVidSts!!.title
        }
        return if (TextUtils.isEmpty(finalTitle)) {
            title
        } else {
            finalTitle
        }
    }

    /**
     * 获取从源中设置的封面 。 如果用户设置了封面，优先使用用户设置的封面。 如果没有，就使用服务器返回的封面
     *
     * @param postUrl 服务器返回的封面
     * @return 最后的封面
     */
    private fun getPostUrl(postUrl: String): String? {
        var finalPostUrl: String? = postUrl
        if (mAliyunLocalSource != null) {
            finalPostUrl = mAliyunLocalSource!!.coverPath
        } else if (mAliyunPlayAuth != null) {
        }
        return if (TextUtils.isEmpty(finalPostUrl)) {
            postUrl
        } else {
            finalPostUrl
        }
    }

    /**
     * 判断是否是本地资源
     *
     * @return
     */
    private val isLocalSource: Boolean
        private get() {
            var scheme: String? = null
            if ("vidsts" == PlayParameter.PLAY_PARAM_TYPE) {
                return false
            }
            if ("localSource" == PlayParameter.PLAY_PARAM_TYPE) {
                val parse = Uri.parse(PlayParameter.PLAY_PARAM_URL)
                scheme = parse.scheme
            }
            return scheme == null
        }

    /**
     * 判断是否是Url播放资源
     */
    private val isUrlSource: Boolean
        private get() {
            var scheme: String? = null
            return if ("vidsts" == PlayParameter.PLAY_PARAM_TYPE) {
                false
            } else {
                val parse = Uri.parse(PlayParameter.PLAY_PARAM_URL)
                scheme = parse.scheme
                scheme != null
            }
        }

    /**
     * 获取视频时长
     *
     * @return 视频时长
     */
    val duration: Int
        get() = mAliyunVodPlayer.duration.toInt()
    /**
     * 获取当前位置
     *
     * @return 当前位置
     */
    //    public int getCurrentPosition() {
    //        if (mAliyunVodPlayer != null && mAliyunVodPlayer.isPlaying()) {
    //            return (int) mAliyunVodPlayer.getCurrentPosition();
    //        }
    //
    //        return 0;
    //    }
    /**
     * 显示错误提示
     *
     * @param errorCode  错误码
     * @param errorEvent 错误事件
     * @param errorMsg   错误描述
     */
    fun showErrorTipView(
        errorCode: Int,
        errorEvent: String?,
        errorMsg: String?
    ) {
        pause()
        stop()
        mControlView.setPlayState(ControlView.PlayState.NotPlaying)
        //隐藏其他的动作,防止点击界面去进行其他操作
        mGestureView.hide(ViewAction.HideType.End)
        mControlView.hide(ViewAction.HideType.End)
        mCoverView.visibility = View.GONE
        mTipsView.showErrorTipView(errorCode, errorEvent, errorMsg)
    }

    private fun hideErrorTipView() {
        mTipsView.hideErrorTipView()
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private fun addSubView(view: View?) {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )
        //添加到布局中
        addView(view, params)
    }

    /**
     * 添加子View到布局中央
     */
    private fun addSubViewByCenter(view: View?) {
        val params = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        params.addRule(CENTER_IN_PARENT)
        addView(view, params)
    }

    /**
     * 添加子View到布局中,在某个View的下方
     *
     * @param view            需要添加的View
     * @param belowTargetView 在这个View的下方
     */
    private fun addSubViewBelow(
        view: View,
        belowTargetView: View
    ) {
        belowTargetView.post {
            val params = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            val measuredHeight = belowTargetView.measuredHeight
            params.topMargin = measuredHeight
            //添加到布局中
            addView(view, params)
        }
    }

    /**
     * 改变屏幕模式：小屏或者全屏。
     *
     * @param targetMode
     */
    fun changeScreenMode(targetMode: AliyunScreenMode, isReverse: Boolean) {
        VcPlayerLog.d(
            TAG,
            "mIsFullScreenLocked = $mIsFullScreenLocked ， targetMode = $targetMode"
        )
        var finalScreenMode: AliyunScreenMode = targetMode
        if (mIsFullScreenLocked) {
            finalScreenMode = AliyunScreenMode.Full
        }

        //这里可能会对模式做一些修改
        if (targetMode !== mCurrentScreenMode) {
            mCurrentScreenMode = finalScreenMode
        }
        mControlView.setScreenModeStatus(finalScreenMode)
        mSpeedView.setScreenMode(finalScreenMode)
        mGuideView.setScreenMode(finalScreenMode)
        val context = context
        if (context is Activity) {
            if (finalScreenMode === AliyunScreenMode.Full) {
                if (lockPortraitMode == null) {
                    //不是固定竖屏播放。
//                    ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    if (isReverse) {
                        context.requestedOrientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    } else {
                        context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }

                    //SCREEN_ORIENTATION_LANDSCAPE只能固定一个横屏方向
                } else {
                    //如果是固定全屏，那么直接设置view的布局，宽高
                    val aliVcVideoViewLayoutParams = layoutParams
                    aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            } else if (finalScreenMode === AliyunScreenMode.Small) {
                if (lockPortraitMode == null) {
                    //不是固定竖屏播放。
                    context.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    //如果是固定全屏，那么直接设置view的布局，宽高
                    val aliVcVideoViewLayoutParams = layoutParams
                    aliVcVideoViewLayoutParams.height =
                        (ScreenUtils.getWidth(context) * 9.0f / 16).toInt()
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        }
    }

    /**
     * 获取当前屏幕模式：小屏、全屏
     *
     * @return 当前屏幕模式
     */
    val screenMode: AliyunScreenMode
        get() = mCurrentScreenMode

    /**
     * 设置准备事件监听
     *
     * @param onPreparedListener 准备事件
     */
    fun setOnPreparedListener(onPreparedListener: OnPreparedListener?) {
        mOutPreparedListener = onPreparedListener
    }

    /**
     * 设置错误事件监听
     *
     * @param onErrorListener 错误事件监听
     */
    fun setOnErrorListener(onErrorListener: OnErrorListener?) {
        mOutErrorListener = onErrorListener
    }

    /**
     * 设置信息事件监听
     *
     * @param onInfoListener 信息事件监听
     */
    fun setOnInfoListener(onInfoListener: OnInfoListener?) {
        mOutInfoListener = onInfoListener
    }

    /**
     * 设置播放完成事件监听
     *
     * @param onCompletionListener 播放完成事件监听
     */
    fun setOnCompletionListener(onCompletionListener: OnCompletionListener?) {
        mOutCompletionListener = onCompletionListener
    }

    /**
     * 设置改变清晰度事件监听
     *
     * @param l 清晰度事件监听
     */
    fun setOnChangeQualityListener(l: OnChangeQualityListener?) {
        mOutChangeQualityListener = l
    }
    /**
     * 设置重播事件监听
     *
     * @param onRePlayListener 重播事件监听
     */
    //    public void setOnRePlayListener(IPlayer.OnRePlayListener onRePlayListener) {
    //        mOutRePlayListener = onRePlayListener;
    //    }
    /**
     * 设置自动播放事件监听
     *
     * @param l 自动播放事件监听
     */
    fun setOnAutoPlayListener(l: OnAutoPlayListener?) {
        mOutAutoPlayListener = l
    }

    interface OnTimeExpiredErrorListener {
        fun onTimeExpiredError()
    }

    /**
     * 设置源超时监听
     *
     * @param l 源超时监听
     */
    fun setOnTimeExpiredErrorListener(l: OnTimeExpiredErrorListener?) {
        mOutTimeExpiredErrorListener = l
    }
    /**
     * 设置鉴权过期监听，在鉴权过期前一分钟回调
     *
     * @param listener
     */
    //    public void setOnUrlTimeExpiredListener(IPlayer.OnUrlTimeExpiredListener listener) {
    //        this.mOutUrlTimeExpiredListener = listener;
    //    }
    /**
     * 设置首帧显示事件监听
     *
     * @param onFirstFrameStartListener 首帧显示事件监听
     */
    fun setOnFirstFrameStartListener(onFirstFrameStartListener: OnRenderingStartListener?) {
        mOutFirstFrameStartListener = onFirstFrameStartListener
    }

    /**
     * 设置seek结束监听
     *
     * @param onSeekCompleteListener seek结束监听
     */
    fun setOnSeekCompleteListener(onSeekCompleteListener: OnSeekCompleteListener?) {
        mOuterSeekCompleteListener = onSeekCompleteListener
    }

    /**
     * 设置停止播放监听
     *
     * @param onStoppedListener 停止播放监听
     */
    fun setOnStoppedListener(onStoppedListener: OnStoppedListener?) {
        mOnStoppedListener = onStoppedListener
    }

    /**
     * 设置加载状态监听
     *
     * @param onLoadingListener 加载状态监听
     */
    fun setOnLoadingListener(onLoadingListener: OnLoadingStatusListener?) {
        mAliyunVodPlayer.setOnLoadingStatusListener(onLoadingListener)
    }

    fun setSeiDataListener(onSeiDataListener: IPlayer.OnSeiDataListener?) {
        mOutSeiDataListener = onSeiDataListener
    }
    /**
     * 设置缓冲监听
     *
     * @param onBufferingUpdateListener 缓冲监听
     */
    //    public void setOnBufferingUpdateListener(IPlayer.OnBufferingUpdateListener onBufferingUpdateListener) {
    //        if (mAliyunVodPlayer != null) {
    //            mAliyunVodPlayer.setOnBufferingUpdateListener(onBufferingUpdateListener);
    //        }
    //    }
    /**
     * 设置视频宽高变化监听
     *
     * @param onVideoSizeChangedListener 视频宽高变化监听
     */
    fun setOnVideoSizeChangedListener(onVideoSizeChangedListener: OnVideoSizeChangedListener?) {
        mAliyunVodPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener)
    }
    /**
     * 设置循环播放开始监听
     *
     * @param onCircleStartListener 循环播放开始监听
     */
    //    public void setOnCircleStartListener(IPlayer.OnCircleStartListener onCircleStartListener) {
    //        if (mAliyunVodPlayer != null) {
    //            mAliyunVodPlayer.setOnCircleStartListener(onCircleStartListener);
    //        }
    //    }
    /**
     * 设置PlayAuth的播放方式
     *
     * @param aliyunPlayAuth auth
     */
    fun setAuthInfo(aliyunPlayAuth: VidAuth) {
        //重置界面
        clearAllSource()
        reset()
        mAliyunPlayAuth = aliyunPlayAuth
        mControlView.setForceQuality(aliyunPlayAuth.isForceQuality)

        //4G的话先提示
        if (!isLocalSource && NetWatchdog.is4GConnected(context)) {
            mTipsView.showNetChangeTipView()
        } else {
            //具体的准备操作
            prepareAuth(aliyunPlayAuth)
        }
    }

    /**
     * 通过playAuth prepare
     *
     * @param aliyunPlayAuth 源
     */
    private fun prepareAuth(aliyunPlayAuth: VidAuth) {
        mTipsView.showNetLoadingTipView()
        mControlView.setIsMtsSource(false)
        mQualityView.setIsMtsSource(false)
        mAliyunVodPlayer.setDataSource(aliyunPlayAuth)
        mAliyunVodPlayer.prepare()
    }

    /**
     * 清空之前设置的播放源
     */
    private fun clearAllSource() {
        mAliyunPlayAuth = null
        mAliyunVidSts = null
        mAliyunLocalSource = null
    }

    /**
     * 设置本地播放源
     *
     * @param aliyunLocalSource 本地播放源
     */
    fun setLocalSource(aliyunLocalSource: UrlSource) {
        clearAllSource()
        reset()
        mAliyunLocalSource = aliyunLocalSource
        mControlView.setForceQuality(true)
        if (!isLocalSource && NetWatchdog.is4GConnected(context)) {
            mTipsView.showNetChangeTipView()
        } else {
            prepareLocalSource(aliyunLocalSource)
        }
    }

    /**
     * prepare本地播放源
     *
     * @param aliyunLocalSource 本地播放源
     */
    private fun prepareLocalSource(aliyunLocalSource: UrlSource) {
        mControlView.setForceQuality(true)
        mControlView.setIsMtsSource(false)
        mQualityView.setIsMtsSource(false)
        mAliyunVodPlayer.isAutoPlay = true
        mAliyunVodPlayer.setDataSource(aliyunLocalSource)
        mAliyunVodPlayer.prepare()
    }

    /**
     * 准备vidsts源
     *
     * @param vidSts 源
     */
    fun setVidSts(vidSts: VidSts) {
        clearAllSource()
        reset()
        mAliyunVidSts = vidSts
        mControlView.setForceQuality(vidSts.isForceQuality)
        if (NetWatchdog.is4GConnected(context)) {
            mTipsView.showNetChangeTipView()
        } else {
            prepareVidsts(mAliyunVidSts)
        }
    }

    /**
     * 准备vidsts 源
     */
    private fun prepareVidsts(vidSts: VidSts?) {
        mTipsView.showNetLoadingTipView()
        mControlView.setIsMtsSource(false)
        mQualityView.setIsMtsSource(false)
        mAliyunVodPlayer.setDataSource(vidSts)
        mAliyunVodPlayer.prepare()
    }

    /**
     * 设置封面信息
     *
     * @param uri url地址
     */
    fun setCoverUri(uri: String) {
        if (uri.isNotEmpty()) {
            ImageLoader(mCoverView).loadAsync(uri)
            mCoverView.visibility = if (isPlaying) View.GONE else View.VISIBLE
        }
    }

    /**
     * 设置封面id
     *
     * @param resId 资源id
     */
    fun setCoverResource(resId: Int) {
        mCoverView.setImageResource(resId)
        mCoverView.visibility = if (isPlaying) View.GONE else View.VISIBLE
    }

    /**
     * 设置边播边存
     *
     * @param enable      是否开启。开启之后会根据maxDuration和maxSize决定有无缓存。
     * @param saveDir     保存目录
     * @param maxDuration 单个文件最大时长 秒
     * @param maxSize     所有文件最大大小 MB
     */
    fun setPlayingCache(
        enable: Boolean,
        saveDir: String?,
        maxDuration: Int,
        maxSize: Long
    ) {
        //            mAliyunVodPlayer.setPlayingCache(enable, saveDir, maxDuration, maxSize);
    }

    /**
     * 设置缩放模式
     *
     * @param scallingMode 缩放模式
     */
    fun setVideoScalingMode(scallingMode: ScaleMode?) {
        mAliyunVodPlayer.scaleMode = scallingMode
    }

    /**
     * 当VodPlayer 没有加载完成的时候,调用onStop 去暂停视频,
     * 会出现暂停失败的问题。
     */
    private class VodPlayerLoadEndHandler(aliyunVodPlayerView: AliyunVodPlayerView) :
        Handler() {
        private val weakReference: WeakReference<AliyunVodPlayerView>
        private var intentPause = false
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg.what == 0) {
                intentPause = true
            }
            if (msg.what == 1) {
                val aliyunVodPlayerView = weakReference.get()
                if (aliyunVodPlayerView != null && intentPause) {
                    aliyunVodPlayerView.onStop()
                    intentPause = false
                }
            }
        }

        init {
            weakReference = WeakReference(aliyunVodPlayerView)
        }
    }

    /**
     * 在activity调用onResume的时候调用。 解决home回来后，画面方向不对的问题
     */
    fun onResume() {
        if (mIsFullScreenLocked) {
            val orientation = resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                changeScreenMode(AliyunScreenMode.Small, false)
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                changeScreenMode(AliyunScreenMode.Full, false)
            }
        }
        mNetWatchdog.startWatch()
        mOrientationWatchDog.startWatch()

        //从其他界面过来的话，也要show。
//        if (mControlView != null) {
//            mControlView.show();
//        }

        //onStop中记录下来的状态，在这里恢复使用
        resumePlayerState()
    }

    /**
     * 暂停播放器的操作
     */
    fun onStop() {
        mNetWatchdog.stopWatch()
        mOrientationWatchDog.stopWatch()

        //保存播放器的状态，供resume恢复使用。
        savePlayerState()
    }

    /**
     * Activity回来后，恢复之前的状态
     */
    private fun resumePlayerState() {
        //恢复前台后需要进行判断,如果是本地资源,则继续播放,如果是4g则给予提示,不会继续播放,否则继续播放
        if (!isLocalSource && NetWatchdog.is4GConnected(context)) {
        } else {
            start()
        }
    }

    /**
     * 保存当前的状态，供恢复使用
     */
    private fun savePlayerState() {
        //然后再暂停播放器
        //如果希望后台继续播放，不需要暂停的话，可以注释掉pause调用。
        pause()
    }

    /**
     * 获取媒体信息
     *
     * @return 媒体信息
     */
    val mediaInfo: MediaInfo?
        get() = mAliyunVodPlayer.mediaInfo

    /**
     * 活动销毁，释放
     */
    fun onDestroy() {
        stop()
        mAliyunVodPlayer.release()
        mAliyunVodPlayer
        playerView
        mGestureView
        mControlView
        mCoverView
        mGestureDialogManager = null
        mNetWatchdog.stopWatch()
        mNetWatchdog
        mTipsView
        currentMediaInfo = null
        mOrientationWatchDog.destroy()
        mOrientationWatchDog
        hasLoadEnd.clear()
    }

    /**
     * 是否处于播放状态：start或者pause了
     *
     * @return 是否处于播放状态
     */
    val isPlaying: Boolean
        get() = playerState == started

    /**
     * 开始播放
     */
    fun start() {
        mControlView.show()
        mControlView.setPlayState(ControlView.PlayState.Playing)
        mGestureView.show()
        if (playerState == paused || playerState == prepared) {
            mAliyunVodPlayer.start()
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mControlView.setPlayState(ControlView.PlayState.NotPlaying)
        if (playerState == started || playerState == prepared) {
            mAliyunVodPlayer.pause()
        }
    }

    /**
     * 停止播放
     */
    private fun stop() {
        var hasLoadedEnd: Boolean? = null
        var mediaInfo: MediaInfo? = null
        mediaInfo = mAliyunVodPlayer.mediaInfo
        hasLoadedEnd = hasLoadEnd[mediaInfo]
        mAliyunVodPlayer.stop()
        mControlView.setPlayState(ControlView.PlayState.NotPlaying)
        hasLoadEnd.remove(mediaInfo)
    }

    /**
     * seek操作
     *
     * @param position 目标位置
     */
    fun seekTo(position: Int) {
        inSeek = true
        realySeekToFunction(position)
    }

    private fun realySeekToFunction(position: Int) {
        isAutoAccurate(position)
        mAliyunVodPlayer.start()
        mControlView.setPlayState(ControlView.PlayState.Playing)
    }

    /**
     * 判断是否开启精准seek
     */
    private fun isAutoAccurate(position: Int) {
        if (duration <= ACCURATE) {
            mAliyunVodPlayer.seekTo(position.toLong(), IPlayer.SeekMode.Accurate)
        } else {
            mAliyunVodPlayer.seekTo(position.toLong(), IPlayer.SeekMode.Inaccurate)
        }
    }

    /**
     * 设置是否显示标题栏
     *
     * @param show true:是
     */
    fun setTitleBarCanShow(show: Boolean) {
        mControlView.setTitleBarCanShow(show)
    }

    /**
     * 设置是否显示控制栏
     *
     * @param show true:是
     */
    fun setControlBarCanShow(show: Boolean) {
        if (mControlView != null) {
            mControlView.setControlBarCanShow(show)
        }
    }

    /**
     * 开启底层日志
     */
    fun enableNativeLog() {
        mAliyunVodPlayer.enableLog(true)
    }

    /**
     * 关闭底层日志
     */
    fun disableNativeLog() {
        mAliyunVodPlayer.enableLog(false)
    }
    /**
     * 设置线程池
     *
     * @param executorService 线程池
     */
    //    public void setThreadExecutorService(ExecutorService executorService) {
    //        if (mAliyunVodPlayer != null) {
    //            mAliyunVodPlayer.setThreadExecutorService(executorService);
    //        }
    //    }
    /**
     * 获取SDK版本号
     *
     * @return SDK版本号
     */
    val sDKVersion: String
        get() = AliPlayerFactory.getSdkVersion()

    /**
     * 设置自动播放
     *
     * @param auto true 自动播放
     */
    fun setAutoPlay(auto: Boolean) {
        mAliyunVodPlayer.isAutoPlay = auto
    }//            return mAliyunVodPlayer.getAllDebugInfo();

    /**
     * 获取底层的一些debug信息
     *
     * @return debug信息
     */
    val allDebugInfo: Map<String, String>?
        get() {
            //            return mAliyunVodPlayer.getAllDebugInfo();
            return null
        }

    /**
     * 锁定竖屏
     *
     * @return 竖屏监听器
     */
    /**
     * 设置锁定竖屏监听
     *
     * @param listener 监听器
     */
    var lockPortraitMode: LockPortraitListener?
        get() = mLockPortraitListener
        set(listener) {
            mLockPortraitListener = listener
        }

    /**
     * 让home键无效
     *
     * @param keyCode 按键
     * @param event   事件
     * @return 是否处理。
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (mCurrentScreenMode === AliyunScreenMode.Full && keyCode != KeyEvent.KEYCODE_HOME
            && keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
        ) {
            changedToPortrait(true)
            return false
        }
        return if (mIsFullScreenLocked && keyCode != KeyEvent.KEYCODE_HOME) {
            false
        } else true
    }

    /**
     * 截图功能
     *
     * @return 图片
     */
    fun snapShot() {
        mAliyunVodPlayer.snapshot()
    }

    /**
     * 设置循环播放
     *
     * @param circlePlay true:循环播放
     */
    fun setCirclePlay(circlePlay: Boolean) {
        mAliyunVodPlayer.isLoop = circlePlay
    }

    /**
     * 设置播放时的镜像模式
     *
     * @param mode 镜像模式
     */
    fun setRenderMirrorMode(mode: MirrorMode?) {
        mAliyunVodPlayer.mirrorMode = mode
    }

    /**
     * 设置播放时的旋转方向
     *
     * @param rotate 旋转角度
     */
    fun setRenderRotate(rotate: RotateMode?) {
        mAliyunVodPlayer.rotateMode = rotate
    }

    /**
     * 播放按钮点击listener
     */
    interface OnPlayStateBtnClickListener {
        fun onPlayBtnClick(playerState: Int)
    }

    /**
     * 设置播放状态点击监听
     */
    fun setOnPlayStateBtnClickListener(listener: OnPlayStateBtnClickListener?) {
        onPlayStateBtnClickListener = listener
    }

    private var onSeekStartListener: OnSeekStartListener? = null

    /**
     * seek开始监听
     */
    interface OnSeekStartListener {
        fun onSeekStart(position: Int)
    }

    fun setOnSeekStartListener(listener: OnSeekStartListener?) {
        onSeekStartListener = listener
    }

    /**
     * Player View Click Type
     */
    enum class PlayViewType {
        /**
         * click download view
         */
        Download,

        /**
         * click screen cast
         */
        ScreenCast
    }

    interface OnPlayerViewClickListener {
        fun onClick(screenMode: AliyunScreenMode?, viewType: PlayViewType?)
    }

    /**
     * 设置播放器view点击事件监听，目前只对外暴露下载按钮和投屏按钮
     */
    fun setmOnPlayerViewClickListener(
        mOnPlayerViewClickListener: OnPlayerViewClickListener?
    ) {
        this.mOnPlayerViewClickListener = mOnPlayerViewClickListener
    }

    /**
     * 屏幕方向改变监听接口
     */
    interface OnOrientationChangeListener {
        /**
         * 屏幕方向改变
         *
         * @param from        从横屏切换为竖屏, 从竖屏切换为横屏
         * @param currentMode 当前屏幕类型
         */
        fun orientationChange(
            from: Boolean,
            currentMode: AliyunScreenMode?
        )
    }

    private var orientationChangeListener: OnOrientationChangeListener? = null
    fun setOrientationChangeListener(
        listener: OnOrientationChangeListener?
    ) {
        orientationChangeListener = listener
    }

    /**
     * 断网/连网监听
     */
    private inner class MyNetConnectedListener(aliyunVodPlayerView: AliyunVodPlayerView?) :
        NetWatchdog.NetConnectedListener {
        override fun onReNetConnected(isReconnect: Boolean) {
            if (mNetConnectedListener != null) {
                mNetConnectedListener!!.onReNetConnected(isReconnect)
            }
        }

        override fun onNetUnConnected() {
            if (mNetConnectedListener != null) {
                mNetConnectedListener!!.onNetUnConnected()
            }
        }
    }

    fun setNetConnectedListener(listener: NetConnectedListener?) {
        mNetConnectedListener = listener
    }

    /**
     * 判断是否有网络的监听
     */
    interface NetConnectedListener {
        /**
         * 网络已连接
         */
        fun onReNetConnected(isReconnect: Boolean)

        /**
         * 网络未连接
         */
        fun onNetUnConnected()
    }

    interface OnFinishListener {
        fun onFinishClick()
    }


    fun setOnShowMoreClickListener(
        listener: ControlView.OnShowMoreClickListener?
    ) {
        mOutOnShowMoreClickListener = listener
    }

    interface OnScreenBrightnessListener {
        fun onScreenBrightness(brightness: Int)
    }

    fun setOnScreenBrightness(listener: OnScreenBrightnessListener?) {
        mOnScreenBrightnessListener = listener
    }

    /** ------------------- 播放器回调 ---------------------------  */
    /**
     * 广告视频播放器准备对外接口监听
     */
    class VideoPlayerPreparedListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnPreparedListener {
        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onPrepared() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerPrepared()
        }
    }

    /**
     * 原视频准备完成
     */
    private fun sourceVideoPlayerPrepared() {
        //需要将mThumbnailPrepareSuccess重置,否则会出现缩略图错乱的问题
        mThumbnailPrepareSuccess = false
        mThumbnailView.setThumbnailPicture(null)
        currentMediaInfo = mAliyunVodPlayer.mediaInfo

        currentMediaInfo?.let { mediaInfo ->
            val thumbnailList: List<Thumbnail> = mediaInfo.thumbnailList
            if (thumbnailList.isNotEmpty()) {
                mThumbnailHelper = ThumbnailHelper(thumbnailList[0].mURL)

                mThumbnailHelper?.let { thumbnailHelper ->
                    thumbnailHelper.setOnPrepareListener(object :
                        ThumbnailHelper.OnPrepareListener {
                        override fun onPrepareSuccess() {
                            mThumbnailPrepareSuccess = true
                        }

                        override fun onPrepareFail() {
                            mThumbnailPrepareSuccess = false
                        }
                    })
                    thumbnailHelper.prepare()
                    thumbnailHelper.setOnThumbnailGetListener(object : OnThumbnailGetListener {
                        override fun onThumbnailGetSuccess(
                            l: Long,
                            thumbnailBitmapInfo: ThumbnailBitmapInfo
                        ) {
                            mThumbnailView.setTime(TimeFormater.formatMs(l))
                            mThumbnailView.setThumbnailPicture(thumbnailBitmapInfo.thumbnailBitmap)
                        }

                        override fun onThumbnailGetFail(l: Long, s: String?) {}
                    })
                }
            }

            //防止服务器信息和实际不一致
            mSourceDuration = mAliyunVodPlayer.duration
            mediaInfo.duration = mSourceDuration.toInt()
            val currentTrack = mAliyunVodPlayer.currentTrack(TrackInfo.Type.TYPE_VOD)
            var currentQuality: String? = "FD"
            if (currentTrack != null) {
                currentQuality = currentTrack.vodDefinition
            }
            mControlView.setMediaInfo(mediaInfo, currentQuality)
            mControlView.setHideType(ViewAction.HideType.Normal)
            mGestureView.setHideType(ViewAction.HideType.Normal)
            mGestureView.show()
            mTipsView.hideNetLoadingTipView()
            playerView.visibility = View.VISIBLE

            //准备成功之后可以调用start方法开始播放
            if (mOutPreparedListener != null) {
                mOutPreparedListener!!.onPrepared()
            }
        }
    }

    private class VideoPlayerErrorListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnErrorListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onError(errorInfo: ErrorInfo) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerError(errorInfo)
        }
    }

    /**
     * 原视频错误监听
     */
    private fun sourceVideoPlayerError(errorInfo: ErrorInfo) {
        mTipsView.hideAll()
        //出错之后解锁屏幕，防止不能做其他操作，比如返回。
        lockScreen(false)

        //errorInfo.getExtra()展示为null,修改为显示errorInfo.getCode的十六进制的值
        showErrorTipView(
            errorInfo.code.value,
            Integer.toHexString(errorInfo.code.value),
            errorInfo.msg
        )
        if (mOutErrorListener != null) {
            mOutErrorListener!!.onError(errorInfo)
        }
    }

    /**
     * 播放器加载状态监听
     */
    private class VideoPlayerLoadingStatusListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnLoadingStatusListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onLoadingBegin() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerLoadingBegin()
        }

        override fun onLoadingProgress(percent: Int, v: Float) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerLoadingProgress(percent)
        }

        override fun onLoadingEnd() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerLoadingEnd()
        }
    }

    /**
     * 原视频开始加载
     */
    private fun sourceVideoPlayerLoadingBegin() {
        mTipsView.showBufferLoadingTipView()
    }

    /**
     * 原视频开始加载进度
     */
    private fun sourceVideoPlayerLoadingProgress(percent: Int) {
        //视频广告,并且广告视频在播放状态,不要展示loading
        mTipsView.updateLoadingPercent(percent)
        if (percent == 100) {
            mTipsView.hideBufferLoadingTipView()
        }
    }

    /**
     * 原视频加载结束
     */
    private fun sourceVideoPlayerLoadingEnd() {
        mTipsView.hideBufferLoadingTipView()
        if (isPlaying) {
            mTipsView.hideErrorTipView()
        }
        hasLoadEnd[currentMediaInfo] = true
        vodPlayerLoadEndHandler.sendEmptyMessage(1)
    }

    /**
     * 播放器状态改变监听
     */
    private class VideoPlayerStateChangedListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnStateChangedListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onStateChanged(newState: Int) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerStateChanged(newState)
        }
    }

    /**
     * 原视频状态改变监听
     */
    private fun sourceVideoPlayerStateChanged(newState: Int) {
        playerState = newState

        if (newState == stopped) {
            mOnStoppedListener?.onStop()
        } else if (newState == started) {
            mControlView.setPlayState(ControlView.PlayState.Playing)
        }
    }

    /**
     * 播放器播放完成监听
     */
    private class VideoPlayerCompletionListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnCompletionListener {
        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onCompletion() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerCompletion()
        }
    }

    /**
     * 原视频播放完成
     */
    private fun sourceVideoPlayerCompletion() {
        inSeek = false
        //如果当前播放资源是本地资源时, 再显示replay
        if (isLocalSource) {
            //隐藏其他的动作,防止点击界面去进行其他操作
            mGestureView.hide(ViewAction.HideType.End)
            mControlView.hide(ViewAction.HideType.End)
            mTipsView.showReplayTipView()
        }
        if (mOutCompletionListener != null) {
            mOutCompletionListener!!.onCompletion()
        }
    }

    /**
     * 播放器Info监听
     */
    private class VideoPlayerInfoListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnInfoListener {
        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onInfo(infoBean: InfoBean) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerInfo(infoBean)
        }

    }

    /**
     * 原视频Info
     */
    private fun sourceVideoPlayerInfo(infoBean: InfoBean) {
        if (infoBean.code == InfoCode.AutoPlayStart) {
            //自动播放开始,需要设置播放状态
            mControlView.setPlayState(ControlView.PlayState.Playing)
            mOutAutoPlayListener?.onAutoPlayStarted()
        } else if (infoBean.code == InfoCode.BufferedPosition) {
            //更新bufferedPosition
            mVideoBufferedPosition = infoBean.extraValue
            mControlView.setVideoBufferPosition(mVideoBufferedPosition.toInt())
        } else if (infoBean.code == InfoCode.CurrentPosition) {
            //更新currentPosition
            mCurrentPosition = infoBean.extraValue
            val min = mCurrentPosition / 1000 / 60
            val sec = mCurrentPosition / 1000 % 60
            if (!inSeek && playerState == started) {
                mControlView.videoPosition = mCurrentPosition.toInt()
            }
        } else if (infoBean.code == InfoCode.AutoPlayStart) {
            //自动播放开始,需要设置播放状态
            mControlView.setPlayState(ControlView.PlayState.Playing)
            mOutAutoPlayListener?.onAutoPlayStarted()
        } else {
            if (mOutInfoListener != null) {
                mOutInfoListener!!.onInfo(infoBean)
            }
        }
    }

    /**
     * 播放器Render监听
     */
    private class VideoPlayerRenderingStartListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnRenderingStartListener {
        private val weakReference: WeakReference<AliyunVodPlayerView>
        override fun onRenderingStart() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerOnVideoRenderingStart()
        }

        init {
            weakReference = WeakReference(aliyunVodPlayerView)
        }
    }

    /**
     * 原视频onVideoRenderingStart
     */
    private fun sourceVideoPlayerOnVideoRenderingStart() {
        mCoverView.visibility = View.GONE
        if (mOutFirstFrameStartListener != null) {
            mOutFirstFrameStartListener!!.onRenderingStart()
        }
    }

    /**
     * 播放器TrackChanged监听
     */
    private class VideoPlayerTrackChangedListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnTrackChangedListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onChangedSuccess(trackInfo: TrackInfo) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerTrackInfoChangedSuccess(trackInfo)
        }

        override fun onChangedFail(
            trackInfo: TrackInfo,
            errorInfo: ErrorInfo
        ) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerTrackInfoChangedFail(trackInfo, errorInfo)
        }
    }

    /**
     * 原视频 trackInfoChangedSuccess
     */
    private fun sourceVideoPlayerTrackInfoChangedSuccess(trackInfo: TrackInfo) {
        //清晰度切换监听
        if (trackInfo.type == TrackInfo.Type.TYPE_VOD) {
            //切换成功后就开始播放
            mControlView.setCurrentQuality(trackInfo.vodDefinition)
            start()
            mTipsView.hideNetLoadingTipView()
            mOutChangeQualityListener?.onChangeQualitySuccess(TrackInfo.Type.TYPE_VOD.name)
        }
    }

    var playerConfig: PlayerConfig?
        get() = mAliyunVodPlayer.config
        set(playerConfig) {
            mAliyunVodPlayer.config = playerConfig
        }

    /**
     * 原视频 trackInfochangedFail
     */
    private fun sourceVideoPlayerTrackInfoChangedFail(
        trackInfo: TrackInfo,
        errorInfo: ErrorInfo
    ) {
        //失败的话，停止播放，通知上层
        mTipsView.hideNetLoadingTipView()
        stop()
        mOutChangeQualityListener?.onChangeQualityFail(0, errorInfo.msg)
    }

    /**
     * 播放器seek完成监听
     */
    private class VideoPlayerOnSeekCompleteListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        OnSeekCompleteListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onSeekComplete() {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerSeekComplete()
        }
    }

    /**
     * 原视频seek完成
     */
    private fun sourceVideoPlayerSeekComplete() {
        inSeek = false
        mOuterSeekCompleteListener?.onSeekComplete()
    }

    /**
     * SEI监听
     */
    private class VideoPlayerOnSeiDataListener(aliyunVodPlayerView: AliyunVodPlayerView) :
        IPlayer.OnSeiDataListener {

        private val weakReference: WeakReference<AliyunVodPlayerView> =
            WeakReference(aliyunVodPlayerView)

        override fun onSeiData(var1: Int, var2: ByteArray) {
            val aliyunVodPlayerView = weakReference.get()
            aliyunVodPlayerView?.sourceVideoPlayerSeiData(var1, var2)
        }
    }

    /**
     * SEI事件出现
     * @param i 类型
     * @param s 内容
     */
    private fun sourceVideoPlayerSeiData(i: Int, s: ByteArray) {
        mOutSeiDataListener?.onSeiData(i, s)
    }

    /**
     * ------------------- 播放器回调 end---------------------------
     */
    private fun hideSystemUI() {
        this@AliyunVodPlayerView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    companion object {
        /**
         * 精准seek开启判断逻辑：当视频时长小于5分钟的时候。
         */
        private const val ACCURATE = 5 * 60 * 1000
        private val TAG = AliyunVodPlayerView::class.java.simpleName
    }
}