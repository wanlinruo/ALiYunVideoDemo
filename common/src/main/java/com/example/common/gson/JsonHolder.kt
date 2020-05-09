package com.example.common.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 作者：CZY
 * 时间：2019/01/20 17:20
 * 描述：
 */

//外部直接引用 serializableHolder 开放出来的接口来进行序列化即可
//即使以后序列化方案有变，直接改变其指向的变量即可
val JsonHolder: ISerializableHolder = LocalSerializableHolder

//以下的各个入参本来是不应该将之设为可空类型的，但为了兼容 Java 代码，所以才这样做
//但外部还是应该尽量控制入参类型为非空
sealed class ISerializableHolder {

    abstract fun toJson(ob: Any?): String

    //获取包含换行缩进形式的 Json
    abstract fun toPrettyJson(ob: Any?): String

    //可能会抛出异常
    abstract fun <T> toBean(json: String?, clazz: Class<T>): T

    //可能会抛出异常
    abstract fun <T> toBean(json: String?, type: Type): T

    //可能会抛出异常
    abstract fun <T> toBean(jsonObject: JsonObject?, clazz: Class<T>): T

    //不会抛出异常，但当序列化失败时会返回 null
    abstract fun <T> toBeanOrNull(json: String?, type: Class<T>): T?

    abstract fun <T> toBeanList(json: String?, clazz: Class<T>): MutableList<T>

}

//这是目前选择的序列化方案
private object LocalSerializableHolder : ISerializableHolder() {

    private val gson = Gson()

    private val prettyGson = GsonBuilder().setPrettyPrinting().create()

    override fun toJson(ob: Any?): String {
        try {
            return gson.toJson(ob)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ""
    }

    override fun toPrettyJson(ob: Any?): String {
        if (ob == null) {
            return ""
        }
        try {
            val toJson = prettyGson.toJson(ob)
            val jsonParser = JsonParser.parseString(toJson)
            return when {
                toJson.startsWith("{") -> {
                    prettyGson.toJson(jsonParser.asJsonObject)
                }
                toJson.startsWith("[") -> {
                    prettyGson.toJson(jsonParser.asJsonArray)
                }
                else -> {
                    ob.toString()
                }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return ob.toString()
    }

    override fun <T> toBean(json: String?, clazz: Class<T>): T {
        return gson.fromJson<T>(json, clazz)
    }

    override fun <T> toBean(json: String?, type: Type): T {
        return gson.fromJson<T>(json, type)
    }

    override fun <T> toBean(jsonObject: JsonObject?, clazz: Class<T>): T {
        return gson.fromJson<T>(jsonObject, clazz)
    }

    override fun <T> toBeanOrNull(json: String?, type: Class<T>): T? {
        try {
            return gson.fromJson<T>(json, type)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    override fun <T> toBeanList(json: String?, clazz: Class<T>): MutableList<T> {
        try {
            val type: Type = ParameterizedTypeImpl(clazz)
            return gson.fromJson(json, type) ?: mutableListOf()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return mutableListOf()
    }

    private class ParameterizedTypeImpl constructor(private val clazz: Class<*>) :
        ParameterizedType {

        override fun getActualTypeArguments(): Array<Type> {
            return arrayOf(clazz)
        }

        override fun getRawType(): Type {
            return MutableList::class.java
        }

        override fun getOwnerType(): Type? {
            return null
        }

    }

}