package com.lasley.kts_provider.database;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

public class DataContentProvider extends ContentProvider {


    private DatabaseHelper databaseHelper;

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(
        @NonNull Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder
    ) {
        System.out.println(
            "query: " + uri +
                ", projection: " + Arrays.toString(projection) +
                ", selection: " + selection +
                ", selectionArgs: " + Arrays.toString(selectionArgs) +
                ", sortOrder: " + sortOrder
        );

        return databaseHelper.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        System.out.println("getType: " + uri);
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        System.out.println("insert: " + uri + ", values: " + values);
        return databaseHelper.insert(uri, values);
    }

    @Override
    public int delete(
        @NonNull Uri uri,
        @Nullable String selection,
        @Nullable String[] selectionArgs
    ) {
        System.out.println(
            "delete: " + uri +
                ", selection: " + selection +
                ", selectionArgs: " + Arrays.toString(selectionArgs)
        );

        return databaseHelper.delete(uri, selection, selectionArgs);
    }

    @Override
    public int update(
        @NonNull Uri uri,
        @Nullable ContentValues values,
        @Nullable String selection,
        @Nullable String[] selectionArgs
    ) {
        System.out.println(
            "update: " + uri +
                ", values: " + values +
                ", selection: " + selection +
                ", selectionArgs: " + Arrays.toString(selectionArgs)
        );

        return databaseHelper.update(uri, values, selection, selectionArgs);
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        System.out.println(
            "bulkInsert: " + uri +
                ", values: " + Arrays.toString(values)
        );

        return databaseHelper.bulkInsert(uri, values);
    }
}
