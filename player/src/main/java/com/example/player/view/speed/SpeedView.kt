package com.example.player.view.speed

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.aliyun.utils.VcPlayerLog
import com.example.player.R
import com.example.player.theme.ITheme
import com.example.player.view.speed.SpeedView
import com.example.player.widget.AliyunScreenMode
import com.example.player.widget.AliyunVodPlayerView

/*
 * Copyright (C) 2010-2018 Alibaba Group Holding Limited.
 */
/**
 * 倍速播放界面。用于控制倍速。
 * 在[AliyunVodPlayerView]中使用。
 */
class SpeedView : RelativeLayout, ITheme {
    private var mSpeedValue: SpeedValue? = null
    private var mMainSpeedView: View = findViewById(R.id.speed_view)

    //显示动画
    private var showAnim: Animation = AnimationUtils.loadAnimation(context, R.anim.view_speed_show)

    //隐藏动画
    private var hideAnim: Animation = AnimationUtils.loadAnimation(context, R.anim.view_speed_hide)

    //动画是否结束
    private var animEnd = true

    // 正常倍速
    private var mNormalBtn: RadioButton? = null

    //1.25倍速
    private var mOneQrtTimeBtn: RadioButton? = null

    //1.5倍速
    private var mOneHalfTimeBtn: RadioButton? = null

    //2倍速
    private var mTwoTimeBtn: RadioButton? = null

    //切换结果的提示
    private var mSpeedTip: TextView? = null

    //屏幕模式
    private var mScreenMode: AliyunScreenMode? = null

    //倍速选择事件
    private var mOnSpeedClickListener: OnSpeedClickListener? = null

    //倍速是否变化
    private var mSpeedChanged = false

    //选中的倍速的指示点的方块
    private var mSpeedDrawable = R.drawable.alivc_speed_dot_blue

    //选中的倍速的指示点的文字
    private var mSpeedTextColor = R.color.alivc_player_theme_blue

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
        //初始化布局
        LayoutInflater.from(context).inflate(R.layout.alivc_view_speed, this, true)
        mMainSpeedView.visibility = View.INVISIBLE

        //找出控件
        mOneQrtTimeBtn =
            findViewById<View>(R.id.one_quartern) as RadioButton
        mNormalBtn = findViewById<View>(R.id.normal) as RadioButton
        mOneHalfTimeBtn =
            findViewById<View>(R.id.one_half) as RadioButton
        mTwoTimeBtn = findViewById<View>(R.id.two) as RadioButton
        mSpeedTip = findViewById<View>(R.id.speed_tip) as TextView
        mSpeedTip!!.visibility = View.INVISIBLE

        //对每个倍速项做点击监听
        mOneQrtTimeBtn!!.setOnClickListener(mClickListener)
        mNormalBtn!!.setOnClickListener(mClickListener)
        mOneHalfTimeBtn!!.setOnClickListener(mClickListener)
        mTwoTimeBtn!!.setOnClickListener(mClickListener)

