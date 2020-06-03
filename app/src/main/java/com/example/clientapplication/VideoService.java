package com.example.clientapplication;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.example.videotransfer.Video;
import com.example.videotransfer.VideoPack;
import com.example.videotransfer.IVideoPackManager;

public class VideoService extends Service {

    public static final String TAG = "VideoService";

    private ArrayList<VideoPack> videoPacks = new ArrayList<>();
    private IVideoPackManager.Stub iVideoPackManager = new IVideoPackManager.Stub() {
        @Override
        public VideoPack getVideoPack(int i) throws RemoteException {
            if(i < videoPacks.size()){
                return videoPacks.get(i);
            }else
                return null;
        }

        @Override
        public int getVideoPackNum(){
            return videoPacks.size();
        }
    };


    /**
     * 使用外存中的mp4文件路径初始化Video类
     * 调用Video类的getVideoPackArray()方法，获得ArrayList<VideoPack>
     */
    @Override
    public void onCreate(){
        Log.e(TAG,"onCreate()");
        String path = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Movie/machinelearning.mp4";
        Video video = new Video(path);
        videoPacks = video.getVideoPackArray();
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return iVideoPackManager;
    }

    @Override
    public void onDestroy(){

    }
}
