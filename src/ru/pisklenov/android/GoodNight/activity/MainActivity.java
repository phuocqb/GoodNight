package ru.pisklenov.android.GoodNight.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.iconcontext.IconContextMenu;
import ru.pisklenov.android.GoodNight.util.BitmapHelper;
import ru.pisklenov.android.GoodNight.util.FileHelper;
import ru.pisklenov.android.GoodNight.util.MD5Helper;
import ru.pisklenov.android.GoodNight.util.PhoneModeHelper;
import ru.pisklenov.android.GoodNight.util.Player;
import ru.pisklenov.android.GoodNight.util.PlayerService;
import ru.pisklenov.android.GoodNight.util.PreferencesHelper;
import ru.pisklenov.android.GoodNight.util.SongsProvider;
import ru.pisklenov.android.GoodNight.util.Track;
import ru.pisklenov.android.GoodNight.util.TrackList;
import ru.pisklenov.android.GoodNight.util.WallpaperList;

public class MainActivity extends Activity {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

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


    TrackList trackList;
    Player player;


    OffTimerTask offTimerTask;
    UpdateWallpapersTask updateWallpapersTask;

    PreferencesHelper preferencesHelper;
    PhoneModeHelper phoneModeHelper;

    int currentPhoneState;

    // Songs list
    public static ArrayList<HashMap<String, String>> songsList = new ArrayList<HashMap<String, String>>();
    public Intent playerService;

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.w(TAG, "MainActivity.onStop()");
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.w(TAG, "MainActivity.onDestroy()");

        if (!PlayerService.mp.isPlaying()) {
            stopService(playerService);
            cancelNotification();
        }

        /*if (playerService != null) {
            stopService(playerService);
        }*/

        if (player != null) {
            player.release();
        }

