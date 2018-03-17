package org.enel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.*;
import org.kendar.entities.*;
import org.kendar.utils.GD2Consumer;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
 
@RunWith(JUnit4.class)
public class StatusServiceLoadDifferencesTest {
    GD2DriveService driveService;
    GD2Settings settings;
    GD2Database db;
    private GD2StatusServiceImpl target;
    private GD2LocalService localService;

    @Before
    public void doSetup(){

        driveService = mock(GD2DriveService.class);
        localService = mock(GD2LocalService.class);
        settings = mock(GD2Settings.class);
        db = mock(GD2Database.class);
        target = new GD2StatusServiceImpl(driveService,settings,db,localService);
    }



    private void mockLoadLocalFiles(List<GD2DriveItem> listOfFiles) throws Exception {
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
                for(GD2DriveItem item :listOfFiles ){
                    GD2Consumer<GD2DriveItem> inv = (GD2Consumer<GD2DriveItem>) invocationOnMock.getArguments()[1];
                    inv.run(item);
                }
                return null;
            }
        }).when(localService).loadLocalFiles(isA(GD2Path.class), Matchers.any());
    }


    @Test
    public void shouldLoadDiffsEquals() throws Exception {
        GD2Path local = GD2Path.get("C:\\localDrive","");
        GD2Path google = GD2Path.get("/dir/");
        GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive", "C:\\localDrive",
                GD2DriveItemFactory.createSimple("file.txt", "C:\\localDrive\\file.txt"),
                GD2DriveItemFactory.createSimple("dir", "C:\\localDrive\\dir",
                        GD2DriveItemFactory.createSimple("dirfile.txt", "C:\\localDrive\\dir\\dirfile.txt")));
        root.setDir(true);

        List<GD2DriveItem> listOfFiles = root.iterator().toList();
        mockLoadLocalFiles(listOfFiles);


        GD2DriveItem rootg = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                GD2DriveItemFactory.createSimple("dir",null,
                        GD2DriveItemFactory.createSimple("dirfile.txt",null)));
        rootg.setId(null);
        rootg.setDir(true);


        when(driveService.loadAllData()).
                thenReturn(rootg);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setGooglePath(google);
        status.setId(local.toString());

        target.loadLocalStatus(local,status);
        target.loadGoogleStatus(google,status);

        List<GD2DriveDelta> result = target.loadDifferences();

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}
