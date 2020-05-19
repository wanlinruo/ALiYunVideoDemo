package com.example.player.util

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Message
import android.widget.Toast
import java.lang.reflect.Field

/**
 * Toast工具类, 主要修复Toast在android 7.1手机上的BadTokenExceptiion
 *
 * @author xlx
 */
object FixedToastUtils {

    private var mFieldTN: Field = Toast::class.java.getDeclaredField("mTN")

    private var mFieldTNHandler: Field = mFieldTN.type.getDeclaredField("mHandler")

    private var mToast: Toast? = null

    init {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                mFieldTN.isAccessible = true
                mFieldTNHandler.isAccessible = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun show(context: Context, message: String?): Toast? {
        if (mToast == null) {
            mToast = Toast.makeText(context.applicationContext, message, Toast.LENGTH_SHORT)
            if (Build.VERSION.SDK_INT == 25) {
                hook(mToast)
            }
        } else {
            mToast!!.duration = Toast.LENGTH_SHORT
            mToast!!.setText(message)
        }
        mToast!!.show()
        return mToast
    }

    fun show(context: Context, resId: Int): Toast? {
        return show(context, context.resources.getString(resId))
    }

    private fun hook(toast: Toast?) {
        try {
            val tn = mFieldTN[toast]
            val preHandler = mFieldTNHandler[tn] as Handler
            mFieldTNHandler[tn] = FiexHandler(preHandler)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private class FiexHandler internal constructor(private val impl: Handler) :
        Handler() {
        override fun dispatchMessage(msg: Message) {
            try {
                super.dispatchMessage(msg)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun handleMessage(msg: Message) {
            impl.handleMessage(msg)
        }

    }
}