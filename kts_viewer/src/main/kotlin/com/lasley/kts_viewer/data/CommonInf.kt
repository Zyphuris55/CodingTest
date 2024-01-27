package com.lasley.kts_viewer.data

import android.content.ContentValues
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lasley.kts_viewer.extensions.toJson
import java.util.UUID

abstract class CommonInf {
    abstract var uuid: String

    var createdTime: Long = 0
        private set
    var updatedTime: Long = 0
        private set

    val dataType: String
        get() = when (this) {
            is Album -> "album"
            is Artist -> "artist"
            else -> ""
        }

    val toContentValues: ContentValues
        get() {
            val values = ContentValues()
            Gson().toJsonTree(this).asJsonObject.asMap()
                .mapValues { it.value.toString() }
                .filter { it.value.isEmpty() }.toMutableMap()
                .forEach { values.put(it.key, it.value) }

            return values
        }

    /**
     * Saves [this] object as a [ContentValues] with the following contents:
     * - [content][String]: This object as a json
     * - [type][String]: What type of data content contains
     *    - "album"
     *    - "artist"
     */
    val asContentData: ContentValues
        get() {
            val values = ContentValues()
            values.put("type", dataType)
            // store object as a string, no need to deal with parsing columns
            values.put("content", toJson())
            return values
        }

    init {
        createdTime = System.currentTimeMillis()
        updatedTime = System.currentTimeMillis()
    }

    fun updateTime() {
        updatedTime = System.currentTimeMillis()
    }
}