package com.example.player.view.control

import android.content.Context
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.core.content.ContextCompat
import com.aliyun.player.nativeclass.MediaInfo
import com.aliyun.player.nativeclass.TrackInfo
import com.aliyun.utils.VcPlayerLog
import com.example.player.R
import com.example.player.constants.PlayParameter
import com.example.player.quality.QualityItem.Companion.getItem
import com.example.player.theme.ITheme
import com.example.player.util.TimeFormater.formatMs
import com.example.player.view.control.ControlView
import com.example.player.view.interfaces.ViewAction
import com.example.player.view.interfaces.ViewAction.HideType
import com.example.player.widget.AliyunScreenMode
import com.example.player.widget.AliyunVodPlayerView
import java.lang.ref.WeakReference
import java.util.*

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 控制条界面。包括了顶部的标题栏，底部 的控制栏，锁屏按钮等等。是界面的主要组成部分。
 */
class ControlView : RelativeLayout, ViewAction, ITheme {
    //标题，控制条单独控制是否可显示
    private var mTitleBarCanShow = true
    private var mControlBarCanShow = true
    private var mTitleBar: View? = null
    private var mControlBar: View? = null

    //这些是大小屏都有的==========START========
    //返回按钮
    private var mTitlebarBackBtn: ImageView? = null

    //标题
    private var mTitlebarText: TextView? = null

    //视频播放状态
    private var mPlayState = PlayState.NotPlaying

    //播放按钮
    private var mPlayStateBtn: ImageView? = null

    //下载
    private var mTitleDownload: ImageView? = null

    //锁定屏幕方向相关
    // 屏幕方向是否锁定
    private var mScreenLocked = false

    //锁屏按钮
    private var mScreenLockBtn: ImageView? = null

    //切换大小屏相关
    private var mAliyunScreenMode = AliyunScreenMode.Small

    //全屏/小屏按钮
    private var mScreenModeBtn: ImageView? = null

    //大小屏公用的信息
    //视频信息，info显示用。
    private var mAliyunMediaInfo: MediaInfo? = null

    //播放的进度
    private var mVideoPosition = 0

    //seekbar拖动状态
    private var isSeekbarTouching = false

    //视频缓冲进度
    private var mVideoBufferPosition = 0

    //这些是大小屏都有的==========END========
    //这些是大屏时显示的
    //大屏的底部控制栏
    private var mLargeInfoBar: View? = null

    //当前位置文字
    private var mLargePositionText: TextView? = null

    //时长文字
    private var mLargeDurationText: TextView? = null

    //进度条
    private var mLargeSeekbar: SeekBar? = null

    //当前的清晰度
    private var mCurrentQuality: String? = null

    //是否固定清晰度
    private var mForceQuality = false

    //改变清晰度的按钮
    private var mLargeChangeQualityBtn: Button? = null

    //更多弹窗按钮
    private var mTitleMore: ImageView? = null

    //这些是小屏时显示的
    //底部控制栏
    private var mSmallInfoBar: View? = null

    //当前位置文字
    private var mSmallPositionText: TextView? = null

    //时长文字
    private var mSmallDurationText: TextView? = null

    //seek进度条
    private var mSmallSeekbar: SeekBar? = null

    //整个view的显示控制：
    //不显示的原因。如果是错误的，那么view就都不显示了。
    private var mHideType: HideType? = null

    //saas,还是mts资源,清晰度的显示不一样
    private var isMtsSource = false

    //各种监听
    // 进度拖动监听
    private var mOnSeekListener: OnSeekListener? = null

    //菜单点击监听
    private var mOnMenuClickListener: OnMenuClickListener? = null

    //下载点击监听
    private var onDownloadClickListener: OnDownloadClickListener? = null

    //标题返回按钮监听
    private var mOnBackClickListener: OnBackClickListener? = null

    //播放按钮点击监听
    private var mOnPlayStateClickListener: OnPlayStateClickListener? = null

