// IVideoPackManager.aidl
package com.example.videotransfer;

// Declare any non-default types here with import statements
import com.example.videotransfer.VideoPack;

interface IVideoPackManager {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    VideoPack getVideoPack(int i);
    int getVideoPackNum();
}
