package com.yanry.lihua.sakura;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {
    private PetalView petalView;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        petalView = (PetalView) findViewById(R.id.petal);

        player = MediaPlayer.create(this, R.raw.fall_wisper);
        player.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
        }
        player.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        petalView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        petalView.resume();
    }
}
