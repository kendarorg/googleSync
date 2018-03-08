package org.enel.utils;

import com.google.api.client.util.DateTime;
import com.google.api.services.drive.model.File;
import com.google.common.hash.Hashing;
import org.enel.entities.FileSync;
import org.enel.entities.FileSyncStatus;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class UpdateChooser {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static FileSyncStatus shouldUpdateFromGoogle(File googleFile,Path localfilePath) throws GDException {
        try {
            FileSyncStatus status = new FileSyncStatus();
            BasicFileAttributes localFile = Files.readAttributes(localfilePath, BasicFileAttributes.class);
            status.setGoogleMd5(googleFile.getMd5Checksum());

            status.setLocaleModified(toRFC3339(localFile.lastModifiedTime()));
            status.setGoogleCreated(toRFC3339(googleFile.getModifiedTime()));
            status.setLocalCreated(toRFC3339(localFile.creationTime()));
            status.setGoogleCreated(toRFC3339(googleFile.getCreatedTime()));

            if(googleFile.getTrashed()){
                if (status.getGoogleModified().isAfter(status.getLocaleModified())) {
                    status.setAction(FileSync.REMOVE_FROM_LOCAL);
                }else{
                    status.setAction(FileSync.UPLOAD_TO_GOOGLE);
                }
                return status;
            }


            if (status.getGoogleModified().isAfter(status.getLocaleModified())) {
                if (googleFile.getSize() != localFile.size()) {
                    status.setAction(FileSync.DOWNLOAD_FROM_GOOGLE);
                    return status;
                }
                status.setLocalMd5(com.google.common.io.Files.
                        asByteSource(localfilePath.toFile()).hash(Hashing.md5()).toString());
                if (status.getLocalMd5().equalsIgnoreCase(status.getGoogleMd5())) {
                    status.setAction(FileSync.DO_NOTHING);
                    return status;
                }
                status.setAction(FileSync.DOWNLOAD_FROM_GOOGLE);
                return status;
            }
            if (googleFile.getSize() != localFile.size()) {
                status.setAction(FileSync.UPLOAD_TO_GOOGLE);
                return status;
            }
            status.setLocalMd5(com.google.common.io.Files.
                    asByteSource(localfilePath.toFile()).hash(Hashing.md5()).toString());
            if (status.getLocalMd5().equalsIgnoreCase(status.getGoogleMd5())) {
                status.setAction(FileSync.DO_NOTHING);
                return status;
            }

            status.setAction(FileSync.UPLOAD_TO_GOOGLE);
            return status;
        }catch (Exception ex){
            throw new GDException("018",ex);
        }
    }

    private static Instant toRFC3339(DateTime modifiedTime) {

        try {
            return sdf.parse(modifiedTime.toStringRfc3339()).toInstant().truncatedTo(ChronoUnit.SECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }



    private static Instant toRFC3339(FileTime d)
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
