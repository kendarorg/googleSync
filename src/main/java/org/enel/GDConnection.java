package org.enel;

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
import com.google.api.services.drive.DriveScopes;
import com.google.common.util.concurrent.RateLimiter;
import org.enel.entities.DriveStatus;
import org.enel.entities.GoogleTaskable;
import org.enel.utils.ExceptionSupplier;
import org.enel.utils.Tasker;
import org.kendar.Quickstart;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GDConnection {
    private final String CLIENT_SECRET = "client_secret.json";
    private final String TOKEN_STORE = "tokent.txt";
    private final String DATA_STORE_PATH = ".googleSync";
    private final Path settingsPath;
    private final Path clientSecretPath;
    private final Path dataStoreFile;
    private final Path tokenStoreFile;
    private GDTokenService tokenService;
    private RateLimiter googleSemaphore;
    private Drive service;
    private int runners = 20;
    private List<Tasker> tasker = new ArrayList<>();

    private boolean areJobsRunning(){
        for(Tasker tk:tasker){
            if(tk.isWorking())return true;
        }
        return false;
    }

    public void waitForJobsCompletion() {
        while(areJobsRunning()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //applicationName "Drive API Java org.kendar.Quickstart";
    public GDConnection(String applicationName, String settingsPath, GDTokenService tokenService) throws GDException {
        APPLICATION_NAME = applicationName;
        this.settingsPath = Paths.get(settingsPath);
        this.clientSecretPath = Paths.get(settingsPath,CLIENT_SECRET);
        this.dataStoreFile = Paths.get(settingsPath,DATA_STORE_PATH);
        this.tokenStoreFile = Paths.get(settingsPath,TOKEN_STORE);
        this.tokenService = tokenService;

        this.tokenService.initialize(tokenStoreFile);
        try {
            Files.createDirectories(this.settingsPath);
        } catch (Exception e) {
            throw new GDException("012",e);
        }

        googleSemaphore = RateLimiter.create(5);


        for(int i =0;i<runners;i++){
            Tasker task = new Tasker(tasksToRun);
            task.start();
            tasker.add(task);
        }
    }
    /** Application name. */
    private final String APPLICATION_NAME;

    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE);

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    private Object locker = new Object();

    private final ConcurrentLinkedQueue<GoogleTaskable> tasksToRun = new ConcurrentLinkedQueue<>();

    public Drive getDriveService() throws GDException {
        if(service==null) {
            synchronized (locker) {
                Credential credential = authorize();
                service = new Drive.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, credential)
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            }
        }
        return service;
    }

    public <T> T runApiCall(String error,ExceptionSupplier runCall) throws GDException {
        try {
            googleSemaphore.acquire();
            return (T)runCall.run(getDriveService());
        } catch(Exception ex){
            throw new GDException(error,ex);
        }
    }

    private Credential authorize() throws GDException {

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(dataStoreFile.toFile());
            // Load client secrets.
            InputStream in =
                    Quickstart.class.getResourceAsStream(clientSecretPath.toString());
            GoogleClientSecrets clientSecrets =
                    GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

            // Build flow and trigger user authorization request.
            GoogleAuthorizationCodeFlow flow =
                    new GoogleAuthorizationCodeFlow.Builder(
                            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                            .setDataStoreFactory(DATA_STORE_FACTORY)
                            .setAccessType("offline")
                            .build();
            Credential credential = new AuthorizationCodeInstalledApp(
                    flow, new LocalServerReceiver()).authorize("user");
            System.out.println(
                    "Credentials saved to " + clientSecretPath.toString());
            return credential;
        } catch (Exception t) {
            throw new GDException("001",t);
        }
    }

    public GDTokenService getTokenService() {
        return tokenService;
    }

    public void doRun(GoogleTaskable taskable){
        tasksToRun.add(taskable);
    }
}
