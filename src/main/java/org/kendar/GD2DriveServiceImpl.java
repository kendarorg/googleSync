package org.kendar;

import com.google.api.client.http.FileContent;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import org.kendar.entities.GD2DriveItem;
import org.kendar.entities.GD2DriveItemUtils;
import org.kendar.utils.GD2Exception;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.kendar.entities.GD2DriveItemUtils.sortDriveItems;
import static org.kendar.utils.TimeUtils.dtToInstant;

@Named("gd2DriveService")
public class GD2DriveServiceImpl implements GD2DriveService {
    public final static String DIR_MIME="application/vnd.google-apps.folder";
    final static String DEFAULT_DRIVE_SPACE = "drive";
    final static String FULL_DIR_SEARCH_FIELDS =
            "nextPageToken, files(id, name, parents,modifiedTime,createdTime,trashed,size,md5checksum)";
    final static String FULL_DIR_SEARCH_QUERY=
            //"mimeType = 'application/vnd.google-apps.folder' and trashed = false";
        "trashed = false";

    private GD2Settings settings;
    private GD2Database db;
    private GD2Connection connection;


    @Inject
    public GD2DriveServiceImpl(GD2Settings settings, GD2Database db, GD2Connection connection) {

        this.settings = settings;
        this.db = db;
        this.connection = connection;
    }



    private void storeItemsOnDb(List<GD2DriveItem> itemsToInsert, GD2DriveItem item) {
        itemsToInsert.add(item);
        if(itemsToInsert.size()>100||item==null) {
            db.saveDriveItems(itemsToInsert);
            itemsToInsert.clear();
        }
    }


    @Override
    public GD2DriveItem loadAllData() throws GD2Exception {
        try {
            GD2DriveItem root = new GD2DriveItem();
            root.setId(null);
            root.setName("");
            root.setDir(true);

            HashMap<String, GD2DriveItem> items = new HashMap<>();
            List<GD2DriveItem> itemsToInsert = new ArrayList<>();

            String nextPageToken = "";
            while (nextPageToken != null) {
                final String npt = nextPageToken;
                FileList result = connection.runGoogle("loadAllData-01",(s)->{
                    Drive.Files.List list = s.files().list();
                    if(npt.length()>0){
                        list = list.setPageToken(npt);
                    }
                    return list.setSpaces(DEFAULT_DRIVE_SPACE)
                            .setFields(FULL_DIR_SEARCH_FIELDS)
                            .setQ(FULL_DIR_SEARCH_QUERY)
                            .execute();
                });

                for (File file : result.getFiles()) {
                    GD2DriveItem item = new GD2DriveItem();
                    item.setId(file.getId());
                    item.setName(file.getName());
                    if (file.getParents() != null && file.getParents().size() > 0) {
                        item.setParentId(file.getParents().get(0));
                    }
                    item.setCreatedTime(dtToInstant(file.getCreatedTime()));
                    item.setModifiedTime(dtToInstant(file.getModifiedTime()));
                    item.setDir(DIR_MIME.equalsIgnoreCase(file.getMimeType()));
                    item.setLocal(false);
                    if(!item.isDir()){
                        item.setSize(file.getSize());
                        item.setMd5(file.getMd5Checksum());
                    }
                    items.put(item.getId(), item);
                    storeItemsOnDb(itemsToInsert,item);
                }
                nextPageToken = result.getNextPageToken();
            }

            storeItemsOnDb(itemsToInsert,null);
            sortDriveItems(items, root);
            return root;
        } catch (GD2Exception ex) {
            throw ex;
        } catch (Exception es) {
            throw new GD2Exception("loadAllData-02", es);
        }
    }

    @Override
    public void addToGoogle(GD2DriveItem local, GD2DriveItem google) {

    }

    @Override
    public void readFromGoogle(GD2DriveItem local, GD2DriveItem google) throws GD2Exception {
        java.io.File file = new  java.io.File(GD2DriveItemUtils.getPath(null,local).toString());
        connection.run(()-> {
            connection.runGoogle("readFromGoogle-01", (c) -> {
                FileOutputStream fop = new FileOutputStream(file);
                c.files().get(google.getId())
                        .executeMediaAndDownloadTo(fop);
                return null;
            });
        });
    }

    @Override
    public void updateOnGoogle(GD2DriveItem local, GD2DriveItem google) {
        Path path = GD2DriveItemUtils.getPath(null,local).toPath();
        java.io.File filePath = new java.io.File(path.toString());
        String mime = URLConnection.guessContentTypeFromName(filePath.getName());

        File fileMetadata = new File();
        fileMetadata.setName(path.getFileName().toString());
        fileMetadata.setId(google.getId());
        fileMetadata.setCreatedTime(new DateTime(local.getCreatedTime().toEpochMilli()));
        fileMetadata.setModifiedTime(new DateTime(local.getModifiedTime().toEpochMilli()));
        fileMetadata.setParents(Arrays.asList(google.getParentId()));
        fileMetadata.setMimeType(mime);


        connection.run(()-> {
            connection.runGoogle("updateOnGoogle-01", (c) -> {
                FileContent mediaContent = new FileContent(mime, filePath);
                File file = c.files().create(fileMetadata, mediaContent)
                        .setFields("id")
                        .execute();
                System.out.println("File ID: " + file.getId());

                return null;
            });
        });
    }

}
