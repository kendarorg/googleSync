package org.kendar;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;

@Named("gd2StatusService")
public class GD2StatusServiceImpl implements GD2StatusService {
    private GD2DriveService driveService;
    private GD2Settings settings;

    @Inject
    public GD2StatusServiceImpl(GD2DriveService driveService,GD2Settings settings){

        this.driveService = driveService;
        this.settings = settings;
    }
    @Override
    public void loadGoogleStatus(String gPath) {
        this.driveService.loadAllDirs();
        throw new NotImplementedException();
    }

    @Override
    public void loadLocalStatus(String lPath) {
        throw new NotImplementedException();
    }
}
