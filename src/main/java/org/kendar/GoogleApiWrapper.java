package org.kendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class GoogleApiWrapper {
    private int RUNNERS = 5;
    private List<Tasker> tasker = new ArrayList<>();
    private final Drive driveService;

    private static Path tokenStore;
    private final boolean dryRun;
    private final String dataStoreDir;
    private String token;

    public GoogleApiWrapper(Drive service, boolean dryRun, String dataStoreDir) {
        this.driveService = service;
        this.dataStoreDir = dataStoreDir;
        tokenStore = Paths.get(cleanUpPath(dataStoreDir)+
                java.io.File.separator  +"drive-token");
        this.dryRun = dryRun;
        token=null;
        initializeDriveToken();
        for(int i =0;i<RUNNERS;i++){
            Tasker task = new Tasker(tasksToRun);
            task.start();
            tasker.add(task);
        }
    }

    ObjectMapper mapper = new ObjectMapper();

    private void doRun(GoogleTaskable taskable){
        tasksToRun.add(taskable);
    }

    private static String cleanUpPath(String path){
        if(path==null)return null;
        return StringUtils.stripEnd(path,"/\\");
    }
    private void initializeDriveToken() {

        try {
            if(Files.exists(tokenStore)) {
                String json =new String(Files.readAllBytes(tokenStore));
                token = "";// mapper.readValue(json,DriveStatus.class);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveDriveToken() {

        if(dryRun)return;
        try {
            Files.write(tokenStore,token.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a file path with starting java.io.File.separator
     * @param file
     * @return
     * @throws IOException
     */
    public String getFilePath(File file) throws IOException {
        String folderPath = "";
        String fullFilePath = null;


        List<String> parentReferencesList = file.getParents();
        List<String> folderList = new ArrayList<String>();

        List<String> finalFolderList = getfoldersList(parentReferencesList, folderList);
        finalFolderList.remove(finalFolderList.size()-1);
        Collections.reverse(finalFolderList);

        for (String folder : finalFolderList) {
            folderPath += java.io.File.separator + folder;
        }

        fullFilePath = cleanUpPath(folderPath) + java.io.File.separator + file.getName();

        return fullFilePath;
    }

    private List<String> getfoldersList(List<String> parentReferencesList, List<String> folderList) throws IOException {
        if(parentReferencesList==null) return new ArrayList<>();
        for (int i = 0; i < parentReferencesList.size(); i++) {
            String id = parentReferencesList.get(i);

            File file = driveService.files().get(id).
                    setFields("id,name,parents,modifiedTime").execute();
            folderList.add(file.getName());

            if (file.getParents()!=null && !file.getParents().isEmpty()) {
                List<String> parentReferenceslist2 = file.getParents();
                getfoldersList(parentReferenceslist2, folderList);
            }
        }
        return folderList;
    }

    public void findAllFolders(ConcurrentHashMap<String, String> folders,
                               ConcurrentHashMap<String, String> ids) throws IOException {
        FileList result = driveService.files().list()
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, parents)")
                .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                .execute();
        for (File file : result.getFiles()) {
            /*ids.put(file.getId())
            String path = src + java.io.File.separator + file.getName();
            System.out.println("Founded " + path);
            folders.put(path, file.getId());
            ids.put(file.getId(), path);
            findAllFolders(false, file.getId(), folders, ids, path);*/
        }
    }

    private ConcurrentLinkedQueue<GoogleTaskable> tasksToRun =new ConcurrentLinkedQueue<>();

    private void findAllFolders(boolean isNew,String parentId,
                                ConcurrentHashMap<String, String> folders,
                                ConcurrentHashMap<String, String> ids,
                                String src) throws  IOException{

        FileList result = driveService.files().list()
                .setSpaces("drive")
                .setFields("nextPageToken, files(id, name, parents)")
                .setQ("'" + parentId + "' in parents and mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                .execute();
        for (File file : result.getFiles()) {
            String path = src + java.io.File.separator + file.getName();
            System.out.println("Founded " + path);
            folders.put(path, file.getId());
            ids.put(file.getId(), path);
            findAllFolders(false, file.getId(), folders, ids, path);
        }

    }

    public void downloadUpdate(String destinationDir,String driveDir) throws Exception {
        if (driveDir == null) driveDir = java.io.File.separator;
        driveDir = StringUtils.stripStart(cleanUpPath(driveDir), "/\\");
        driveDir = java.io.File.separator + driveDir;
        destinationDir = cleanUpPath(destinationDir);
        if (token == null) {
            StartPageToken response = driveService.changes()
                    .getStartPageToken().execute();
            token = "298620";// response.getStartPageToken();

        }

        String pageToken = token;
        while (pageToken != null) {
            ChangeList changes = driveService.changes().list(pageToken)
                    .execute();
            //Object[] changez = .toArray();
            for (Change change : changes.getChanges()) {
                //Change change = (Change)changeo;
                // Process change
                File file = driveService.files().get(change.getFileId())
                        .setFields("id,name,parents,modifiedTime").execute();

                String relativeFilePath = getFilePath(file);
                if (!relativeFilePath.startsWith(driveDir)) {
                    continue;
                }

                if (!dryRun) {
                    String filePath = destinationDir + relativeFilePath;
                    java.io.File localFile = new java.io.File(filePath);
                    Path pathToFile = Paths.get(filePath);
                    Files.createDirectories(pathToFile.getParent());
                    FileOutputStream fop = new FileOutputStream(localFile);
                    driveService.files().get(file.getId())
                            .executeMediaAndDownloadTo(fop);
                }
                System.out.println("Change found for file: " + relativeFilePath);
            }
            pageToken = changes.getNextPageToken();
            if (pageToken != null) {
                if (driveDir.equalsIgnoreCase(java.io.File.separator))
                    // Last page, save this token for the next polling interval
                    token = pageToken;
                saveDriveToken();
            }

        }
    }

    public void resetToken() {
        try {
            if(Files.exists(tokenStore)) {
                Files.delete(tokenStore);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
