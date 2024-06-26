package com.powerrun.akenergycaveremake;

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
    private String logFilePath = "app_usage_log.txt";

    private AppUsageLogger() {
    }

    public static synchronized AppUsageLogger getInstance() {
        if (instance == null) {
            instance = new AppUsageLogger();
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
        String log = "App used at: " + sdf.format(new Date(startTime)) + ", usage time: " + usageTime + " ms\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}