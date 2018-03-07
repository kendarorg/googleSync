package org.enel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.enel.entities.DirectoryStatus;
import org.enel.entities.DriveItem;
import org.enel.entities.DriveStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class GDTokenService {

    private ObjectMapper mapper = new ObjectMapper();
    private DriveStatus token;
    private Path tokenStore;

    public void initialize(Path tokenStore) throws GDException {

        if(tokenStore==null) throw new GDException("010");
        this.tokenStore = tokenStore;
        try {
            if(Files.exists(tokenStore)) {
                String json =new String(Files.readAllBytes(tokenStore));
                token = mapper.readValue(json, DriveStatus.class);
            }else{
                token = new DriveStatus();
            }
        } catch (IOException e) {
            token = new DriveStatus();
            e.printStackTrace();
        }
    }

    public DirectoryStatus getDirStatus(DriveItem item){
        String path = item.getFullPath();
        for(DirectoryStatus st:token.getDirectoryStatusList()){
            if(st.getDirectoryPath().equalsIgnoreCase(path)){
                return st;
            }
        }
        DirectoryStatus res = new DirectoryStatus();
        res.setDirectoryPath(path);
        res.setDirectoryId(item.getId());
        return res;
    }

    public void saveDirStatus(boolean dryRun) throws GDException {

        if(tokenStore==null) throw new GDException("011");
        if(dryRun)return;
        try {
            String value = mapper.writeValueAsString(token);
            Files.write(tokenStore,value.getBytes(), StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
