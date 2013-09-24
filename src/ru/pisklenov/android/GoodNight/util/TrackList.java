package ru.pisklenov.android.GoodNight.util;

import java.io.Serializable;
import java.util.ArrayList;

import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.util.Track;

/**
 * Created by anpi0413 on 10.09.13.
 */
public class TrackList implements Serializable {
    private ArrayList<Track> tracks;
    private int currentTrackNum;

    public TrackList(int initTrack) {
        this.tracks = getTracks();
        this.currentTrackNum = initTrack;
    }

    public int getCurrentTrackNum() {
        return currentTrackNum;
    }

    public Track getNextTrack() {
        currentTrackNum++;
        if (currentTrackNum >= tracks.size()) {
            currentTrackNum = 0;
        }

        return tracks.get(currentTrackNum);
    }

    public Track getPrevTrack() {
        currentTrackNum--;
        if (currentTrackNum < 0) {
            currentTrackNum = tracks.size() - 1;
        }

        return tracks.get(currentTrackNum);
    }

    public Track getCurrentTrack() {
        return tracks.get(currentTrackNum);
    }

    public void setCurrentTrack(String name) {
        currentTrackNum = 0;
        for (Track track: getTracks()) {
            if (name.equals(track.title)) {
                break;
            }
            currentTrackNum++;
        }
    }

    public ArrayList<Track> getTracks() {
        ArrayList<Track> arrayList = new ArrayList<Track>();


        arrayList.add(new Track("Jungle 1", R.raw.jungle_01));
        arrayList.add(new Track("Jungle 2", R.raw.jungle_02));
        arrayList.add(new Track("Jungle 3", R.raw.jungle_01));
        arrayList.add(new Track("Jungle 4", R.raw.jungle_02));
        arrayList.add(new Track("Jungle 5", R.raw.jungle_01));

        /*arrayList.add(new Track("Sleep away", R.raw.sleep_away));
        arrayList.add(new Track("Maid with the flaxen hair", R.raw.maid_with_the_flaxen_hair));
        arrayList.add(new Track("Johann Sebastian Bach - Minuet in G from Anna Magdalena", R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena));
        arrayList.add(new Track("Sleep away 2", R.raw.sleep_away));
        arrayList.add(new Track("Maid with the flaxen hair 2", R.raw.maid_with_the_flaxen_hair));
        arrayList.add(new Track("Johann Sebastian Bach - Minuet in G from Anna Magdalena 2", R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena));*/

        return arrayList;
    }

}
