package com.lasley.kts_viewer.data

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Artist(
    var name: String = ""
) : CommonInf() {

    @SerializedName("artist_id")
    override var uuid: String = UUID.randomUUID().toString()

    @SerializedName("album_ids")
    var albumIDs = ""
        private set

    val albums: List<Album>
        get() = albumID_List.mapNotNull { LocalDatabase[it] as? Album }

    val albumID_List: List<String>
        get() = albumIDs.split(",")

    fun addAlbum(vararg album: Album) {
        albumIDs = (albumID_List + album.map { it.uuid }).distinct().joinToString(",")
    }

    /**
     * Removes a single instance of the specified element from this collection, if it is present.
     *
     * @return
     * - true: if the element has been successfully removed
     * - false: if it was not present in the collection
     */
    fun removeAlbum(album: Album): Boolean {
        val list = albumID_List.toMutableList()
        return if (list.remove(album.uuid)) {
            albumIDs = list.joinToString(",")
            true
        } else false
    }

    companion object {
        fun create(config: Artist.() -> Unit = {}): Artist {
            return Artist().apply(config).apply {
                uuid = UUID.randomUUID().toString()
            }
        }
    }
}
