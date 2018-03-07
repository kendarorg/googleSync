package org.enel;

import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;

import java.nio.file.Path;
import java.nio.file.Paths;

public class GD {
    private GDConnection connection;
    private GDDir dirManager;
    private GDTokenService tokenService;


    public GD(GDConnection connection, GDDir dirManager) throws GDException {
        this.connection = connection;
        this.dirManager = dirManager;
    }

    public void forceDownload(String gdDir, String targetDir,boolean dryRun) throws GDException {
        DriveItem getGooleDir = this.dirManager.getAllDirs(gdDir);
        Path targetDirPath = Paths.get(targetDir);

        DirectoryStatus status = connection.getTokenService().getDirStatus(getGooleDir);
        status.setLastUpdate(null);
        this.dirManager.getAllFiles(getGooleDir, targetDirPath, dryRun);
    }

    public void download(String gdDir, String targetDir,boolean dryRun) throws GDException {
        DriveItem getGooleDir = this.dirManager.getAllDirs(gdDir);
        Path targetDirPath = Paths.get(targetDir);

        DirectoryStatus status = connection.getTokenService().getDirStatus(getGooleDir);
        if(status.getLastUpdate()==null || status.getLastUpdate().isEmpty()) {
            this.dirManager.getAllFiles(getGooleDir, targetDirPath, dryRun);
        }else{

            this.dirManager.updateAllFiles(getGooleDir, targetDirPath, dryRun);
        }
    }
}
