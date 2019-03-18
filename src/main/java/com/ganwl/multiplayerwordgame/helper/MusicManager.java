package com.ganwl.multiplayerwordgame.helper;

import android.content.Context;
import android.media.MediaPlayer;

import com.ganwl.multiplayerwordgame.R;

/**
 * Created by gan on 10/5/17.
 */

public class MusicManager {

    private static MediaPlayer mediaPlayer; // used for background music, whereas soundPool play all the short click sounds
    private static Context mContext;

    public static boolean isGoingNextActivity = false;

    public static void stopMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public static void initializeAndPlayMediaPlayer(Context context, boolean isSoundMuted) {
        mContext = context;
        mediaPlayer = MediaPlayer.create(mContext, R.raw.mainmenusound);
        if (mediaPlayer != null && !isSoundMuted) {
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }

    public static void pauseMediaPlayer(){
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            // pause background music
            mediaPlayer.pause();
        }
    }

    public static void playMediaPlayer(){
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            // start playing background music
            mediaPlayer.start();
            mediaPlayer.setLooping(true);
        }
    }
}
