package org.enel.entities;

import org.old.DriveItem;

import java.time.Instant;
import java.util.UUID;

public class DirectoryStatus {
    private String realPath;

    public DirectoryStatus(){

    }
    private UUID id;
    private String directoryId;
    private String directoryPath;
    private String lastUpdate;
    private Instant lastUpdateTime;

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

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Instant lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}
