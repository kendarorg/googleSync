package org.kendar;

import org.enel.GDConnection;
import org.enel.GDDir;
import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;

@Named("gd2")
public class GD2 {
    private GD2StatusService statusService;
    private GD2DriveService driveService;
    private GD2Settings settings;

    @Inject
    public GD2(GD2StatusService statusService,GD2DriveService driveService,GD2Settings settings){

        this.statusService = statusService;
        this.driveService = driveService;
        this.settings = settings;
    }
    public void run(String gPath, String lPath){
        this.statusService.loadGoogleStatus(gPath);
        this.statusService.loadLocalStatus(lPath);
    }
}
