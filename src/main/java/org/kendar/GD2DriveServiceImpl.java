package org.kendar;

import org.kendar.entities.GD2DriveItem;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.inject.Inject;
import javax.inject.Named;

@Named("gd2DriveService")
public class GD2DriveServiceImpl implements GD2DriveService{
    private GD2Settings settings;

    @Inject
    public GD2DriveServiceImpl(GD2Settings settings){

        this.settings = settings;
    }
    @Override
    public GD2DriveItem loadAllDirs() {
        throw new NotImplementedException();
    }

}
