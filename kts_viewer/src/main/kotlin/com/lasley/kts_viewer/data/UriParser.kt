package com.lasley.kts_viewer.data

import android.net.Uri

val Uri.toMap: Map<UriToken, Any?>
    get() = buildMap {
        put(UriToken.Scheme, scheme)
        put(UriToken.Authority, authority)
        put(UriToken.Path, path)
        put(UriToken.PathSegments, pathSegments)
        put(UriToken.LastSegment, lastPathSegment)
        put(UriToken.Query, query)
    }


enum class UriToken {
    Scheme, Authority,
    Path, PathSegments, LastSegment,
    Query,
}
