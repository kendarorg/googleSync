package org.kendar.utils;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2Path;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

import static org.kendar.utils.TimeUtils.ftToInstant;

public class LocalFileUtils {
    public static GD2DriveItem loadLocalDriveItem(GD2Path path) throws GD2Exception {
        try {
            BasicFileAttributes localFile = Files.readAttributes(path.toPath(), BasicFileAttributes.class);
            return loadLocalDriveItem(path,localFile);
        }catch(GD2Exception ex){
            throw ex;
        }catch(Exception es){
            throw new GD2Exception("localFileUtils-01",es);
        }
    }

    public static GD2DriveItem loadLocalDriveItem(GD2Path path,BasicFileAttributes localFile) throws GD2Exception {
        try {
            GD2DriveItem item = new GD2DriveItem();
            item.setId(path.toString());
            item.setParentId(path.getParent().toString());
            item.setName(path.getFileName());
            item.setCreatedTime(ftToInstant(localFile.creationTime()));
            item.setModifiedTime(ftToInstant(localFile.lastModifiedTime()));
            item.setDir(localFile.isDirectory());
            if(!localFile.isDirectory()) {
                item.setSize(localFile.size());
            }
            item.setLocal(true);
            return item;
        }catch(Exception ex){
            throw new GD2Exception("localFileUtils-01",ex);
        }
    }
}
