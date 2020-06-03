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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

import com.example.videotransfer.Video;
import com.example.videotransfer.VideoPack;
import com.example.videotransfer.IVideoPackManager;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String TAG = "MainActivity";

    private Button bind_video_service,start_play_video_activity,start_second_activity;
    private IVideoPackManager video_pack_manager;
    private Video video;
    private boolean connected;
    private String video_store_path;

    private int start_sec;
    private int start_mill_sec;
    private int end_sec;
    private int end_mill_sec;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            video_pack_manager = IVideoPackManager.Stub.asInterface(iBinder);
            connected = true;
            Log.e(TAG,"VideoService连接成功");

            VideoPack videoPack = null;
            ArrayList<VideoPack> videoPacks = new ArrayList<>();
            int i = 0;
            while(i == 0 || videoPack != null){
                try {
                    Calendar calendar = Calendar.getInstance();
                    start_sec = calendar.get(Calendar.SECOND);
                    start_mill_sec = calendar.get(Calendar.MILLISECOND);
                    videoPack = video_pack_manager.getVideoPack(i++);
                    Log.e(TAG, "获得第"+ i +"个VideoPack包");
                    if(videoPack != null)
                        videoPacks.add(videoPack);
                }catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            // 计算传输过程花费的时间
            Log.e(TAG,"视频传输完成");
            Calendar calendar = Calendar.getInstance();
            end_sec = calendar.get(Calendar.SECOND);
            end_mill_sec = calendar.get(Calendar.MILLISECOND);
            int cost_time = (end_sec-start_sec)*1000+end_mill_sec-start_mill_sec;
            Log.e(TAG,"传输过程花费时间"+cost_time+"毫秒");

            // 将接收到的VideoPack数组写入本地路径下的视频文件
            video = new Video(videoPacks);
            video.videoWriteTo(video_store_path);

//            try{
//                FileOutputStream fos = new FileOutputStream(video_store_path);
//                int j = 0;
//                while(j < videoPacks.size() && (videoPacks.get(j) != null)){
//                    fos.write(videoPacks.get(j++).getBytes());
//                }
//                Log.e(TAG,"视频写入完成");
//
//                fos.flush();
//                fos.close();
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    int i = 0;
//                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                    while(i == 0 || videoPack != null){
//                        try {
//                            Log.e(TAG, "第"+Integer.toString(i)+"个VideoPack包");
//                            if(i != 0 && videoPack == null)
//                                break;;
//                            videoPack = videoPackManager.getVideoPack(i++);
//                            baos.write(videoPack.getBytes());
//                        }catch (RemoteException e) {
//                            e.printStackTrace();
//                        }catch (IOException e){
//                            e.printStackTrace();
//                        }
//                    }
//
//                    try{
//                        FileOutputStream fos = new FileOutputStream(videoStorePath);
//                        baos.writeTo(fos);
//                        fos.close();
//                        baos.close();
//                    }catch (FileNotFoundException e){
//                        e.printStackTrace();
//                    }catch (IOException e){
//                        e.printStackTrace();
//                    }
//                }
//            }).start();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bind_video_service = findViewById(R.id.bind_video_service);
        start_play_video_activity = findViewById(R.id.start_video_play_activity);
        start_second_activity = findViewById(R.id.start_second_activity);
        bind_video_service.setOnClickListener(this);
        start_play_video_activity.setOnClickListener(this);
        start_second_activity.setOnClickListener(this);

        connected = false;
        video_store_path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movie/main_activity.mp4";
        Log.e(TAG,"MainActivity初始化完成");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.bind_video_service:
                if(connected)
                    unbindService(serviceConnection);
                Log.e(TAG,"准备连接VideoService");
                Intent intent = new Intent();
                intent.setPackage("com.example.clientapplication");
                intent.setAction("com.example.clientapplication.START_VIDEO_SERVICE");
                intent.addCategory("com.example.clientapplication.MY_CATEGORY");
                bindService(intent,serviceConnection, Context.BIND_AUTO_CREATE);
                break;
            case R.id.start_video_play_activity:
                if(connected){
                    Intent intentPlayVideo = new Intent(MainActivity.this, VideoPlayActivity.class);
                    intentPlayVideo.putExtra("video_path",video_store_path);
                    startActivity(intentPlayVideo);
                }else{
                    Toast.makeText(MainActivity.this,"未获取视频",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.start_second_activity:
                Intent intentSecondActivity = new Intent(MainActivity.this,SecondActivity.class);
                startActivity(intentSecondActivity);
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
