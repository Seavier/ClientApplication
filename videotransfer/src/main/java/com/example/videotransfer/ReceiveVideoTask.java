package com.example.videotransfer;

import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;


public class ReceiveVideoTask extends AsyncTask<ArrayList<Integer>, Void, Integer> {

    private static final String TAG = "ReceiveVideoTask";
    public static final int TYPE_SUCCESS = 0;
    public static final int TYPE_LOSS = 1;
    public static final int TYPE_FAIL = 2;

    private TransferListener transfer_listener;
    private IVideoPackManager video_pack_manager;
    private ArrayList<VideoPack> video_pack_array;
    private ArrayList<Integer> video_pack_loss;

    private int time_cost;

    public ReceiveVideoTask(TransferListener transferListener, IVideoPackManager iVideoPackManager,ArrayList<VideoPack> videoPacks){
        this.transfer_listener = transferListener;
        this.video_pack_manager = iVideoPackManager;
        if(videoPacks != null)
            this.video_pack_array = videoPacks;
        else
            this.video_pack_array = new ArrayList<>();
    }


    @Override
    protected Integer doInBackground(ArrayList<Integer>... lossLists) {
        ArrayList<Integer> lossList = new ArrayList<>();
        if(lossLists.length == 0){
            try{
                int len = video_pack_manager.getVideoPackNum();
                for(int i = 0;i < len;i++) {
                    lossList.add(i);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }else if(lossLists.length == 1 && !lossLists[0].isEmpty()){
            lossList = (ArrayList<Integer>) lossLists[0].clone();
        }else{
            return TYPE_FAIL;
        }

        boolean transfered = true;
        int start_sec = 0;
        int start_mill_sec = 0;
        int end_sec, end_mill_sec;

//         除了初始化的时候，previous_video_pack永远指向刚刚丢失的包的后一位
//         除了初始化的时候，end_video_pack永远指向刚刚成功获得的包
//        int previous_video_pack = lossList.get(0);
//        int end_video_pack = lossList.get(0);
        // lossList存储的是需要获取的VideoPack的序号
        // 在接下来的for循环中，lossList使用索引i获取lossList中存储的VideoPack序号
        // previous和end也是lossList的索引，而不是VideoPack序号
        // 如果获取失败，那么end不变，previous = i+1
        // 如果获取成功，那么previous不变，end = i
        int previous = 0;
        int end = -1;
        int len = lossList.size();

        video_pack_loss = new ArrayList<>();
        VideoPack videoPack;
        for(int i = 0;i < len;i++){
            try{
                Calendar calendar = Calendar.getInstance();
                start_sec = calendar.get(Calendar.SECOND);
                start_mill_sec = calendar.get(Calendar.MILLISECOND);

                videoPack = video_pack_manager.getVideoPack(lossList.get(i));
                if(videoPack == null){
                    /**
                     * 第一个if条件，end > previous，说明前面好几个循环都成功获得了VideoPack，end更新了自己的值
                     * 所以考虑把这些连续的VideoPack包一起打印
                     * (lossList.get(end)-lossList.get(previous)) == end - previous
                     * 说明VideoPack序号和i是一一对应，完全相等的，即此时不是在获取独立的、不连续的、之前丢失的VideoPack包
                     * 第二个else if指的就是获取丢失VideoPack包的情况，这是各个VideoPack包仍然需要单独打印
                     * 第三个else if指的是前一个循环成功，但是更前一个循环和这一个循环都失败了
                     * 所以单独打印前一个循环的VideoPack包
                     */
                    if(end > previous && ((lossList.get(end)-lossList.get(previous)) == end - previous))
                        Log.e(TAG, "获得了"+ lossList.get(previous) +"-->"+lossList.get(end)+"的VideoPack包");
                    else if(end > previous){
                        for(int temp = previous;temp <= end;temp++){
                            Log.e(TAG,"获得了第"+ lossList.get(temp)+"个VideoPack包");
                        }
                    }
                    else if(end == previous)
                        Log.e(TAG, "获得了第"+ lossList.get(previous) +"个VideoPack包");
                    previous = i + 1;

                    transfered = false;
                    video_pack_loss.add(lossList.get(i));
                    Log.e(TAG, "丢失了第"+ lossList.get(i) +"个VideoPack包");
//                    if(i != 0 && end_video_pack > previous_video_pack)
//                        Log.e(TAG, "获得了"+ previous_video_pack +"-->"+end_video_pack+"的VideoPack包");
//                    else if(i != 0 && end_video_pack == previous_video_pack)
//                        Log.e(TAG, "获得了第"+ previous_video_pack +"个VideoPack包");
//                    previous_video_pack = lossList.get(i) + 1;
//
//                    transfered = false;
//                    video_pack_loss.add(lossList.get(i));
//                    Log.e(TAG, "丢失了第"+ lossList.get(i) +"个VideoPack包");
                }
                else{
                    end = i;
                    // end_video_pack = lossList.get(i);
                }

                /**
                 * if条件为true，说明这是客户端第一次获取VideoPack数组
                 * 此时会对video_pack_array初始化
                 * 如果VideoPack丢失，仍将null值填入对应位置
                 * if条件为false，说明这是客户端在尝试获取丢失的VideoPack
                 * 通过索引直接找到之前填入的null值，并替换
                 */
                if(lossList.get(i) == video_pack_array.size())
                    video_pack_array.add(lossList.get(i), videoPack);
                else
                    video_pack_array.set(lossList.get(i),videoPack);
            }catch (RemoteException e) {
                return TYPE_FAIL;
            }
        }
        if(end > previous && ((lossList.get(end)-lossList.get(previous)) == end - previous))
            Log.e(TAG, "获得了"+ lossList.get(previous) +"-->"+lossList.get(end)+"的VideoPack包");
        else if(end > previous){
            for(int temp = previous;temp <= end;temp++){
                Log.e(TAG,"获得了第"+ lossList.get(temp)+"个VideoPack包");
            }
        }
        else if(end == previous)
            Log.e(TAG, "获得了第"+ lossList.get(previous) +"个VideoPack包");
        Log.e(TAG,"视频传输完成");

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
                transfer_listener.onLoss(new TransferResult(time_cost,video_pack_loss));
                break;
            case TYPE_FAIL:
                transfer_listener.onFail();
                break;
        }
    }

//    public void videoWriteTo(String string){
//        videoWriteTo(new File(string));
//    }
//
//    public void videoWriteTo(File file){
//        video = new Video(video_pack_array);
//        video.videoWriteTo(file);
//    }

    public Video getVideo(){
        return new Video(video_pack_array);
    }
}
