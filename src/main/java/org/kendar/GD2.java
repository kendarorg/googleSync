package org.kendar;

import org.enel.GDConnection;
import org.enel.GDDir;
import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.utils.GD2Exception;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;

@Named("gd2")
public class GD2 {
    private GD2StatusService statusService;
    private GD2DriveService driveService;
    private GD2Settings settings;
    private GD2Connection connection;

    @Inject
    public GD2(GD2StatusService statusService,GD2DriveService driveService,GD2Settings settings,GD2Connection connection){

        this.statusService = statusService;
        this.driveService = driveService;
        this.settings = settings;
        this.connection = connection;
    }
    public void run(String gPath, String lPath) throws GD2Exception {
        Path localPath = Paths.get(lPath);
        GD2DriveStatus status = this.statusService.loadDriveStatus(gPath,localPath);

        connection.waitAll(
                ()->this.statusService.loadLocalStatus(localPath,status),
                ()->this.statusService.loadGoogleStatus(gPath,status));

        ;
        this.statusService.loadDifferences();


    }
}
