package org.kendar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class RootDriveItem extends DriveItem {
    private HashMap<String,DriveItem> driveItems =new HashMap<>();
    private HashMap<String,DriveItem> drivePaths =new HashMap<>();
    private HashSet<String> folderIdsToIgnore =new HashSet<>();

    public void addId(String id,DriveItem item){
        driveItems.put(id,item);
    }
    public void addIgnore(String id){
        folderIdsToIgnore.add(id);
    }
    public HashMap<String, DriveItem> getDriveItems() {
        return driveItems;
    }
    public HashMap<String, DriveItem> getDrivePaths() {
        return drivePaths;
    }

    public void setDriveItems(HashMap<String, DriveItem> driveItems) {
        this.driveItems = driveItems;
    }
}
