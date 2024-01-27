package com.lasley.kts_provider.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lasley.kts_provider.Constants;
import com.lasley.kts_provider.data.Album;
import com.lasley.kts_provider.data.Artist;
import com.lasley.kts_provider.data.HistoryStamp;

@Database(entities = {Artist.class, Album.class, HistoryStamp.class}, version = 1)
public abstract class ContentDatabase extends RoomDatabase {
    public abstract DataDao dataDao();

    private static ContentDatabase instance = null;

    public static synchronized ContentDatabase getInstance(Context context) {
        if (instance == null)
            instance = Room
                .databaseBuilder(context, ContentDatabase.class, Constants.DATABASE_NAME)
                .allowMainThreadQueries()
                .build();
        return instance;
    }
}
