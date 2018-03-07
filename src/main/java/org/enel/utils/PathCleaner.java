package org.enel.utils;

import org.apache.commons.lang3.StringUtils;

public class PathCleaner {
    public static String cleanRootPath(String path){
        return StringUtils.stripEnd(path,"\\/");
    }

    public static String cleanPath(String path){
        return StringUtils.strip(path,"\\/");
    }
}
