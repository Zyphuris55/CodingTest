package com.lasley.kts_viewer.data

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Album(
    val name: String? = null,
    @SerializedName("artist_id")
    val artistID: String
) : CommonInf() {

    @SerializedName("album_id")
    override var uuid: String = UUID.randomUUID().toString()

    val artist: Artist?
        get() = LocalDatabase[artistID] as? Artist

    val artistName: String
        get() = artist?.name.orEmpty()

    companion object {
        fun create(name: String, artist: Artist): Album {
            return Album(name, artist.uuid).apply {
                uuid = UUID.randomUUID().toString()
                artist.addAlbum(this)
            }
        }
    }
}