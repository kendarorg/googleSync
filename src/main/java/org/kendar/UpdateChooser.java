package org.kendar;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class UpdateChooser {
    boolean shouldUpdate(File googleFile,Path localfilePath,String md5LocalFile) throws IOException {
        BasicFileAttributes localFile = Files.readAttributes(localfilePath, BasicFileAttributes.class);
        googleFile.getCreatedTime();
        googleFile.getModifiedTime();
        googleFile.getMd5Checksum();

        String localLastModified = toRFC3339(localFile.lastModifiedTime());
        String googleLastModified = toRFC3339(googleFile.getModifiedTime());


    private String toRFC3339(DateTime modifiedTime) {
        return modifiedTime.toStringRfc3339();
    }

    private SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    private String toRFC3339(FileTime d)
    {
        return rfc3339.format(d.toInstant()).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
    }

}
