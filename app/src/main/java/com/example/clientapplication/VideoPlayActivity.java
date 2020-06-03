package com.example.clientapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayActivity extends AppCompatActivity implements View.OnClickListener{

    private Button play,pause,replay;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_play);
        Intent intent = getIntent();
        String video_path = intent.getStringExtra("video_path");
        play= (Button) findViewById(R.id.play);
        pause= (Button) findViewById(R.id.pause);
        replay= (Button) findViewById(R.id.replay);
        videoView= (VideoView) findViewById(R.id.videoView);

        play.setOnClickListener(this);
        pause.setOnClickListener(this);
        replay.setOnClickListener(this);
        initVideoPath(video_path);
    }

    private void initVideoPath(String path) {
        File file=new File(path);
        videoView.setVideoPath(file.getPath());
    }

    @Override
    public void onClick(View v) {
        if (v==play){
            if (!videoView.isPlaying()){
                videoView.start();
            }
        }
        if (v==pause){
            if (videoView.isPlaying()){
                videoView.pause();
            }
        }
        if (v==replay){
            if (videoView.isPlaying()){
                videoView.resume();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView!=null){
            videoView.suspend();
        }
    }
}
