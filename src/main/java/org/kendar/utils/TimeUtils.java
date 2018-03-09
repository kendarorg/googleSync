package org.kendar.utils;

import com.google.api.client.util.DateTime;

import java.nio.file.attribute.FileTime;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static SimpleDateFormat rfc3339 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    public static Instant dtToInstant(DateTime modifiedTime) {

        try {
            return sdf.parse(modifiedTime.toStringRfc3339()).toInstant().truncatedTo(ChronoUnit.SECONDS);
        } catch (ParseException e) {
            e.printStackTrace();
            return Instant.now().truncatedTo(ChronoUnit.SECONDS);
        }
    }

    public static Instant ftToInstant(FileTime d)
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
