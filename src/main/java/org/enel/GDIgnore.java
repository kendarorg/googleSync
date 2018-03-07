package org.enel;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.enel.entities.DriveItem;

import java.util.ArrayList;
import java.util.List;

public class GDIgnore {

    private GDConnection settings;

    public GDIgnore(GDConnection settings) {

        this.settings = settings;
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
                                .setQ("name = '.googleIgnore' and trashed = false")
                                .execute());
            }else{
                final String ntp = nextPageToken;
                result = settings.runApiCall("003",(s)->s.files().list()
                        .setPageToken(ntp)
                        .setSpaces("drive")
                        .setFields("nextPageToken, files(id, name, parents)")
                        .setQ("name = '.googleIgnore' and trashed = false")
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
