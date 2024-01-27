package com.lasley.kts_viewer.helpers

import android.database.Cursor
import com.lasley.kts_viewer.extensions.fromJson

fun Cursor?.toJsonSeq(): Sequence<String> {
    if (this == null) return emptySequence()

    if (count == 0) return emptySequence()
    if (position < -1) moveToPosition(-1)
    return generateSequence {
        if (!moveToNext()) return@generateSequence null
        val contents = columnNames
            .mapIndexed { index, name -> name to getString(index) }
            .joinToString { (key, value) -> """"$key":"$value"""" }
        "{$contents}"
    }
}

inline fun <reified T> Cursor?.toObjSeq(): Sequence<T> {
    return toJsonSeq().mapNotNull { it.fromJson() }
}