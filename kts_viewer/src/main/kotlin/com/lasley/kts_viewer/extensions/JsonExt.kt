package com.lasley.kts_viewer.extensions

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

/**
 * Converts [this] to a standard Json string
 */
fun Any.toJson(): String = Gson().toJson(this)

// ==== From Json to Generic
/**
 * Converts a [json] input to the expected output [T]
 *
 * @param onFinish Returns the result
 * - On parsing failure, [Exception][Pair.second] will be set with the [data][Pair.first] being null
 */
inline fun <reified T> Gson.fromJson(
    json: String?,
    onFinish: (Pair<T?, Exception?>) -> Unit = {}
): T? {
    if (json == null) return null
    var data: T? = null
    var error: Exception? = null
    try {
        data = fromJson(json, object : TypeToken<T>() {}.type)
    } catch (e: Exception) {
        error = e
    }
    onFinish(data to error)

    return data
}

/**
 * Converts [this] as the class [T]
 */
inline fun <reified T> String.fromJson(): T? = Gson().fromJson(this)

/**
 * Attempts to parse [item] as this [type][T].
 *
 * @return [null][T] if [item] is null or  unable to parse the data to [type][T]
 */
inline fun <reified T> Gson.fromJson(item: Any?): T? = fromJson(item.toString())

/**
 * Attempts to parse [jsonObj] as this [type][T].
 *
 * @return [null][T] if [jsonObj] is null or unable to parse the data to [type][T]
 */
inline fun <reified T> Gson.fromJson(jsonObj: JSONObject?): T? = fromJson(jsonObj.toString())

/**
 * Attempts to parse [this] as this [type][T].
 *
 * @return [null][T] if [this] is null or unable to parse the data to [type][T]
 */
inline fun <reified T> JSONObject?.fromJson(): T? = Gson().fromJson(toString())