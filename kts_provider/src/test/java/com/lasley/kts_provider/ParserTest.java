package com.lasley.kts_provider;

import com.google.gson.Gson;
import com.lasley.kts_provider.data.Album;
import com.lasley.kts_provider.data.Artist;

import org.junit.Assert;
import org.junit.Test;

public class ParserTest {
    @Test
    public void parseData() {
        Artist artist = new Artist("Artist 123");
        Album album = new Album("Album 123", artist);

        Gson gson = new Gson();
        String json = gson.toJson(album);
        Album parsed = gson.fromJson(json, Album.class);

        Assert.assertEquals("Album 123", album.name);
        Assert.assertEquals("Album 123", parsed.name);

        Assert.assertEquals(artist.uuid, album.artist);
        Assert.assertEquals(artist.uuid, parsed.artist);

        Assert.assertTrue(artist.albumIDs().contains(album.uuid));
        Assert.assertTrue(artist.albumIDs().contains(parsed.uuid));
    }
}
