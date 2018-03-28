package org.kendar;

import org.kendar.entities.GD2DriveItem;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class GD2IgnoreParserImpl implements GD2IgnoreParser {
    @Override
    public void loadIgnore(GD2DriveItem path) {
        throw new NotImplementedException();
    }

    @Override
    public boolean shouldIgnore(GD2DriveItem path) {
        throw new NotImplementedException();
    }
}
