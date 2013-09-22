package ru.pisklenov.android.GoodNight.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.iconcontext.IconContextMenu;
import ru.pisklenov.android.GoodNight.util.BitmapHelper;
import ru.pisklenov.android.GoodNight.util.Player;
import ru.pisklenov.android.GoodNight.util.PreferencesHelper;
import ru.pisklenov.android.GoodNight.util.Track;
import ru.pisklenov.android.GoodNight.util.TrackList;
import ru.pisklenov.android.GoodNight.util.WallpaperList;


public class MainActivity extends Activity {
    private static final boolean DEBUG = GN.DEBUG;
    //RelativeLayout relativeLayoutBack;

    ImageButton imageButtonPlay;
    ImageButton imageButtonNext;
    ImageButton imageButtonPrev;
    ImageButton imageButtonTimer;
    ImageButton imageButtonTrackList;
    ImageButton imageButtonPhoneControl;

    ImageView imageViewWallpaper;

    TextView textViewTitle;
    TextView textViewOffTimer;

    ProgressBar progressBar;
    TrackList trackList;
    Player player;

    Thread updateTrackPosThread;
    //UpdateTrackPosTask updateTrackPosTask;
    UpdateWallpapers updateWallpapersTask;
    OffTimerTask offTimerTask;

    ListenToPhoneState listener;

    PreferencesHelper preferencesHelper;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public void onStop() {
        super.onStop();

        if (DEBUG) Log.w(GN.TAG, "MainActivity.onStop()");
    }

    @Override
    public void onBackPressed() {
        player.release();
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (DEBUG) Log.w(GN.TAG, "MainActivity.onPause()");

        if (updateTrackPosThread != null) {
            updateTrackPosThread.interrupt();
        }
        /*if (updateTrackPosTask != null) {
            updateTrackPosTask.cancel(true);
        }*/

        if (updateWallpapersTask != null) {
            updateWallpapersTask.cancel(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DEBUG) Log.w(GN.TAG, "MainActivity.onResume()");
        /*if (saveThisObjects != null && saveThisObjects.updateTrackPosTask != null) {
            updateTrackPosTask = saveThisObjects.updateTrackPosTask;
        } else {
            updateTrackPosTask = new UpdateTrackPosTask();
        }*/


        if (updateTrackPosThread != null) {
            //updateTrackPosThread.
            //updateTrackPosThread.
            updateTrackPosThread.interrupt();
        }
        updateTrackPosThread = new Thread(new UpdateTrackPosThread());
        updateTrackPosThread.start();

        /*updateTrackPosTask = new UpdateTrackPosTask();
        updateTrackPosTask.execute();*/


        updateWallpapersTask = new UpdateWallpapers();
        updateWallpapersTask.execute();
    }

    class TEST extends Thread {
        boolean isRunningNow = true;

        public TEST() {
            super(new Runnable() {
                @Override
                public void run() {
                    while (isRunningNow) {

                        //Log.e(GN.TAG, "UpdateTrackPosThread ok");

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (player != null && player.isPlaying()) {
                                    progressBar.setProgress(Math.round(((float) player.getCurrentPosition() / (float) player.getDuration()) * 100));
                                } else {
                                    //Log.d(GN.TAG, "player == null || !player.isPlaying()");
                                    //if (progressBar != null) progressBar.setProgress(0);
                                }
                            }
                        });

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {}
                    }
                }
            });
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (DEBUG) Log.w(GN.TAG, "MainActivity.onCreate()");

        // create preferences class
        preferencesHelper = new PreferencesHelper(MainActivity.this);


        // set min volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(maxVolume * 0.2), 0);
        //audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        Log.d(GN.TAG, String.valueOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
        Log.d(GN.TAG, String.valueOf((int) Math.round(maxVolume * 0.2)));



        /*TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        listener = new ListenToPhoneState();
        tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);*/
        //relativeLayoutBack = (RelativeLayout) findViewById(R.id.relativeLayoutBody);

        imageViewWallpaper = (ImageView) findViewById(R.id.imageViewWallpaper);

