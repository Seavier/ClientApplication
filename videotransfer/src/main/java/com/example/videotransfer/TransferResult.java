package com.example.videotransfer;

import java.util.ArrayList;

public class TransferResult {
    private int time_cost;
    private ArrayList<Integer> video_pack_loss;

    public TransferResult(int time_cost, ArrayList<Integer> video_pack_loss){
        this.time_cost = time_cost;
        this.video_pack_loss = video_pack_loss;
    }

    public int getTimeCost(){
        return time_cost;
    }

    public ArrayList<Integer> getVideoPackLoss(){
        return video_pack_loss;
    }
}
