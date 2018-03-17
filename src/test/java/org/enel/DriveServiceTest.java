package org.enel;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.*;
import org.kendar.entities.GD2DriveItem;
import org.kendar.utils.GD2ConnectedFunction;
import org.kendar.utils.GD2Exception;
import org.mockito.Matchers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnit4.class)
public class DriveServiceTest {
    private GD2DriveServiceImpl target;
    GD2Settings settings;
    GD2Database db;
    GD2Connection connection;
    final String MIME_DEFAULT ="text/html";
    final String MD5_DEFAULT ="11111";
    final long SIZE_DEFAULT = 1000;

    @Before
    public void setUp(){
        connection = mock(GD2Connection.class);
        settings = mock(GD2Settings.class);
        db = mock(GD2Database.class);
        target = new GD2DriveServiceImpl(settings,db,connection);
    }

    @Test
    public void shouldLoadSingleDir() throws GD2Exception {
        FileList fileList = new FileList();
        File dir = new File();
        dir.setId(UUID.randomUUID().toString());
        dir.setName("test");
        dir.setCreatedTime(new DateTime(1000));
        dir.setModifiedTime(new DateTime(2000));
        dir.setMimeType(GD2DriveServiceImpl.DIR_MIME);

        fileList.setFiles(Arrays.asList(dir ));
        when(connection.runGoogle(
                eq("loadAllData-01"), any(GD2ConnectedFunction.class))).
                thenReturn(fileList);


        GD2DriveItem root = target.loadAllData();

        assertNotNull(root);
    }


    @Test
    public void shouldLoadSingleFile() throws GD2Exception {
        FileList fileList = new FileList();
        File file = new File();
        file.setId(UUID.randomUUID().toString());
        file.setName("test.txt");
        file.setCreatedTime(new DateTime(1000));
        file.setModifiedTime(new DateTime(2000));
        file.setMimeType(MIME_DEFAULT);
        file.setMd5Checksum(MD5_DEFAULT);
        file.setSize(SIZE_DEFAULT);

        fileList.setFiles(Arrays.asList(file ));
        when(connection.runGoogle(
                eq("loadAllData-01"), any(GD2ConnectedFunction.class))).
                thenReturn(fileList);


        GD2DriveItem root = target.loadAllData();

        assertNotNull(root);
    }

    @Test
    public void shouldLoadMediumStruct() throws GD2Exception {
        FileList fileList = createMediumStructure();
        when(connection.runGoogle(
                eq("loadAllData-01"), any(GD2ConnectedFunction.class))).
                thenReturn(fileList);


        GD2DriveItem root = target.loadAllData();

        assertNotNull(root);
        assertEquals(2,root.getChildren().size());
    }

    private FileList createMediumStructure() {
        FileList fileList = new FileList();
        File dir = new File();
        dir.setId(UUID.randomUUID().toString());
        dir.setName("test");
        dir.setCreatedTime(new DateTime(1000));
        dir.setModifiedTime(new DateTime(2000));
        dir.setMimeType(GD2DriveServiceImpl.DIR_MIME);

        File file = new File();
        file.setId(UUID.randomUUID().toString());
        file.setName("test.txt");
        file.setCreatedTime(new DateTime(1000));
        file.setModifiedTime(new DateTime(2000));
        file.setMimeType(MIME_DEFAULT);
        file.setMd5Checksum(MD5_DEFAULT);
        file.setSize(SIZE_DEFAULT);

        File dirFile = new File();
        dirFile.setId(UUID.randomUUID().toString());
        dirFile.setName("dirFile.txt");
        dirFile.setParents(Arrays.asList());
        dirFile.setCreatedTime(new DateTime(3000));
        dirFile.setModifiedTime(new DateTime(4000));
        dirFile.setMimeType(MIME_DEFAULT);
        dirFile.setMd5Checksum(MD5_DEFAULT+"A");
        dirFile.setSize(SIZE_DEFAULT*2);
        dirFile.setParents(Arrays.asList(dir.getId()));

        fileList.setFiles(Arrays.asList(dirFile,dir,file));
        return fileList;
    }
}