        //倍速view使用到的动画
        showAnim = AnimationUtils.loadAnimation(context, R.anim.view_speed_show)
        hideAnim = AnimationUtils.loadAnimation(context, R.anim.view_speed_hide)
        showAnim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                //显示动画开始的时候，将倍速view显示出来
                animEnd = false
                mMainSpeedView.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                animEnd = true
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        hideAnim.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                animEnd = false
            }

            override fun onAnimationEnd(animation: Animation) {

                //隐藏动画结束的时候，将倍速view隐藏掉
                mMainSpeedView.visibility = View.INVISIBLE
                if (mOnSpeedClickListener != null) {
                    mOnSpeedClickListener!!.onHide()
                }

                //如果倍速有变化，会提示倍速变化的消息
                if (mSpeedChanged) {
                    var times = ""
                    if (mSpeedValue == SpeedValue.OneQuartern) {
                        times = resources.getString(R.string.alivc_speed_optf_times)
                    } else if (mSpeedValue == SpeedValue.Normal) {
                        times = resources.getString(R.string.alivc_speed_one_times)
                    } else if (mSpeedValue == SpeedValue.OneHalf) {
                        times = resources.getString(R.string.alivc_speed_opt_times)
                    } else if (mSpeedValue == SpeedValue.Twice) {
                        times = resources.getString(R.string.alivc_speed_twice_times)
                    }
                    val tips =
                        context.getString(R.string.alivc_speed_tips, times)
                    mSpeedTip!!.text = tips
                    mSpeedTip!!.visibility = View.VISIBLE
                    mSpeedTip!!.postDelayed({ mSpeedTip!!.visibility = View.INVISIBLE }, 1000)
                }
                animEnd = true
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })
        setSpeed(SpeedValue.Normal)
        //监听view的Layout事件
        viewTreeObserver.addOnGlobalLayoutListener(MyLayoutListener())
    }

    /**
     * 设置主题
     *
     * @param theme 支持的主题
     */
    override fun setTheme(theme: AliyunVodPlayerView.Theme) {
        mSpeedDrawable = R.drawable.alivc_speed_dot_blue
        mSpeedTextColor = R.color.alivc_player_theme_blue
        //根据主题变化对应的颜色
        if (theme === AliyunVodPlayerView.Theme.Blue) {
            mSpeedDrawable = R.drawable.alivc_speed_dot_blue
            mSpeedTextColor = R.color.alivc_player_theme_blue
        } else if (theme === AliyunVodPlayerView.Theme.Green) {
            mSpeedDrawable = R.drawable.alivc_speed_dot_green
            mSpeedTextColor = R.color.alivc_player_theme_green
        } else if (theme === AliyunVodPlayerView.Theme.Orange) {
            mSpeedDrawable = R.drawable.alivc_speed_dot_orange
            mSpeedTextColor = R.color.alivc_player_theme_orange
        } else if (theme === AliyunVodPlayerView.Theme.Red) {
            mSpeedDrawable = R.drawable.alivc_speed_dot_red
            mSpeedTextColor = R.color.alivc_player_theme_red
        }
        updateBtnTheme()
    }

    /**
     * 更新按钮的颜色之类的
     */
    private fun setRadioButtonTheme(button: RadioButton?) {
        if (button!!.isChecked) {
            button.setCompoundDrawablesWithIntrinsicBounds(0, mSpeedDrawable, 0, 0)
            button.setTextColor(ContextCompat.getColor(context, mSpeedTextColor))
        } else {
            button.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            button.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.alivc_common_font_white_light
                )
            )
        }
    }

    private inner class MyLayoutListener : OnGlobalLayoutListener {
        private var lastLayoutMode: AliyunScreenMode? = null
        override fun onGlobalLayout() {
            if (mMainSpeedView.visibility == View.VISIBLE) {

                //防止重复设置
                if (lastLayoutMode === mScreenMode) {
                    return
                }
                setScreenMode(mScreenMode)
                lastLayoutMode = mScreenMode
            }
        }
    }

    private val mClickListener =
        OnClickListener { view ->
            if (mOnSpeedClickListener == null) {
                return@OnClickListener
            }
            if (view === mNormalBtn) {
                mOnSpeedClickListener!!.onSpeedClick(SpeedValue.Normal)
            } else if (view === mOneQrtTimeBtn) {
                mOnSpeedClickListener!!.onSpeedClick(SpeedValue.OneQuartern)
            } else if (view === mOneHalfTimeBtn) {
                mOnSpeedClickListener!!.onSpeedClick(SpeedValue.OneHalf)
            } else if (view === mTwoTimeBtn) {
                mOnSpeedClickListener!!.onSpeedClick(SpeedValue.Twice)
            }
        }

    /**
     * 设置倍速点击事件
     *
     * @param l
     */
    fun setOnSpeedClickListener(l: OnSpeedClickListener?) {
        mOnSpeedClickListener = l
    }

    /**
     * 设置当前屏幕模式。不同的模式，speedView的大小不一样
     *
     * @param screenMode
     */
    fun setScreenMode(screenMode: AliyunScreenMode?) {
        val speedViewParam = mMainSpeedView.layoutParams
        if (screenMode === AliyunScreenMode.Small) {
            //小屏的时候，是铺满整个播放器的
            speedViewParam.width = width
            speedViewParam.height = height
        } else if (screenMode === AliyunScreenMode.Full) {
            //如果是全屏的，就显示一半
            val parentView = parent as AliyunVodPlayerView
            val lockPortraitListener = parentView.lockPortraitMode
            if (lockPortraitListener == null) {
                //没有设置这个监听，说明不是固定模式，按正常的界面显示就OK
                speedViewParam.width = width / 2
            } else {
                speedViewParam.width = width
            }
            speedViewParam.height = height
        }
        VcPlayerLog.d(
            TAG,
            "setScreenModeStatus screenMode = " + screenMode!!.name + " , width = " + speedViewParam.width + " , height = " + speedViewParam.height
        )
        mScreenMode = screenMode
        mMainSpeedView.layoutParams = speedViewParam
    }

    /**
     * 倍速监听
     */
    interface OnSpeedClickListener {
        /**
         * 选中某个倍速
         *
         * @param value 倍速值
         */
        fun onSpeedClick(value: SpeedValue?)

        /**
         * 倍速界面隐藏
         */
        fun onHide()
    }

    /**
     * 倍速值
     */
    enum class SpeedValue {
        /**
         * 正常倍速
         */
        Normal,

        /**
         * 1.25倍速
         */
        OneQuartern,

        /**
         * 1.5倍速
         */
        OneHalf,

        /**
         * 2倍速
         */
        Twice
    }

    /**
     * 设置显示的倍速
     *
     * @param speedValue 倍速值
     */
    fun setSpeed(speedValue: SpeedValue?) {
        if (speedValue == null) {
            return
        }
        if (mSpeedValue != speedValue) {
            mSpeedValue = speedValue
            mSpeedChanged = true
            updateSpeedCheck()
        } else {
            mSpeedChanged = false
        }
        hide()
    }

    /**
     * 更新倍速选项的状态
     */
    private fun updateSpeedCheck() {
        mOneQrtTimeBtn!!.isChecked = mSpeedValue == SpeedValue.OneQuartern
        mNormalBtn!!.isChecked = mSpeedValue == SpeedValue.Normal
        mOneHalfTimeBtn!!.isChecked = mSpeedValue == SpeedValue.OneHalf
        mTwoTimeBtn!!.isChecked = mSpeedValue == SpeedValue.Twice
        updateBtnTheme()
    }

    /**
     * 更新选项的Theme
     */
    private fun updateBtnTheme() {
        setRadioButtonTheme(mNormalBtn)
        setRadioButtonTheme(mOneQrtTimeBtn)
        setRadioButtonTheme(mOneHalfTimeBtn)
        setRadioButtonTheme(mTwoTimeBtn)
    }

    /**
     * 显示倍速view
     *
     * @param screenMode 屏幕模式
     */
    fun show(screenMode: AliyunScreenMode?) {
        setScreenMode(screenMode)
        mMainSpeedView.startAnimation(showAnim)
    }

    /**
     * 隐藏
     */
    private fun hide() {
        if (mMainSpeedView.visibility == View.VISIBLE) {
            mMainSpeedView.startAnimation(hideAnim)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        //动画没有结束的时候，触摸是没有效果的
        if (mMainSpeedView.visibility == View.VISIBLE && animEnd) {
            hide()
            return true
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private val TAG = SpeedView::class.java.simpleName
    }
}