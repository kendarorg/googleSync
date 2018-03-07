package org.enel.utils;

import org.enel.entities.GoogleTaskable;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Tasker extends Thread {
    private final ConcurrentLinkedQueue<GoogleTaskable> findAllFolders;
    private volatile boolean doRun=true;
    private volatile boolean isRunning=false;

    public boolean isWorking(){
        return isRunning;
    }
    public void stopRunning(){
        doRun=false;
    }
    public Tasker(ConcurrentLinkedQueue<GoogleTaskable> tasksToRun){
        this.findAllFolders = tasksToRun;
    }
    @Override
    public void run(){
        while(doRun) {
            while (!findAllFolders.isEmpty()) {
                GoogleTaskable taskable = findAllFolders.poll();
                isRunning=true;
                try {
                    taskable.run();
                }catch(Exception ex){

                }
                isRunning= false;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {

            }
        }
    }
}
