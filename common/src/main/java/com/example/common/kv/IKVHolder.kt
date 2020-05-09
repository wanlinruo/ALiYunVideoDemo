package com.example.common.kv

/**
 * 作者：leavesC
 * 时间：2019/03/02 21:05
 * 描述：
 */
interface IBaseKVHolder {

    fun get(key: String, default: String): String

    fun get(key: String, default: Int): Int

    fun get(key: String, default: Long): Long

    fun get(key: String, default: Float): Float

    fun get(key: String, default: Boolean): Boolean

    //当 key 不存在或反序列化失败时会抛出异常
    @Throws(Exception::class)
    fun <T : Any> getBean(key: String, clazz: Class<T>): T

    //不会抛出异常，而是返回 null
    fun <T : Any> getBeanOrNull(key: String, clazz: Class<T>): T?

    fun <T : Any> getList(key: String, clazz: Class<T>): List<T>

    fun containsKey(key: String): Boolean

    fun remove(vararg keys: String)

    fun clear()

}

interface IKVHolder : IBaseKVHolder {

    //数据分组，用于标明不同范围内的数据缓存
    val keyGroup: String

    val verifyBeforePut: ((key: String) -> Boolean)

    fun put(key: String, value: String)

    fun put(key: String, value: Int)

    fun put(key: String, value: Long)

    fun put(key: String, value: Float)

    fun put(key: String, value: Boolean)

    fun <T : Any> putBean(key: String, value: T)

    fun <T : Any> putList(key: String, value: List<T>)

    fun allKeyValue(): Map<String, Any?>

}