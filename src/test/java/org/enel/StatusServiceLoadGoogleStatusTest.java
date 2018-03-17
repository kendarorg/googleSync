package org.enel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.*;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveItemFactory;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.entities.GD2Path;
import org.kendar.utils.GD2Exception;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class StatusServiceLoadGoogleStatusTest {
    GD2DriveService driveService;
    GD2Settings settings;
    GD2Database db;
    private GD2StatusServiceImpl target;
    private GD2LocalService localService;

    @Before
    public void doSetup(){

        driveService = mock(GD2DriveService.class);
        settings = mock(GD2Settings.class);
        db = mock(GD2Database.class);
        localService = mock(GD2LocalService.class);
        target = new GD2StatusServiceImpl(driveService,settings,db,localService);
    }


    @Test
    public void shouldLoadGoogleStatusEMPTY() throws GD2Exception {
        GD2Path google = GD2Path.get("");
        GD2Path local = GD2Path.get("C:\\localDrive");

        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                GD2DriveItemFactory.createSimple("dir",null,
                        GD2DriveItemFactory.createSimple("dirfile.txt",null)));
        root.setId(null);
        root.setDir(true);


        when(driveService.loadAllData()).
                thenReturn(root);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setGooglePath(google);
        status.setId(local.toString());

        target.loadGoogleStatus(google,status);

        GD2DriveItem founded = target.getGoogleRoot();

        assertNotNull(founded);
        assertEquals("",founded.getName());
    }

    @Test
    public void shouldLoadGoogleStatusSL() throws GD2Exception {
        GD2Path google = GD2Path.get("/");
        GD2Path local = GD2Path.get("C:\\localDrive");

        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                GD2DriveItemFactory.createSimple("dir",null,
                        GD2DriveItemFactory.createSimple("dirfile.txt",null)));
        root.setId(null);
        root.setDir(true);


        when(driveService.loadAllData()).
                thenReturn(root);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setGooglePath(google);
        status.setId(local.toString());

        target.loadGoogleStatus(google,status);

        GD2DriveItem founded = target.getGoogleRoot();

        assertNotNull(founded);
        assertEquals("",founded.getName());
    }


    @Test
    public void shouldLoadGoogleStatusSubSL_NAME() throws GD2Exception {
        GD2Path google = GD2Path.get("/dir");
        GD2Path local = GD2Path.get("C:\\localDrive");

        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                GD2DriveItemFactory.createSimple("dir",null,
                        GD2DriveItemFactory.createSimple("dirfile.txt",null)));
        root.setId(null);
        root.setDir(true);


        when(driveService.loadAllData()).
                thenReturn(root);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setGooglePath(google);
        status.setId(local.toString());

        target.loadGoogleStatus(google,status);

        GD2DriveItem founded = target.getGoogleRoot();

        assertNotNull(founded);
        assertEquals("dir",founded.getName());
    }


    @Test
    public void shouldLoadGoogleStatusSubSL_NAME_SL() throws GD2Exception {
        GD2Path google = GD2Path.get("/dir/");
        GD2Path local = GD2Path.get("C:\\localDrive");

        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                GD2DriveItemFactory.createSimple("dir",null,
                        GD2DriveItemFactory.createSimple("dirfile.txt",null)));
        root.setId(null);
        root.setDir(true);


        when(driveService.loadAllData()).
                thenReturn(root);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setGooglePath(google);
        status.setId(local.toString());

        target.loadGoogleStatus(google,status);

        GD2DriveItem founded = target.getGoogleRoot();

        assertNotNull(founded);
        assertEquals("dir",founded.getName());
    }
}
