package com.example.clientapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.videotransfer.IVideoPackManager;
import com.example.videotransfer.ReceiveVideoTask;
import com.example.videotransfer.TransferListener;
import com.example.videotransfer.TransferResult;
import com.example.videotransfer.Video;
import com.example.videotransfer.VideoPack;

import java.util.ArrayList;
import java.util.Calendar;

public class SecondActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "SecondActivity";

    private Button bind_video_service_second,start_play_video_activity_second,start_task,write_video,get_loss;
    private IVideoPackManager video_pack_manager;
    private Video video;
    private boolean connected;
    private boolean playable;
    private String video_store_path;
    ReceiveVideoTask receive_video_task;
    private ArrayList<Integer> video_pack_loss;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            video_pack_manager = IVideoPackManager.Stub.asInterface(iBinder);
            connected = true;
            Log.e(TAG,"VideoServiceSecond连接成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private TransferListener transferListener = new TransferListener() {
        @Override
        public void onSuccess() {
            Log.e(TAG,"获得了全部的VideoPack包");
            write_video.setVisibility(View.VISIBLE);
        }

        @Override
        public void onLoss(TransferResult transferResult) {
            Log.e(TAG,"丢失了部分VideoPack包");
            video_pack_loss = transferResult.getVideoPackLoss();
            video = receive_video_task.getVideo();

        }

        @Override
        public void onFail() {
            Log.e(TAG,"传输失败");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        bind_video_service_second = findViewById(R.id.bind_video_service_second);
        start_play_video_activity_second = findViewById(R.id.start_video_play_activity_second);
        start_task = findViewById(R.id.start_task);
        write_video = findViewById(R.id.write_video);
        get_loss = findViewById(R.id.get_loss);
        bind_video_service_second.setOnClickListener(this);
        start_play_video_activity_second.setOnClickListener(this);
        start_task.setOnClickListener(this);
        write_video.setOnClickListener(this);
        get_loss.setOnClickListener(this);

        write_video.setVisibility(View.INVISIBLE);

        connected = false;
        playable = false;
        video_store_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movie/second_activity.mp4";
        Log.e(TAG,"SecondActivity初始化完成");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bind_video_service_second:
                if(connected)
                    unbindService(serviceConnection);
                Log.e(TAG,"准备连接VideoService");
                Intent intent = new Intent();
                intent.setPackage("com.example.clientapplication");
                intent.setAction("com.example.clientapplication.START_VIDEO_SERVICE_SECOND");
                intent.addCategory("com.example.clientapplication.MY_CATEGORY_SECOND");
                bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
                break;
            case R.id.start_video_play_activity_second:
                if(playable){
                    Intent intentPlayVideo = new Intent(SecondActivity.this, VideoPlayActivity.class);
                    intentPlayVideo.putExtra("video_path",video_store_path);
                    startActivity(intentPlayVideo);
                }else{
                    Toast.makeText(SecondActivity.this,"未获取视频",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.start_task:
                if(connected) {
                    receive_video_task = new ReceiveVideoTask(transferListener,video_pack_manager,null);
                    receive_video_task.execute();
                }else{
                    Toast.makeText(SecondActivity.this,"服务尚未连接成功",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.get_loss:
                if(connected && video_pack_loss != null){
                    receive_video_task = new ReceiveVideoTask(transferListener,video_pack_manager,video.getVideoPackArray());
                    receive_video_task.execute(video_pack_loss);
                }else{
                    Toast.makeText(SecondActivity.this,"服务尚未连接||尚无丢包",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.write_video:
                video = receive_video_task.getVideo();
                video.videoWriteTo(video_store_path);
                playable = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(connected)
            unbindService(serviceConnection);
    }


}