        imageButtonTimer = (ImageButton) findViewById(R.id.buttonTimer);
        imageButtonTimer.setOnClickListener(new ButtonTimerOnClickListener());

        imageButtonPlay = (ImageButton) findViewById(R.id.buttonPlay);
        imageButtonPlay.setOnClickListener(new ButtonPlayOnClickListener());

        imageButtonNext = (ImageButton) findViewById(R.id.buttonNext);
        imageButtonNext.setOnClickListener(new ButtonNextOnClickListener());

        imageButtonPrev = (ImageButton) findViewById(R.id.buttonPrev);
        imageButtonPrev.setOnClickListener(new ButtonPrevOnClickListener());

        imageButtonTrackList = (ImageButton) findViewById(R.id.buttonTrackList);
        imageButtonTrackList.setOnClickListener(new ButtonShowTrackListOnClickListener());


        imageButtonPhoneControl = (ImageButton) findViewById(R.id.buttonPhoneControl);
        imageButtonPhoneControl.setOnClickListener(new ButtonPhoneControlOnClickListener());


        textViewOffTimer = (TextView) findViewById(R.id.textViewOffTimer);


        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax(100);

        SaveThisObjects saveThisObjects = (SaveThisObjects) getLastNonConfigurationInstance();

        if (saveThisObjects != null && saveThisObjects.trackList != null) {
            trackList = saveThisObjects.trackList;
        } else {
            trackList = new TrackList(0);
        }

        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        textViewTitle.setText(trackList.getCurrentTrack().title);

        if (saveThisObjects != null && saveThisObjects.player != null) {
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
        });


    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        SaveThisObjects saveThisObjects = new SaveThisObjects();

        saveThisObjects.player = player;
        saveThisObjects.offTimerTask = offTimerTask;
        //saveThisObjects.updateTrackPosTask = updateTrackPosTask;
        saveThisObjects.trackList = trackList;

        return saveThisObjects;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();


        switch (v.getId()) {
            case R.id.buttonTimer:
                Log.i(GN.TAG, "THIS IS buttonTimer");
                break;
            case R.id.buttonPhoneControl:
                Log.i(GN.TAG, "THIS IS buttonPhoneControl");
                break;
        }


        Log.d(GN.TAG, "onCreateContextMenu");
        inflater.inflate(R.menu.off_timer, menu);
    }


    class UpdateTrackPosThread implements Runnable {
        public boolean isOk = true;

        @Override
        public void run() {

            while (!Thread.interrupted()) {

                //Log.e(GN.TAG, "UpdateTrackPosThread ok");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (player != null && player.isPlaying()) {
                            progressBar.setProgress(Math.round(((float) player.getCurrentPosition() / (float) player.getDuration()) * 100));
                        } else {
                            //Log.d(GN.TAG, "player == null || !player.isPlaying()");
                            //if (progressBar != null) progressBar.setProgress(0);
                        }
                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
            }
        }
    }
