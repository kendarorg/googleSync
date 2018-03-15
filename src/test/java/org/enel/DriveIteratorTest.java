package org.enel;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveIterator;
import org.kendar.utils.GD2Exception;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

public class DriveIteratorTest {
    @Test
    public void shouldDoStuffs() throws GD2Exception {
        GD2DriveItem i0 = new GD2DriveItem("0",
            new GD2DriveItem("01",
                    new GD2DriveItem("011"),
                    new GD2DriveItem("012")
            ),
            new GD2DriveItem("02",
                    new GD2DriveItem("021"),
                    new GD2DriveItem("022")
            )
        );

        GD2DriveIterator target = new GD2DriveIterator(i0);
        StringBuffer result = new StringBuffer();
        result.append(",");
        while(target.moveNext()){
            result.append(target.getCurrent().getName());
            result.append(",");
        }
        String ress = result.toString();
        assertTrue(ress.contains(",022,"));
        assertTrue(ress.contains(",021,"));
        assertTrue(ress.contains(",02,"));
        assertTrue(ress.contains(",01,"));
        assertTrue(ress.contains(",012,"));
        assertTrue(ress.contains(",011,"));
        assertTrue(ress.contains(",0,"));
    }
}
