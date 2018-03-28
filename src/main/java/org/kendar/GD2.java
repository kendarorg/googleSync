package org.kendar;

import org.enel.GDConnection;
import org.enel.GDDir;
import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;
import org.kendar.entities.GD2DriveDelta;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.entities.GD2Path;
import org.kendar.utils.GD2Exception;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
    public void firstRun(String gPath, String lPath) throws GD2Exception {
        GD2Path localPath = GD2Path.get(Paths.get(lPath));
        GD2Path googlePath = GD2Path.get(Paths.get(gPath));
        GD2DriveStatus status = this.statusService.loadDriveStatus(googlePath,localPath);

        connection.waitAll(
                ()->this.statusService.loadLocalStatus(localPath,status),
                ()->this.statusService.loadGoogleStatus(googlePath,status));


        List<GD2DriveDelta> result = this.statusService.loadDifferences();
        updateData(result);
        this.statusService.saveDriveStatus(status);
    }

    private void updateData(List<GD2DriveDelta> result) throws GD2Exception {
        for(GD2DriveDelta delta :result){
            switch(delta.getAction()){
                case UPDATE_FROM_LOCAL:
                    driveService.updateOnGoogle(delta.getLocal(),delta.getRemote());
                    break;
                case UPDATE_FROM_GOOGLE:
                    driveService.readFromGoogle(delta.getLocal(),delta.getRemote());
                    break;
                case ADD_TO_GOOGLE:
                    driveService.addToGoogle(delta.getLocal(),delta.getRemote());
                    break;
                case ADD_TO_LOCAL:
                case DELETE_FROM_GOOGLE:
                case DELETE_FROM_LOCAL:
                    throw new NotImplementedException();
                case NOP:
                default:
                    break;
            }
        }
        connection.waitForJobs();
    }
}
