package org.enel;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.*;
import org.enel.entities.*;
import org.enel.utils.GDException;
import org.enel.utils.ValueContainer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

import static org.enel.utils.PathCleaner.cleanRootPath;
import static org.enel.utils.UpdateChooser.shouldUpdateFromGoogle;

public class GDDir {
    private GDConnection connection;
    private GDIgnore ignore;

    public GDDir(GDConnection connection, GDIgnore ignore) {
        this.connection = connection;
        this.ignore = ignore;
    }

    public DriveItem getAllDirs(String rootGoogleDir,boolean dryRun) throws GDException {
        rootGoogleDir = cleanRootPath(rootGoogleDir);
        RootDriveItem root = loadAllDirs(dryRun);
        return root.getDrivePaths().get(rootGoogleDir);
    }

    private RootDriveItem loadAllDirs(boolean dryRun) throws GDException {
        RootDriveItem root = new RootDriveItem();
        root.setId(null);
        root.setName("");

        HashMap<String,DriveItem> items = new HashMap<>();

        String nextPageToken = "";
        int dirCount = 0;
        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = connection.runApiCall("004",(s)->s.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents,modifiedTime,createdTime,trashed)")
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = connection.runApiCall("005",(s)->s.files().list()
                        .setPageToken(npt)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents,modifiedTime,createdTime,trashed)")
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
                item.setCreatedTime(toInstant(file.getCreatedTime()));
                item.setModifiedTime(toInstant(file.getModifiedTime()));
                item.setDir(true);
                items.put(item.getId(),item);
                connection.getDb().saveDriveItem(item,dryRun);
            }
            nextPageToken = result.getNextPageToken();
        }


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

        for(DriveItem item : ignore.getIgnoredGoogleDirs()){
            DriveItem toIgnore = root.getDriveItems().get(item.getParentId());
            toIgnore.setIgnore(true);
            toIgnore.getItem().clear();
        }
        root.setId("root");
        return root;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    private Instant toInstant(DateTime modifiedTime) {

        try {
            return sdf.parse(modifiedTime.toStringRfc3339()).toInstant().truncatedTo(ChronoUnit.SECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public void getAllFiles(DriveItem getGooleDir, Path targetDir, boolean dryRun) throws GDException {
        DirectoryStatus status = connection.getDb().getDirStatus(getGooleDir,targetDir.toString());
        String tokenString = null;

        StartPageToken response =connection.runApiCall("014",(s)-> s.changes()
                .getStartPageToken()
                .execute());
        tokenString = response.getStartPageToken();

        connection.waitForJobsCompletion();
        status.setLastUpdate(tokenString);
        downloadFresh(targetDir,getGooleDir,dryRun,getGooleDir,true);
        connection.getDb().saveDirStatus(status,dryRun);
    }

    public void updateAllFiles(DriveItem getGooleDir, Path targetDir, boolean dryRun) throws GDException {
        DirectoryStatus status = connection.getDb().getDirStatus(getGooleDir,targetDir.toString());
        RootDriveItem root = getGooleDir.getRoot();
        Path driveDirPath = Paths.get(getGooleDir.getFullPath());

        String pageToken = status.getLastUpdate();
        while (pageToken != null) {

            status.setLastUpdate(pageToken);
            final String pt = pageToken;
            ChangeList changes = connection.runApiCall("030", (s) -> s.changes().list(pt)
                    .setFields("newStartPageToken,nextPageToken,changes(removed,fileId,kind,time)")
                    .execute());

            for (Change change : changes.getChanges()) {

                File file = connection.runApiCall("031", (s) -> s.files().get(change.getFileId())
                        .setFields("id,name,parents,modifiedTime,createdTime,md5Checksum,mimeType")
                        .execute());
                if(change.getRemoved()){
                    file.setModifiedTime(change.getTime());
                    file.setTrashed(true);
                }
                DriveItem parent;
                if (file.getParents() == null || file.getParents().isEmpty()) {
                    parent = root;
                } else {
                    parent = root.getDriveItems().get(file.getParents().get(0));
                }
                if (parent == null) {
                    parent = root;
                }
                final DriveItem finalParent = parent;



                Path relativeFilePath = Paths.get(parent.getFullPath(), file.getName());
                final Path currentDirPath = Paths.get(targetDir.toString(),parent.getFullPath(getGooleDir));
                if(parent.isIgnore()){
                    ignore.writeIgnore(currentDirPath);
                    continue;
                }
                if (!relativeFilePath.startsWith(driveDirPath)) {
                    continue;
                }
                if (!dryRun) {
                    connection.doRun(new GoogleTaskable() {
                        @Override
                        public void run() throws GDException {
                            doWriteFile(finalParent,currentDirPath,  file,dryRun);
                        }
                    });
                }

            }

            connection.waitForJobsCompletion();
            if (pageToken != null) {
                status.setLastUpdate(pageToken);
                connection.getDb().saveDirStatus(status,dryRun);
            }
            pageToken = changes.getNextPageToken();
        }
    }

    private void downloadFresh(Path targetDir, DriveItem sourceDir, boolean dryRun, DriveItem localRoot, boolean first) throws GDException {
        String nextPageToken = "";
        final Path currentDirPath ;
        RootDriveItem realRoot = localRoot.getRoot();
        if(first){
            currentDirPath = Paths.get(targetDir.toString(),"");
        }else{
            currentDirPath = Paths.get(targetDir.toString(),sourceDir.getFullPath(localRoot));
        }
        try {
            Files.createDirectory(currentDirPath);
        }catch(IOException ex){
            throw new GDException("040",ex);
        }
        if(sourceDir.isIgnore()){
            ignore.writeIgnore(currentDirPath);
            return;
        }


        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = connection.runApiCall("015",(s)->s.files().list()
                        .setSpaces("drive")
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime,trashed)")
                        .setQ("'"+sourceDir.getId()+"' in parents and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = connection.runApiCall("016",(s)->s.files().list()
                        .setPageToken(npt)
                        .setSpaces("drive")
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime,trashed)")
                        .setQ("'"+sourceDir.getId()+"' in parents and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                if(isDirectory(file)){
                    if(!realRoot.getDriveItems().containsKey(file.getId())){
                        throw new GDException("Missing dir on google "+currentDirPath+"/"+file.getName());
                    }
                }
                if (!dryRun) {
                    connection.doRun(new GoogleTaskable() {
                        @Override
                        public void run() throws GDException {
                            doWriteFile(sourceDir,currentDirPath,  file,dryRun);
                        }
                    });
                }
            }

            nextPageToken = result.getNextPageToken();
        }
        for(DriveItem son : sourceDir.getItem()){
            downloadFresh(targetDir,son,dryRun,localRoot,false);
        }
    }



    private void doWriteFile(DriveItem parent,Path currentDirPath, File file,boolean dryRun) throws GDException {
        try {
            Path localFilePath = Paths.get(currentDirPath.toString(), file.getName());

            if(!Files.exists(localFilePath) && file.getTrashed()){
                return;
            }
            BasicFileAttributeView attributesView = null;
            BasicFileAttributes attributesData = null;
            if(Files.exists(localFilePath)){
                attributesView = Files.getFileAttributeView(localFilePath, BasicFileAttributeView.class);
                attributesData = attributesView.readAttributes();
            }
            final BasicFileAttributeView attributes = attributesView;

            if(isDirectory(file)){

                if(Files.exists(localFilePath) && file.getTrashed()){
                    Instant trashedTime = toInstant(file.getModifiedTime());
                    if(attributesData.lastModifiedTime().toInstant().isAfter(trashedTime)||
                            attributesData.creationTime().toInstant().isAfter(trashedTime)||
                            somethingInFolderIsNewer(localFilePath,trashedTime)){
                        scheduleFileDirUpload(parent, file, localFilePath, true);
                    }else{
                        ScheduledOperation op = new ScheduledOperation();
                        op.setDir(true);
                        op.setOperation(FileSync.REMOVE_FROM_LOCAL);
                        op.setLocalPath(localFilePath.toString());
                        op.setRemotePath(parent.getFullPath()+java.io.File.separator+file.getName());
                        connection.getDb().scheduleOperation(op);
                    }
                }else{
                    DriveItem newDriveItem = new DriveItem();
                    newDriveItem.setMd5("");
                    newDriveItem.setIgnore(false);
                    newDriveItem.setParentId(parent.getId());
                    newDriveItem.setParent(parent);
                    newDriveItem.setName(file.getName());
                    newDriveItem.setCreatedTime(toInstant(file.getCreatedTime()));
                    newDriveItem.setModifiedTime(toInstant(file.getModifiedTime()));
                    newDriveItem.setDir(true);
                    newDriveItem.getRoot().addId(newDriveItem.getId(),newDriveItem);
                    newDriveItem.getRoot().getDrivePaths().put(newDriveItem.getFullPath(),newDriveItem);
                    connection.getDb().saveDriveItem(newDriveItem,true);
                }
            }else {
                FileSyncStatus status = shouldUpdateFromGoogle(file, currentDirPath);



                switch (status.getAction()) {
                    case DOWNLOAD_FROM_GOOGLE: {
                        FileOutputStream fop = new FileOutputStream(localFilePath.toString());
                        connection.runApiCall("021", (s) -> {
                            s.files().get(file.getId())
                                    .executeMediaAndDownloadTo(fop);

                            attributes.setTimes(
                                    FileTime.from(status.getGoogleModified()),
                                    FileTime.from(Instant.now()),
                                    FileTime.from(status.getGoogleCreated()));
                            return null;
                        });
                        DriveItem item = createFileDriveItem(parent, file, status);
                        connection.getDb().saveDriveItem(item, dryRun);
                        break;
                    }
                    case REMOVE_FROM_LOCAL: {
                        Files.delete(localFilePath);
                        break;
                    }
                    case UPLOAD_TO_GOOGLE: {
                        scheduleFileDirUpload(parent, file, localFilePath, false);
                        break;
                    }
                    case DO_NOTHING:
                        attributes.setTimes(
                                FileTime.from(status.getGoogleModified()),
                                FileTime.from(Instant.now()),
                                FileTime.from(status.getGoogleCreated()));
                        DriveItem item = createFileDriveItem(parent, file, status);
                        connection.getDb().saveDriveItem(item, dryRun);
                        break;
                    default:
                        break;
                }
            }
        }catch(IOException ex){
            throw new GDException("022",ex);
        }
    }

    private void scheduleFileDirUpload(DriveItem parent, File file, Path localFilePath, boolean dir) {
        ScheduledOperation op = new ScheduledOperation();
        op.setDir(dir);
        op.setOperation(FileSync.UPLOAD_TO_GOOGLE);
        op.setLocalPath(localFilePath.toString());
        op.setRemotePath(parent.getFullPath()+java.io.File.separator+file.getName());
        connection.getDb().scheduleOperation(op);
    }

    private boolean somethingInFolderIsNewer(Path localFilePath, Instant trashedTime) {
        ValueContainer<Boolean> founded = new ValueContainer<>(false);
        try {

            Files.walkFileTree(localFilePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(attrs.lastModifiedTime().toInstant().isAfter(trashedTime)){
                        founded.setValue(true);
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {

        }
        return founded.getValue();
    }

    private void removeDirectory(Path localFilePath) {

        try {
            Files.walkFileTree(localFilePath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {

        }
    }

    private boolean isDirectory(File file) {
        return file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder");
    }

    private DriveItem createFileDriveItem(DriveItem parent, File file, FileSyncStatus status) {
        DriveItem item = new DriveItem();
        item.setDir(false);
        item.setId(file.getId());
        item.setModifiedTime(status.getGoogleModified());
        item.setCreatedTime(status.getGoogleCreated());
        item.setName(file.getName());
        item.setParent(parent);
        item.setMd5(file.getMd5Checksum());
        item.setParentId(parent.getId());
        item.setIgnore(false);
        return item;
    }


}
