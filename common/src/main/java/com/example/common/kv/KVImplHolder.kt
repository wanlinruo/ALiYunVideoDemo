package com.example.common.kv

import android.content.Context
import android.text.TextUtils
import com.example.common.gson.JsonHolder
import com.tencent.mmkv.MMKV
import com.tencent.mmkv.MMKVLogLevel

/**
 * 作者：leavesC
 * 时间：2020/3/7 22:04
 * 描述：
 */
/**
 * @param context
 * @param selfGroup 用于指定数据分组，不同分组下的数据互不关联
 * @param encryptKey 加密 key，如果为空则表示不进行加密
 */
sealed class BaseMMKVKVHolder constructor(
    context: Context,
    selfGroup: String,
    encryptKey: String
) : IKVHolder {

    init {
        MMKV.initialize(context)
        MMKV.setLogLevel(MMKVLogLevel.LevelNone)
    }

    final override val keyGroup: String = selfGroup

    override val verifyBeforePut: (key: String) -> Boolean = {
        true
    }

    private val kv =
        if (encryptKey.isBlank()) MMKV.mmkvWithID(
            keyGroup,
            MMKV.MULTI_PROCESS_MODE
        ) else MMKV.mmkvWithID(keyGroup, MMKV.MULTI_PROCESS_MODE, encryptKey)

    override fun put(key: String, value: String) {
        if (verifyBeforePut(key)) {
            kv.putString(key, value)
        }
    }

    override fun put(key: String, value: Int) {
        if (verifyBeforePut(key)) {
            kv.putInt(key, value)
        }
    }

    override fun put(key: String, value: Long) {
        if (verifyBeforePut(key)) {
            kv.putLong(key, value)
        }
    }

    override fun put(key: String, value: Float) {
        if (verifyBeforePut(key)) {
            kv.putFloat(key, value)
        }
    }

    override fun put(key: String, value: Boolean) {
        if (verifyBeforePut(key)) {
            kv.putBoolean(key, value)
        }
    }

    override fun <T : Any> putBean(key: String, value: T) {
        put(key, JsonHolder.toJson(value))
    }

    override fun <T : Any> putList(key: String, value: List<T>) {
        put(key, JsonHolder.toJson(value))
    }

    override fun get(key: String, default: String): String {
        return kv.getString(key, default) ?: ""
    }

    override fun get(key: String, default: Int): Int {
        return kv.getInt(key, default)
    }

    override fun get(key: String, default: Long): Long {
        return kv.getLong(key, default)
    }

    override fun get(key: String, default: Float): Float {
        return kv.getFloat(key, default)
    }

    override fun get(key: String, default: Boolean): Boolean {
        return kv.getBoolean(key, default)
    }

    @Throws(Exception::class)
    override fun <T : Any> getBean(key: String, clazz: Class<T>): T {
        if (!containsKey(key)) {
            throw Exception("BaseMMKVKVHolder 不存在此 key：$key")
        }
        val json = get(key, "")
        if (json.isBlank()) {
            throw Exception("BaseMMKVKVHolder json 值为空，key：$key")
        }
        return JsonHolder.toBean(json, clazz)
    }

    override fun <T : Any> getBeanOrNull(key: String, clazz: Class<T>): T? {
        try {
            val json = get(key, "")
            if (json.isNotBlank()) {
                return JsonHolder.toBean(json, clazz)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    override fun <T : Any> getList(key: String, clazz: Class<T>): List<T> {
        try {
            val json = get(key, "")
            if (json.isNotBlank()) {
                return JsonHolder.toBeanList(json, clazz)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return mutableListOf()
    }

    override fun remove(vararg keys: String) {
        kv.removeValuesForKeys(keys)
    }

    override fun containsKey(key: String): Boolean {
        return kv.containsKey(key)
    }

    override fun allKeyValue(): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        kv.allKeys()?.forEach {
            map[it] = getObjectValue(kv, it)
        }
        return map
    }

    private fun getObjectValue(
        mmkv: MMKV,
        key: String
    ): Any? { // 因为其他基础类型value会读成空字符串,所以不是空字符串即为string or string-set类型
        try {
            val value = mmkv.decodeString(key)
            if (!TextUtils.isEmpty(value)) { // 判断 string or string-set
                return if (value[0].toInt() == 0x01) {
                    mmkv.decodeStringSet(key)
                } else {
                    value
                }
            }
            // float double类型可通过string-set配合判断
            // 通过数据分析可以看到类型为float或double时string类型为空字符串且string-set类型读出空数组
            // 最后判断float为0或NAN的时候可以直接读成double类型,否则读float类型
            // 该判断方法对于非常小的double类型数据 (0d < value <= 1.0569021313E-314) 不生效
            val set = mmkv.decodeStringSet(key)
            if (set != null && set.size == 0) {
                val valueFloat = mmkv.decodeFloat(key)
                val valueDouble = mmkv.decodeDouble(key)
                return if (valueFloat.compareTo(0f) == 0 || valueFloat.compareTo(Float.NaN) == 0) {
                    valueDouble
                } else {
                    valueFloat
                }
            }
            // int long bool 类型的处理放在一起, int类型1和0等价于bool类型true和false
            // 判断long或int类型时, 如果数据长度超出int的最大长度, 则long与int读出的数据不等, 可确定为long类型
            val valueInt = mmkv.decodeInt(key)
            val valueLong = mmkv.decodeLong(key)
            return if (valueInt.toLong() != valueLong) {
                valueLong
            } else {
                valueInt
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return "failed get"
    }

    override fun clear() {
        kv.clearAll()
    }

}

class MMKVKVHolder constructor(context: Context, selfGroup: String, encryptKey: String = "") :
    BaseMMKVKVHolder(context, selfGroup, encryptKey)

//保存后不支持改变 key 对应的 value
class MMKVKVFinalHolder constructor(context: Context, selfGroup: String, encryptKey: String = "") :
    BaseMMKVKVHolder(context, selfGroup, encryptKey) {

    override val verifyBeforePut: (key: String) -> Boolean = {
        !containsKey(it)
    }

}