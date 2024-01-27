package com.lasley.kts_provider.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;
import com.lasley.kts_provider.Constants;
import com.lasley.kts_provider.data.Album;
import com.lasley.kts_provider.data.Artist;

import java.util.regex.Pattern;


/**
 * @noinspection unused
 */
public class DatabaseHelper {

    static ContentDatabase database;

    static final UriMatcher uriMatcher;

    private static final Pattern idPattern;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        for (PathCode path : PathCode.values()) {
            path.appendPath(uriMatcher);
        }

        // https://www.ietf.org/rfc/rfc4122.txt
        // about format/ version number: https://stackoverflow.com/a/38191078
        idPattern = Pattern.compile("(?:\\w+-){4}\\w+");
    }

    public DatabaseHelper(@Nullable Context context) {
        if (context == null) return;
        database = ContentDatabase.getInstance(context);
        DatabaseHistory.init(context);
    }

    @VisibleForTesting
    public DatabaseHelper(ContentDatabase database) {
        DatabaseHelper.database = database;
    }

    private boolean validateID(@Nullable String id) {
        if (id == null) return false;
        return idPattern.matcher(id).find();
    }

    private Uri.Builder buildUri(String path) {
        return new Uri.Builder()
            .scheme("content")
            .authority(Constants.AUTHORITY)
            .appendPath(path);
    }

    @Nullable
    private Album parseToAlbum(@Nullable String content) {
        if (content == null) return null;
        try {
            return new Gson().fromJson(content, Album.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private Artist parseToArtist(@Nullable String content) {
        if (content == null) return null;
        try {
            return new Gson().fromJson(content, Artist.class);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public Cursor query(
        @NonNull Uri uri,
        @Nullable String[] projection,
        @Nullable String selection,
        @Nullable String[] selectionArgs,
        @Nullable String sortOrder
    ) {
        DataDao access = database.dataDao();
//        String queryCheck = uri.getQueryParameter("contains");
        String itemID = uri.getQueryParameter("id");
        HistoryActionToken idCheck;
        if (validateID(itemID))
            idCheck = HistoryActionToken.OK;
        else
            idCheck = HistoryActionToken.Unknown_ID;

        /* Examples
            content://com.lasley.provider/album?artist=1234

            albums -> all albums
            albums?contains=abc -> albums (names containing "abc)
            album/1234 -> single album

            artists -> all artists
            artists?contains=abc -> albums (names containing "abc)
            artist/1234 -> single artist "1234"
            artist/albums/1234 -> albums from artist "1234"
         */

        switch (PathCode.parseToCode(uriMatcher.match(uri))) {
            case Albums: {
                DatabaseHistory.query(HistoryActionToken.OK, "AllAlbums");
                return access.getAllAlbums();
            }

            case Album: {
                if (itemID == null) {
                    DatabaseHistory.query(HistoryActionToken.Missing_ID, "album");
                    return null;
                }
                DatabaseHistory.query(idCheck, "album");
                return access.getAlbum(itemID);
            }

            case Artists: {
                DatabaseHistory.query(HistoryActionToken.OK, "AllArtists");
                return access.getAllArtists();
            }

            case Artist: {
                if (itemID == null) {
                    DatabaseHistory.query(HistoryActionToken.Missing_ID, "artist");
                    return null;
                }
                DatabaseHistory.query(idCheck, "artist");
                return access.getArtist(itemID);
            }

            case Artist_albums: {
                if (itemID == null) return null;
                return access.getArtistAlbums(itemID);
            }

            default:
                DatabaseHistory.query(HistoryActionToken.Unknown_URI);
                return null;
        }
    }

    public Uri insert(
        @NonNull Uri uri,
        @Nullable ContentValues values
    ) {
        DataDao access = database.dataDao();
        String itemID = uri.getQueryParameter("id");
        if (itemID == null) {
            DatabaseHistory.insert(HistoryActionToken.Missing_ID, "");
            return null;
        }
        if (!validateID(itemID)) {
            DatabaseHistory.insert(HistoryActionToken.Unknown_ID, itemID);
            return null;
        }
        if (values == null) {
            DatabaseHistory.insert(HistoryActionToken.Missing_Content, "");
            return null;
        }

        String content = values.getAsString("content");

        switch (PathCode.parseToCode(uriMatcher.match(uri))) {
            case Album: {
                Album parsed = parseToAlbum(content);
                if (parsed == null) {
                    DatabaseHistory.insert(HistoryActionToken.Parse_Failed, itemID);
                    return null;
                }

                access.insert(parsed);
                return buildUri("album")
                    .appendQueryParameter("id", parsed.uuid).build();
            }

            case Artist: {
                Artist parsed = parseToArtist(content);
                if (parsed == null) {
                    DatabaseHistory.insert(HistoryActionToken.Parse_Failed, itemID);
                    return null;
                }

                access.insert(parsed);
                return buildUri("artist")
                    .appendQueryParameter("id", parsed.uuid).build();
            }

            default:
                DatabaseHistory.insert(HistoryActionToken.Unknown_URI, itemID);
                return null;
        }
    }

    public int delete(
        @NonNull Uri uri,
        @Nullable String selection,
        @Nullable String[] selectionArgs
    ) {
        DataDao access = database.dataDao();
        String itemID = uri.getQueryParameter("id");
        if (itemID == null) {
            DatabaseHistory.delete(HistoryActionToken.Missing_ID, "");
            return 0;
        }
        if (!validateID(itemID)) {
            DatabaseHistory.delete(HistoryActionToken.Unknown_ID, itemID);
            return 0;
        }

        switch (PathCode.parseToCode(uriMatcher.match(uri))) {
            case Album: {
                Album album = access.getAlbumItem(itemID);
                if (album == null) {
                    DatabaseHistory.delete(HistoryActionToken.Item_Missing, itemID);
                    return 0;
                }
                int rows = access.delete(album);
                if (rows == 0)
                    DatabaseHistory.delete(HistoryActionToken.Failed, itemID);
                else {
                    DatabaseHistory.delete(HistoryActionToken.OK, itemID);

                    // Remove album from the artist
                    Artist artist = access.getArtistItem(album.artist);
                    if (artist.removeAlbum(itemID)) {
                        access.update(artist);
                        DatabaseHistory.update(HistoryActionToken.OK, artist.uuid);
                    } else
                        DatabaseHistory.update(HistoryActionToken.Failed, artist.uuid);
                }

                return rows;
            }

            case Artist: {
                Artist artist = access.getArtistItem(itemID);
                if (artist == null) {
                    DatabaseHistory.delete(HistoryActionToken.Item_Missing, itemID);
                    return 0;
                }

                int deleteCount = access.delete(artist);

                // remove the artist -> remove all the albums linked to the artist
                for (String albumID : artist.albumIDs()) {
                    Album album = access.getAlbumItem(albumID);
                    if (album == null)
                        DatabaseHistory.delete(HistoryActionToken.Failed, itemID);
                    else {
                        DatabaseHistory.delete(HistoryActionToken.OK, itemID);
                        deleteCount += access.delete();
                    }
                }

                if (deleteCount == 0)
                    DatabaseHistory.delete(HistoryActionToken.Failed, itemID);
                else
                    DatabaseHistory.delete(HistoryActionToken.OK, itemID);

                return deleteCount;
            }

            default:
                DatabaseHistory.delete(HistoryActionToken.Unknown_URI, itemID);
                return 0;
        }
    }

    public int update(
        @NonNull Uri uri,
        @Nullable ContentValues values,
        @Nullable String selection,
        @Nullable String[] selectionArgs
    ) {
        DataDao access = database.dataDao();
        String itemID = uri.getQueryParameter("id");
        if (!validateID(itemID)) {
            if (itemID == null)
                DatabaseHistory.update(HistoryActionToken.Missing_ID, "");
            else
                DatabaseHistory.update(HistoryActionToken.Unknown_ID, itemID);
            return 0;
        }

        if (values == null) {
            DatabaseHistory.update(HistoryActionToken.Missing_Content, itemID);
            return 0;
        }

        String content = values.getAsString("content");
        if (content == null) {
            DatabaseHistory.update(HistoryActionToken.Parse_Failed, itemID);
            return 0;
        }

        switch (PathCode.parseToCode(uriMatcher.match(uri))) {
            case Album: {
                Album parsed = parseToAlbum(content);
                if (parsed == null) {
                    DatabaseHistory.update(HistoryActionToken.Parse_Failed, itemID);
                    return 0;
                }
                int rows = access.update(parsed);
                if (rows == 0)
                    DatabaseHistory.update(HistoryActionToken.Failed, itemID);
                else
                    DatabaseHistory.update(HistoryActionToken.OK, itemID);
                return rows;
            }

            case Artist: {
                Artist parsed = parseToArtist(content);
                if (parsed == null) {
                    DatabaseHistory.update(HistoryActionToken.Parse_Failed, itemID);
                    return 0;
                }

                int rows = access.update(parsed);
                if (rows == 0)
                    DatabaseHistory.update(HistoryActionToken.Failed, itemID);
                else
                    DatabaseHistory.update(HistoryActionToken.OK, itemID);
                return rows;
            }

            default:
                DatabaseHistory.update(HistoryActionToken.Unknown_URI, itemID, uri.toString());
                return 0;
        }
    }

    public int bulkInsert(
        @NonNull Uri uri,
        @NonNull ContentValues[] values
    ) {
        DataDao access = database.dataDao();
        int updatedItems = 0;
        DatabaseHistory.insert(HistoryActionToken.OK, "", "Bulk insert: " + values.length);


        for (ContentValues value : values) {
            String dataType = value.getAsString("type");
            String dataContent = value.getAsString("content");

            switch (dataType) {
                case "album": {
                    Album parsed = parseToAlbum(dataContent);
                    if (parsed == null)
                        DatabaseHistory.insert(HistoryActionToken.Parse_Failed, "", dataType);
                    else {
                        if (access.containsAlbum(parsed.uuid)) {
                            updatedItems += access.update(parsed);
                            DatabaseHistory.update(HistoryActionToken.OK, parsed.uuid);
                        } else {
                            access.insert(parsed);
                            if (access.containsAlbum(parsed.uuid)) {
                                updatedItems++;
                                DatabaseHistory.insert(HistoryActionToken.OK, parsed.uuid);
                            } else
                                DatabaseHistory.insert(HistoryActionToken.Failed, parsed.uuid);
                        }
                    }
                    break;
                }
                case "artist": {
                    Artist parsed = parseToArtist(dataContent);
                    if (parsed == null)
                        DatabaseHistory.insert(HistoryActionToken.Parse_Failed, "", dataType);
                    else {
                        if (access.containsArtist(parsed.uuid)) {
                            updatedItems += access.update(parsed);
                            DatabaseHistory.update(HistoryActionToken.OK, parsed.uuid);
                        } else {
                            access.insert(parsed);
                            if (access.containsArtist(parsed.uuid)) {
                                updatedItems++;
                                DatabaseHistory.insert(HistoryActionToken.OK, parsed.uuid);
                            } else
                                DatabaseHistory.insert(HistoryActionToken.Failed, parsed.uuid);
                        }
                        updatedItems += access.update(parsed);
                        DatabaseHistory.insert(HistoryActionToken.OK, parsed.uuid);
                    }
                    break;
                }
                default:
                    DatabaseHistory.insert(HistoryActionToken.Unknown_Type, "", dataType);
                    break;
            }
        }

        return updatedItems;
    }
}
