package ru.pisklenov.android.GoodNight.util;

import android.content.Context;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

import ru.pisklenov.android.GoodNight.R;

public class SongsProvider {
	// Put your Music Folder in SD Card here
	final String MEDIA_PATH = new String("/sdcard/Music");
    Context context;

	// --------------------------------------------------//
/*	final int[] mp3RawList = { R.raw.jungle_01,
			R.raw.jungle_02, R.raw.jungle_01, R.raw.jungle_02,
			R.raw.jungle_01 };*/

	private ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();

	// Constructor
	public SongsProvider(Context context) {
        this.context = context;
	}

	/**
	 * Function to read all mp3 files from sdcard and store the details in
	 * ArrayList
	 * */
	public ArrayList<HashMap<String, String>> getPlayList() {
        String path = context.getFilesDir() + "/";

        songsList.clear();

        HashMap<String, String> song = new HashMap<String, String>();
        song.put("songTitle", "maid_with_the_flaxen_hair.mp3");
        song.put("songPath", path + "maid_with_the_flaxen_hair.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);

        song = new HashMap<String, String>();
        song.put("songTitle", "sleep_away.mp3");
        song.put("songPath", path + "sleep_away.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);

        song = new HashMap<String, String>();
        song.put("songTitle", "johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3");
        song.put("songPath", path + "johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);


        song = new HashMap<String, String>();
        song.put("songTitle", "johannes_brahms_waltz_no_15.mp3");
        song.put("songPath", path + "johannes_brahms_waltz_no_15.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);

        song = new HashMap<String, String>();
        song.put("songTitle", "robert_schumann_kinderscene_op_15.mp3");
        song.put("songPath", path + "robert_schumann_kinderscene_op_15.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);


        /*song = new HashMap<String, String>();
        song.put("songTitle", "jungle_01.mp3");
        song.put("songPath", path + "jungle_01.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);

        song = new HashMap<String, String>();
        song.put("songTitle", "jungle_02.mp3");
        song.put("songPath", path + "jungle_02.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_01.mp3");
        songsList.add(song);*/

//        song = new HashMap<String, String>();
//        song.put("songTitle", "johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3");
//        song.put("songPath", "johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3");
//        //song.put("songPath", "file:///android_asset/music/jungle_02.mp3");
//        songsList.add(song);

        //songsList.clear();

       /* song = new HashMap<String, String>();
        song.put("songTitle", "maid_with_the_flaxen_hair.mp3");
        song.put("songPath", "maid_with_the_flaxen_hair.mp3");
        songsList.add(song);

       /* song = new HashMap<String, String>();
        song.put("songTitle", "sleep_away.mp3");
        song.put("songPath", "sleep_away.mp3");
        //song.put("songPath", "file:///android_asset/music/jungle_02.mp3");
        songsList.add(song);
*/


//        String uri = "android.resource://" + getPackageName() + "/"+R.raw.filename;
//

		/*File musicFolder = new File(MEDIA_PATH);

		if (musicFolder.listFiles(new FileExtensionFilter()).length > 0) {
			for (File file : musicFolder.listFiles(new FileExtensionFilter())) {
				HashMap<String, String> song = new HashMap<String, String>();
				song.put(
						"songTitle",
						file.getName().substring(0,
								(file.getName().length() - 4)));
				song.put("songPath", file.getPath());
				// Adding each song to SongList
				songsList.add(song);
			}
		}*/
		// return songs list array


		return songsList;
	}

	/**
	 * The class is used to filter files which are having .mp3 extension
	 * */
	class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3"));
		}
	}
}
