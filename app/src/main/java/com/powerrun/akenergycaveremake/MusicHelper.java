package com.powerrun.akenergycaveremake;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MusicHelper {
    private static final String TAG = "MusicHelper";
    private final int[] musicIndex = new int[]{
            R.raw.music_1_yunning,
            R.raw.music_2_hehe,
            R.raw.music_3_tianjian,
            R.raw.music_4_pojian,
            R.raw.music_5_zhanfang
    };
    private int currentIndex = 0;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    public void create(Context context) {
        Log.i(TAG, "create");
        mContext = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(MediaPlayer::start);
    }
    public void destroy() {
        Log.i(TAG, "destroy");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
    public int getPosition() {
        Log.i(TAG, "getPosition: index=" + currentIndex);
        return currentIndex;
    }
    public void playSong(int index) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.reset();
        Log.i(TAG, "playSong: index= " + index);
        currentIndex = index;
        try {
            Uri setDataSourceuri = Uri.parse("android.resource://com.powerrun.akenergycaveremake/" + musicIndex[index]);
            mediaPlayer.setDataSource(mContext, setDataSourceuri);
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void pause() {
        Log.i(TAG, "pause");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}
