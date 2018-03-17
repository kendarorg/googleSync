package org.kendar;

import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2Path;
import org.kendar.utils.GD2Consumer;
import org.kendar.utils.GD2Exception;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import static org.kendar.utils.LocalFileUtils.loadLocalDriveItem;

public class GD2LocalServiceImpl implements GD2LocalService {
    @Override
    public void loadLocalFiles(GD2Path path,GD2Consumer<GD2DriveItem> multiConsumer) throws GD2Exception {
        Path lPath = path.toPath();
        try {
            Files.walkFileTree(lPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    try {
                        GD2DriveItem item = loadLocalDriveItem(GD2Path.get(file),attrs);
                        multiConsumer.run(item);
                    } catch (Exception e) {
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    try {
                        GD2DriveItem item = loadLocalDriveItem(GD2Path.get(dir));
                        multiConsumer.run(item);
                    } catch (Exception e) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new GD2Exception("localService-01",e);
        }
    }
}
