package org.enel;

import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;

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

    public void download(String gdDir, String targetDir,boolean dryRun,boolean force) throws GDException {
        DriveItem getGooleDir = this.dirManager.getAllDirs(gdDir);
        Path targetDirPath = Paths.get(targetDir);

        DirectoryStatus status = connection.getDb().getDirStatus(getGooleDir);
        if(force){
            status.setLastUpdate(null);
        }
        if(status.getLastUpdate()==null || status.getLastUpdate().isEmpty()) {
            this.dirManager.getAllFiles(getGooleDir, targetDirPath, dryRun);
        }else{

            this.dirManager.updateAllFiles(getGooleDir, targetDirPath, dryRun);
        }
    }
}
