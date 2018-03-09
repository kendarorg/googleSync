package org.kendar;

import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class GD2Test {
    @Test
    public void itShouldBePossibleToDoFirstRun(){
        GD2DriveService drive = mock(GD2DriveService.class);
        GD2StatusService status = mock(GD2StatusService.class);
        GD2Settings settings = mock(GD2Settings.class);

        String gPath = "/path";
        String lPath = "/home/test/other";

        GD2 target = new GD2(status,drive,settings);


        target.run(gPath,lPath);
    }

    @Test
    public void itShouldBePossibleToLoadRemoteStatus(){
        GD2DriveService drive = mock(GD2DriveService.class);
        GD2Settings settings = mock(GD2Settings.class);

        String gPath = "/path";
        GD2StatusService target= new GD2StatusServiceImpl(drive,settings);
        target.loadGoogleStatus(gPath, status);
    }

    @Test
    public void itShouldBePossibleToLoadLocalStatus(){
        GD2DriveService drive = mock(GD2DriveService.class);
        GD2Settings settings = mock(GD2Settings.class);

        String lPath = "/home/test/other";
        GD2StatusService target= new GD2StatusServiceImpl(drive,settings);
        target.loadLocalStatus(lPath, status);
    }


    @Test
    public void itShouldBePossibleToLoadGoogleDirs(){
        GD2Settings settings = mock(GD2Settings.class);

        String lPath = "/home/test/other";
        GD2DriveService drive = new GD2DriveServiceImpl(settings);
        drive.loadAllData();
    }
}
