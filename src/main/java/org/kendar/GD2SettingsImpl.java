package org.kendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.DriveScopes;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Named("gd2Settings")
public class GD2SettingsImpl implements GD2Settings {
    private final String SETTINGS_DIR = ".googleSync";
    private final String CLIENT_SECRET = "client_secret.json";
    private final String DB_DIR = "db";
    private final String DB_FILE = "gd2.data";
    private final String DATA_STORE_FILE= "googleSync.ds";

    private final Path rootPath;
    private final Path dataStorePath;
    private final Path settingsDir;
    private final Path dbFile;
    private final GoogleClientSecrets clientSecrets;
    private final String dbConnectionString;
    private String applicationName=null;

    private JsonFactory jsonFactory;
    private List<String> scopes;

    public GD2SettingsImpl(String rootPath) throws IOException {
        this.rootPath = Paths.get(rootPath);
        this.settingsDir = Paths.get(rootPath,SETTINGS_DIR);
        if(!Files.exists(settingsDir)){
            Files.createDirectory(settingsDir);
        }
        this.dataStorePath = Paths.get(rootPath,SETTINGS_DIR,DATA_STORE_FILE);
        this.dbFile = Paths.get(rootPath,SETTINGS_DIR,DB_DIR,DB_FILE);
        if(!Files.exists(dbFile.getParent())){
            Files.createDirectory(dbFile.getParent());
        }
        jsonFactory =JacksonFactory.getDefaultInstance();
        scopes = Arrays.asList(DriveScopes.DRIVE);

        dbConnectionString = "jdbc:sqlite:"+this.dbFile.toString();

        InputStream in =
                GD2SettingsImpl.class.getResourceAsStream(java.io.File.separator+CLIENT_SECRET);
        clientSecrets =
                GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));
    }

    @Override
    public Path getRootPath() {
        return rootPath;
    }

    @Override
    public Path getSettingsDir() {
        return settingsDir;
    }

    @Override
    public Path getDbFile() {
        return dbFile;
    }

    @Override
    public GoogleClientSecrets getClientSecrets() {
        return clientSecrets;
    }

    @Override
    public List<String> getScopes(){
        return scopes;
    }
    @Override
    public JsonFactory getJsonFactory(){
        return jsonFactory;
    }

    @Override
    public String getDbConnectionString() {
        return dbConnectionString;
    }

    @Override
    public String getApplicationName() {
        return applicationName;
    }

    @Override
    public Path getDataStorePath() {
        return dataStorePath;
    }
}
