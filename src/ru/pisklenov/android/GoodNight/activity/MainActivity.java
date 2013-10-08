package ru.pisklenov.android.GoodNight.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.iconcontext.IconContextMenu;
import ru.pisklenov.android.GoodNight.internet.Download;
import ru.pisklenov.android.GoodNight.internet.InternetHelper;
import ru.pisklenov.android.GoodNight.util.BitmapHelper;
import ru.pisklenov.android.GoodNight.util.FileHelper;
import ru.pisklenov.android.GoodNight.util.MD5Helper;
import ru.pisklenov.android.GoodNight.util.PhoneModeHelper;
import ru.pisklenov.android.GoodNight.play.PlayerService;
import ru.pisklenov.android.GoodNight.util.PreferencesHelper;
import ru.pisklenov.android.GoodNight.play.TrackList;
import ru.pisklenov.android.GoodNight.util.VolumeHelper;
import ru.pisklenov.android.GoodNight.util.WallpaperList;

public class MainActivity extends Activity {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    private static final String GOOGLE_PLAY_LINK = "http://play.google.com/store/search?q=pub:Andrei Pisklenov";
    private static final String MARKET_LINK = "market://search?q=pub:Andrei Pisklenov";
    //private static final String GOOGLE_PLAY_LINK = "market://search?q=pub:Andrei Pisklenov";

    public static ImageButton imageButtonPlay;
    public static ImageButton imageButtonNext;
    public static ImageButton imageButtonPrev;

    public static TextView textViewSongCurrentDuration;
    public static TextView textViewTitle;
    public static TextView textViewTotalDuration;

    public static SeekBar seekBar;

    ImageButton imageButtonTimer;
    ImageButton imageButtonPhoneControl;
    ImageButton imageButtonTrackList;
    ImageView imageViewWallpaper;
    TextView textViewOffTimer;


    public static TrackList trackList;
    //Player player;
    //AlertDialog.Builder trackListDialog = null;
    boolean isTrackListDialogShowing = false;

    OffTimerTask offTimerTask;
    UpdateWallpapersTask updateWallpapersTask;

    PreferencesHelper preferencesHelper;
    PhoneModeHelper phoneModeHelper;

    int currentPhoneState;
    int offTimerCount = 0;

    // Songs list
    public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    public Intent playerService;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.w(TAG, "MainActivity.onStop()");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.w(TAG, "MainActivity.onDestroy()");

        //if (!PlayerService.mp.isPlaying()) {


        // stopService(new Intent(MainActivity.this, PlayerService.class));


        //stopService(playerService);
        //cancelNotification();
        // }


