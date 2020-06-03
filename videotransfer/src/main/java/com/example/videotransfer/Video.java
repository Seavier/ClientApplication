package com.example.videotransfer;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * Video类用来将外存中的视频文件读取到内存之中
 * Video类持有一个ArrayList数组，数组的元素就是VideoPack类
 * 外存中的视频文件先转换为一个个byte数组后，然后存入VideoPack数组
 * Video类提供了一个videoWriteTo方法，用来将Video类的数据写入外存的某一个视频文件
 * Video类不算一个真正的视频文件，不能直接播放
 */
public class Video {
    private static final String TAG = "Video";

    private ArrayList<VideoPack> videoPacks;

    public Video(String string){
        this(new File(string));
    }

    public Video(File file){
        videoPacks = initVideoPackArray(file);
    }

    public Video(ArrayList<VideoPack> videoPacks){
        this.videoPacks = videoPacks;
    }


    /**
     * 为了避免读取大视频文件时出现OOM问题
     * 尽量一边读取文件流，一边做切割初始化VideoPack
     * 初始化完成的VideoPack加入VideoPack数组
     */
    private ArrayList<VideoPack> initVideoPackArray(File file){
        ArrayList<VideoPack> videoPacksTemp = new ArrayList<>();
        try{
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[VideoPack.PACK_LENGTH];
            int len = 0;
            while((len = fis.read(buffer)) != -1){
                byte[] bytes = new byte[VideoPack.PACK_LENGTH];
                for(int j = 0;j < len;j++){
                    bytes[j] = buffer[j];
                }
                videoPacksTemp.add(new VideoPack(bytes));
            }
            fis.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return videoPacksTemp;
    }

//    private ArrayList<VideoPack> initVideoPackArray(File file){
//        ArrayList<VideoPack> videoPacksTemp = new ArrayList<>();
//        byte[] bytes = fileConvertToByteArray(file);
//        int i = 0;
//        int j = 0;
//        while(i < bytes.length){
//            byte[] bytesTemp = new byte[VideoPack.PACK_LENGTH];
//            for(j = 0;i < bytes.length && j < VideoPack.PACK_LENGTH;j++){
//                bytesTemp[j] = bytes[i++];
//            }
//            videoPacksTemp.add(new VideoPack(bytesTemp));
//        }
//        return videoPacksTemp;
//    }
//
//
//    private byte[] fileConvertToByteArray(File file) {
//        byte[] data = null;
//        try {
//            FileInputStream fis = new FileInputStream(file);
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//
//            int len;
//            byte[] buffer = new byte[1024];
//            while ((len = fis.read(buffer)) != -1) {
//                baos.write(buffer, 0, len);
//            }
//            data = baos.toByteArray();
//            baos.close();
//            fis.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return data;
//    }


    public void videoWriteTo(String path){
        videoWriteTo(new File(path));
    }

    public void videoWriteTo(File file){
        try{
            FileOutputStream fos = new FileOutputStream(file);
            int j = 0;
            while(j < videoPacks.size() && (videoPacks.get(j) != null)){
                fos.write(videoPacks.get(j++).getBytes());
            }
            Log.e(TAG,"视频写入完成");

            fos.flush();
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public ArrayList<VideoPack> getVideoPackArray(){
        return videoPacks;
    }
}
