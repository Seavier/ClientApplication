package com.example.videotransfer;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

public class ReceiveVideoTaskTest extends AsyncTask<Integer[], Void, Integer> {

    private static final String TAG = "ReceiveVideoTask";
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_LOSS = 1;
    public static final int TYPE_FAIL = 2;

    private TransferListener transfer_listener;
    private IVideoPackManager video_pack_manager;
    private ArrayList<VideoPack> video_pack_array;
    private Integer[] video_pack_loss;
    private Video video;

    private int time_cost;

    public ReceiveVideoTaskTest(TransferListener transferListener, IVideoPackManager iVideoPackManager){
        this.transfer_listener = transferListener;
        this.video_pack_manager = iVideoPackManager;
    }


    @Override
    protected Integer doInBackground(Integer[]... integersList) {
        Integer[] integers = new Integer[0];
        if(integersList.length == 0){
            try{
                Integer[] integersCopy = new Integer[video_pack_manager.getVideoPackNum()];
                for(int i = 0;i < integersCopy.length;i++) {
                    integersCopy[i] = i;
                }
                integers = integersCopy.clone();
            }catch(Exception e){
                e.printStackTrace();
            }
        }else if(integersList.length == 1){
            integers = integersList[0].clone();
        }else{
            return TYPE_FAIL;
        }

        boolean transfered = true;
        int start_sec = 0;
        int start_mill_sec = 0;
        int end_sec, end_mill_sec;
        int previous_video_pack = 0;
        int end_video_pack = 0;

        video_pack_array = new ArrayList<>();
        // ArrayList<Integer>用来临时存储丢失的数据包编号，因为ArrayList便于直接添加
        // 回头再转换为Integer[]
        ArrayList<Integer> video_pack_loss = new ArrayList<>();
        VideoPack videoPack;
        for(int i = 0;i < integers.length;i++){
            try{
                Calendar calendar = Calendar.getInstance();
                start_sec = calendar.get(Calendar.SECOND);
                start_mill_sec = calendar.get(Calendar.MILLISECOND);

                videoPack = video_pack_manager.getVideoPack(integers[i]);
                if(videoPack == null){
                    if(end_video_pack > previous_video_pack)
                        Log.e(TAG, "获得了"+ previous_video_pack +"-->"+end_video_pack+"的VideoPack包");
                    else if(end_video_pack == previous_video_pack)
                        Log.e(TAG, "获得了第"+ previous_video_pack +"个VideoPack包");
                    previous_video_pack = integers[i] + 1;

                    transfered = false;
                    video_pack_loss.add(integers[i]);
                    Log.e(TAG, "丢失了第"+ integers[i] +"个VideoPack包");
                }
                else{
                    end_video_pack = integers[i];
                }

                /** ----此处逻辑待验证-----
                 * if条件为true，说明这是客户端第一次获取VideoPack数组
                 * 此时会对video_pack_array初始化
                 * 如果VideoPack丢失，仍将null值填入对应位置
                 * if条件为false，说明这是客户端在尝试获取丢失的VideoPack
                 * 通过索引直接找到之前填入的null值，并替换
                 */
                if(integers[i] == video_pack_array.size()+1)
                    video_pack_array.add(integers[i], videoPack);
                else
                    video_pack_array.set(integers[i],videoPack);
            }catch (RemoteException e) {
                return TYPE_FAIL;
            }
        }
        Log.e(TAG, "获得了"+ previous_video_pack +"-->"+end_video_pack+"个VideoPack包");
        Log.e(TAG,"视频传输完成");

        this.video_pack_loss = video_pack_loss.toArray(new Integer[video_pack_loss.size()]);
        Calendar calendar = Calendar.getInstance();
        end_sec = calendar.get(Calendar.SECOND);
        end_mill_sec = calendar.get(Calendar.MILLISECOND);
        time_cost = (end_sec-start_sec)*1000+end_mill_sec-start_mill_sec;
        Log.e(TAG,"传输过程花费时间"+time_cost+"毫秒");

        return (transfered ? TYPE_SUCCESS : TYPE_LOSS);
    }

    @Override
    protected void onPostExecute(Integer status){
        switch (status){
            case TYPE_SUCCESS:
                transfer_listener.onSuccess();
                break;
            case TYPE_LOSS:
                break;
            case TYPE_FAIL:
                transfer_listener.onFail();
                break;
        }
    }


    public void videoWriteTo(String string){
        videoWriteTo(new File(string));
    }

    public void videoWriteTo(File file){
        video = new Video(video_pack_array);
        video.videoWriteTo(file);
    }

    public Video getVideo(){
        return new Video(video_pack_array);
    }
}
