package org.old;

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
    public Tasker(ConcurrentLinkedQueue<GoogleTaskable> findAllFolders){
        this.findAllFolders = findAllFolders;
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
