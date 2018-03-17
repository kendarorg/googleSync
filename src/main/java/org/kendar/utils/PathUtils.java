package org.kendar.utils;

import org.apache.commons.lang3.StringUtils;
import org.kendar.entities.GD2Path;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtils {
    public static final String SEPARATOR = File.separator;
    public static GD2Path cleanGooglePath(GD2Path path){
        return path;
        /*if(path==null || path.toString().equals(SEPARATOR)|| path.toString().equals("")) {
            return Paths.get("");
        }
        String paths= path.toString();
        if(!paths.startsWith(SEPARATOR)) {
            return Paths.get(SEPARATOR+paths);
        }
        return path;*/
    }
}
