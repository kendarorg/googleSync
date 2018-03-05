package org.kendar;

public class DirectoryStatus {
    public DirectoryStatus(){

    }
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
}
