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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.InstanceState;
import com.googlecode.androidannotations.annotations.OptionsMenu;
import com.googlecode.androidannotations.annotations.ViewById;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.iconcontext.IconContextMenu;
import ru.pisklenov.android.GoodNight.util.BitmapHelper;
import ru.pisklenov.android.GoodNight.util.PhoneModeHelper;
import ru.pisklenov.android.GoodNight.util.Player;
import ru.pisklenov.android.GoodNight.util.PreferencesHelper;
import ru.pisklenov.android.GoodNight.util.Track;
import ru.pisklenov.android.GoodNight.util.TrackList;
import ru.pisklenov.android.GoodNight.util.WallpaperList;

@EActivity (R.layout.main)
@OptionsMenu(R.menu.main_menu)
public class MainActivity extends Activity {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    @ViewById
    ImageButton imageButtonPlay;
    @ViewById
    ImageButton imageButtonNext;
    @ViewById
    ImageButton imageButtonPrev;
    @ViewById
    ImageButton imageButtonTimer;
    @ViewById
    ImageButton imageButtonTrackList;
    @ViewById
    ImageButton imageButtonPhoneControl;

    @ViewById
    ImageView imageViewWallpaper;

    @ViewById
    TextView textViewTitle;

    @ViewById
    TextView textViewOffTimer;

    @ViewById
    ProgressBar progressBar;

    //@InstanceState
    TrackList trackList;
    //@InstanceState
    Player player;

    //@InstanceState
    OffTimerTask offTimerTask;

    UpdateTrackPosTask updateTrackPosTask;
    UpdateWallpapers updateWallpapersTask;

    PreferencesHelper preferencesHelper;
    PhoneModeHelper phoneModeHelper;

    @InstanceState
    int currentPhoneState;


    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG) Log.w(TAG, "MainActivity.onStop()");


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (DEBUG) Log.w(TAG, "MainActivity.onDestroy()");

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

        if (updateTrackPosTask != null) {
            updateTrackPosTask.cancel(true);
            updateTrackPosTask = null;
        }

        if (updateWallpapersTask != null) {
            updateWallpapersTask.cancel(true);
            updateWallpapersTask = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DEBUG) Log.w(TAG, "MainActivity.onResume()");

        updateTrackPosTask = new UpdateTrackPosTask();
        updateTrackPosTask.execute();

        updateWallpapersTask = new UpdateWallpapers();
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
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round(maxVolume * 0.2), 0);

        Log.d(TAG, String.valueOf(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)));
        Log.d(TAG, String.valueOf((int) Math.round(maxVolume * 0.2)));


        SaveThisObjects saveThisObjects = (SaveThisObjects) getLastNonConfigurationInstance();
        if (saveThisObjects != null && saveThisObjects.player != null) {
            player.setContext(MainActivity.this);
        } else {
            player = getPlayer();
        }

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

    @AfterViews
    void afterViews() {
        imageButtonTimer.setOnClickListener(new ButtonTimerOnClickListener());
        imageButtonPlay.setOnClickListener(new ButtonPlayOnClickListener());
        imageButtonNext.setOnClickListener(new ButtonNextOnClickListener());
        imageButtonPrev.setOnClickListener(new ButtonPrevOnClickListener());
        imageButtonTrackList.setOnClickListener(new ButtonShowTrackListOnClickListener());
        imageButtonPhoneControl.setOnClickListener(new ButtonPhoneControlOnClickListener());

        //progressBar.setMax(100);

        if (trackList == null) {
            trackList = new TrackList(0);
        }

        textViewTitle.setText(trackList.getCurrentTrack().title);


        if (player == null) {
            player = getPlayer();
        }

       /* if (player == null) {
            player = new Player(MainActivity.this);
            player.createPlayer(trackList.getCurrentTrack());
        }

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
        });*/
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

    class UpdateTrackPosTask extends AsyncTask<Void, Void, Void> {
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
    }

    class UpdateWallpapers extends AsyncTask<Void, Void, Void> {
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

                default: ;
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
                    player.createPlayer(track, true);

                    dialogInterface.dismiss();
                }
            });

            builderSingle.show();
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
