package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import ru.pisklenov.android.GoodNight.GN;

/**
 * Created by dns on 10.09.13.
 */
public class Player {
    static final boolean DEBUG = GN.DEBUG;

    Context context;
    MediaPlayer mediaPlayer;
    public boolean isLoading;
    Track currentTrack;

    OnTrackChangeEventListener mListener;

    public Player(Context context) {
        this.context = context;

        this.isLoading = false;
    }

    public void createPlayer(Track track) {
        release();

        currentTrack = track;

        if (DEBUG) Log.d(GN.TAG, "createPlayer() " + track.title);

        mediaPlayer = MediaPlayer.create(context, currentTrack.resID);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if (DEBUG) Log.e(GN.TAG, "MediaPlayer.onError " + i + " " + i2);

                return false;
            }
        });

        isLoading = true;

        onTrackChange(currentTrack);
    }

    public void playRes(Track track) {
        release();

        currentTrack = track;

        if (DEBUG) Log.d(GN.TAG, "playRes() " + track.title);

        mediaPlayer = MediaPlayer.create(context, currentTrack.resID);
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if (DEBUG) Log.e(GN.TAG, "MediaPlayer.onError " + i + " " + i2);

                return false;
            }
        });
        isLoading = true;

        mediaPlayer.start();

        onTrackChange(currentTrack);
    }

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();

            if (DEBUG) Log.d(GN.TAG, "Player.start()");
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();

            if (DEBUG) Log.d(GN.TAG, "Player.pause()");
        }
    }

    public void release(){
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {}

            isLoading = false;

            if (DEBUG) Log.d(GN.TAG, "Player.release()");
        }
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null && isLoading) {
            return mediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    public int getDuration() {
        if (mediaPlayer != null && isLoading) {
            return mediaPlayer.getDuration();
        }

        return 0;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(listener);
        }
    }

    public boolean isPlaying() {
        if (mediaPlayer != null) {
            //try {
                return mediaPlayer.isPlaying();
            //} catch (Exception e) { }

        }

        return false;
    }

    private void onTrackChange(Object o) {
        if (mListener != null) {
            mListener.onEvent(o);
        }
    }

    public void setTrackChangeEventListener(OnTrackChangeEventListener eventListener) {
        mListener = eventListener;
    }

    public interface OnTrackChangeEventListener {
        public void onEvent(Object o);
    }
}