        // return default phone mode state
        if (phoneModeHelper != null) {
            phoneModeHelper.setMode(currentPhoneState);
        }
    }

    @Override
    public void onBackPressed() {
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

        initViews();

        imageButtonPhoneControl.setOnClickListener(new ButtonPhoneControlOnClickListener());
        imageButtonTimer.setOnClickListener(new ButtonTimerOnClickListener());
        imageButtonTrackList.setOnClickListener(new ButtonShowTrackListOnClickListener());

        if (DEBUG) Log.w(TAG, "MainActivity.onCreate()");


        new UnpackTask().execute();

       /* File file = new File("file:///android_asset/jungle_02.mp3");
        if(file.exists()) {
            Log.e(TAG, "file.exists() " + "file:///android_asset/jungle_02.mp3");
        } else {
            Log.e(TAG, "!!!file.exists() " + "file:///android_asset/jungle_02.mp3");
        }*/
/*
        if (trackList == null) {
            trackList = new TrackList(0);
        }*/

        SongsProvider plm = new SongsProvider(MainActivity.this);
        songsList = plm.getPlayList();

        // create preferences class
        preferencesHelper = new PreferencesHelper(MainActivity.this);

        // create control mode class
        phoneModeHelper = new PhoneModeHelper(MainActivity.this);
        currentPhoneState = phoneModeHelper.getCurrentMode();
        if (currentPhoneState == PhoneModeHelper.MODE_SILENT) {

        }

        // set min volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(maxVolume * 0.2), 0);

        Log.d(TAG, String.valueOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
        Log.d(TAG, String.valueOf((int) Math.round(maxVolume * 0.2)));



        playerService = new Intent(this, PlayerService.class);
        playerService.putExtra("songIndex", PlayerService.currentSongIndex);
        startService(playerService);

        /*SaveThisObjects saveThisObjects = (SaveThisObjects) getLastNonConfigurationInstance();
        if (saveThisObjects != null && saveThisObjects.player != null) {
            player.setContext(MainActivity.this);
        } else {
            player = getPlayer();
        }*/


        /*new DownloadTask().execute();*/


        /*SaveThisObjects saveThisObjects = (SaveThisObjects) getLastNonConfigurationInstance();
        if (saveThisObjects != null && saveThisObjects.trackList != null) {
            trackList = saveThisObjects.trackList;
        } else {
            trackList = new TrackList(0);
        }*/

        /*if (saveThisObjects != null && saveThisObjects.player != null) {
            player = saveThisObjects.player;
        } else {
            player = new Player(MainActivity.this);
            Track track = trackList.getCurrentTrack();
            player.createPlayer(track);

            textViewTitle.setText(track.title);
        }

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                imageButtonNext.performClick();
            }
        });
        player.setTrackChangeEventListener(new Player.OnTrackChangeEventListener() {
            @Override
            public void onEvent(Object o) {
                textViewTitle.setText(((Track) o).title);
            }
        });*/
    }

    private void initViews() {
        imageButtonTimer = (ImageButton) findViewById(R.id.imageButtonTimer);
        imageButtonPlay = (ImageButton) findViewById(R.id.imageButtonPlay);
        imageButtonNext = (ImageButton) findViewById(R.id.imageButtonNext);
        imageButtonPrev = (ImageButton) findViewById(R.id.imageButtonPrev);
        imageButtonTrackList = (ImageButton) findViewById(R.id.imageButtonTrackList);
        imageButtonPhoneControl = (ImageButton) findViewById(R.id.imageButtonPhoneControl);

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewSongCurrentDuration = (TextView) findViewById(R.id.textViewSongCurrentDuration);
        textViewTotalDuration = (TextView) findViewById(R.id.textViewTotalDuration);
        textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
    }


    // -- Cancel Notification
    public void cancelNotification() {
        String notificationServiceStr = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(notificationServiceStr);
        mNotificationManager.cancel(PlayerService.NOTIFICATION_ID);
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private Player getPlayer() {
        Player player = new Player(MainActivity.this);

        player.createPlayer(trackList.getCurrentTrack(), false);

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (DEBUG) Log.i(TAG, "player.OnCompletionListener() go next track");
                imageButtonNext.performClick();
            }
        });
        player.setTrackChangeEventListener(new Player.OnTrackChangeEventListener() {
            @Override
            public void onEvent(Object o) {
                if (DEBUG) Log.i(TAG, "player.OnTrackChangeEventListener() track changed");
                textViewTitle.setText(((Track) o).title);
            }
        });

        return player;
    }


    @Override
    public Object onRetainNonConfigurationInstance() {
        SaveThisObjects saveThisObjects = new SaveThisObjects();

        saveThisObjects.player = player;
        //saveThisObjects.offTimerTask = offTimerTask;
        //saveThisObjects.updateTrackPosTask = updateTrackPosTask;
        saveThisObjects.trackList = trackList;

        return saveThisObjects;
    }

   /* @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();


        switch (v.getId()) {
            case R.id.imageButtonTimer:
                Log.i(TAG, "THIS IS buttonTimer");
                break;
            case R.id.imageButtonPhoneControl:
                Log.i(TAG, "THIS IS buttonPhoneControl");
                break;
        }

        Log.d(TAG, "onCreateContextMenu");
        inflater.inflate(R.menu.off_timer, menu);
    }*/


    class OffTimerTask extends AsyncTask<Void, Void, Void> implements Serializable {
        int secondCounter; // ony seconds
        Context context;

        OffTimerTask(int secondCounter) {
            this.secondCounter = secondCounter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    publishProgress();
                    secondCounter--;

                    if (secondCounter <= 0) {
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
            if (DEBUG) Log.d(TAG, "OffTimerTask " + secondCounter);

            //TextView tmp_textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);
            if (textViewOffTimer != null && textViewOffTimer.getVisibility() != View.VISIBLE) {
                textViewOffTimer.setVisibility(View.VISIBLE);
            }

            if (textViewOffTimer != null) {
                int min = secondCounter / 60;
                int sec = secondCounter - 60 * min;
                textViewOffTimer.setText(min + ":" + sec);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            TextView tmp_textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);
            if (tmp_textViewOffTimer != null) {
                tmp_textViewOffTimer.setVisibility(View.INVISIBLE);
            }

            if (player != null && player.isPlaying()) {
                player.pause();
                player.release();
            }
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
            int newWallpaperID = WallpaperList.getRandWallpaperID(prevResID);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), newWallpaperID);
            BitmapHelper.ImageViewAnimatedChange(MainActivity.this, imageViewWallpaper, bitmap);

            prevResID = newWallpaperID;
        }
    }


    class PhoneControlIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            Log.d(TAG, "onContextItemSelected");

            switch (item.getItemId()) {
                case R.id.phone_control_mute_on:
                    phoneModeHelper.setModeSilent();
                    break;
                case R.id.phone_control_mute_off:
                    phoneModeHelper.setModeNormal();
                    break;

                default:
                    ;
            }
        }
    }

    class TimerIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            Log.d(TAG, "onContextItemSelected");

            if (offTimerTask != null) {
                offTimerTask.cancel(true);
                textViewOffTimer.setVisibility(View.INVISIBLE);
            }

            switch (item.getItemId()) {
                case R.id.timer_off:
                    //
                    break;
                case R.id.timer_10:
                    offTimerTask = new OffTimerTask(60);
                    offTimerTask.execute();
                    break;
                case R.id.timer_20:
                    offTimerTask = new OffTimerTask(120);
                    offTimerTask.execute();
                    break;
                case R.id.timer_30:
                    offTimerTask = new OffTimerTask(180);
                    offTimerTask.execute();
                    break;
                default:
                    ;
            }
        }
    }

    class ButtonPlayOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!player.isLoading && !player.isPlaying()) {
                Track track = trackList.getCurrentTrack();
                player.createPlayer(track, true);

                return;
            }

            if (player.isPlaying()) {
                player.pause();

                ((ImageView) view).setImageResource(R.drawable.play);
                //view.setBackgroundResource(R.drawable.play);
                Log.i(TAG, "pause");
            } else {
                player.start();

                ((ImageView) view).setImageResource(R.drawable.pause);
                //view.setBackgroundResource(R.drawable.pause);
                Log.i(TAG, "start");
            }
        }
    }

    class ButtonNextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "next");

            //player.release();
            if (player != null) {
                player.createPlayer(trackList.getNextTrack(), false);
                imageButtonPlay.performClick();
            }
           /* Track track = trackList.getNextTrack();
            player.playTrack(track);*/
        }
    }

    class ButtonPrevOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "prev");

            //player.release();
            if (player != null) {
                player.createPlayer(trackList.getPrevTrack(), false);
                imageButtonPlay.performClick();
            }
            /*Track track = trackList.getPrevTrack();
            player.playTrack(track);*/
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

    class ButtonShowTrackListOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(MainActivity.this);
            //builderSingle.setIcon(R.drawable.ic_launcher);
            builderSingle.setTitle(R.string.select_track);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    MainActivity.this, android.R.layout.select_dialog_singlechoice);


            for (HashMap<String, String> map: songsList) {
                arrayAdapter.add(map.get("songTitle"));
            }
            /*for (Track track : trackList.getTracks()) {
                arrayAdapter.add(track.title);
            }*/

            builderSingle.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            int checkedItem = 0;//trackList.getCurrentTrackNum();
            builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String strName = arrayAdapter.getItem(i);
                    trackList.setCurrentTrack(strName);

                    Track track = trackList.getCurrentTrack();
                    player.createPlayer(track, true);

                    dialogInterface.dismiss();
                }
            });

            builderSingle.show();
        }
    }

    private void trustEveryone() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }});
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new X509TrustManager[]{new X509TrustManager(){
                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {}
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }}}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    context.getSocketFactory());
        } catch (Exception e) { // should never happen
            e.printStackTrace();
        }
    }


    class UnpackTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (DEBUG) Log.i(TAG, "UnpackTask start");

            HashMap<String, Integer> internalTracks = new HashMap<String, Integer>();
            String path = MainActivity.this.getFilesDir() + "/";
            internalTracks.put(path + "maid_with_the_flaxen_hair.mp3", R.raw.maid_with_the_flaxen_hair);
            internalTracks.put(path + "sleep_away.mp3", R.raw.sleep_away);
            internalTracks.put(path + "johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3", R.raw.johann_sebastian_bach_minuet_in_g_from_anna_magdalena);
            internalTracks.put(path + "johannes_brahms_waltz_no_15.mp3", R.raw.johannes_brahms_waltz_no_15);
            internalTracks.put(path + "robert_schumann_kinderscene_op_15.mp3", R.raw.robert_schumann_kinderscene_op_15);

            for(Map.Entry<String, Integer> entry: internalTracks.entrySet()) {
                if (isCancelled()) return null;

                if (!FileHelper.isFileExists(entry.getKey())) {
                    FileHelper.copyFromResToInternal(MainActivity.this, entry.getValue(), entry.getKey());
                }
            }

            if (DEBUG) Log.i(TAG, "UnpackTask finish");
            return null;
        }
    }

    class DownloadTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (DEBUG) Log.i(TAG, "DownloadTask start");
            try {

                trustEveryone();


                //set the download URL, a url that points to a file on the internet
                //this is the file to be downloaded
                //https://drive.google.com/uc?id=0B96N99jRte7mMk93eDczODliZ2c&export=download
                URL url = new URL("https://drive.google.com/uc?id=0B96N99jRte7mMk93eDczODliZ2c&export=download");
                if (DEBUG) Log.i(TAG, "DownloadTask url ok");

                //create the new connection
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                if (DEBUG) Log.i(TAG, "DownloadTask urlConnection ok");

                //set up some things on the connection
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                //urlConnection.setSSLSocketFactory(MainActivity.this.getSocketFactory());

                //and connect!
                urlConnection.connect();
                if (DEBUG) Log.i(TAG, "DownloadTask connect ok");

                //set the path where we want to save the file
                //in this case, going to save it on the root directory of the
                //sd card.
                File trackListFile;
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    if (DEBUG) Log.i(TAG, "DownloadTask trackListFile = Environment.MEDIA_MOUNTED");
                    trackListFile = Environment.getExternalStorageDirectory();
                } else {
                    if (DEBUG) Log.i(TAG, "DownloadTask trackListFile = getFilesDir");
                    trackListFile = getFilesDir();
                }

                //File trackListFile = Environment.getExternalStorageDirectory();
                //create a new file, specifying the path, and the filename
                //which we want to save the file as.
                File outputFile = new File(trackListFile, "somefile.ext");
                if (DEBUG) Log.i(TAG, "DownloadTask file ok");

                //this will be used to write the downloaded data into the file we created
                FileOutputStream fileOutput = new FileOutputStream(outputFile);

                //this will be used in reading the data from the internet
                InputStream inputStream = urlConnection.getInputStream();

                //this is the total size of the file
                int totalSize = urlConnection.getContentLength();
                //variable to store total downloaded bytes
                int downloadedSize = 0;

                //create a buffer...
                byte[] buffer = new byte[1024];
                int bufferLength = 0; //used to store a temporary size of the buffer

                //now, read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    //add the data in the buffer to the file in the file output stream (the file on the sd card
                    fileOutput.write(buffer, 0, bufferLength);
                    //add up the size so we know how much is downloaded
                    downloadedSize += bufferLength;


                    //this is where you would do something to report the prgress, like this maybe
                    //updateProgress(downloadedSize, totalSize);

                    //if (DEBUG) Log.i(TAG, "downloadedSize = " + downloadedSize + " / " + totalSize);
                }
                //close the output stream when done
                fileOutput.close();

                if (DEBUG) Log.i(TAG, "DownloadTask OK");

                if (DEBUG) Log.i(TAG, "DownloadTask calculateMD5 " + MD5Helper.calculateMD5(outputFile));



//catch some possible errors...
            } catch (MalformedURLException e) {
                if (DEBUG) Log.e(TAG, "MalformedURLException " + e.getMessage());
                //e.printStackTrace();
            } catch (IOException e) {
                if (DEBUG) Log.e(TAG, "IOException " + e.getMessage());
                if (DEBUG) Log.e(TAG, "IOException " + e.getLocalizedMessage());
                //e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {

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

    class SaveThisObjects {
        public Player player;
        //public UpdateTrackPosTask updateTrackPosTask;
        //public OffTimerTask offTimerTask;
        public TrackList trackList;
    }
}
