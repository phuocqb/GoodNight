package ru.pisklenov.android.GoodNight.play;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Random;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;
import ru.pisklenov.android.GoodNight.activity.MainActivity;
import ru.pisklenov.android.GoodNight.util.Utilities;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {
    private static String TAG = GN.TAG;
    private static final boolean DEBUG = GN.DEBUG;

    // Push Notification when Service is running.
    // Set up the notification ID
    public static final int NOTIFICATION_ID = 1;
    public static MediaPlayer mp;
    public static int currentSongIndex = -1;

    private WeakReference<ImageButton> btnRepeat, btnShuffle;
    private WeakReference<ImageButton> btnPlay, btnForward, btnBackward, btnNext, btnPrevious;
    private WeakReference<SeekBar> songProgressBar;
    private WeakReference<TextView> songTitleLabel;
    private WeakReference<TextView> songCurrentDurationLabel;
    private WeakReference<TextView> songTotalDurationLabel;
    private Handler progressBarHandler = new Handler();
    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private boolean isShuffle = false;
    private boolean isRepeat = false;
    //private ArrayList<HashMap<String, String>> songsListingSD = new ArrayList<HashMap<String, String>>();
    private TrackList trackList;

    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        if (DEBUG) Log.i(TAG, " -- PlayerService.onCreate()");

        mp = new MediaPlayer();
        mp.setOnCompletionListener(this);
        mp.reset();
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);//
        utils = new Utilities();

        trackList = MainActivity.trackList;
        //songsListingSD = MainActivity.songsList;

        songCurrentDurationLabel = new WeakReference<TextView>(MainActivity.textViewSongCurrentDuration);
        super.onCreate();
    }

    // --------------onStartCommand-----------------------------------------//
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) Log.w(TAG, " -- PlayerService.onStartCommand()");

        initUI();
        int songIndex = intent.getIntExtra("songIndex", 0);
        if (songIndex != currentSongIndex) {
            if (DEBUG) Log.w(TAG, "songIndex != currentSongIndex");
            if (DEBUG) Log.w(TAG, "songIndex == " + songIndex + " currentSongIndex == " + currentSongIndex);

            playSong(songIndex);
            //initNotification(songIndex);
            currentSongIndex = songIndex;
        } else {
            if (DEBUG) Log.w(TAG, "songIndex == currentSongIndex");

            if (currentSongIndex != -1) {
                if (DEBUG) Log.w(TAG, "currentSongIndex != -1");

                songTitleLabel.get().setText(trackList.getTracks(getApplicationContext()).get(currentSongIndex).title);
                //songTitleLabel.get().setText(songsListingSD.get(currentSongIndex).get("songTitle"));

                if (mp.isPlaying())
                    btnPlay.get().setImageResource(R.drawable.button_pause);
                else
                    btnPlay.get().setImageResource(R.drawable.button_play);
            } else {
                if (DEBUG) Log.w(TAG, "currentSongIndex == -1");
                //currentSongIndex = 0;
                //songTitleLabel.get().setText(trackList.getTracks().get(currentSongIndex).title);
            }
        }

        super.onStart(intent, startId);
        return START_STICKY;
    }

    /**
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Init UI
     */
    private void initUI() {
        songTitleLabel = new WeakReference<TextView>(MainActivity.textViewTitle);
        songCurrentDurationLabel = new WeakReference<TextView>(MainActivity.textViewSongCurrentDuration);
        songTotalDurationLabel = new WeakReference<TextView>(MainActivity.textViewTotalDuration);

        btnPlay = new WeakReference<ImageButton>(MainActivity.imageButtonPlay);
       /* btnForward = new WeakReference<ImageView>(MainActivity.btnForward);
        btnBackward = new WeakReference<ImageView>(MainActivity.btnBackward);*/
        btnNext = new WeakReference<ImageButton>(MainActivity.imageButtonNext);
        btnPrevious = new WeakReference<ImageButton>(MainActivity.imageButtonPrev);
        /*btnRepeat = new WeakReference<ImageButton>(MainActivity.btnRepeat);
        btnShuffle = new WeakReference<ImageButton>(MainActivity.btnShuffle);*/

        if (btnPlay.get() != null) btnPlay.get().setOnClickListener(this);
       /* btnForward.get().setOnClickListener(this);
        btnBackward.get().setOnClickListener(this);*/
        if (btnNext.get() != null) btnNext.get().setOnClickListener(this);
        if (btnPrevious.get() != null) btnPrevious.get().setOnClickListener(this);
       /* btnRepeat.get().setOnClickListener(this);
        btnShuffle.get().setOnClickListener(this);*/
        // TODO Auto-generated method stub

        songProgressBar = new WeakReference<SeekBar>(MainActivity.seekBar);
        try {
            songProgressBar.get().setOnSeekBarChangeListener(this);
        } catch (Exception e) {}
    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.imageButtonPlay:
                if (currentSongIndex != -1) {
                    if (mp.isPlaying()) {
                        if (mp != null) {
                            mp.pause();
                            // Changing button image to play button
                            btnPlay.get().setImageResource(R.drawable.button_play);
                            if (DEBUG) Log.d(TAG, "Pause");

                        }
                    } else {
                        // Resume song
                        if (mp != null) {
                            mp.start();
                            // Changing button image to button_pause button
                            btnPlay.get().setImageResource(R.drawable.button_pause);
                            if (DEBUG) Log.d(TAG, "Play");
                        }
                    }
                } else {
                    btnNext.get().performClick();
                }

                break;

            case R.id.imageButtonNext:
                // check if button_next song is there or not
                if (DEBUG) Log.d(TAG, "Next");


                if (currentSongIndex < (trackList.getTracks(getApplicationContext()).size() - 1)) {
                //if (currentSongIndex < (songsListingSD.size() - 1)) {
                    playSong(currentSongIndex + 1);
                    currentSongIndex = currentSongIndex + 1;
                } else {
                    // play first song
                    playSong(0);
                    currentSongIndex = 0;
                }
                break;

            case R.id.imageButtonPrev:

                if (currentSongIndex > 0) {
                    playSong(currentSongIndex - 1);
                    currentSongIndex = currentSongIndex - 1;
                } else {
                    // play last song


                    playSong(trackList.getTracks(getApplicationContext()).size() - 1);
                    currentSongIndex = trackList.getTracks(getApplicationContext()).size() - 1;

//                    playSong(songsListingSD.size() - 1);
//                    currentSongIndex = songsListingSD.size() - 1;
                }
                break;

            /*case R.id.btn_forward_imageview:

                // get current song position
                int currentPosition = mp.getCurrentPosition();
                // check if seekForward time is lesser than song duration
                if (currentPosition + seekForwardTime <= mp.getDuration()) {
                    // forward song
                    mp.seekTo(currentPosition + seekForwardTime);
                } else {
                    // forward to end position
                    mp.seekTo(mp.getDuration());
                }
                break;

            case R.id.btn_backward_imagview:
                // get current song position
                int currentPosition2 = mp.getCurrentPosition();
                // check if seekBackward time is greater than 0 sec
                if (currentPosition2 - seekBackwardTime >= 0) {
                    // forward song
                    mp.seekTo(currentPosition2 - seekBackwardTime);
                } else {
                    // backward to starting position
                    mp.seekTo(0);
                }
                break;*/



            /*case R.id.btnRepeat:

                if (isRepeat) {
                    isRepeat = false;
                    Toast.makeText(getApplicationContext(), "Repeat is OFF",
                            Toast.LENGTH_SHORT).show();
                    btnRepeat.get().setImageResource(R.drawable.btn_repeat);
                } else {
                    // make repeat to true
                    isRepeat = true;
                    Toast.makeText(getApplicationContext(), "Repeat is ON",
                            Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isShuffle = false;
                    btnRepeat.get().setImageResource(R.drawable.btn_repeat_focused);
                    btnShuffle.get().setImageResource(R.drawable.btn_shuffle);
                }
                break;

            case R.id.btnShuffle:

                if (isShuffle) {
                    isShuffle = false;
                    Toast.makeText(getApplicationContext(), "Shuffle is OFF",
                            Toast.LENGTH_SHORT).show();
                    btnShuffle.get().setImageResource(R.drawable.btn_shuffle);
                } else {
                    // make repeat to true
                    isShuffle = true;
                    Toast.makeText(getApplicationContext(), "Shuffle is ON",
                            Toast.LENGTH_SHORT).show();
                    // make shuffle to false
                    isRepeat = false;
                    btnShuffle.get().setImageResource(
                            R.drawable.btn_shuffle_focused);
                    btnRepeat.get().setImageResource(R.drawable.btn_repeat);
                }
                break;*/
        }
    }

    /**
     * Function to play a song
     *
     * @param songIndex - index of song
     */
    public void playSong(int songIndex) {
        if (DEBUG) Log.i(TAG, " -- PlayerService.playSong()");

        // Play song
        try {
           /* File file = new File(songsListingSD.get(songIndex).get("songPath"));
            if(file.exists()) {
                Log.e(TAG, "file.exists() " + songsListingSD.get(songIndex).get("songPath"));
            } else {
                Log.e(TAG, "!file.exists() " + songsListingSD.get(songIndex).get("songPath"));
            }*/
            //AssetFileDescriptor afd = getAssets().openFd("johann_sebastian_bach_minuet_in_g_from_anna_magdalena.mp3");


            //mp.setDa

            mp.reset();
            //mp.setDataSource(afd.getFileDescriptor());
            //mp.setDataSource(PlayerService.this, uri);

           /* String trackPath = null;
            if (trackList.getTracks().get(songIndex).typeID == TrackList.Track.INTERNAL) {
                trackPath = getFilesDir() + "/" + trackList.getTracks().get(songIndex).pathToFile;
            }
            FileInputStream fileInputStream = new FileInputStream(trackPath);
            //FileInputStream fileInputStream = new FileInputStream(songsListingSD.get(songIndex).get("songPath"));
            */

            //InputStream fdstream = getApplicationContext().getResources().openRawResource(trackList.getTracks().get(songIndex).resID);
            //FileDescriptor fd = fdstream.getFD();

            Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + trackList.getTracks(getApplicationContext()).get(songIndex).resID); //do not add any extension
            mp.setDataSource(getApplicationContext(), uri);


            //mp.setDataSource(fd);

            //mp.setDataSource(songsListingSD.get(songIndex).get("songPath"));
            mp.prepare();
            mp.start();

            // Displaying Song title


            String songTitle = trackList.getTracks(getApplicationContext()).get(songIndex).title;
            //String songTitle = songsListingSD.get(songIndex).get("songTitle");

            songTitleLabel.get().setText(songTitle);
            // Changing Button Image to button_pause image
            btnPlay.get().setImageResource(R.drawable.button_pause);
            // set Progress bar values
            songProgressBar.get().setProgress(0);
            songProgressBar.get().setMax(100);

            // Updating progress bar
            updateProgressBar();

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        progressBarHandler.postDelayed(mUpdateTimeTask, 100);
    }

    //--on Seekbar Change Listener
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
    }

    /**
     * When user starts moving the progress handler
     */
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        progressBarHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    public void onStopTrackingTouch(SeekBar seekBar) {
        progressBarHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);
        // forward or backward to certain seconds
        mp.seekTo(currentPosition);
        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed if repeat is ON play same song again if shuffle
     * is ON play random song
     */
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playSong(currentSongIndex);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            Random rand = new Random();


            currentSongIndex = rand.nextInt((trackList.getTracks(getApplicationContext()).size() - 1) - 0 + 1) + 0;
            //currentSongIndex = rand.nextInt((songsListingSD.size() - 1) - 0 + 1) + 0;

            playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play button_next song


            if (currentSongIndex < (trackList.getTracks(getApplicationContext()).size() - 1)) {
            //if (currentSongIndex < (songsListingSD.size() - 1)) {


                playSong(currentSongIndex + 1);
                currentSongIndex = currentSongIndex + 1;
            } else {
                // play first song
                playSong(0);
                currentSongIndex = 0;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currentSongIndex = -1;
        //Remove progress bar update Hanlder callBacks
        progressBarHandler.removeCallbacks(mUpdateTimeTask);

        if (DEBUG) Log.d(TAG, "Player Service Stopped");
        if (mp != null) {
            if (mp.isPlaying()) {
                mp.stop();
            }
            mp.release();
        }
    }

   /* // Create Notification
    private void initNotification(int songIndex) {
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        int icon = R.drawable.play;
        CharSequence tickerText = "Audio Book";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        Context context = getApplicationContext();


        CharSequence songName = trackList.getTracks().get(songIndex).title;
        //CharSequence songName = songsListingSD.get(songIndex).get("songTitle");



        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                notificationIntent, 0);
        notification.setLatestEventInfo(context, songName, null, contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }*/

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = 0;
            try {
                totalDuration = mp.getDuration();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }

            long currentDuration = 0;
            try {
                currentDuration = mp.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            // Displaying Total Duration time
            songTotalDurationLabel.get().setText(
                    "" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            songCurrentDurationLabel.get().setText(
                    "" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = (int) (utils.getProgressPercentage(currentDuration, totalDuration));
            // if (DEBUG) Log.d("Progress", ""+progress);
            songProgressBar.get().setProgress(progress);

            // Running this thread after 100 milliseconds
            progressBarHandler.postDelayed(this, 500);
            // if (DEBUG) Log.d("AndroidBuildingMusicPlayerActivity","Runable  progressbar");
        }
    };
}
