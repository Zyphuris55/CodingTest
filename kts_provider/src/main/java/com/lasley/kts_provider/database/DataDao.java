package com.lasley.kts_provider.database;

import android.database.Cursor;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.lasley.kts_provider.data.Album;
import com.lasley.kts_provider.data.Artist;
import com.lasley.kts_provider.data.HistoryStamp;

import java.util.List;

@Dao
public abstract class DataDao {

    @Query("SELECT * FROM _album")
    @Nullable
    public abstract Cursor getAllAlbums();

    @Query("SELECT * FROM _album WHERE album_id = :id")
    public abstract Cursor getAlbum(String id);

    @Query("SELECT * FROM _artist")
    public abstract Cursor getAllArtists();

    @Query("SELECT * FROM _artist WHERE artist_id = :id")
    public abstract Cursor getArtist(String id);

//    @Query(value = """SELECT * FROM domain ORDER BY CASE :order WHEN 1 THEN 'id ASC' ELSE 'id DESC' END""")
//    fun selectAll(order: Boolean = true): Cursor

    @Query("SELECT * FROM _album WHERE artist_id = :id")
    public abstract Cursor getArtistAlbums(String id);

    @Query("SELECT 1 FROM _artist WHERE artist_id = :id")
    public abstract boolean containsArtist(String id);

    @Query("SELECT 1 FROM _album WHERE album_id = :id")
    public abstract boolean containsAlbum(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Artist artist);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract List<Long> insert(Album... albums);

    @Insert
    public abstract long appendHistory(HistoryStamp item);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract int update(Album... albums);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    public abstract int update(Artist artist);

    @Query("SELECT * FROM _artist")
    public abstract List<Artist> getAllArtistItems();

    @Query("SELECT * FROM _artist WHERE artist_id = :id")
    public abstract Artist getArtistItem(String id);

    @Query("SELECT * FROM _album WHERE album_id = :id")
    @Nullable
    public abstract Album getAlbumItem(String id);

    @Delete
    public abstract int delete(Artist artist);

    @Delete
    public abstract int delete(Album... album);
}
