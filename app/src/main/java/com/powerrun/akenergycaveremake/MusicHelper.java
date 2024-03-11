package com.powerrun.akenergycaveremake;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

public class MusicHelper {
    private static final String TAG = "MusicHelper";
    private int[] musicIndex = new int[]{
            R.raw.music_1_yunning,
            R.raw.music_2_hehe,
            R.raw.music_3_tianjian,
            R.raw.music_4_pojian,
            R.raw.music_5_zhanfang
    };
    private int currentIndex = 0;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private void create(Context context) {
        Log.i(TAG, "create");
        mContext = context;
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }
    private void destroy() {
        Log.i(TAG, "destroy");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
    private int getPosition() {
        Log.i(TAG, "getPosition: index=" + currentIndex);
        return currentIndex;
    }
    private void playSong(int index) {
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
    private void pause() {
        Log.i(TAG, "pause");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
}
