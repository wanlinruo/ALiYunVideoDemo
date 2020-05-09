package com.example.common.kv

import android.content.Context
import android.os.Build
import com.example.common.BuildConfig
import com.example.common.extend.ContextExtend

/**
 * 作者：leavesC
 * 时间：2020/3/8 21:29
 * 描述：
 */
//直接引用 IKVHolder 开放出来的接口来进行存储即可
//即使以后缓存方案有变，直接改变其指向的变量即可
//MMKVKVHolder 的第二个参数 selfGroup 用于指定数据分组，不同分组之间的数据互不关联，即不同分组的数据可以定义同个 key

private val context: Context
    get() = ContextExtend.appContext

//和用户强绑定的数据，在退出登录时需要全部清除，例如用户ID
private val userKVHolder: IKVHolder =
    MMKVKVHolder(context, "user", "2f7dcb865c658d0c57cebbbc30f94bb8")

//和用户不强关联的数据，在退出登录时无需清除，例如夜间模式、字体大小等
private val preferenceKVHolder: IKVHolder =
    MMKVKVHolder(context, "preference")

//用于存储不会二次变更只用于历史溯源的数据，例如首次安装app的时间、版本号、版本名等
//只有不包含此 key 时，value 才会被存储。finalKVHolder 本身已经加上了判重逻辑，使用者无需主动去重，直接赋值即可
//需要注意，此类数据应该尽量只在 FinalKV 的 init 代码块中进行赋值操作
//因为 FinalKV 本身是一个 object 单例，当应用启动时 init 代码块会自动被执行，无需外部调用
private val finalKVHolder: IKVHolder =
    MMKVKVFinalHolder(context, "final")

fun init() {
    //以下三行代码本身没有实际意义，只是为了触发 FinalKV 进行初始化，所以不能删除以下代码
    FinalKV.firstVersionCode.toString()
    FinalKV.firstVersionName.toString()
    FinalKV.firstInstallTime.toString()
}

object UserKV : IKVHolder by userKVHolder {

    //存储上传凭证
    var saveUploadAuth: String
        get() = PreferenceKV.get("saveUploadAuth", "")
        set(value) = PreferenceKV.put("saveUploadAuth", value)
}

object PreferenceKV : IKVHolder by preferenceKVHolder {

}

//由于 object 貌似是等到外部主动调用了它才开始进行初始化的
//所以 FinalKV 正式启用的时间应该是 2020/04/19  v5.3.10
object FinalKV : IKVHolder by finalKVHolder {

    var firstInstallTime: Long
        get() = get("firstInstallTime", System.currentTimeMillis())
        private set(value) {
            put("firstInstallTime", value)
        }

    var firstVersionCode: Int
        get() = get("firstVersionCode", BuildConfig.VERSION_CODE)
        private set(value) = put("firstVersionCode", value)

    var firstVersionName: String
        get() = get("firstVersionName", BuildConfig.VERSION_NAME)
        private set(value) = put("firstVersionName", value)

    var systemVersion: Int
        get() = get("systemVersion", 0)
        private set(value) = put("systemVersion", value)

    init {
        firstInstallTime = System.currentTimeMillis()
        firstVersionCode = BuildConfig.VERSION_CODE
        firstVersionName = BuildConfig.VERSION_NAME
        systemVersion = Build.VERSION.SDK_INT
    }

    override fun toString(): String {
        return "firstInstallTime: $firstInstallTime firstVersionCode: $firstVersionCode firstVersionName: $firstVersionName"
    }

}