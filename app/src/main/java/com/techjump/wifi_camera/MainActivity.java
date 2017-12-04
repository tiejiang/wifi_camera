package com.techjump.wifi_camera;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

public class MainActivity extends AppCompatActivity {

    public StreamMadiaPlayer mStreamMadiaPlayer;
    public SurfaceView mVideoSurfaceView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.layout);
        mStreamMadiaPlayer = new StreamMadiaPlayer(this);
//        mVideoSurfaceView = (SurfaceView)findViewById(R.id.main_surface);

    }
}
