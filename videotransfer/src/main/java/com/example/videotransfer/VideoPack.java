package com.example.videotransfer;

import android.os.Parcel;
import android.os.Parcelable;

public class VideoPack implements Parcelable {

    /**
     * PACK_LENGTH = 1024*1024时会报错
     * 似乎是因为Binder内核提供的共享内存最大为1MB
     */
    public static final int PACK_LENGTH = 1024*512;

    private byte[] bytes;

    public VideoPack(byte[] bytes){
        this.bytes = bytes;
    }

    protected VideoPack(Parcel in) {
        bytes = in.createByteArray();
    }

    public static final Creator<VideoPack> CREATOR = new Creator<VideoPack>() {
        @Override
        public VideoPack createFromParcel(Parcel in) {
            return new VideoPack(in);
        }

        @Override
        public VideoPack[] newArray(int size) {
            return new VideoPack[size];
        }
    };

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out,int flags){
        out.writeByteArray(bytes);
    }

    public byte[] getBytes(){
        return bytes;
    }

}

