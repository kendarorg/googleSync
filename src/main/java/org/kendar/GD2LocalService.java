package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2Path;
import org.kendar.utils.GD2Consumer;
import org.kendar.utils.GD2Exception;

import java.util.List;

public interface GD2LocalService {
    void loadLocalFiles(GD2Path path,GD2Consumer<GD2DriveItem> multiConsumer) throws GD2Exception;
}
