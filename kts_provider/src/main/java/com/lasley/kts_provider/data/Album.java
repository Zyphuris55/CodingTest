package com.lasley.kts_provider.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

@Entity(tableName = "_album")
public class Album {
    @PrimaryKey
    @ColumnInfo(name = "album_id")
    @SerializedName("album_id")
    @NonNull
    public String uuid = UUID.randomUUID().toString();
    public String name;

    @ColumnInfo(name = "artist_id", index = true)
    @SerializedName("artist_id")
    public String artist;


    public long createdTime = System.currentTimeMillis();
    public long updatedTime = System.currentTimeMillis();

    public Album() {
    }

    public Album(String name) {
        this.name = name;
    }

    public Album(String name, Artist artist) {
        this.name = name;
        this.artist = artist.uuid;

        if (!artist.albums.isEmpty())
            artist.albums += ",";
        artist.albums += uuid;
    }
}
