package com.lasley.kts_provider.data;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity(tableName = "_artist")
public class Artist {
    @PrimaryKey
    @ColumnInfo(name = "artist_id")
    @SerializedName("artist_id")
    @NonNull
    public String uuid = UUID.randomUUID().toString();

    public String name;
    @ColumnInfo(name = "album_ids", index = true)
    @SerializedName("album_ids")
    public String albums = "";

    public long createdTime = System.currentTimeMillis();
    public long updatedTime = System.currentTimeMillis();

    public Artist() {
    }

    public Artist(String name) {
        this.name = name;
    }

    public Artist(String name, List<Album> albums) {
        this(name);
//        this.albums.addAll(albums);
    }

    public List<String> albumIDs() {
        if (albums.isEmpty()) return new ArrayList<>();
        return Arrays.asList(albums.split(","));
    }

    public boolean removeAlbum(String id) {
        ArrayList<String> ids = new ArrayList<>(albumIDs());
        if (ids.contains(id) && ids.remove(id)) {
            albums = idsToString(ids);
            return true;
        } else
            return false;
    }


    public boolean addAlbum(String id) {
        ArrayList<String> ids = new ArrayList<>(albumIDs());
        if (ids.contains(id)) return false;
        ids.add(id);
        albums = idsToString(ids);
        return true;
    }

    private String idsToString(List<String> ids) {
        StringBuilder newAlbums = new StringBuilder();
        for (String item : ids) {
            newAlbums.append(item).append(",");
        }
        // remove final ","
        if (newAlbums.length() > 1)
            newAlbums.deleteCharAt(newAlbums.length() - 1);
        return newAlbums.toString();
    }
}
