package com.example.common.extend


/**
 * 作者：CZY
 * 时间：2020/5/6 17:07
 * 描述：
 */
fun getSign(encryption: Map<String, Any>?): Map<String, Any> {
    return EncryptionUtils.setEncryption(encryption)
}

internal object EncryptionUtils {

    //Android固定值
    private const val ANDROID_VALUE_T = "b1522de9a4860f5a"

    fun setEncryption(encryption: Map<String, Any>?): Map<String, Any> {
        val time = System.currentTimeMillis().toString()
        val param = mutableListOf<String>()
        param.add(time)
        if (encryption != null) {
            val iterator = encryption.entries.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                param.add(next.value.toString())
            }
        }
        param.add(ANDROID_VALUE_T)
        param.sort()
        val map = mutableMapOf<String, Any>()
        map["sign"] = MD5Util.md5(toAddStr(param))
        map["timestamp"] = time
        return map
    }

}