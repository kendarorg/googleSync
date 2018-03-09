package org.kendar;

import org.kendar.entities.GD2DriveDelta;
import org.kendar.entities.GD2DriveStatus;
import org.kendar.utils.GD2Exception;

import java.nio.file.Path;
import java.util.List;

public interface GD2StatusService {
    void loadGoogleStatus(String gPath, GD2DriveStatus status) throws GD2Exception;

    void loadLocalStatus(Path lPath, GD2DriveStatus status);

    List<GD2DriveDelta> loadDifferences() throws GD2Exception;

    GD2DriveStatus loadDriveStatus(String googlePath,Path localPath);
}
