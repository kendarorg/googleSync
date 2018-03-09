package org.kendar.utils;

import org.apache.commons.lang3.StringUtils;

public class PathUtils {
    public static String cleanGooglePath(String path){
        if(path==null || path.equals("/")|| path.equals("")) return "";
        if(!path.startsWith("/")) return "/"+path;
        return StringUtils.stripEnd(path,"/");
    }
}
