package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.Serializable;

import ru.pisklenov.android.GoodNight.GN;

/**
 * Created by dns on 10.09.13.
 */
public class Player {
    private static final boolean DEBUG = GN.DEBUG;
    //private static final long serialVersionUID = 0L;

    Context context;
    MediaPlayer mediaPlayer;
    public boolean isLoading;
    Track currentTrack;

    OnTrackChangeEventListener mListener;
    MediaPlayer.OnCompletionListener onCompletionListener;

    public Player(Context context) {
        this.context = context;

        this.isLoading = false;
    }


    public void createPlayer(Track track, boolean autoPlay) {
        release();

        currentTrack = track;

        if (DEBUG) Log.d(GN.TAG, "createPlayer() " + track.title);
        mediaPlayer = getMediaPlayer(context, currentTrack.resID);

        isLoading = true;

        if (autoPlay) {
            mediaPlayer.start();
        }

        onTrackChange(currentTrack);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /*public void playTrack(Track track) {
        release();

        currentTrack = track;

        if (DEBUG) Log.d(GN.TAG, "playTrack() " + track.title);

        mediaPlayer = getMediaPlayer(context, currentTrack.resID);
        isLoading = true;

        mediaPlayer.start();

        onTrackChange(currentTrack);
    }*/

    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();

            if (DEBUG) Log.d(GN.TAG, "Player.start()");
        }
    }


    private MediaPlayer getMediaPlayer(Context ctx, int resID) {
        MediaPlayer mediaPlayer1 = MediaPlayer.create(ctx, resID);
        mediaPlayer1.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                if (DEBUG) Log.e(GN.TAG, "MediaPlayer.onError " + i + " " + i2);
                return false;
            }
        });

        mediaPlayer1.setOnCompletionListener(onCompletionListener);

        return mediaPlayer1;
    }
    /*public void loadTrack(Track track) {
        if (mediaPlayer != null) {
            mediaPlayer.start();

            if (DEBUG) Log.d(GN.TAG, "Player.start()");
        }
    }*/

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



    public boolean isPlaying() {
        if (mediaPlayer != null) {
            //try {
                return mediaPlayer.isPlaying();
            //} catch (Exception e) { }

        }

        return false;
    }

    public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
        this.onCompletionListener = listener;

        if (mediaPlayer != null) {
            mediaPlayer.setOnCompletionListener(listener);
        }
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