    //清晰度按钮点击监听
    private var mOnQualityBtnClickListener: OnQualityBtnClickListener? = null

    //锁屏按钮点击监听
    private var mOnScreenLockClickListener: OnScreenLockClickListener? = null

    //大小屏按钮点击监听
    private var mOnScreenModeClickListener: OnScreenModeClickListener? = null

    // 显示更多
    private var mOnShowMoreClickListener: OnShowMoreClickListener? = null

    //屏幕截图
    private var mOnScreenShotClickListener: OnScreenShotClickListener? = null

    //录制
    private var mOnScreenRecoderClickListener: OnScreenRecoderClickListener? = null

    //原视频时长
    private val mSourceDuration: Long = 0
    private var mScreenShot: ImageView? = null
    private var mScreenRecorder: ImageView? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context,
        attrs
    ) {
        init()
    }

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        //Inflate布局
        LayoutInflater.from(context).inflate(R.layout.alivc_view_control, this, true)
        findAllViews() //找到所有的view
        setViewListener() //设置view的监听事件
        updateAllViews() //更新view的显示
    }

    private fun findAllViews() {
        mTitleBar = findViewById(R.id.titlebar)
        mControlBar = findViewById(R.id.controlbar)
        mTitlebarBackBtn =
            findViewById<View>(R.id.alivc_title_back) as ImageView
        mTitlebarText = findViewById<View>(R.id.alivc_title_title) as TextView
        mTitleDownload =
            findViewById<View>(R.id.alivc_title_download) as ImageView
        mTitleMore = findViewById(R.id.alivc_title_more)
        mScreenModeBtn =
            findViewById<View>(R.id.alivc_screen_mode) as ImageView
        mScreenLockBtn =
            findViewById<View>(R.id.alivc_screen_lock) as ImageView
        mPlayStateBtn =
            findViewById<View>(R.id.alivc_player_state) as ImageView
        mScreenShot = findViewById(R.id.alivc_screen_shot)
        mScreenRecorder = findViewById(R.id.alivc_screen_recoder)
        mLargeInfoBar = findViewById(R.id.alivc_info_large_bar)
        mLargePositionText =
            findViewById<View>(R.id.alivc_info_large_position) as TextView
        mLargeDurationText =
            findViewById<View>(R.id.alivc_info_large_duration) as TextView
        mLargeSeekbar = findViewById<View>(R.id.alivc_info_large_seekbar) as SeekBar
        mLargeChangeQualityBtn =
            findViewById<View>(R.id.alivc_info_large_rate_btn) as Button
        mSmallInfoBar = findViewById(R.id.alivc_info_small_bar)
        mSmallPositionText =
            findViewById<View>(R.id.alivc_info_small_position) as TextView
        mSmallDurationText =
            findViewById<View>(R.id.alivc_info_small_duration) as TextView
        mSmallSeekbar = findViewById<View>(R.id.alivc_info_small_seekbar) as SeekBar
    }

    private fun setViewListener() {
        //标题的返回按钮监听
        mTitlebarBackBtn!!.setOnClickListener {
            if (mOnBackClickListener != null) {
                mOnBackClickListener!!.onClick()
            }
        }
        //下载菜单监听
        mTitleDownload!!.setOnClickListener {
            if (onDownloadClickListener != null) {
                onDownloadClickListener!!.onDownloadClick()
            }
        }
        //控制栏的播放按钮监听
        mPlayStateBtn!!.setOnClickListener {
            if (mOnPlayStateClickListener != null) {
                mOnPlayStateClickListener!!.onPlayStateClick()
            }
        }
        //锁屏按钮监听
        mScreenLockBtn!!.setOnClickListener {
            if (mOnScreenLockClickListener != null) {
                mOnScreenLockClickListener!!.onClick()
            }
        }

        // 截图按钮监听
        mScreenShot!!.setOnClickListener {
            if (mOnScreenShotClickListener != null) {
                mOnScreenShotClickListener!!.onScreenShotClick()
            }
        }

        // 录制按钮监听
        mScreenRecorder!!.setOnClickListener {
            if (mOnScreenRecoderClickListener != null) {
                mOnScreenRecoderClickListener!!.onScreenRecoderClick()
            }
        }

        //大小屏按钮监听
        mScreenModeBtn!!.setOnClickListener {
            if (mOnScreenModeClickListener != null) {
                mOnScreenModeClickListener!!.onClick()
            }
        }

        //seekbar的滑动监听
        val seekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    //这里是用户拖动，直接设置文字进度就行，
                    // 无需去updateAllViews() ， 因为不影响其他的界面。
                    if (mAliyunScreenMode === AliyunScreenMode.Full) {
                        //全屏状态.
                        mLargePositionText!!.text = formatMs(progress.toLong())
                    } else if (mAliyunScreenMode === AliyunScreenMode.Small) {
                        //小屏状态
                        mSmallPositionText!!.text = formatMs(progress.toLong())
                    }
                    if (mOnSeekListener != null) {
                        mOnSeekListener!!.onProgressChanged(progress)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeekbarTouching = true
                mHideHandler.removeMessages(WHAT_HIDE)
                if (mOnSeekListener != null) {
                    mOnSeekListener!!.onSeekStart(seekBar.progress)
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (mOnSeekListener != null) {
                    mOnSeekListener!!.onSeekEnd(seekBar.progress)
                }
                isSeekbarTouching = false
                mHideHandler.removeMessages(WHAT_HIDE)
                mHideHandler.sendEmptyMessageDelayed(
                    WHAT_HIDE,
                    DELAY_TIME.toLong()
                )
            }
        }
        //seekbar的滑动监听
        mLargeSeekbar!!.setOnSeekBarChangeListener(seekBarChangeListener)
        mSmallSeekbar!!.setOnSeekBarChangeListener(seekBarChangeListener)
        //全屏下的切换分辨率按钮监听
        mLargeChangeQualityBtn!!.setOnClickListener { v ->
            //点击切换分辨率 显示分辨率的对话框
            if (mOnQualityBtnClickListener != null && mAliyunMediaInfo != null) {
                val qualityTrackInfos: MutableList<TrackInfo> =
                    ArrayList()
                val trackInfos =
                    mAliyunMediaInfo!!.trackInfos
                for (trackInfo in trackInfos) {
                    //清晰度
                    if (trackInfo.type == TrackInfo.Type.TYPE_VOD) {
                        qualityTrackInfos.add(trackInfo)
                    }
                }
                mOnQualityBtnClickListener!!.onQualityBtnClick(
                    v,
                    qualityTrackInfos,
                    mCurrentQuality?:""
                )
            }
        }

        // 更多按钮点击监听
        mTitleMore!!.setOnClickListener {
            if (mOnShowMoreClickListener != null) {
                mOnShowMoreClickListener!!.showMore()
            }
        }
    }

    /**
     * 是不是MTS的源 //MTS的清晰度显示与其他的不太一样，所以这里需要加一个作为区分
     *
     * @param isMts true:是。false:不是
     */
    fun setIsMtsSource(isMts: Boolean) {
        isMtsSource = isMts
    }

    /**
     * 设置当前播放的清晰度
     *
     * @param currentQuality 当前清晰度
     */
    fun setCurrentQuality(currentQuality: String?) {
        mCurrentQuality = currentQuality
        updateLargeInfoBar()
        updateChangeQualityBtn()
    }

    /**
     * 设置是否强制清晰度。如果是强制，则不会显示切换清晰度按钮
     *
     * @param forceQuality true：是
     */
    fun setForceQuality(forceQuality: Boolean) {
        mForceQuality = forceQuality
        updateChangeQualityBtn()
    }

    /**
     * 设置是否显示标题栏。
     *
     * @param show false:不显示
     */
    fun setTitleBarCanShow(show: Boolean) {
        mTitleBarCanShow = show
        updateAllTitleBar()
    }

    /**
     * 设置是否显示控制栏
     *
     * @param show fase：不显示
     */
    fun setControlBarCanShow(show: Boolean) {
        mControlBarCanShow = show
        updateAllControlBar()
    }

    /**
     * 设置弹幕开关样式
     */
    fun setDanmuText(text: String?) {
//        if (mLargeDanmuTextView != null) {
//            mLargeDanmuTextView.setText(text);
//        }
    }

    /**
     * 设置当前屏幕模式：全屏还是小屏
     *
     * @param mode [AliyunScreenMode.Small]：小屏. [AliyunScreenMode.Full]:全屏
     */
    override fun setScreenModeStatus(mode: AliyunScreenMode) {
        mAliyunScreenMode = mode
        updateLargeInfoBar()
        updateSmallInfoBar()
        updateScreenLockBtn()
        updateScreenModeBtn()
        updateShowMoreBtn()
        updateScreenShotBtn()
        updateScreenRecorderBtn()
        updateDownloadBtn()
    }

    /**
     * 更新下载按钮的显示和隐藏
     */
    fun updateDownloadBtn() {
        if (mAliyunScreenMode === AliyunScreenMode.Full || "localSource" == PlayParameter.PLAY_PARAM_TYPE) {
            mTitleDownload!!.visibility = View.GONE
        } else if (mAliyunScreenMode === AliyunScreenMode.Small || "vidsts" == PlayParameter.PLAY_PARAM_TYPE) {
            mTitleDownload!!.visibility = View.VISIBLE
        }
    }

    /**
     * 更新录屏按钮的显示和隐藏
     */
    private fun updateScreenRecorderBtn() {
//        if (mAliyunScreenMode == AliyunScreenMode.Full) {
//            mScreenRecorder.setVisibility(VISIBLE);
//        } else {
//            mScreenRecorder.setVisibility(GONE);
//        }
        mScreenRecorder!!.visibility = View.GONE
    }

    /**
     * 更新截图按钮的显示和隐藏
     */
    private fun updateScreenShotBtn() {
//        if (mAliyunScreenMode == AliyunScreenMode.Full) {
//            mScreenShot.setVisibility(VISIBLE);
//        } else {
//            mScreenShot.setVisibility(GONE);
//        }
        mScreenShot!!.visibility = View.GONE
    }

    /**
     * 更新更多按钮的显示和隐藏
     */
    private fun updateShowMoreBtn() {
        if (mAliyunScreenMode === AliyunScreenMode.Full) {
            mTitleMore!!.visibility = View.VISIBLE
            mTitleDownload!!.visibility = View.GONE
        } else {
            mTitleMore!!.visibility = View.GONE
            mTitleDownload!!.visibility = View.VISIBLE
        }
    }

    /**
     * 设置主题色
     *
     * @param theme 支持的主题
     */
    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        updateSeekBarTheme(theme)
    }

    /**
     * 设置当前的播放状态
     *
     * @param playState 播放状态
     */
    fun setPlayState(playState: PlayState) {
        mPlayState = playState
        updatePlayStateBtn()
    }

    /**
     * 设置视频信息
     *
     * @param aliyunMediaInfo 媒体信息
     * @param currentQuality  当前清晰度
     */
    fun setMediaInfo(aliyunMediaInfo: MediaInfo, currentQuality: String?) {
        mAliyunMediaInfo = aliyunMediaInfo
        mCurrentQuality = currentQuality
        updateLargeInfoBar()
        updateChangeQualityBtn()
        updateTitleView()
    }

    fun showMoreButton() {
        mTitleMore!!.visibility = View.VISIBLE
    }

    fun hideMoreButton() {
        mTitleMore!!.visibility = View.GONE
    }

    /**
     * 更新当前主题色
     *
     * @param theme 设置的主题色
     */
    private fun updateSeekBarTheme(theme: AliyunVodPlayerView.Theme) {
        //获取不同主题的图片
        var progressDrawableResId = R.drawable.alivc_info_seekbar_bg_blue
        var thumbResId = R.drawable.alivc_info_seekbar_thumb_blue
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            progressDrawableResId = R.drawable.alivc_info_seekbar_bg_blue
            thumbResId = R.drawable.alivc_seekbar_thumb_blue
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            progressDrawableResId = R.drawable.alivc_info_seekbar_bg_green
            thumbResId = R.drawable.alivc_info_seekbar_thumb_green
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            progressDrawableResId = R.drawable.alivc_info_seekbar_bg_orange
            thumbResId = R.drawable.alivc_info_seekbar_thumb_orange
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            progressDrawableResId = R.drawable.alivc_info_seekbar_bg_red
            thumbResId = R.drawable.alivc_info_seekbar_thumb_red
        }


        //这个很有意思。。哈哈。不同的seekbar不能用同一个drawable，不然会出问题。
        // https://stackoverflow.com/questions/12579910/seekbar-thumb-position-not-equals-progress

        //设置到对应控件中
        val resources = resources
        val smallProgressDrawable =
            ContextCompat.getDrawable(context, progressDrawableResId)
        val smallThumb = ContextCompat.getDrawable(context, thumbResId)
        mSmallSeekbar!!.progressDrawable = smallProgressDrawable
        mSmallSeekbar!!.thumb = smallThumb
        val largeProgressDrawable =
            ContextCompat.getDrawable(context, progressDrawableResId)
        val largeThumb = ContextCompat.getDrawable(context, thumbResId)
        mLargeSeekbar!!.progressDrawable = largeProgressDrawable
        mLargeSeekbar!!.thumb = largeThumb
    }

    /**
     * 是否锁屏。锁住的话，其他的操作界面将不会显示。
     *
     * @param screenLocked true：锁屏
     */
    fun setScreenLockStatus(screenLocked: Boolean) {
        mScreenLocked = screenLocked
        updateScreenLockBtn()
        updateAllTitleBar()
        updateAllControlBar()
        updateShowMoreBtn()
        updateScreenShotBtn()
        updateScreenRecorderBtn()
        updateDownloadBtn()
    }

    /**
     * 判断当前播放进度是否在中间广告位置
     */
    //    private boolean isVideoPositionInMiddle(int mVideoPosition){
    //        if(mAdvPosition == MutiSeekBarView.AdvPosition.ALL
    //                || mAdvPosition == MutiSeekBarView.AdvPosition.START_END
    //                || mAdvPosition == MutiSeekBarView.AdvPosition.START_MIDDLE){
    //            return (mVideoPosition >= mSourceDuration / 2 + mAdvDuration) && (mVideoPosition <= mSourceDuration / 2 + mAdvDuration);
    //        }else{
    //            return mVideoPosition >= mSourceDuration / 2 && mVideoPosition <= mSourceDuration / 2;
    //        }
    //    }
    /**
     * 获取视频进度
     *
     * @return 视频进度
     */
    /**
     * 更新视频进度
     *
     * @param position 位置，ms
     */
    var videoPosition: Int
        get() = mVideoPosition
        set(position) {
            mVideoPosition = position
            updateSmallInfoBar()
            updateLargeInfoBar()
        }

    private fun updateAllViews() {
        updateTitleView() //更新标题信息，文字
        updateScreenLockBtn() //更新锁屏状态
        updatePlayStateBtn() //更新播放状态
        updateLargeInfoBar() //更新大屏的显示信息
        updateSmallInfoBar() //更新小屏的显示信息
        updateChangeQualityBtn() //更新分辨率按钮信息
        updateScreenModeBtn() //更新大小屏信息
        updateAllTitleBar() //更新标题显示
        updateAllControlBar() //更新控制栏显示
        updateShowMoreBtn()
        updateScreenShotBtn()
        updateScreenRecorderBtn()
        updateDownloadBtn()
    }

    /**
     * 更新切换清晰度的按钮是否可见，及文字。
     * 当forceQuality的时候不可见。
     */
    private fun updateChangeQualityBtn() {
        if (mLargeChangeQualityBtn != null) {
            VcPlayerLog.d(
                TAG,
                "mCurrentQuality = $mCurrentQuality , isMts Source = $isMtsSource , mForceQuality = $mForceQuality"
            )
            mLargeChangeQualityBtn!!.text = getItem(
                context,
                mCurrentQuality!!,
                isMtsSource
            ).name
            mLargeChangeQualityBtn!!.visibility = if (mForceQuality) View.GONE else View.VISIBLE
        }
    }

    /**
     * 更新控制条的显示
     */
    private fun updateAllControlBar() {
        //单独设置可以显示，并且没有锁屏的时候才可以显示
        val canShow = mControlBarCanShow && !mScreenLocked
        if (mControlBar != null) {
            mControlBar!!.visibility = if (canShow) View.VISIBLE else View.INVISIBLE
        }
    }

    /**
     * 更新标题栏的显示
     */
    private fun updateAllTitleBar() {
        //单独设置可以显示，并且没有锁屏的时候才可以显示
        val canShow = mTitleBarCanShow && !mScreenLocked
        if (mTitleBar != null) {
            mTitleBar!!.visibility = if (canShow) View.VISIBLE else View.INVISIBLE
        }
    }

    /**
     * 更新标题栏的标题文字
     */
    private fun updateTitleView() {
        if (mAliyunMediaInfo != null && mAliyunMediaInfo!!.title != null && "null" != mAliyunMediaInfo!!.title) {
            mTitlebarText!!.text = mAliyunMediaInfo!!.title
        } else {
            mTitlebarText!!.text = ""
        }
    }

    /**
     * 更新小屏下的控制条信息
     */
    private fun updateSmallInfoBar() {
        if (mAliyunScreenMode === AliyunScreenMode.Full) {
            mSmallInfoBar!!.visibility = View.INVISIBLE
        } else if (mAliyunScreenMode === AliyunScreenMode.Small) {
            //先设置小屏的info数据
            if (mAliyunMediaInfo != null) {
                mSmallDurationText!!.text = "/" + formatMs(
                    mAliyunMediaInfo!!.duration.toLong()
                )
                mSmallSeekbar!!.max = mAliyunMediaInfo!!.duration
            } else {
                mSmallDurationText!!.text = "/" + formatMs(0)
                mSmallSeekbar!!.max = 0
            }
            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
                mSmallSeekbar!!.secondaryProgress = mVideoBufferPosition
                mSmallSeekbar!!.progress = mVideoPosition
                mSmallPositionText!!.text = formatMs(mVideoPosition.toLong())
            }
            //然后再显示出来。
            mSmallInfoBar!!.visibility = View.VISIBLE
        }
    }

    /**
     * 更新大屏下的控制条信息
     */
    private fun updateLargeInfoBar() {
        if (mAliyunScreenMode === AliyunScreenMode.Small) {
            //里面包含了很多按钮，比如切换清晰度的按钮之类的
            mLargeInfoBar!!.visibility = View.INVISIBLE
        } else if (mAliyunScreenMode === AliyunScreenMode.Full) {
            //先更新大屏的info数据
            if (mAliyunMediaInfo != null) {
                mLargeDurationText!!.text = "/" + formatMs(
                    mAliyunMediaInfo!!.duration.toLong()
                )
                mLargeSeekbar!!.max = mAliyunMediaInfo!!.duration
            } else {
                mLargeDurationText!!.text = "/" + formatMs(0)
                mLargeSeekbar!!.max = 0
            }
            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
                mLargeSeekbar!!.secondaryProgress = mVideoBufferPosition
                mLargeSeekbar!!.progress = mVideoPosition
                mLargePositionText!!.text = formatMs(mVideoPosition.toLong())
            }
            mLargeChangeQualityBtn!!.text = getItem(
                context,
                mCurrentQuality!!,
                isMtsSource
            ).name
            //然后再显示出来。
            mLargeInfoBar!!.visibility = View.VISIBLE
        }
    }

    /**
     * 更新切换大小屏按钮的信息
     */
    private fun updateScreenModeBtn() {
        if (mAliyunScreenMode === AliyunScreenMode.Full) {
            mScreenModeBtn!!.setImageResource(R.drawable.alivc_screen_mode_small)
        } else {
            mScreenModeBtn!!.setImageResource(R.drawable.alivc_screen_mode_large)
        }
    }

    /**
     * 更新锁屏按钮的信息
     */
    private fun updateScreenLockBtn() {
        if (mScreenLocked) {
            mScreenLockBtn!!.setImageResource(R.drawable.alivc_screen_lock)
        } else {
            mScreenLockBtn!!.setImageResource(R.drawable.alivc_screen_unlock)
        }
        if (mAliyunScreenMode === AliyunScreenMode.Full) {
            mScreenLockBtn!!.visibility = View.VISIBLE
            //            mScreenRecorder.setVisibility(VISIBLE);
//            mScreenShot.setVisibility(VISIBLE);
            mTitleMore!!.visibility = View.VISIBLE
        } else {
            mScreenLockBtn!!.visibility = View.GONE
            //            mScreenRecorder.setVisibility(GONE);
//            mScreenShot.setVisibility(GONE);
            mTitleMore!!.visibility = View.GONE
        }
    }

    /**
     * 更新播放按钮的状态
     */
    private fun updatePlayStateBtn() {
        if (mPlayState == PlayState.NotPlaying) {
            mPlayStateBtn!!.setImageResource(R.drawable.alivc_playstate_play)
        } else if (mPlayState == PlayState.Playing) {
            mPlayStateBtn!!.setImageResource(R.drawable.alivc_playstate_pause)
        }
    }

    /**
     * 监听view是否可见。从而实现5秒隐藏的功能
     */
    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == View.VISIBLE) {
            //如果变为可见了。启动五秒隐藏。
            hideDelayed()
        }
    }

    fun setHideType(hideType: HideType?) {
        mHideType = hideType
    }

    /**
     * 隐藏类
     */
    private class HideHandler(controlView: ControlView) : Handler() {
        private val controlViewWeakReference: WeakReference<ControlView>
        override fun handleMessage(msg: Message) {
            val controlView = controlViewWeakReference.get()
            if (controlView != null) {
                if (!controlView.isSeekbarTouching) {
                    controlView.hide(HideType.Normal)
                }
            }
            super.handleMessage(msg)
        }

        init {
            controlViewWeakReference = WeakReference(controlView)
        }
    }

    private val mHideHandler = HideHandler(this)
    private fun hideDelayed() {
        mHideHandler.removeMessages(WHAT_HIDE)
        mHideHandler.sendEmptyMessageDelayed(
            WHAT_HIDE,
            DELAY_TIME.toLong()
        )
    }

    /**
     * 重置状态
     */
    override fun reset() {
        mHideType = null
        mAliyunMediaInfo = null
        mVideoPosition = 0
        mPlayState = PlayState.NotPlaying
        isSeekbarTouching = false
        updateAllViews()
    }

    /**
     * 显示画面
     */
    override fun show() {
        if (mHideType === HideType.End) {
            //如果是由于错误引起的隐藏，那就不能再展现了
            visibility = View.GONE
            hideQualityDialog()
        } else {
            updateAllViews()
            visibility = View.VISIBLE
        }
    }

    /**
     * 隐藏画面
     */
    override fun hide(hideType: HideType?) {
        if (mHideType !== HideType.End) {
            mHideType = hideType
        }
        visibility = View.GONE
        hideQualityDialog()
    }

    /**
     * 隐藏清晰度对话框
     */
    private fun hideQualityDialog() {
        if (mOnQualityBtnClickListener != null) {
            mOnQualityBtnClickListener!!.onHideQualityView()
        }
    }

    /**
     * 设置当前缓存的进度，给seekbar显示
     *
     * @param mVideoBufferPosition 进度，ms
     */
    fun setVideoBufferPosition(mVideoBufferPosition: Int) {
        this.mVideoBufferPosition = mVideoBufferPosition
        updateSmallInfoBar()
        updateLargeInfoBar()
    }

    fun setOnMenuClickListener(l: OnMenuClickListener?) {
        mOnMenuClickListener = l
    }

    interface OnMenuClickListener {
        /**
         * 按钮点击事件
         */
        fun onMenuClick()
    }

    interface OnDownloadClickListener {
        /**
         * 下载点击事件
         */
        fun onDownloadClick()
    }

    fun setOnDownloadClickListener(
        onDownloadClickListener: OnDownloadClickListener?
    ) {
        this.onDownloadClickListener = onDownloadClickListener
    }

    fun setOnQualityBtnClickListener(l: OnQualityBtnClickListener?) {
        mOnQualityBtnClickListener = l
    }

    interface OnQualityBtnClickListener {
        /**
         * 清晰度按钮被点击
         *
         * @param v              被点击的view
         * @param qualities      支持的清晰度
         * @param currentQuality 当前清晰度
         */
        fun onQualityBtnClick(
            v: View,
            qualities: List<TrackInfo>,
            currentQuality: String
        )

        /**
         * 隐藏
         */
        fun onHideQualityView()
    }

    fun setOnScreenLockClickListener(l: OnScreenLockClickListener?) {
        mOnScreenLockClickListener = l
    }

    interface OnScreenLockClickListener {
        /**
         * 锁屏按钮点击事件
         */
        fun onClick()
    }

    fun setOnScreenModeClickListener(l: OnScreenModeClickListener?) {
        mOnScreenModeClickListener = l
    }

    interface OnScreenModeClickListener {
        /**
         * 大小屏按钮点击事件
         */
        fun onClick()
    }

    fun setOnBackClickListener(l: OnBackClickListener?) {
        mOnBackClickListener = l
    }

    interface OnBackClickListener {
        /**
         * 返回按钮点击事件
         */
        fun onClick()
    }

    interface OnSeekListener {
        /**
         * seek结束事件
         */
        fun onSeekEnd(position: Int)

        /**
         * seek开始事件
         */
        fun onSeekStart(position: Int)

        /**
         * seek进度改变事件
         */
        fun onProgressChanged(progress: Int)
    }

    fun setOnSeekListener(onSeekListener: OnSeekListener?) {
        mOnSeekListener = onSeekListener
    }

    /**
     * 播放状态
     */
    enum class PlayState {
        /**
         * Playing:正在播放
         * NotPlaying: 停止播放
         */
        Playing, NotPlaying
    }

    interface OnPlayStateClickListener {
        /**
         * 播放按钮点击事件
         */
        fun onPlayStateClick()
    }

    fun setOnPlayStateClickListener(onPlayStateClickListener: OnPlayStateClickListener?) {
        mOnPlayStateClickListener = onPlayStateClickListener
    }

    /**
     * 横屏下显示更多
     */
    interface OnShowMoreClickListener {
        fun showMore()
    }

    fun setOnShowMoreClickListener(
        listener: OnShowMoreClickListener
    ) {
        mOnShowMoreClickListener = listener
    }

    /**
     * 屏幕截图
     */
    interface OnScreenShotClickListener {
        fun onScreenShotClick()
    }

    fun setOnScreenShotClickListener(listener: OnScreenShotClickListener?) {
        mOnScreenShotClickListener = listener
    }

    /**
     * 录制
     */
    interface OnScreenRecoderClickListener {
        fun onScreenRecoderClick()
    }

    fun setOnScreenRecoderClickListener(listener: OnScreenRecoderClickListener?) {
        mOnScreenRecoderClickListener = listener
    }

    companion object {
        private val TAG = ControlView::class.java.simpleName
        private const val WHAT_HIDE = 0
        private const val DELAY_TIME = 5 * 1000 //5秒后隐藏
    }
}