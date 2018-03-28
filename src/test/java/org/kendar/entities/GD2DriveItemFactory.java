package org.kendar.entities;

import org.kendar.entities.GD2DriveItem;

import java.time.Instant;
import java.util.UUID;

public class GD2DriveItemFactory {
    public static  GD2DriveItem createSimple(String name,String id,GD2DriveItem... items){
        GD2DriveItem result = new GD2DriveItem();
        result.name = name;
        result.dir =true;
        result.id = id==null? UUID.randomUUID().toString():id;
        result.setCreatedTime(Instant.ofEpochSecond(100L));
        result.setModifiedTime(Instant.ofEpochSecond(200L));
        result.setSize(300);
        result.setMd5("400");
        for(GD2DriveItem i :items){
            result.addChild(i);
        }
        return result;
    }


    public static  GD2DriveItem enrich(Long size,Long created,Long modif,String md5,GD2DriveItem src){

        src.setCreatedTime(created==null?src.getCreatedTime():Instant.ofEpochSecond(created));
        src.setModifiedTime(modif==null?src.getModifiedTime():Instant.ofEpochSecond(modif));
        if(size!=null && size!=src.size ){
            md5=UUID.randomUUID().toString();
        }
        src.setSize(size==null?src.size:1000L);
        src.setMd5(md5==null||md5.isEmpty()?src.md5:"4000");
        return src;
    }
}
