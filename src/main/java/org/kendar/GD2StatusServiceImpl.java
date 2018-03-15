package org.kendar;

import org.kendar.entities.GD2DriveDelta;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveIterator;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.utils.GD2Exception;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.kendar.entities.GD2DriveItemUtils.findByPath;
import static org.kendar.entities.GD2DriveItemUtils.sortDriveItems;
import static org.kendar.utils.LocalFileUtils.loadLocalDriveItem;

@Named("gd2StatusService")
public class GD2StatusServiceImpl implements GD2StatusService {
    private GD2DriveService driveService;
    private GD2Settings settings;
    private GD2Database db;
    private GD2DriveItem googleRoot;
    private GD2DriveItem localRoot;

    @Inject
    public GD2StatusServiceImpl(GD2DriveService driveService,GD2Settings settings,GD2Database db){

        this.driveService = driveService;
        this.settings = settings;
        this.db = db;
    }
    @Override
    public void loadGoogleStatus(String gPath, GD2DriveStatus status) throws GD2Exception {
        if(status.getLastGoogleToken()==null) {
            GD2DriveItem root = this.driveService.loadAllData();
            googleRoot = findByPath(root,gPath);
        }else{
            throw new NotImplementedException();
        }
    }

    @Override
    public void loadLocalStatus(Path lPath, GD2DriveStatus status) {


        HashMap<String, GD2DriveItem> items = new HashMap<>();
        GD2DriveItem root = new GD2DriveItem();
        root.setName("");
        root.setId(lPath.toString());

        List<GD2DriveItem> itemsToInsert = new ArrayList<>();

        try {
            Files.walkFileTree(lPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        GD2DriveItem item = loadLocalDriveItem(file,attrs);
                        items.put(item.getId(),item);
                        storeItemsOnDb(itemsToInsert,item);
                    } catch (Exception e) {
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        GD2DriveItem item = loadLocalDriveItem(dir);
                        items.put(item.getId(),item);
                        storeItemsOnDb(itemsToInsert,item);
                    } catch (Exception e) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
            storeItemsOnDb(itemsToInsert,null);
            sortDriveItems(items, root);
            localRoot = root;
        } catch (IOException e) {

        }
    }

    private void storeItemsOnDb(List<GD2DriveItem> itemsToInsert, GD2DriveItem item) {
        itemsToInsert.add(item);
        if(itemsToInsert.size()>100||item==null) {
            db.saveDriveItems(itemsToInsert);
            itemsToInsert.clear();
        }
    }

    @Override
    public List<GD2DriveDelta> loadDifferences() throws GD2Exception {
        List<GD2DriveDelta> result = new ArrayList<>();
        GD2DriveIterator iterator = googleRoot.iterator();

        while(iterator.moveNext()){
            GD2DriveItem current = iterator.getCurrent();
        }

        //First folder present on drive not present locally

        throw new NotImplementedException();
    }

    @Override
    public GD2DriveStatus loadDriveStatus(String googlePath,Path localPath) {
        GD2DriveStatus currentStatus = db.getDriveStatus(localPath);
        if(currentStatus==null){
            currentStatus = new GD2DriveStatus();
            currentStatus.setGooglePath(googlePath);
            currentStatus.setId(localPath.toString());
            db.saveDriveStatus(currentStatus);
        }
        return currentStatus;
    }


}
