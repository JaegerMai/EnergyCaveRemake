package com.powerrun.akenergycaveremake;

import java.util.concurrent.ConcurrentLinkedQueue;

public class BluetoothDataProcessor {
    private ConcurrentLinkedQueue<byte []> queue;
    private DataListener listener;

    public BluetoothDataProcessor(DataListener listener) {
        this.listener = listener;
        queue = new ConcurrentLinkedQueue<>();
        Thread thread = new Thread(() -> {
            while (true) {
                processNextData();
            }
        });
        thread.start();
    }
    public void addData(byte [] data){
        queue.add(data);
    }
    private void processNextData() {
        byte[] data = queue.poll();
        if (data != null) {
            listener.onDataReceived(data);
        } else {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void clearQueue(){
        queue.clear();
    }
    public interface DataListener {
        void onDataReceived(byte[] data);
    }
}
