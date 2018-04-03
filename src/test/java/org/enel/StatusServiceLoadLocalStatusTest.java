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
import org.kendar.utils.GD2Consumer;
import org.kendar.utils.GD2Exception;
import org.kendar.utils.GD2Md5Calculator;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.utils.PoiService;

import java.util.List;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class StatusServiceLoadLocalStatusTest {
    GD2DriveService driveService;
    GD2Settings settings;
    GD2Database db;
    private GD2StatusServiceImpl target;
    private GD2LocalService localService;
    private GD2Md5Calculator md5calculator;

    @Before
    public void doSetup(){

        driveService = mock(GD2DriveService.class);
        localService = mock(GD2LocalService.class);
        settings = mock(GD2Settings.class);
        md5calculator = mock(GD2Md5Calculator.class);
        db = mock(GD2Database.class);
        target = new GD2StatusServiceImpl(driveService,settings,db,localService,md5calculator);
    }


    @Test
    public void shouldLoadLocalStatusEMPTY() throws Exception {

        GD2Path local = GD2Path.get("C:\\localDrive","");

        GD2DriveItem root =PoiService.loadFile("StatusServiceLoadLocalStatus/shouldLoadLocalStatusEMPTY.xlsx",true);
        /*GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive","C:\\localDrive",
                GD2DriveItemFactory.createSimple("file.txt","C:\\localDrive\\file.txt"),
                GD2DriveItemFactory.createSimple("dir","C:\\localDrive\\dir",
                        GD2DriveItemFactory.createSimple("dirfile.txt","C:\\localDrive\\dir\\dirfile.txt")));
        root.setDir(true);*/

        List<GD2DriveItem> listOfFiles = root.iterator().toList();
        mockLoadLocalFiles(listOfFiles);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setId(local.toString());

        target.loadLocalStatus(local,status);

        GD2DriveItem founded = target.getLocalRoot();

        assertNotNull(founded);
        assertEquals("localDrive",founded.getName());
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
    public void shouldLoadLocalStatusSL() throws Exception {
        GD2Path local = GD2Path.get("C:\\localDrive\\","");

        GD2DriveItem root =PoiService.loadFile("StatusServiceLoadLocalStatus/shouldLoadLocalStatusEMPTY.xlsx",true);

        List<GD2DriveItem> listOfFiles = root.iterator().toList();
        mockLoadLocalFiles(listOfFiles);

        GD2DriveStatus status = new GD2DriveStatus();
        status.setId(local.toString());

        target.loadLocalStatus(local,status);

        GD2DriveItem founded = target.getLocalRoot();

        assertNotNull(founded);
        assertEquals("localDrive",founded.getName());
    }
}
