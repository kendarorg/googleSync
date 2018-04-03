package org.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.entities.GD2DriveItem;
import org.utils.PoiService;

import static org.junit.Assert.assertTrue;

@RunWith(JUnit4.class)
public class PoiServiceTest {
    @Test
    public void ShouldLoadFile(){
        GD2DriveItem root = PoiService.loadFile("Sample.xlsx",true);
        assertTrue(root.isLocal());
    }
}
