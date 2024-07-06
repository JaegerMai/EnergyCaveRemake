package com.powerrun.akenergycaveremake;

import android.content.Context;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AppUsageLogger {
    private static AppUsageLogger instance;
    private long startTime = 0;
    private long endTime;
    private String logFilePath;
    private Context context;

    private AppUsageLogger(Context context) {
        this.context = context;
        this.logFilePath = context.getFilesDir() + "/app_usage_log.txt";
    }

    public static synchronized AppUsageLogger getInstance(Context context) {
        if (instance == null) {
            instance = new AppUsageLogger(context);
        }
        return instance;
    }

    public void startLogging() {
        startTime = System.currentTimeMillis();
    }

    public void stopLogging() {
        if (startTime == 0) {
            // startLogging() hasn't been called, so we do nothing
            return;
        }
        endTime = System.currentTimeMillis();
        long usageTime = endTime - startTime;
        saveLogToLocal(usageTime);
        // Reset startTime for the next logging session
        startTime = 0;
    }

    private void saveLogToLocal(long usageTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        long hours = usageTime / 3600000;
        long minutes = (usageTime % 3600000) / 60000;
        long seconds = (usageTime % 60000) / 1000;

        String log = "App used at: " + sdf.format(new Date(startTime)) + ", usage time: " + hours + " hours " + minutes + " minutes " + seconds + " seconds\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}