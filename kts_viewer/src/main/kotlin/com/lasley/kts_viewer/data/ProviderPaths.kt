package com.lasley.kts_viewer.data

import android.net.Uri
import com.lasley.kts_viewer.Constants

sealed class ProviderPaths {
    companion object {
        val contentRoot: String
            get() = "content://${Constants.providerID}"
    }

    val path: String get() = this::class.simpleName.orEmpty().lowercase()
    abstract val url: String
    val uri: Uri get() = Uri.parse(url)

   internal fun appendQueries(
        builder: StringBuilder,
        vararg queries: Pair<String, String>
    ) {
        // todo; sanitize queries like [Uri.Builder]
        val query = queries.filterNot { it.first.isBlank() || it.second.isBlank() }
            .joinToString("&", "?") { "${it.first}=${it.second}" }
        if (query.isNotEmpty())
            builder.append(query)
    }

    data object Bulk : ProviderPaths() {
        override val url: String
            get() = "$contentRoot/$path"
    }

    data object Albums : ProviderPaths() {
        override val url: String
            get() = "$contentRoot/$path"
    }

    data object Artists : ProviderPaths() {
        override val url: String
            get() = "$contentRoot/$path"
    }

    class Album(
        val id: String = "",
        val query: String = ""
    ) : ProviderPaths() {
        override val url: String
            get() {
                return buildString {
                    append("$contentRoot/$path")
                    appendQueries(
                        this,
                        ("id" to id),
                        ("contains" to query)
                    )
                }
            }
    }

    class Artist(
        val id: String = "",
        val query: String = ""
    ) : ProviderPaths() {
        override val url: String
            get() {
                return buildString {
                    append("$contentRoot/$path")
                    appendQueries(
                        this,
                        ("id" to id),
                        ("contains" to query)
                    )
                }
            }
    }

    class ArtistAlbums(
        val id: String
    ) : ProviderPaths() {
        override val url: String
            get() {
                return buildString {
                    append("$contentRoot/artist/albums")
                    appendQueries(this, ("id" to id))
                }
            }
    }
}
