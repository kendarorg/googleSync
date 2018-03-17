package org.kendar;

import org.kendar.entities.GD2DriveDelta;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.entities.GD2Path;
import org.kendar.utils.GD2Exception;

import java.nio.file.Path;
import java.util.List;

public interface GD2StatusService {
    void loadGoogleStatus(GD2Path gPath, GD2DriveStatus status) throws GD2Exception;

    void loadLocalStatus(GD2Path lPath, GD2DriveStatus status) throws GD2Exception;

    List<GD2DriveDelta> loadDifferences() throws GD2Exception;

    GD2DriveStatus loadDriveStatus(GD2Path googlePath,GD2Path localPath);
}
