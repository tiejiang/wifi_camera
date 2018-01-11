package com.intchip.media;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

public class MainActivity extends Activity {

    public StreamMadiaPlayer mStreamMadiaPlayer;
    public SurfaceView mVideoSurfaceView;
    public static MainActivity mMainActivityInstance;
    private DistanceUDPRequire mDistanceUDPRequire;
    private boolean isOnLine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        Log.d("TIEJIANG", "MainActivity---onCreate");
        setContentView(R.layout.layout);
        mMainActivityInstance = this;
        mStreamMadiaPlayer = (StreamMadiaPlayer)findViewById(R.id.main_surface);
//        mStreamMadiaPlayer = new StreamMadiaPlayer(this);
        mDistanceUDPRequire = new DistanceUDPRequire();
        isOnLine = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isOnLine){
                    try{
                        mDistanceUDPRequire.distanceUDP();
                        Thread.sleep(500);
                        Log.d("TIEJIANG", "MainActivity---onCreate"+" get distance");
                    }catch (InterruptedException i){
                        i.printStackTrace();
                        Log.d("TIEJIANG", "MainActivity---onCreate InterruptedException= "+i.getMessage());
                    }
                }
            }
        }).start();
    }

    public static MainActivity getMainActivityInstance(){

        return mMainActivityInstance;
    }

    public StreamMadiaPlayer getStreamMadiaPlayerInstance(){

        return mStreamMadiaPlayer;
    }
    @Override
    protected void onStart() {
        super.onStart();
//        Log.d("TIEJIANG", "mStreamMadiaPlayer= "+mStreamMadiaPlayer);
        mStreamMadiaPlayer.onNetworkConnected();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStreamMadiaPlayer.onDestory();
        isOnLine = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        mStreamMadiaPlayer.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStreamMadiaPlayer.onResume();
    }
}
