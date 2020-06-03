package com.example.videotransfer;

public interface TransferListener {

    void onSuccess();

    void onLoss(TransferResult transferResult);

    void onFail();
}
