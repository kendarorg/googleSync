package org.kendar;

import org.enel.entities.FileSync;
import org.kendar.entities.*;
import org.kendar.utils.GD2Exception;
import org.kendar.utils.ValueContainer;
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
import static org.kendar.entities.GD2DriveItemUtils.getPath;
import static org.kendar.entities.GD2DriveItemUtils.sortDriveItems;
import static org.kendar.utils.LocalFileUtils.loadLocalDriveItem;

@Named("gd2StatusService")
public class GD2StatusServiceImpl implements GD2StatusService {
    private GD2DriveService driveService;
    private GD2Settings settings;
    private GD2Database db;
    private GD2LocalService localService;
    private GD2DriveItem googleRoot;
    private GD2DriveItem localRoot;

    @Inject
    public GD2StatusServiceImpl(
            GD2DriveService driveService,GD2Settings settings,GD2Database db,
            GD2LocalService localService){

        this.driveService = driveService;
        this.settings = settings;
        this.db = db;
        this.localService = localService;
    }
    @Override
    public void loadGoogleStatus(GD2Path gPath, GD2DriveStatus status) throws GD2Exception {
        if(status.getLastGoogleToken()==null) {
            GD2DriveItem root = this.driveService.loadAllData();
            googleRoot = findByPath(root,gPath);
        }else{
            throw new NotImplementedException();
        }
    }

    @Override
    public void loadLocalStatus(GD2Path lPathg, GD2DriveStatus status) throws GD2Exception {
        Path lPath =lPathg.toPath();
        HashMap<String, GD2DriveItem> items = new HashMap<>();
        ValueContainer<GD2DriveItem> root = new ValueContainer<>();

        List<GD2DriveItem> itemsToInsert = new ArrayList<>();

        localService.loadLocalFiles(lPathg,(item)->{

            if(item.getParentId()==null){
                root.setValue(item);
            }else{
                items.put(item.getId(),item);
            }
            storeItemsOnDb(itemsToInsert,item);
        });
        storeItemsOnDb(itemsToInsert,null);
        sortDriveItems(items, root.getValue());
        localRoot = root.getValue();
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
        GD2DriveIterator iterator = localRoot.iterator();

        while(iterator.moveNext()){
            GD2DriveItem local = iterator.getCurrent();
            GD2Path path = getPath(localRoot,local);
            GD2DriveItem google = findByPath(googleRoot,path);


            if(google.getModifiedTime().isAfter(local.getModifiedTime())){
                if(google.isThrashed()) {
                    result.add(new GD2DriveDelta(GD2DriveActionEnum.DELETE_FROM_LOCAL,
                            local, google));
                    continue;
                }
            }

            if(!google.getMd5().equalsIgnoreCase(local.getMd5())){
                result.add(new GD2DriveDelta(GD2DriveActionEnum.ADD_TO_GOOGLE,
                        local,google));
            }

            //}else
                if(google.getModifiedTime().isAfter(local.getModifiedTime())){
                if ( google.getSize() != local.getSize()) {
                    result.add(new GD2DriveDelta(GD2DriveActionEnum.ADD_TO_LOCAL,
                            local,google));
                }else if(!google.getMd5().equalsIgnoreCase(local.getMd5())){

                }
            }else{
                result.add(new GD2DriveDelta(GD2DriveActionEnum.ADD_TO_GOOGLE,
                        local,google));
            }
            throw new NotImplementedException();
           //Path localPath =
            /*if(current.isDir()){

            }*/

        }

        //First folder present on drive not present locally

        throw new NotImplementedException();
    }

    @Override
    public GD2DriveStatus loadDriveStatus(GD2Path googlePath,GD2Path localPath) {
        GD2DriveStatus currentStatus = db.getDriveStatus(localPath);
        if(currentStatus==null){
            currentStatus = new GD2DriveStatus();
            currentStatus.setGooglePath(googlePath);
            currentStatus.setId(localPath.toString());
            db.saveDriveStatus(currentStatus);
        }
        return currentStatus;
    }


    public GD2DriveItem getGoogleRoot() {
        return googleRoot;
    }

    public GD2DriveItem getLocalRoot() {
        return localRoot;
    }
}
