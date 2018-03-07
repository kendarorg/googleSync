package org.enel;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.enel.entities.DriveItem;
import org.enel.utils.GDException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GDIgnore {
    private final String IGNORE_FILE = ".googleIgnore";
    private GDConnection settings;

    public GDIgnore(GDConnection settings) {

        this.settings = settings;
    }

    public void writeIgnore(Path currentDirPath) throws GDException {
        try {
            Path ignoreFile = Paths.get(currentDirPath.toString(),IGNORE_FILE);
            if(Files.exists(ignoreFile)) {
                return;
            }
            Files.write(ignoreFile,new byte[]{});
        } catch (IOException ex) {
            throw new GDException("041",ex);
        }
    }

    public List<DriveItem> getIgnoredGoogleDirs() throws GDException {
        List<DriveItem> resultIds = new ArrayList<>();

        String nextPageToken = "";
        int dirCount = 0;
        while(nextPageToken!=null){

            FileList result = null;

            if(nextPageToken.length()==0) {
                result = settings.runApiCall("002",
                        (s)->s.files().list()
                                .setSpaces("drive")
                                .setFields("nextPageToken, files(id, name, parents)")
                                .setQ("name = '"+IGNORE_FILE+"' and trashed = false")
                                .execute());
            }else{
                final String ntp = nextPageToken;
                result = settings.runApiCall("003",(s)->s.files().list()
                        .setPageToken(ntp)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("name = '"+IGNORE_FILE+"' and trashed = false")
                        .execute());
            }

            for (File file : result.getFiles()) {
                dirCount++;
                DriveItem item = new DriveItem();
                item.setId(file.getId());
                item.setName(file.getName());
                if(file.getParents()!=null && file.getParents().size()>0) {
                    item.setParentId(file.getParents().get(0));
                }
                item.setDir(false);
                resultIds.add(item);
            }
            nextPageToken = result.getNextPageToken();
        }
        return resultIds;
    }
}
