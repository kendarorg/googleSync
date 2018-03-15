package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveStatus;

import java.nio.file.Path;
import java.util.List;

public interface GD2Database {

    void saveDriveItem(GD2DriveItem item);

    void saveDriveItems(List<GD2DriveItem> itemsToInsert);

    GD2DriveStatus getDriveStatus(Path localPath);

    void saveDriveStatus(GD2DriveStatus currentStatus);
}
