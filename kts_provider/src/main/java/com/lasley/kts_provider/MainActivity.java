package com.lasley.kts_provider;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;

import com.lasley.kts_provider.data.Album;
import com.lasley.kts_provider.data.Artist;
import com.lasley.kts_provider.database.ContentDatabase;
import com.lasley.kts_provider.database.DataDao;
import com.lasley.kts_provider.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Function;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Timer timer;

    // 5 minutes
    private final int maxTime = 5 * 60 * 1000;

    ContentDatabase database = ContentDatabase.getInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());

        binding.repeatSeconds.setMinValue(1);
        binding.repeatSeconds.setMaxValue(maxTime);
        binding.repeatSeconds.setValue(30);

        initReloadActions();
        dbSetup();
    }

    private void initReloadActions(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                genActions();
            }
        };

        binding.genState.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                binding.genText.setText("Enabled");

                long delay = binding.repeatSeconds.getValue() * 1000L;

                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                timer = new Timer();
                timer.scheduleAtFixedRate(timerTask, 0, delay);
                // todo; show a progress until next timer action runs
            } else {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                binding.genText.setText("Disabled");
            }
        });
    }

    private CountDownTimer createTimer(
        Long delay,
        Function<Long, Boolean> action
    ) {
        return new CountDownTimer(delay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                action.apply(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                genActions();
            }
        };
    }

    private void genActions() {
        boolean newArtists = binding.optionArtists.isChecked();
        boolean newAlbums = binding.optionArtists.isChecked();
        boolean shuffleAlbum = binding.optionArtists.isChecked();

        DataDao access = database.dataDao();

        if (newArtists) {
            int artistID = Math.abs(new Random().nextInt(300));
            Artist newArtist = new Artist("Artist #" + artistID);
            access.insert(newArtist);
        }

        if (newAlbums) {
            List<Artist> artists = access.getAllArtistItems();
            int artistIndex = new Random().nextInt(artists.size());
            Artist artist = artists.get(artistIndex);
            int albumID = Math.abs(new Random().nextInt(300));
            Album album = new Album("Album #" + albumID, artist);
            access.insert(album);
            access.update(artist);
        }

        if (shuffleAlbum) {
            /*
            1. get a random artist (remove from list)
            2. get a random album from that artist
            3. get a new random artist (or make one, if none are available)
            4. change the album's (name, artist)
            5. save data
             */

            boolean createdAlbum = false;
            boolean createdArtist = false;

            List<Artist> artists = new ArrayList<>(access.getAllArtistItems());
            int artistIndex = new Random().nextInt(artists.size());
            Artist artist = artists.get(artistIndex);
            artists.remove(artist);

            List<String> albumIDs = artist.albumIDs();
            Album album;
            if (!albumIDs.isEmpty()) {
                int albumIndex = new Random().nextInt(albumIDs.size());
                album = access.getAlbumItem(albumIDs.get(albumIndex));
                System.out.println("get album");
            } else {
                // artist has no albums, so make one for later
                createdAlbum = true;
                int albumID = Math.abs(new Random().nextInt(300));
                album = new Album("Album #" + albumID, artist);
                System.out.println("new album");
            }

            Artist newArtist;
            if (artists.isEmpty()) {
                createdArtist = true;
                int artistID = Math.abs(new Random().nextInt(300));
                newArtist = new Artist("Artist #" + artistID);
            } else {
                int newArtistIndex = new Random().nextInt(artists.size());
                newArtist = artists.get(newArtistIndex);
            }

            long updatingTime = System.currentTimeMillis();
            artist.updatedTime = updatingTime;
            album.updatedTime = updatingTime;
            newArtist.updatedTime = updatingTime;

            artist.removeAlbum(album.uuid);
            album.artist = newArtist.uuid;
            newArtist.addAlbum(album.uuid);

            access.update(artist);
            if (createdAlbum)
                access.insert(album);
            else
                access.update(album);

            if (createdArtist)
                access.insert(newArtist);
            else
                access.update(newArtist);
        }

        if (newArtists || newAlbums || shuffleAlbum) {
//            runOnUiThread(() -> {
//                Toast.makeText(this, "Data Changed", Toast.LENGTH_SHORT).show();
//            });

            getContentResolver().notifyChange(Uri.parse(Constants.AUTHORITY), null);
        }
    }

    public void dbSetup() {
        database.clearAllTables();
        DataDao dataDeo = database.dataDao();

        Artist newArtist = new Artist("artist 1");

        Album[] albums = {
            new Album("ab1", newArtist),
            new Album("ab2", newArtist),
            new Album("ab3", newArtist),
        };

        dataDeo.insert(newArtist);
        dataDeo.insert(albums);
    }
}