package com.lasley.kts_provider.helpers;


import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

class UriParser {
    public static Map<UriToken, Object> parse(Uri input) {
        HashMap<UriToken, Object> data = new HashMap<>();
        data.put(UriToken.Scheme, input.getScheme());
        data.put(UriToken.Authority, input.getAuthority());
        data.put(UriToken.Path, input.getPath());
        data.put(UriToken.PathSegments, input.getPathSegments());
        data.put(UriToken.LastSegment, input.getLastPathSegment());
        data.put(UriToken.Query, input.getQuery());

        return data;
    }
}


enum UriToken {
    Scheme, Authority,
    Path, PathSegments, LastSegment,
    Query,
}