/*

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        Log.d(GN.TAG, "onContextItemSelected");

        if (offTimerTask != null) {
            offTimerTask.cancel(true);
            textViewOffTimer.setVisibility(View.INVISIBLE);
        }

        switch (item.getItemId()) {
            case R.id.timer_off:
                //
                return true;
            case R.id.timer_10:
                offTimerTask = new OffTimerTask(60);
                offTimerTask.execute();
                return true;
            case R.id.timer_20:
                offTimerTask = new OffTimerTask(120);
                offTimerTask.execute();
                return true;
            case R.id.timer_30:
                offTimerTask = new OffTimerTask(180);
                offTimerTask.execute();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }*/

    class OffTimerTask extends AsyncTask<Void, Void, Void> {
        int secondCounter; // ony seconds

        OffTimerTask(int secondCounter) {
            this.secondCounter = secondCounter;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                publishProgress();
                secondCounter--;
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            if (textViewOffTimer.getVisibility() != View.VISIBLE) {
                textViewOffTimer.setVisibility(View.VISIBLE);
            }

            int min = secondCounter / 60;
            int sec = secondCounter - 60 * min;
            textViewOffTimer.setText(min + ":" + sec);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (textViewOffTimer != null) {
                textViewOffTimer.setVisibility(View.INVISIBLE);
            }

            if (player != null && player.isPlaying()) {
                player.pause();
                player.release();
            }
        }
    }

    class UpdateTrackPosTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            while (!Thread.interrupted()) {
                publishProgress();

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... voids) {
            if (Thread.interrupted()) return;

            if (player != null && player.isPlaying()) {

                //Log.d(GN.TAG, String.valueOf(player.getCurrentPosition() + " " + player.getDuration()));
                //Log.d(GN.TAG, String.valueOf(Math.round(((float)player.getCurrentPosition() / (float)player.getDuration()) * 100)));

                progressBar.setProgress(Math.round(((float) player.getCurrentPosition() / (float) player.getDuration()) * 100));
            } else {
                //if (progressBar != null) progressBar.setProgress(0);
            }
        }
    }

    class UpdateWallpapers extends AsyncTask<Void, Void, Void> {
        static final int CHANGE_WALLPAPER_PERIOD = 10; // 10 second
        int secCounter = 0;
        int prevResID = 0;

        @Override
        protected Void doInBackground(Void... voids) {
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }

                secCounter++;

                if (secCounter >= CHANGE_WALLPAPER_PERIOD) {
                    secCounter = 0;
                    publishProgress();
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

    class ButtonTimerOnClickListener implements Button.OnClickListener {
        @Override
        public void onClick(View view) {
            IconContextMenu cm = new IconContextMenu(MainActivity.this, R.menu.off_timer);
            cm.setTitle(R.string.menu_timer_off_title);
            cm.setOnIconContextItemSelectedListener(new TimerIconContextMenuSelectedListener());
            cm.show();
        }
    }

    class PhoneControlIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            Log.d(GN.TAG, "onContextItemSelected");

            switch (item.getItemId()) {
                case R.id.phone_control_mute_on:
                    //
                    break;
                case R.id.phone_control_mute_off:

                    break;

                default: ;
            }
        }
    }

    class TimerIconContextMenuSelectedListener implements IconContextMenu.IconContextItemSelectedListener {
        @Override
        public void onIconContextItemSelected(MenuItem item, Object info) {
            Log.d(GN.TAG, "onContextItemSelected");

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
                default: ;
            }
        }
    }

    class ButtonPlayOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if (!player.isLoading && !player.isPlaying()) {
                Track track = trackList.getCurrentTrack();
                player.playRes(track);

                return;
            }

            if (player.isPlaying()) {
                player.pause();

                ((ImageView) view).setImageResource(R.drawable.play);
                //view.setBackgroundResource(R.drawable.play);
                Log.i(GN.TAG, "pause");
            } else {
                player.start();

                ((ImageView) view).setImageResource(R.drawable.pause);
                //view.setBackgroundResource(R.drawable.pause);
                Log.i(GN.TAG, "start");
            }
        }
    }

    class ButtonNextOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.i(GN.TAG, "next");

            //player.release();

            Track track = trackList.getNextTrack();
            player.playRes(track);
        }
    }

    class ButtonPrevOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Log.i(GN.TAG, "prev");

            //player.release();

            Track track = trackList.getPrevTrack();
            player.playRes(track);
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

            for (Track track : trackList.getTracks()) {
                arrayAdapter.add(track.title);
            }

            builderSingle.setNegativeButton(R.string.cancel,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

            int checkedItem = trackList.getCurrentTrackNum();
            builderSingle.setSingleChoiceItems(arrayAdapter, checkedItem, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    String strName = arrayAdapter.getItem(i);
                    trackList.setCurrentTrack(strName);

                    Track track = trackList.getCurrentTrack();
                    player.playRes(track);

                    dialogInterface.dismiss();
                }
            });

            builderSingle.show();
        }
    }

    private class ListenToPhoneState extends PhoneStateListener {
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
    }

    class SaveThisObjects {
        public Player player;
        //public UpdateTrackPosTask updateTrackPosTask;
        public OffTimerTask offTimerTask;
        public TrackList trackList;
    }
}