        // return default phone mode state
        if (phoneModeHelper != null) {
            phoneModeHelper.setMode(currentPhoneState);
        }
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(MainActivity.this, PlayerService.class));

        finish();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (DEBUG) Log.w(TAG, "MainActivity.onPause()");

       /* if (updateTrackPosTask != null) {
            updateTrackPosTask.cancel(true);
            updateTrackPosTask = null;
        }*/

        if (updateWallpapersTask != null) {
            updateWallpapersTask.cancel(true);
            updateWallpapersTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DEBUG) Log.w(TAG, "MainActivity.onResume()");

        /*updateTrackPosTask = new UpdateTrackPosTask();
        updateTrackPosTask.execute();
*/
        updateWallpapersTask = new UpdateWallpapersTask();
        updateWallpapersTask.execute();
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (DEBUG) Log.w(TAG, "MainActivity.onCreate()");

        initViews();

        imageButtonPhoneControl.setOnClickListener(new ButtonPhoneControlOnClickListener());
        imageButtonTimer.setOnClickListener(new ButtonTimerOnClickListener());
        imageButtonTrackList.setOnClickListener(new ButtonShowTrackListOnClickListener());


        // set min volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);


        SaveThisObjects saveThisObjects = (SaveThisObjects) getLastNonConfigurationInstance();
        if (saveThisObjects != null && saveThisObjects.offTimerCount != 0) {
            offTimerTask = new OffTimerTask(saveThisObjects.offTimerCount);
            offTimerTask.execute();
        }

        if (saveThisObjects != null && saveThisObjects.trackList != null) {
            trackList = saveThisObjects.trackList;
        } else {
            if (trackList == null) trackList = new TrackList(MainActivity.this, 0);
        }

        if (saveThisObjects != null) {
            if (isTrackListDialogShowing) showTrackListDialog();
        }


        // create preferences class
        preferencesHelper = new PreferencesHelper(MainActivity.this);

        // create control mode class
        phoneModeHelper = new PhoneModeHelper(MainActivity.this);
        currentPhoneState = phoneModeHelper.getCurrentMode();

        if (preferencesHelper.getBoolean("PhoneModeSilent", true)) {
            phoneModeHelper.setModeSilent();
        }


        playerService = new Intent(this, PlayerService.class);
        playerService.putExtra("songIndex", PlayerService.currentSongIndex);
        startService(playerService);

        //new DownloadTask().execute();
    }

    private void initViews() {
        imageButtonTimer = (ImageButton) findViewById(R.id.imageButtonTimer);
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        imageButtonNext = (ImageButton) findViewById(R.id.imageButtonNext);
        imageButtonPrev = (ImageButton) findViewById(R.id.imageButtonPrev);
        imageButtonTrackList = (ImageButton) findViewById(R.id.imageButtonTrackList);
        imageButtonPhoneControl = (ImageButton) findViewById(R.id.imageButtonPhoneControl);

        imageViewWallpaper = (ImageView) findViewById(R.id.imageViewWallpaper);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewSongCurrentDuration = (TextView) findViewById(R.id.textViewSongCurrentDuration);
        textViewTotalDuration = (TextView) findViewById(R.id.textViewTotalDuration);
        textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                AlertDialog.Builder helpDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.menu_help_title)
                        .setMessage(R.string.menu_help_message)
                        .setIcon(R.drawable.menu_help)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                helpDialog.show();
                return true;
            case R.id.about:
                AlertDialog.Builder aboutDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.menu_about_title)
                        .setMessage(R.string.menu_about_message)
                        .setIcon(R.drawable.menu_help)
                        .setPositiveButton(R.string.button_visit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                /*Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_PLAY_LINK));
                                startActivity(browserIntent);*/

                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(MARKET_LINK)));
                                } catch (android.content.ActivityNotFoundException e) {
                                    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(GOOGLE_PLAY_LINK)));
                                }

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                aboutDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*private Player getPlayer() {
        Player player = new Player(MainActivity.this);

        player.createPlayer(trackList.getCurrentTrack(), false);

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (DEBUG) Log.i(TAG, "player.OnCompletionListener() go button_next track");
                imageButtonNext.performClick();
            }
        });
        player.setTrackChangeEventListener(new Player.OnTrackChangeEventListener() {
            @Override
            public void onEvent(Object o) {
                if (DEBUG) Log.i(TAG, "player.OnTrackChangeEventListener() track changed");
                textViewTitle.setText(((TrackList.Track) o).title);
            }
        });

        return player;
    }*/


    class OffTimerTask extends AsyncTask<Void, Void, Void> implements Serializable {
        OffTimerTask(int secondCounter) {
            offTimerCount = secondCounter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    publishProgress();
                    offTimerCount--;

                    if (offTimerCount <= 0) {
                        return null;
                    }

                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            if (DEBUG) Log.d(TAG, "OffTimerTask " + offTimerCount);

            //TextView tmp_textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);
            if (textViewOffTimer != null && textViewOffTimer.getVisibility() != View.VISIBLE) {
                textViewOffTimer.setVisibility(View.VISIBLE);
            }

            if (textViewOffTimer != null) {
                int min = offTimerCount / 60;
                int sec = offTimerCount - 60 * min;
                textViewOffTimer.setText(min + ":" + sec);
            }

            if (offTimerCount < 10 &&
                    VolumeHelper.getCurrentVolumeInPercent(MainActivity.this) > VolumeHelper.MEDIUM_VOLUME_PERCENT) {
                VolumeHelper.setMediumVolume(MainActivity.this);
            }

            if (offTimerCount < 5 &&
                    VolumeHelper.getCurrentVolumeInPercent(MainActivity.this) > VolumeHelper.LOW_VOLUME_PERCENT) {
                VolumeHelper.setLowVolume(MainActivity.this);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tmp_textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);
            if (tmp_textViewOffTimer != null) {
                tmp_textViewOffTimer.setVisibility(View.INVISIBLE);
            }

            stopService(new Intent(MainActivity.this, PlayerService.class));

            finish();
        }
    }

    /* class UpdateTrackPosTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    publishProgress();

                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            //if (Thread.interrupted()) return;

            if (player != null && player.isPlaying()) {

                //Log.d(TAG, String.valueOf(player.getCurrentPosition() + " " + player.getDuration()));
                //Log.d(TAG, String.valueOf(Math.round(((float)player.getCurrentPosition() / (float)player.getDuration()) * 100)));

                progressBar.setProgress(Math.round(((float) player.getCurrentPosition() / (float) player.getDuration()) * 100));
            } else {
                //if (progressBar != null) progressBar.setProgress(0);
            }
        }
    }*/

    class UpdateWallpapersTask extends AsyncTask<Void, Void, Void> {
        static final int CHANGE_WALLPAPER_PERIOD = 10; // 10 second
        int secCounter = 0;
        int prevResID = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    TimeUnit.SECONDS.sleep(1);

                    secCounter++;

                    if (secCounter >= CHANGE_WALLPAPER_PERIOD) {
                        secCounter = 0;
                        publishProgress();
                    }
                } catch (InterruptedException e) {
                    return null;
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            if (DEBUG) Log.d(TAG, "UpdateWallpapersTask.onProgressUpdate");

            int newWallpaperID = WallpaperList.getRandWallpaperID(prevResID);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), newWallpaperID);
            BitmapHelper.ImageViewAnimatedChange(MainActivity.this, imageViewWallpaper, bitmap);

            prevResID = newWallpaperID;
        }
    }


    class PhoneControlIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            if (DEBUG) Log.d(TAG, "onContextItemSelected");

            switch (item.getItemId()) {
                case R.id.phone_control_silent_on:
                    preferencesHelper.setBoolean("PhoneModeSilent", true);
                    phoneModeHelper.setModeSilent();
                    break;
                case R.id.phone_control_silent_off:
                    preferencesHelper.setBoolean("PhoneModeSilent", false);
                    phoneModeHelper.setModeNormal();
                    break;

                default: ;
            }
        }
    }

    class TimerIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            if (DEBUG) Log.d(TAG, "onContextItemSelected");

            if (offTimerTask != null) {
                offTimerTask.cancel(true);
                textViewOffTimer.setVisibility(View.INVISIBLE);
            }

            switch (item.getItemId()) {
                case R.id.timer_off:
                    //
                    break;
                case R.id.timer_10:
                    offTimerTask = new OffTimerTask(10*60);
                    offTimerTask.execute();
                    break;
                case R.id.timer_20:
                    offTimerTask = new OffTimerTask(20*60);
                    offTimerTask.execute();
                    break;
                case R.id.timer_30:
                    offTimerTask = new OffTimerTask(30*60);
                    offTimerTask.execute();
                    break;
                default:
                    ;
            }
        }
    }

    class ButtonTimerOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            IconContextMenu cm = new IconContextMenu(MainActivity.this, R.menu.off_timer);
            cm.setTitle(R.string.menu_timer_off_title);
            cm.setOnIconContextItemSelectedListener(new TimerIconContextMenuSelectedListener());
            cm.show();
        }
    }

    class ButtonPhoneControlOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            IconContextMenu cm = new IconContextMenu(MainActivity.this, R.menu.phone_control);
            cm.setTitle(R.string.menu_phone_control_title);
            cm.setOnIconContextItemSelectedListener(new PhoneControlIconContextMenuSelectedListener());
            cm.show();
        }
    }


    private void showTrackListDialog() {
        AlertDialog.Builder trackListDialog = new AlertDialog.Builder(MainActivity.this);
        //trackListDialog.setCancelable(true);
        //trackListDialog.setIcon(R.drawable.ic_launcher);
        //trackListDialog.setTitle(R.string.select_track);
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_item);
        //final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.select_dialog_singlechoice);


        for (TrackList.Track track : trackList.getTracks(MainActivity.this)) {
            arrayAdapter.add(track.title);
        }
            /*for (Track track : trackList.getTracks()) {
                arrayAdapter.add(track.title);
            }*/

        trackListDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                isTrackListDialogShowing = false;
            }
        });

        trackListDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        int checkedItem = 0;//trackList.getCurrentTrackNum();
        trackListDialog.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String strName = arrayAdapter.getItem(i);
                  /*  trackList.setCurrentTrack(strName);

                    TrackList.Track track = trackList.getCurrentTrack();
*/
                playerService = new Intent(MainActivity.this, PlayerService.class);
                playerService.putExtra("songIndex", i);
                startService(playerService);

                //player.createPlayer(track, true);

                dialogInterface.dismiss();
            }
        });
        /*    trackListDialog.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String strName = arrayAdapter.getItem(i);
                  *//*  trackList.setCurrentTrack(strName);

                    TrackList.Track track = trackList.getCurrentTrack();
*//*
                    playerService = new Intent(MainActivity.this, PlayerService.class);
                    playerService.putExtra("songIndex", i);
                    startService(playerService);

                    //player.createPlayer(track, true);

                    dialogInterface.dismiss();
                }
            });*/

        trackListDialog.show();
        isTrackListDialogShowing = true;
    }

    class ButtonShowTrackListOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            showTrackListDialog();
        }
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {
        static final String HTTPS_DRIVE_GOOGLE_COM = "https://drive.google.com/uc?id=%s&export=download";
        //static final String DRIVE_GOOGLE_MAIN_FILE_ID = "0B96N99jRte7mLWNyNDVNdWFsQ28";
        static final String DRIVE_GOOGLE_MAIN_FILE_ID = "0B96N99jRte7mSUN1R21GYktPRXc";
        //static final String DRIVE_GOOGLE_MAIN_FILE_ID = "0B96N99jRte7mSUN1R21GYktPRXc";

        //0B96N99jRte7mSUN1R21GYktPRXc  - tracklist.txt

        @Override
        protected Void doInBackground(Void... voids) {
            if (DEBUG) Log.i(TAG, "DownloadTask start");

            //URI raw = URI.create("android.resource://" + MainActivity.this.getPackageName() + "/raw/" + R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena);
            /*File file = new File("android.resource://" + MainActivity.this.getPackageName() + "/raw/" + R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena);
            if (DEBUG) Log.e(TAG, "MD5 " + MD5Helper.calculateMD5(file));*/

            // Prepare
            InternetHelper.trustEveryone();

            final File availablePath = FileHelper.getAvailablePath(MainActivity.this);
            File trackListFile = new File(availablePath, "tracklist.txt");


            final Download downloadTrackListFile = new Download(String.format(HTTPS_DRIVE_GOOGLE_COM, DRIVE_GOOGLE_MAIN_FILE_ID), trackListFile);
            downloadTrackListFile.setOnCompleteListener(new Download.ActionOnCompleteListener() {
                @Override
                public void onComplete(Boolean isCompleteOk, File outputFile) {
                    if (isCancelled()) return;

                    if (DEBUG) Log.w(TAG, "DOWNLOAD COMPLETE !!! " + isCompleteOk);

                    if (isCompleteOk) {
                        if (DEBUG) Log.w(TAG, "trackListFile.getPath() " + outputFile.getPath());
                        if (DEBUG)
                            Log.w(TAG, "trackListFile.getAbsolutePath() " + outputFile.getAbsolutePath());
                        //if (DEBUG) Log.w(TAG, "trackListFile.getCanonicalPath() " + trackListFile.getCanonicalPath());
                        preferencesHelper.setString("trackListPath", outputFile.getPath());

                        // Step 1. Tracklist loaded. Need parse
                        ArrayList<TrackList.DownloadedTrackItem> downloadedTrackItems = new ArrayList<TrackList.DownloadedTrackItem>();

                        ArrayList<String> strings = FileHelper.getLinesFromFile(outputFile);
                        for (String line : strings) {
                            if (DEBUG) Log.w(TAG, "line " + line);
                            downloadedTrackItems.add(new TrackList.DownloadedTrackItem(line));
                        }

                        for (final TrackList.DownloadedTrackItem downloadedTrackItem : downloadedTrackItems) {
                            if (preferencesHelper.getString(downloadedTrackItem.md5, null) != null) {
                                if (DEBUG) Log.i(TAG, "file already exist = " + downloadedTrackItem.md5);
                                continue;
                            }


                            // Step 2. Load every track from list
                            final Download downloadTrack = new Download(String.format(HTTPS_DRIVE_GOOGLE_COM,
                                    downloadedTrackItem.googleDriveID),
                                    new File(availablePath, downloadedTrackItem.md5 + ".mp3"));

                            downloadTrack.setOnCompleteListener(new Download.ActionOnCompleteListener() {
                                @Override
                                public void onComplete(Boolean isCompleteOk, File outputFile) {
                                    if (isCancelled()) return;

                                    if (isCompleteOk) {
                                        String md5 = MD5Helper.calculateMD5(outputFile);

                                        if (downloadedTrackItem.md5.equals(md5)) {
                                            preferencesHelper.setString(md5, "OK");
                                        }


                                    }
                                }
                            });

                            downloadTrack.startDownload();
                        }
                    }
                }
            });
            downloadTrackListFile.startDownload();

            return null;
        }
    }

    /* private class ListenToPhoneState extends PhoneStateListener {
        int currentRingerMode;

        public void onCallStateChanged(int state, String incomingNumber) {
            Log.w("telephony-example", "State changed: " + stateName(state));
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    audioManager.setRingerMode(currentRingerMode);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK: ;
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    currentRingerMode = audioManager.getRingerMode();

                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;
            }
        }

        String stateName(int state) {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE: return "Idle";
                case TelephonyManager.CALL_STATE_OFFHOOK: return "Off hook";
                case TelephonyManager.CALL_STATE_RINGING: return "Ringing";
            }
            return Integer.toString(state);
        }
    }*/

    @Override
    public Object onRetainNonConfigurationInstance() {
        SaveThisObjects saveThisObjects = new SaveThisObjects();

        // saveThisObjects.updateTrackPosTask = updateTrackPosTask;
        // saveThisObjects.player = player;

        saveThisObjects.offTimerCount = offTimerCount;
        saveThisObjects.trackList = trackList;
        saveThisObjects.isTrackListDialogShowing = isTrackListDialogShowing;

        return saveThisObjects;
    }

    class SaveThisObjects {
        //public Player player;
        //public UpdateTrackPosTask updateTrackPosTask;

        public int offTimerCount;
        public TrackList trackList;
        public boolean isTrackListDialogShowing;
    }
}
