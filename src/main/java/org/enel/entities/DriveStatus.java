package org.enel.entities;


import java.util.ArrayList;
import java.util.List;

public class DriveStatus {
    private List<DirectoryStatus> directoryStatusList = new ArrayList<>();

    public List<DirectoryStatus> getDirectoryStatusList() {
        return directoryStatusList;
    }

    public void setDirectoryStatusList(List<DirectoryStatus> directoryStatusList) {
        this.directoryStatusList = directoryStatusList;
    }

    public void writeStatus(DirectoryStatus status){
        if(directoryStatusList==null)directoryStatusList = new ArrayList<>();
        for(DirectoryStatus stat:directoryStatusList){
            if(stat.getDirectoryPath().equalsIgnoreCase(status.getDirectoryPath())){
                stat.setDirectoryId(status.getDirectoryId());
                stat.setLastUpdate(status.getLastUpdate());
                return;
            }
        }
        directoryStatusList.add(status);
    }

    public String readStatus(DirectoryStatus status){
        if(directoryStatusList==null)directoryStatusList = new ArrayList<>();
        for(DirectoryStatus stat:directoryStatusList){
            if(stat.getDirectoryPath().equalsIgnoreCase(status.getDirectoryPath())){
                return stat.getLastUpdate();
            }
        }
        return null;
    }
}
