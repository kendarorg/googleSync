package org.enel;

import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveItemFactory;
import org.kendar.entities.GD2DriveIterator;
import org.kendar.utils.GD2Exception;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


@RunWith(JUnit4.class)
public class DriveIteratorTest {
    @Test
    public void shouldDoStuffs() throws GD2Exception {
        GD2DriveItem i0 = GD2DriveItemFactory.createSimple("0","0",
            GD2DriveItemFactory.createSimple("01","01",
                    GD2DriveItemFactory.createSimple("011","011"),
                    GD2DriveItemFactory.createSimple("012","012")
            ),
            GD2DriveItemFactory.createSimple("02","02",
                    GD2DriveItemFactory.createSimple("021","021"),
                    GD2DriveItemFactory.createSimple("022","022")
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
