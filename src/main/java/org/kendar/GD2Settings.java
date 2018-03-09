package org.kendar;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.json.JsonFactory;

import java.nio.file.Path;
import java.util.List;

public interface GD2Settings {
    Path getRootPath();

    Path getSettingsDir();

    Path getDbFile();

    GoogleClientSecrets getClientSecrets();

    List<String> getScopes();

    JsonFactory getJsonFactory();

    String getDbConnectionString();

    String getApplicationName();
    Path getDataStorePath();
}
