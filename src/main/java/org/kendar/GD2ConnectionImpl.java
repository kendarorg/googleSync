package org.kendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.common.util.concurrent.RateLimiter;
import org.kendar.utils.*;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Named("gd2Connection")
public class GD2ConnectionImpl implements GD2Connection {

    private final static int GOOGLE_RATE_LIMIT =5;
    private final static int LOCAL_RATE_LIMIT =20;

    private final RateLimiter googleApiRateLimiter;
    private GD2Settings settings;
    private Object lock=new Object();
    private Drive drive = null;
    private static HttpTransport httpTransport;
    private static final JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    private static FileDataStoreFactory fileDataStoreFactory;
    private List<GD2LocalTask> tasks = new ArrayList<>();
    private final ConcurrentLinkedQueue<GD2Runner> tasksToRun = new ConcurrentLinkedQueue<>();

    public GD2ConnectionImpl(GD2Settings settings){
        this.settings = settings;
        this.googleApiRateLimiter = RateLimiter.create(GOOGLE_RATE_LIMIT);
        for(int i=0;i<LOCAL_RATE_LIMIT;i++){
            GD2LocalTask runner = new GD2LocalTask(tasksToRun);
            tasks.add(runner);
            runner.run();
        }
    }

    public boolean areJobsRunning(){
        for(GD2LocalTask tk:tasks){
            if(tk.isRunning())return true;
        }
        return false;
    }

    public void waitForJobs() {
        while(areJobsRunning()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Credential authorize() throws GD2Exception {
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            fileDataStoreFactory = new FileDataStoreFactory(settings.getDataStorePath().toFile());
            // Load client secrets.

            GoogleClientSecrets clientSecrets =settings.getClientSecrets();

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            httpTransport, jsonFactory, clientSecrets, settings.getScopes())
                            .setDataStoreFactory(fileDataStoreFactory)
                            .setAccessType("offline")
                            .build();
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow, new LocalServerReceiver()).authorize("user");
            System.out.println(
                    "Credentials saved!");
            return credential;
        } catch (Exception ex) {
            throw new GD2Exception("gd2DriveService-01",ex);
        }
    }

    private void initialize() throws GD2Exception {
        if(drive==null){
            synchronized (lock){
                try{
                    Credential credential = authorize();
                    drive = new Drive.Builder(
                            httpTransport, jsonFactory, credential)
                            .setApplicationName(settings.getApplicationName())
                            .build();
                }catch(Exception ex){
                    throw new GD2Exception("gd2DriveService-02",ex);
                }
            }
        }
    }

    @Override
    public <T> T runGoogle(String errorMessage, GD2ConnectedFunction action) throws GD2Exception {
        initialize();
        try{
            googleApiRateLimiter.acquire();
            return (T)action.run(drive);
        }catch(GD2Exception ex){
            throw ex;
        }catch(Exception es){
            throw new GD2Exception(errorMessage,es);
        }
    }

    @Override
    public void run(GD2Runner action){
        tasksToRun.add(action);
    }


    @Override
    public void waitAll(GD2Runner ... actions){
        for(GD2Runner action:actions) {
            tasksToRun.add(action);
        }
        waitForJobs();
    }
}
