package org.kendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.*;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
//import org.hsqldb.server.Server;
import org.omg.CORBA.Environment;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Quickstart {
    /** Application name. */
    private static final String APPLICATION_NAME =
            "Drive API Java org.kendar.Quickstart";

    /** Directory to store user credentials for this application. */
    private static java.io.File dataStoreDir;

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/drive-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE);
    private static HelpFormatter formatter;
    private static Options options;
    //private static Server server;
    private static int serverPort;


    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                Quickstart.class.getResourceAsStream(java.io.File.separator+"client_secret.json");
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
                "Credentials saved to " + dataStoreDir.getAbsolutePath());
        return credential;
    }

    /**
     * Build and return an authorized Drive client service.
     * @return an authorized Drive client service
     * @throws IOException
     */
    public static Drive getDriveService() throws IOException {
        Credential credential = authorize();
        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private static CommandLine loadOptions(String[] args){
        options = new Options();

        Option input = new Option(
                "du", "downloadUpdate", true,
                "Download and update to the specified google drive");
        input.setRequired(false);
        options.addOption(input);

        Option help = new Option(
                "h", "help", false,
                "Show Help");
        help.setRequired(false);
        options.addOption(help);

        Option dir = new Option(
                "dir", "directories", false,
                "Show Help");
        help.setRequired(false);
        options.addOption(dir);


        Option dryRun = new Option(
                "d", "Dry Run", false,
                "Show Help");
        dryRun.setRequired(false);
        options.addOption(dryRun);


        Option dataStoreDirPath = new Option(
                "ds", "dataStoreDir", true,
                "Directory for this instance settings");
        dryRun.setRequired(false);
        options.addOption(dataStoreDirPath);

        Option resetDriveStatus = new Option(
                "r", "resetDrive", true,
                "Reset Drive status as clean");
        resetDriveStatus.setRequired(false);
        options.addOption(resetDriveStatus);

        Option output = new Option(
                "uu", "uploadUpdate", true,
                "Upload and update to the specified google drive");
        input.setRequired(false);
        options.addOption(output);

        Option targetDriveDir = new Option(
                "rd", "relativeDriveDir", true,
                "Google Drive directory to update, default is root");
        input.setRequired(false);
        options.addOption(targetDriveDir);

        CommandLineParser parser = new DefaultParser();
        formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse (options, args);
            if(cmd.getOptionValue("du")==null && cmd.getOptionValue("uu")==null){
                throw new ParseException("du or uu are mandatory!");
            }
            if(cmd.getOptionValue("du")!=null && cmd.getOptionValue("uu")!=null){
                throw new ParseException("du or uu are mutually exclusive!");
            }
            return cmd;
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("googleSync", options);

            System.exit(1);
            return null;
        }
    }

    public static void mainStart(String[] args) throws Exception {
        // Build a new authorized API client service.


        CommandLine cmd = loadOptions(args);
        if(cmd.hasOption("h")){
            formatter.printHelp("googleSync", options);
            System.exit(0);
        }
        String downloadUpdate = cleanUpPath(cmd.getOptionValue("du"));
        String uploadUpdate = cleanUpPath(cmd.getOptionValue("uu"));
        String resetDriveStatus = cleanUpPath(cmd.getOptionValue("r"));
        String dataStoreDirPath = cleanUpPath(cmd.getOptionValue("ds"));
        if(dataStoreDirPath==null){
            dataStoreDirPath = System.getProperty("user.home")+java.io.File.separator+
                    ".googleSync";
        }
        boolean dryRun = cmd.hasOption("d");

        HsqlDb repo =runHsqlServer(dataStoreDirPath);


        dataStoreDir = new java.io.File(dataStoreDirPath);

        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(dataStoreDir);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }

        Drive service = getDriveService();
        //listLastUpdate(service);
        GoogleApiWrapper2 wrapper = new GoogleApiWrapper2(
                service,dryRun,dataStoreDirPath,5,repo);

        if(cmd.hasOption("dir")){
            RootDriveItem root = wrapper.findFolders();
        }else
        if(resetDriveStatus!=null) {
            String uploadUpdateTarget = cmd.getOptionValue("rd");
            if(uploadUpdateTarget==null){
                uploadUpdateTarget=java.io.File.separator;
            }
            wrapper.uploadUpdate(uploadUpdate,uploadUpdateTarget);
            //wrapper.resetToken();
        }else if(downloadUpdate!=null){
            String downloadUpdateTarget = cmd.getOptionValue("rd");
            if(downloadUpdateTarget==null){
                downloadUpdateTarget=java.io.File.separator;
            }
            wrapper.downloadUpdate(downloadUpdate,downloadUpdateTarget);
        }else if(uploadUpdate!=null){
            throw new Exception("Upload Update not yet implemented");
        }else{
            formatter.printHelp("googleSync", options);
        }
        System.exit(0);
    }

    private static int findPort(){
        for(int i=10000;i<20000;i++){
            try {
                ServerSocket ss = new ServerSocket(i);
                ss.close();
                return i;
            } catch (IOException ex) {
                continue; // try next port
            }
        }
        return -1;
    }
    private static HsqlDb runHsqlServer(String dataStoreDirPath) throws InterruptedException, IOException {
        dataStoreDirPath = StringUtils.stripEnd(dataStoreDirPath,"\\/")+
            java.io.File.separator+"db";
        //serverPort = findPort();

        //String dbPath =StringUtils.stripEnd(dataStoreDirPath,"\\/")+
        //        java.io.File.separator+"db"+java.io.File.separator+"db.sqlite";
        Path pathToFile = Paths.get(dataStoreDirPath);

        Files.createDirectories(pathToFile);



        /*server = new Server();
        server.setDatabaseName(0,"its");
        server.setDatabasePath(0,"file:"+dataStoreDirPath);
        server.setPort(serverPort);
        server.setLogWriter(new CustomOutStream(dataStoreDirPath+
                java.io.File.separator+"database.log"));
        server.start();
        Thread.sleep(1000);

        return new HsqlDb("jdbc:hsqldb:hsql://localhost:"+serverPort+"/its","sa","");*/
        return new HsqlDb("jdbc:sqlite:"+dataStoreDirPath+
                java.io.File.separator+"gs.sqlite","","");
    }

    private static String cleanUpPath(String path){
        if(path==null)return null;
        return StringUtils.stripEnd(path,"/\\");
    }

    private static void listLastUpdate(Drive service) throws IOException {
        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }
    }

}