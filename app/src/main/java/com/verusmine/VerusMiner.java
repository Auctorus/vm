package com.verusmine;

public class VerusMiner {

    static {
        System.loadLibrary("ccminer");
    }

    // JNI functions
    public native void startMiner(int threads, String workerName);
    public native void stopMiner();
}
