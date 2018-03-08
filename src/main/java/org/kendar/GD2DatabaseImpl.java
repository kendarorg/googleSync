package org.kendar;

import javax.inject.Inject;
import javax.inject.Named;

@Named("gd2Database")
public class GD2DatabaseImpl implements GD2Database{
    private GD2Settings settings;

    @Inject
    public GD2DatabaseImpl(GD2Settings settings){
        this.settings = settings;
    }

}
