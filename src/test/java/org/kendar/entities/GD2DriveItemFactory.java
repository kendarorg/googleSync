package org.kendar.entities;

import org.kendar.entities.GD2DriveItem;

import java.util.UUID;

public class GD2DriveItemFactory {
    public static  GD2DriveItem createSimple(String name,String id,GD2DriveItem... items){
        GD2DriveItem result = new GD2DriveItem();
        result.name = name;
        result.dir =true;
        result.id = id==null? UUID.randomUUID().toString():id;
        for(GD2DriveItem i :items){
            result.addChild(i);
        }
        return result;
    }
}
