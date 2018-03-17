package org.kendar.entities;

import java.nio.file.Path;
import java.time.Instant;

public class GD2DriveStatus {
    //Local path
    private String id;
    private GD2Path googlePath;
    private String lastGoogleToken;
    private Instant lastGoogleUpdate;
    private Instant lastLocalUpdate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GD2Path getGooglePath() {
        return googlePath;
    }

    public void setGooglePath(GD2Path googlePath) {
        this.googlePath = googlePath;
    }

    public String getLastGoogleToken() {
        return lastGoogleToken;
    }

    public void setLastGoogleToken(String lastGoogleToken) {
        this.lastGoogleToken = lastGoogleToken;
    }

    public Instant getLastGoogleUpdate() {
        return lastGoogleUpdate;
    }

    public void setLastGoogleUpdate(Instant lastGoogleUpdate) {
        this.lastGoogleUpdate = lastGoogleUpdate;
    }

    public Instant getLastLocalUpdate() {
        return lastLocalUpdate;
    }

    public void setLastLocalUpdate(Instant lastLocalUpdate) {
        this.lastLocalUpdate = lastLocalUpdate;
    }
}
