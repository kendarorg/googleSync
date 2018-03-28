package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.utils.GD2Exception;

public interface GD2DriveService {
    GD2DriveItem loadAllData() throws GD2Exception;

    void addToGoogle(GD2DriveItem local, GD2DriveItem google);

    void readFromGoogle(GD2DriveItem local, GD2DriveItem google) throws GD2Exception;

    void updateOnGoogle(GD2DriveItem local, GD2DriveItem google);
}
