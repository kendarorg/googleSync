package org.kendar;

import org.kendar.entities.GD2DriveItem;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;

@Named("gd2Database")
public class GD2DatabaseImpl implements GD2Database{
    private GD2Settings settings;

    @Inject
    public GD2DatabaseImpl(GD2Settings settings){
        this.settings = settings;
    }

    @Override
    public void saveDriveItem(GD2DriveItem item) {
        throw new NotImplementedException();
    }
}
