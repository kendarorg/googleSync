package org.kendar.utils;

import com.google.common.hash.Hashing;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveItemUtils;

import javax.inject.Named;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Named("gD2Md5Calculator")
public class GD2Md5CalculatorImpl implements GD2Md5Calculator {
    public void setupLocalMd5(GD2DriveItem local) throws GD2Exception {
        try{
            if(local.getMd5()!=null && !local.getMd5().isEmpty())return;
            Path path = Paths.get(GD2DriveItemUtils.getPath(null,local).toString());
            local.setMd5(com.google.common.io.Files.
                    asByteSource(path.toFile()).hash(Hashing.md5()).toString());
        }catch (IOException ex){
            throw new GD2Exception("STATUS-22",ex);
        }
    }
}
