package com.lasley.kts_viewer.data

import android.graphics.Color

enum class ActionColor(val color: Int) {
    /** green-ish for "ok, you can edit" */
    Edit(Color.argb(20, 76, 175, 80)),

    /** yellow-ish for "please wait..." */
    Loading(Color.argb(20, 255, 193, 7)),

    /** red for "there was an error */
    Error(Color.argb(20, 244, 67, 54))
}

enum class SaveResult {
    Waiting, Success, Error, Locked
}