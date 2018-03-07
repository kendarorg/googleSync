package org.enel.entities;

import org.kendar.DriveItem;

import java.util.UUID;

public class DirectoryStatus {
    private String realPath;

    public DirectoryStatus(){

    }
    private UUID id;
    private String directoryId;
    private String directoryPath;
    private String lastUpdate;

    public DirectoryStatus(DriveItem rootItem) {
        directoryId = rootItem.getId();
        directoryPath = rootItem.getFullPath();
    }

    public String getDirectoryId() {
        return directoryId;
    }

    public void setDirectoryId(String directoryId) {
        this.directoryId = directoryId;
    }

    public String getDirectoryPath() {
        return directoryPath;
    }

    public void setDirectoryPath(String directoryPath) {
        this.directoryPath = directoryPath;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRealPath(String realPath) {
        this.realPath = realPath;
    }

    public String getRealPath() {
        return realPath;
    }
}
