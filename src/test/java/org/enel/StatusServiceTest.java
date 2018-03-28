package org.enel;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.*;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.entities.GD2Path;
import org.junit.Test;
import org.kendar.utils.GD2Exception;
import org.kendar.utils.GD2Md5Calculator;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class StatusServiceTest {
    GD2DriveService driveService;
    GD2Settings settings;
    GD2Database db;
    private GD2StatusServiceImpl target;
    private GD2LocalService localService;
    private GD2Md5Calculator md5calculator;

    @Before
    public void doSetup(){

        driveService = mock(GD2DriveService.class);
        settings = mock(GD2Settings.class);
        db = mock(GD2Database.class);
        localService = mock(GD2LocalService.class);
        md5calculator = mock(GD2Md5Calculator.class);
        target = new GD2StatusServiceImpl(driveService,settings,db,localService,md5calculator);
    }
    @Test
    public void shouldCreateStatus(){
        GD2Path google = GD2Path.get("/");
        GD2Path local = GD2Path.get("C:\\localDrive");

        GD2DriveStatus result = target.loadDriveStatus(google,local);


        assertNull(result.getLastGoogleToken());
        assertEquals(result.getGooglePath().toString(),google.toString());
        assertEquals(result.getId().toString(),local.toString());
    }
}
