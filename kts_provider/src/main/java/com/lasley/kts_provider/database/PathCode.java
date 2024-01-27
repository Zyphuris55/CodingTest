package com.lasley.kts_provider.database;

import android.content.UriMatcher;

import com.lasley.kts_provider.Constants;

public enum PathCode {
    Unknown,
    Bulk,
    Albums,
    Album,
    Artists,
    Artist,
    Artist_albums,
    Status;

    public static PathCode parseToCode(int code) {
        for (PathCode path : values()) {
            if (path.ordinal() == code) return path;
        }

        return Unknown;
    }

    public String pathName() {
        return name().toLowerCase().replace("_", "/");
    }

    public void appendPath(UriMatcher matcher) {
        matcher.addURI(Constants.PROVIDER_NAME, pathName(), ordinal());
    }
}
