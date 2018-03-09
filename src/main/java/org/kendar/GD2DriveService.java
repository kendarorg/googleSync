package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.utils.GD2Exception;

public interface GD2DriveService {
    GD2DriveItem loadAllData() throws GD2Exception;
}
