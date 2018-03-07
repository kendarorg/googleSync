package org.kendar;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.*;
import com.google.common.hash.Hashing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;

public class UpdateChooser {
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    boolean shouldUpdateFromGoogle(File googleFile,Path localfilePath) throws IOException {
        BasicFileAttributes localFile = Files.readAttributes(localfilePath, BasicFileAttributes.class);
        googleFile.getCreatedTime();
        googleFile.getModifiedTime();
        String md5GoogleFile = googleFile.getMd5Checksum();

        Instant localLastModified = toRFC3339(localFile.lastModifiedTime());
        Instant googleLastModified = toRFC3339(googleFile.getModifiedTime());

        Instant localCreated = toRFC3339(localFile.creationTime());
        Instant googleCreated = toRFC3339(googleFile.getCreatedTime());


        if(googleLastModified.isAfter(localLastModified)){
            if(googleFile.getSize()!=localFile.size()){
                return true;
            }
            String md5 = com.google.common.io.Files.
                    asByteSource(localfilePath.toFile()).hash(Hashing.md5()).toString();
            if(md5.equalsIgnoreCase(md5GoogleFile)){
                return false;
            }
            return true;
        }
        if(googleFile.getSize()!=localFile.size()){
            return true;
        }
        String md5 = com.google.common.io.Files.
                asByteSource(localfilePath.toFile()).hash(Hashing.md5()).toString();
        if(md5.equalsIgnoreCase(md5GoogleFile)){
            return false;
        }

        return true;
    }

    private Instant toRFC3339(DateTime modifiedTime) {

        try {
            return sdf.parse(modifiedTime.toStringRfc3339()).toInstant().truncatedTo(ChronoUnit.SECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }



    private Instant toRFC3339(FileTime d)
    {
        try {
            String tmp = rfc3339.format(d.toInstant()).replaceAll("(\\d\\d)(\\d\\d)$", "$1:$2");
            return sdf.parse(tmp).toInstant().truncatedTo(ChronoUnit.SECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }

}
