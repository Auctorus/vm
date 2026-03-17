package com.verusmine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class MinerService extends Service {

    private static final String CHANNEL_ID = "MinerServiceChannel";
    private VerusMiner miner;

    @Override
    public void onCreate() {
        super.onCreate();
        miner = new VerusMiner();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int threads = intent.getIntExtra("threads", 0);
        String worker = intent.getStringExtra("worker");
        if (worker == null || worker.isEmpty()) {
            worker = "worker002"; // default worker
        }

        // Start foreground notification
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Verus Miner Running")
                .setContentText("Mining in background")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        startForeground(1, notification);

        // Start miner in a new thread
        new Thread(() -> miner.startMiner(threads, worker)).start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        miner.stopMiner();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // not a bound service
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Verus Miner Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }
}
