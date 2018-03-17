package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.entities.GD2Path;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.file.Path;
import java.util.List;

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

    @Override
    public void saveDriveItems(List<GD2DriveItem> itemsToInsert) {

    }

    @Override
    public GD2DriveStatus getDriveStatus(GD2Path localPath) {
        return null;
    }

    @Override
    public void saveDriveStatus(GD2DriveStatus currentStatus) {

    }
}
