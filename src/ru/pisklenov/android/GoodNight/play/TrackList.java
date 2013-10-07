package ru.pisklenov.android.GoodNight.play;

import android.content.Context;

import java.util.ArrayList;

import ru.pisklenov.android.GoodNight.R;

/**
 * Created by anpi0413 on 10.09.13.
 */
public class TrackList {
    private ArrayList<Track> tracks;
    private int currentTrackNum;

    public TrackList(Context context, int initTrack) {
        this.tracks = getTracks(context);
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

    /*public void setCurrentTrack(String name) {
        currentTrackNum = 0;
        for (Track track: getTracks()) {
            if (name.equals(track.title)) {
                break;
            }
            currentTrackNum++;
        }
    }*/

    public ArrayList<Track> getTracks(Context context) {
        ArrayList<Track> arrayList = new ArrayList<Track>();

        arrayList.add(new Track(context.getString(R.string.antonin_dvorak_symphony_no_9), R.raw.antonin_dvorak_symphony_no_9));
        arrayList.add(new Track(context.getString(R.string.antonio_vivaldi_winter), R.raw.antonio_vivaldi_winter));
        arrayList.add(new Track(context.getString(R.string.johann_pachelbel_canon), R.raw.johann_pachelbel_canon));
        arrayList.add(new Track(context.getString(R.string.johann_sebastian_bach_minuet_in_g), R.raw.johann_sebastian_bach_minuet_in_g));
        arrayList.add(new Track(context.getString(R.string.johann_sebastian_bach_suite_no_3), R.raw.johann_sebastian_bach_suite_no_3));

        arrayList.add(new Track(context.getString(R.string.johannes_brahms_waltz_no_15), R.raw.johannes_brahms_waltz_no_15));
        arrayList.add(new Track(context.getString(R.string.ludwig_van_beethoven_sonata_no_14), R.raw.ludwig_van_beethoven_sonata_no_14));
        arrayList.add(new Track(context.getString(R.string.ludwig_van_beethoven_sonata_no_8), R.raw.ludwig_van_beethoven_sonata_no_8));
        arrayList.add(new Track(context.getString(R.string.robert_schumann_kinderscene_op_15), R.raw.robert_schumann_kinderscene_op_15));
        arrayList.add(new Track(context.getString(R.string.wolfgang_amadeus_mozart_piano_sonata_in_c), R.raw.wolfgang_amadeus_mozart_piano_sonata_in_c));
        arrayList.add(new Track(context.getString(R.string.wolfgang_amadeus_mozart_twinkle), R.raw.wolfgang_amadeus_mozart_twinkle));


        /*arrayList.add(new Track("Jungle 1", R.raw.jungle_01));
        arrayList.add(new Track("Jungle 2", R.raw.jungle_02));
        arrayList.add(new Track("Jungle 3", R.raw.jungle_01));
        arrayList.add(new Track("Jungle 4", R.raw.jungle_02));
        arrayList.add(new Track("Jungle 5", R.raw.jungle_01));
*/

        /*arrayList.add(new Track("Sleep away", R.raw.sleep_away));
        arrayList.add(new Track("Maid with the flaxen hair", R.raw.maid_with_the_flaxen_hair));
        arrayList.add(new Track("Johann Sebastian Bach - Minuet in G from Anna Magdalena", R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena));
        arrayList.add(new Track("Sleep away 2", R.raw.sleep_away));
        arrayList.add(new Track("Maid with the flaxen hair 2", R.raw.maid_with_the_flaxen_hair));
        arrayList.add(new Track("Johann Sebastian Bach - Minuet in G from Anna Magdalena 2", R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena));*/

        return arrayList;
    }



    public static class DownloadedTrackItem {
        public String googleDriveID = null;
        public String title = null;
        public String md5 = null;

        public DownloadedTrackItem(String lineToParse) {
            String arr[];// = new String[2];

            arr = lineToParse.split("\\|");

            googleDriveID = arr[0];
            title = arr[1];
            md5 = arr[2];
        }
    }

    /**
    * Created by dns on 10.09.13.
    */
    public static class Track {
        public static final int INTERNAL = 1;
        public static final int EXTERNAL = 2;


        public String title = null;
        public String pathToFile = null;
        public int resID = 0;
        public int typeID = 0;

        public Track(String title, String pathToFile) {
            this.title = title;
            this.pathToFile = pathToFile;

            this.typeID = EXTERNAL;
        }

        public Track(String title, int resID) {
            this.title = title;
            this.pathToFile = String.valueOf(resID) + ".mp3";
            this.resID = resID;

            this.typeID = INTERNAL;
        }

       /* public Track(String title, String pathToFile, int resID, int typeID) {
            this.title = title;
            this.pathToFile = pathToFile;
            this.resID = resID;
            this.typeID = typeID;
        }*/
    }
}
