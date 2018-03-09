package org.kendar.utils;

import java.util.concurrent.ConcurrentLinkedQueue;

public class GD2LocalTask extends Thread {
    private final ConcurrentLinkedQueue<GD2Runner> availableTasks;
    private volatile boolean canRun =true;
    private volatile boolean running =false;

    public boolean isRunning(){
        return running;
    }
    public void stopRunning(){
        canRun =false;
    }
    public GD2LocalTask(ConcurrentLinkedQueue<GD2Runner> availableTasks){
        this.availableTasks = availableTasks;
    }

    @Override
    public void run(){
        while(canRun) {
            while (!availableTasks.isEmpty()) {
                GD2Runner runnable = availableTasks.poll();
                running =true;
                try {
                    runnable.run();
                }catch(Exception ex){

                }
                running = false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {

            }
        }
    }
}
