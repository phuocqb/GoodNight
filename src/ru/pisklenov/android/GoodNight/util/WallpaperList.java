package ru.pisklenov.android.GoodNight.util;

import java.util.Random;

import ru.pisklenov.android.GoodNight.R;

/**
 * Created by anpi0413 on 13.09.13.
 */
public class WallpaperList {
    private static int[] wallpapersIDs = new int[] {
            R.drawable.wallpaper_1, R.drawable.wallpaper_2, R.drawable.wallpaper_3,
            R.drawable.wallpaper_4, R.drawable.wallpaper_5, R.drawable.wallpaper_6,
            R.drawable.wallpaper_7, R.drawable.wallpaper_8, R.drawable.wallpaper_9,
            R.drawable.wallpaper_10, R.drawable.wallpaper_11, R.drawable.wallpaper_12,
            R.drawable.wallpaper_13, R.drawable.wallpaper_14};


    public static int getRandWallpaperID() {
        int rand = new Random().nextInt(wallpapersIDs.length);

        return wallpapersIDs[rand];
    }

    public static int getRandWallpaperID(int exceptID) {
        int rand = new Random().nextInt(wallpapersIDs.length);

        while (wallpapersIDs[rand] == exceptID) {
            rand = new Random().nextInt(wallpapersIDs.length);
        }

        return wallpapersIDs[rand];
    }
}
