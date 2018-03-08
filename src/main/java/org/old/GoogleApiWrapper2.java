package org.old;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.*;
import com.google.common.hash.Hashing;
import com.google.common.util.concurrent.RateLimiter;
import org.apache.commons.lang3.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GoogleApiWrapper2 {
    private final ConcurrentLinkedQueue<GoogleTaskable> tasksToRun = new ConcurrentLinkedQueue<>();
    private final RateLimiter googleSemaphore;
    private final HsqlDb repo;
    private int runners = 20;
    private static final int MAX_GOOGLE_PER_SEC=8;
    Thread backOffLimit;
    private List<Tasker> tasker = new ArrayList<>();
    private final Drive driveService;

    private static Path tokenStore;
    private final boolean dryRun;
    private final String dataStoreDir;
    private DriveStatus token;

    public GoogleApiWrapper2(Drive service, boolean dryRun, String dataStoreDir, int runners,HsqlDb repo) {
        this.driveService = service;
        this.repo = repo;
        this.dataStoreDir = dataStoreDir;
        this.runners = runners>0?runners:this.runners;
        tokenStore = Paths.get(cleanUpPath(dataStoreDir)+
                java.io.File.separator  +"drive-token");
        this.dryRun = dryRun;
        token=null;
        initializeDriveToken();
        for(int i =0;i<runners;i++){
            Tasker task = new Tasker(tasksToRun);
            task.start();
            tasker.add(task);
        }

        googleSemaphore = RateLimiter.create(5);
    }

    public Object runApiCall(ExceptionSupplier runCall) throws Exception {
        try {
            googleSemaphore.acquire();
            return runCall.run();
        } catch(Exception ex){
            throw ex;
        }
    }

    private boolean areJobsRunnin(){
        for(Tasker tk:tasker){
            if(tk.isWorking())return true;
        }
        return false;
    }
    private void doRun(GoogleTaskable taskable){
        tasksToRun.add(taskable);
    }

    private static String cleanUpPath(String path){
        if(path==null)return null;
        return StringUtils.stripEnd(path,"/\\");
    }
    private static String cleanUpPathAll(String path){
        if(path==null)return null;
        return StringUtils.strip(path,"/\\");
    }

    ObjectMapper mapper = new ObjectMapper();

    private void initializeDriveToken() {

        try {
            if(Files.exists(tokenStore)) {
                String json =new String(Files.readAllBytes(tokenStore));
                token = mapper.readValue(json,DriveStatus.class);
            }else{
                token = new DriveStatus();
            }
        } catch (IOException e) {
            token = new DriveStatus();
            e.printStackTrace();
        }
    }

    private void saveDriveToken() {

        if(dryRun)return;
        try {
            String value = mapper.writeValueAsString(token);
            Files.write(tokenStore,value.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<DriveItem> findGoogleIgnoreFilesIds() throws Exception {
        List<DriveItem> resultIds = new ArrayList<>();

        String nextPageToken = "";
        int dirCount = 0;
        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = (FileList) runApiCall(
                        ()->driveService.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("name = '.googleIgnore' and trashed = false")
                        .execute());
            }else{
                final String ntp = nextPageToken;
                result = (FileList) runApiCall(()->driveService.files().list()
                        .setPageToken(ntp)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("name = '.googleIgnore' and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                dirCount++;
                DriveItem item = new DriveItem();
                item.setId(file.getId());
                item.setName(file.getName());
                if(file.getParents()!=null && file.getParents().size()>0) {
                    item.setParentId(file.getParents().get(0));
                }
                item.setDir(false);
                resultIds.add(item);
            }
            System.out.print(".");
            nextPageToken = result.getNextPageToken();
        }
        System.out.println("");
        return resultIds;
    }

    public RootDriveItem findFolders() throws Exception {
        RootDriveItem root = new RootDriveItem();
        root.setId(null);
        root.setName("");

        HashMap<String,DriveItem> items = new HashMap<>();

        String nextPageToken = "";
        int dirCount = 0;
        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = (FileList)runApiCall(()->driveService.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = (FileList) runApiCall(()->driveService.files().list()
                        .setPageToken(npt)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                dirCount++;
                DriveItem item = new DriveItem();
                item.setId(file.getId());
                item.setName(file.getName());
                if(file.getParents()!=null && file.getParents().size()>0) {
                    item.setParentId(file.getParents().get(0));
                }
                item.setDir(true);
                items.put(item.getId(),item);
            }
            System.out.print(".");
            nextPageToken = result.getNextPageToken();
        }

        System.out.println("");

        for(DriveItem item :items.values()){
            root.addId(item.getId(),item);
            if(item.getParentId()==null){
                root.getItem().add(item);
                item.setParent(root);
            }else if(items.containsKey(item.getParentId())){
                DriveItem parent = items.get(item.getParentId());
                item.setParent(parent);
                parent.getItem().add(item);
            }
        }
        for(DriveItem item :items.values()){
            root.getDrivePaths().put(item.getFullPath(),item);
        }

        for(DriveItem item : findGoogleIgnoreFilesIds()){
            DriveItem toIgnore = root.getDriveItems().get(item.getParentId());
            toIgnore.setIgnore(true);
        }
        root.setId("root");
        return root;
    }

    public void downloadUpdate(String destinationDir,String driveDir) throws Exception {
        //RootDriveItem root = new RootDriveItem();
        final String lastDestDir = destinationDir;
        if (driveDir == null) driveDir = java.io.File.separator;
        driveDir = cleanUpPathAll(driveDir);
        driveDir = java.io.File.separator + driveDir;
        destinationDir = cleanUpPath(destinationDir);

        System.out.println("Folders loading");
        RootDriveItem root = findFolders();
        System.out.println("Folders loaded");
        DriveItem rootItem = root;
        if(!driveDir.equalsIgnoreCase("/")&&!driveDir.equalsIgnoreCase("\\")&&
                driveDir!=null){
            rootItem = root.getDrivePaths().get(driveDir);
        }
        DirectoryStatus currentStatus = new DirectoryStatus(rootItem);
        String tokenString = token.readStatus(currentStatus);
        if (tokenString == null) {

            StartPageToken response = (StartPageToken) runApiCall(()-> driveService.changes()
                    .getStartPageToken()
                    .execute());
            tokenString = response.getStartPageToken();
            currentStatus.setLastUpdate(tokenString);
            token.writeStatus(currentStatus);

            System.out.println("First time download started");
            simplyDownload(destinationDir,rootItem,root);
            waitForJobsCompletion();

            saveDriveToken();

            System.out.println("First time download completed");

        }else{
            System.out.println("Update started");
            String pageToken = tokenString;
            while (pageToken != null) {
                final String pt = pageToken;
                ChangeList changes = (ChangeList) runApiCall(()->driveService.changes().list(pt)
                        //.setFields("changes,kind,newStartPageToken,nextPageToken")
                        .execute());
                //Object[] changez = .toArray();
                for (Change change : changes.getChanges()) {
                    //Change change = (Change)changeo;
                    // Process change
                    File file = (File) runApiCall(()->driveService.files().get(change.getFileId())
                            .setFields("id,name,parents,modifiedTime,md5Checksum,mimeType")
                            .execute());
                    DriveItem parent = null;
                    if (file.getParents() == null || file.getParents().isEmpty()) {
                        parent = root;
                    } else {
                        parent = root.getDriveItems().get(file.getParents().get(0));
                    }
                    if(parent==null){parent = root;}

                    String relativeFilePath = parent.getFullPath() + java.io.File.separator + file.getName();
                    if (!relativeFilePath.startsWith(driveDir)) {
                        continue;
                    }
                    if (!dryRun) {
                        this.doRun(new GoogleTaskable() {
                            @Override
                            public void run() {
                                writeChangedFileWithDirIfNew(lastDestDir, relativeFilePath, file);
                            }
                        });

                    }

                }

                waitForJobsCompletion();
                if (pageToken != null) {
                    currentStatus.setLastUpdate(pageToken);
                    token.writeStatus(currentStatus);
                    saveDriveToken();
                }
                pageToken = changes.getNextPageToken();
            }

            System.out.println("Update completed");
        }
    }

    private void waitForJobsCompletion() {
        while(areJobsRunnin()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void simplyDownload(String destinationDir, DriveItem rootItem,RootDriveItem first) throws Exception {
        String nextPageToken = "";
        String rootFullPath = rootItem.getFullPath();
        int dirCount = 0;
        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = (FileList) runApiCall(()->driveService.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime)")
                        .setQ("'"+rootItem.getId()+"' in parents and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = (FileList) runApiCall(()->driveService.files().list()
                        .setPageToken(npt)
                        .setSpaces("drive")
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime)")
                        .setQ("'"+rootItem.getId()+"' in parents and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                dirCount++;
                if(file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")){
                    if(!first.getDriveItems().containsKey(file.getId())){
                        System.out.println("MISSING "+rootItem.getFullPath()+"/"+file.getName());
                        continue;
                    }
                }else if (!dryRun) {

                    this.doRun(new GoogleTaskable() {
                        @Override
                        public void run() {
                            writeFileWithDirIfNew(destinationDir, rootFullPath, file);
                        }
                    });

                    //System.out.println("Change found for file: " + relativeFilePath);

                }
            }

            nextPageToken = result.getNextPageToken();
        }
        for(DriveItem son : rootItem.getItem()){
            simplyDownload(destinationDir,son,first);
        }
    }

    private void writeFileWithDirIfNew(String destinationDir, String rootFullPath, File file) {
        try {
            String filePath = cleanUpPath(destinationDir) + java.io.File.separator +
                    cleanUpPathAll(rootFullPath) + java.io.File.separator +
                    file.getName();
            java.io.File localFile = new java.io.File(filePath);
            Path pathToFile = Paths.get(filePath);
            if (downloadFileWithMd5Check(file, filePath, localFile, pathToFile)) return;
            System.out.println("+ "+filePath);
        }catch(Exception ex){

        }
    }

    private void writeChangedFileWithDirIfNew(String lastDestDir, String relativeFilePath, File file) {
        try {
            String filePath = cleanUpPath(lastDestDir) +java.io.File.separator+
                    cleanUpPathAll(relativeFilePath);
            java.io.File localFile = new java.io.File(filePath);
            Path pathToFile = Paths.get(filePath);

            if (downloadFileWithMd5Check(file, filePath, localFile, pathToFile)) return;
            System.out.println("~ "+filePath);
        }catch(Exception ex){

            System.out.println(ex);
        }
    }

    private boolean downloadFileWithMd5Check(File file, String filePath, java.io.File localFile, Path pathToFile) throws Exception {
        boolean updateMd5=false;
        if(Files.exists(pathToFile)){
            String md5 = repo.getMd5(file.getId());
            if(md5 ==null) {
                md5 = com.google.common.io.Files.
                        asByteSource(localFile).hash(Hashing.md5()).toString();
                repo.addMd5(file.getId(),md5);
            }



            if(file.getMd5Checksum().equalsIgnoreCase(md5)){
                //System.out.println("@ "+filePath);
                return true;
            }
            updateMd5 = true;
        }
        Files.createDirectories(pathToFile.getParent());
        FileOutputStream fop = new FileOutputStream(localFile);
        runApiCall(()->{driveService.files().get(file.getId())
                .executeMediaAndDownloadTo(fop);return null;});
        if(updateMd5){
            repo.updateMd5(file.getId(), file.getMd5Checksum());
        }else {
            repo.addMd5(file.getId(), file.getMd5Checksum());
        }
        return false;
    }

    public void uploadUpdate(String uploadUpdate, String uploadUpdateTarget) {


    }
}
