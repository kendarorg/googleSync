package org.kendar;

import org.kendar.entities.GD2DriveDelta;
import org.kendar.entities.GD2DriveItem;

import java.nio.file.Path;

public interface GD2IgnoreParser {
    void loadIgnore(GD2DriveItem path);
    boolean shouldIgnore(GD2DriveItem path);
}
