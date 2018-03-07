package org.enel;

import com.google.api.services.drive.model.*;
import org.enel.entities.*;
import org.enel.utils.GDException;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
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

    public DriveItem getAllDirs(String rootGoogleDir) throws GDException {
        rootGoogleDir = cleanRootPath(rootGoogleDir);
        RootDriveItem root = loadAllDirs();
        return root.getDrivePaths().get(rootGoogleDir);
    }

    private RootDriveItem loadAllDirs() throws GDException {
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
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("mimeType = 'application/vnd.google-apps.folder' and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = connection.runApiCall("005",(s)->s.files().list()
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

        for(DriveItem item : ignore.getIgnoredGoogleDirs()){
            DriveItem toIgnore = root.getDriveItems().get(item.getParentId());
            toIgnore.setIgnore(true);
            toIgnore.getItem().clear();
        }
        root.setId("root");
        return root;
    }

    public void getAllFiles(DriveItem getGooleDir, Path targetDir, boolean dryRun) throws GDException {
        DirectoryStatus status = connection.getDb().getDirStatus(getGooleDir);
        String tokenString = null;
        //if(status.getLastUpdate()==null || status.getLastUpdate().isEmpty()){
        StartPageToken response = (StartPageToken) connection.runApiCall("014",(s)-> s.changes()
                .getStartPageToken()
                .execute());
        tokenString = response.getStartPageToken();

        connection.waitForJobsCompletion();
        status.setLastUpdate(tokenString);
        downloadFresh(targetDir,getGooleDir,dryRun,getGooleDir,true);
        connection.getDb().saveDirStatus(status,dryRun);
        //}
    }

    public void updateAllFiles(DriveItem getGooleDir, Path targetDir, boolean dryRun) throws GDException {
        DirectoryStatus status = connection.getDb().getDirStatus(getGooleDir);
        RootDriveItem root = getGooleDir.getRoot();
        Path driveDirPath = Paths.get(getGooleDir.getFullPath());

        String pageToken = status.getLastUpdate();
        while (pageToken != null) {

            status.setLastUpdate(pageToken);
            final String pt = pageToken;
            ChangeList changes = connection.runApiCall("030", (s) -> s.changes().list(pt)
                    //.setFields("changes,kind,newStartPageToken,nextPageToken")
                    .execute());
            //Object[] changez = .toArray();
            for (Change change : changes.getChanges()) {
                //Change change = (Change)changeo;
                // Process change
                File file = connection.runApiCall("031", (s) -> s.files().get(change.getFileId())
                        .setFields("id,name,parents,modifiedTime,createdTime,md5Checksum,mimeType")
                        .execute());
                DriveItem parent = null;
                if (file.getParents() == null || file.getParents().isEmpty()) {
                    parent = root;
                } else {
                    parent = root.getDriveItems().get(file.getParents().get(0));
                }
                if (parent == null) {
                    parent = root;
                }



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
                            doWriteFile(currentDirPath,  file);
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
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime)")
                        .setQ("'"+sourceDir.getId()+"' in parents and trashed = false")
                        .execute());
            }else{
                final String npt = nextPageToken;
                result = connection.runApiCall("016",(s)->s.files().list()
                        .setPageToken(npt)
                        .setSpaces("drive")
                        .setFields("nextPageToken,files(id, name, parents,md5Checksum,mimeType,modifiedTime,createdTime)")
                        .setQ("'"+sourceDir.getId()+"' in parents and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                if(file.getMimeType().equalsIgnoreCase("application/vnd.google-apps.folder")){
                    if(!realRoot.getDriveItems().containsKey(file.getId())){
                        throw new GDException("Missing dir on google "+currentDirPath+"/"+file.getName());
                    }
                }
                if (!dryRun) {
                    connection.doRun(new GoogleTaskable() {
                        @Override
                        public void run() throws GDException {
                            doWriteFile(currentDirPath,  file);
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



    private void doWriteFile(Path currentDirPath, File file) throws GDException {
        try {
            Path localFilePath = Paths.get(currentDirPath.toString(), file.getName());
            FileSyncStatus status = shouldUpdateFromGoogle(file, currentDirPath);

            BasicFileAttributeView attributes = Files.getFileAttributeView(localFilePath, BasicFileAttributeView.class);

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

                    break;
                }
                case UPLOAD_TO_GOOGLE:{
                    throw new NotImplementedException();
                }
                case DO_NOTHING:
                    attributes.setTimes(
                            FileTime.from(status.getGoogleModified()),
                            FileTime.from(Instant.now()),
                            FileTime.from(status.getGoogleCreated()));
                    break;
                default:
                    break;
            }
        }catch(IOException ex){
            throw new GDException("022",ex);
        }
    }


}
