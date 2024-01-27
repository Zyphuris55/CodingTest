package com.lasley.kts_viewer

import android.app.Application
import android.content.ContentResolver
import android.content.ContentValues
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lasley.kts_viewer.data.Album
import com.lasley.kts_viewer.data.Artist
import com.lasley.kts_viewer.data.CommonInf
import com.lasley.kts_viewer.data.LocalDatabase
import com.lasley.kts_viewer.data.ProviderPaths
import com.lasley.kts_viewer.data.SaveResult
import com.lasley.kts_viewer.extensions.toJson
import com.lasley.kts_viewer.helpers.ContentResolverHelper
import com.lasley.kts_viewer.helpers.toObjSeq
import com.lasley.kts_viewer.helpers.tryOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.TestOnly

class ActivityViewModel(context: Application) : AndroidViewModel(context) {

    val liveAlbumData = MutableLiveData<List<Album>>()
    val reloadReady = MutableLiveData<Boolean>()

    /**
     * Collection of (Artist names, UUID), sorted alphabetically
     */
    val artists: List<Pair<String, String>>
        get() = LocalDatabase.values.asSequence()
            .filterIsInstance<Artist>().map { it.name to it.uuid }.toList()
            .sortedBy { it.first }

    /**
     * Collection of albums, sorted by time updated
     */
    val albums: List<Album>
        get() = LocalDatabase.values.asSequence()
            .filterIsInstance<Album>().toList()
            .sortedBy { it.updatedTime }
//            .sortedBy { it.name }.sortedBy { it.artistName }

    private val appContext: Application
        get() = getApplication()

    private val contentResolver: ContentResolver
        get() = appContext.contentResolver

    val resolverExists: Boolean
        get() = Constants.providerExists(appContext)

    @TestOnly
    var resolverHelper: ContentResolverHelper = ContentResolverHelper(contentResolver)

    init {
        val handler = Handler()

        contentResolver.registerContentObserver(
            Uri.parse(ProviderPaths.contentRoot),
            true,
            object : ContentObserver(handler) {
                override fun onChange(selfChange: Boolean) {
                    println("Artist update: $selfChange")
                    reloadReady.postValue(true)
                }
            })
    }

    fun loadData(onFinish: () -> Unit = {}) {
        LocalDatabase.clearData()
        viewModelScope.launch(Dispatchers.IO) {
            val albums = contentResolver.query(
                ProviderPaths.Albums.uri, null, null, null, null
            )?.use { it.toObjSeq<Album>().toList() }.orEmpty()

            albums.map { it.uuid to it }.forEach {
                LocalDatabase.update(it.first, it.second)
            }

            contentResolver.query(
                ProviderPaths.Artists.uri, null, null, null, null
            )?.use { it.toObjSeq<Artist>().toList() }.orEmpty()
                .map { it.uuid to it }
                .forEach { LocalDatabase.update(it.first, it.second) }

            liveAlbumData.postValue(albums)
            withContext(Dispatchers.Main) {
                onFinish()
                reloadReady.postValue(false)
            }
        }
    }

    fun addData(
        newData: CommonInf,
        results: (SaveResult, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val submission = ContentValues()
            submission.put("content", newData.toJson())

            val uri = when (newData) {
                is Album -> ProviderPaths.Album(newData.uuid).uri
                is Artist -> ProviderPaths.Artist(newData.uuid).uri
                else -> null
            }

            val updated = if (uri != null) {
                tryOrNull { contentResolver.insert(uri, submission) }
            } else null

            val result = when (updated) {
                null -> SaveResult.Error
                else -> SaveResult.Success
            }

            if (result == SaveResult.Success)
                loadData { results(result, "") }
            else
                withContext(Dispatchers.Main) {
                    results(result, "")
                }
        }
    }

    fun updateData(
        updateData: List<CommonInf>,
        results: (SaveResult, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedItems: Int?

            when (updateData.size) {
                0 -> return@launch withContext(Dispatchers.Main) { results(SaveResult.Success, "") }

                1 -> {
                    val (type, content) = updateData
                        .map { it.dataType to it.asContentData }.first()

                    val uri = when (type) {
                        "album" -> ProviderPaths.Album(updateData.first().uuid)
                        "artist" -> ProviderPaths.Artist(updateData.first().uuid)
                        else -> null
                    }?.uri

                    updatedItems = tryOrNull {
                        contentResolver.update(
                            uri!!, content, null, null
                        )
                    }
                }

                else -> {
                    val contents = updateData.map { it.asContentData }

                    updatedItems = tryOrNull {
                        contentResolver.bulkInsert(
                            ProviderPaths.Bulk.uri,
                            contents.toTypedArray()
                        )
                    }
                }
            }

            val result = when (updatedItems) {
                null, 0 -> SaveResult.Error
                else -> SaveResult.Success
            }

            if (result == SaveResult.Success)
                loadData { results(result, "") }
            else
                withContext(Dispatchers.Main) {
                    results(result, "")
                }
        }
    }

    fun removeData(
        data: CommonInf,
        results: (SaveResult, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val uri = when (data) {
                is Album -> ProviderPaths.Album(data.uuid).uri
                is Artist -> ProviderPaths.Artist(data.uuid).uri
                else -> null
            }
            val updated = if (uri != null) {
                tryOrNull {
                    contentResolver.delete(uri, null, null)
                }
            } else null

            val result = when (updated) {
                null, 0 -> SaveResult.Error
                else -> SaveResult.Success
            }

            withContext(Dispatchers.Main) { results(result, "") }
            if (result == SaveResult.Success)
                loadData()
        }
    }
}