package org.enel;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveItemFactory;
import org.kendar.entities.GD2DriveItemUtils;
import org.kendar.entities.GD2Path;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;

@RunWith(JUnit4.class)
public class DriveItemUtilsTest {
    @Test
    public void shouldFindSubInLocal(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir", "C:\\localDrive\\dir",
                GD2DriveItemFactory.createSimple("dirfile.txt",
                        "C:\\localDrive\\dir\\dirfile.txt"));

        GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive", "C:\\localDrive",
        GD2DriveItemFactory.createSimple("file.txt", "C:\\localDrive\\file.txt"),
                sub);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(root, GD2Path.get("dir"));
        assertNotNull(founded);
        assertEquals("dir",founded.getName());
    }

    @Test
    public void shouldFindRootInLocal(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir", "C:\\localDrive\\dir",
                GD2DriveItemFactory.createSimple("dirfile.txt",
                        "C:\\localDrive\\dir\\dirfile.txt"));

        GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive", "C:\\localDrive",
                GD2DriveItemFactory.createSimple("file.txt", "C:\\localDrive\\file.txt"),
                sub);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(root, GD2Path.get(""));
        assertNotNull(founded);
        assertEquals("localDrive",founded.getName());
    }


    @Test
    public void shouldFindRootInLocalSL(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir", "C:\\localDrive\\dir",
                GD2DriveItemFactory.createSimple("dirfile.txt",
                        "C:\\localDrive\\dir\\dirfile.txt"));

        GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive", "C:\\localDrive",
                GD2DriveItemFactory.createSimple("file.txt", "C:\\localDrive\\file.txt"),
                sub);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(root, GD2Path.get("\\"));
        assertNotNull(founded);
        assertEquals("localDrive",founded.getName());
    }

    @Test
    public void shouldFindRootInLocalBSL(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir", "C:\\localDrive\\dir",
                GD2DriveItemFactory.createSimple("dirfile.txt",
                        "C:\\localDrive\\dir\\dirfile.txt"));

        GD2DriveItem root = GD2DriveItemFactory.createSimple("localDrive", "C:\\localDrive",
                GD2DriveItemFactory.createSimple("file.txt", "C:\\localDrive\\file.txt"),
                sub);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(root, GD2Path.get("/"));
        assertNotNull(founded);
        assertEquals("localDrive",founded.getName());
    }

    @Test
    public void shouldFindAllInGoogle(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir",null,
                GD2DriveItemFactory.createSimple("dirfile.txt",null));
        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                sub);
        root.setId(null);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(root, GD2Path.get("dir"));
        assertNotNull(founded);
        assertEquals("dir",founded.getName());
    }


    @Test
    public void shouldFindSubInGoogle(){
        GD2DriveItem sub = GD2DriveItemFactory.createSimple("dir",null,
                GD2DriveItemFactory.createSimple("dirfile.txt",null));
        GD2DriveItem root = GD2DriveItemFactory.createSimple("",null,
                GD2DriveItemFactory.createSimple("file.txt",null),
                sub);
        root.setId(null);
        root.setDir(true);

        GD2DriveItem founded = GD2DriveItemUtils.findByPath(sub, GD2Path.get("dirfile.txt"));
        assertNotNull(founded);
        assertEquals("dirfile.txt",founded.getName());
    }
}
