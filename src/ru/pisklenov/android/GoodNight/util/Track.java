package ru.pisklenov.android.GoodNight.util;

/**
* Created by dns on 10.09.13.
*/
public class Track {
    public String title = null;
    public String pathToFile = null;
    public int resID = 0;

    public Track(String title, String pathToFile) {
        this.title = title;
        this.pathToFile = pathToFile;
        this.resID = 0;
    }

    public Track(String title, String pathToFile, int resID) {
        this.title = title;
        this.pathToFile = pathToFile;
        this.resID = resID;
    }
}
