package org.enel.entities;

import java.time.Instant;

public class FileSyncStatus {
    private Instant googleCreated;
    private Instant googleModified;
    private Instant localCreated;
    private Instant localeModified;

    private String googleMd5;
    private String localMd5;

    private FileSync action;

    private boolean shouldDownloadFromGoogle;

    public Instant getGoogleCreated() {
        return googleCreated;
    }

    public void setGoogleCreated(Instant googleCreated) {
        this.googleCreated = googleCreated;
    }

    public Instant getGoogleModified() {
        return googleModified;
    }

    public void setGoogleModified(Instant googleModified) {
        this.googleModified = googleModified;
    }

    public Instant getLocalCreated() {
        return localCreated;
    }

    public void setLocalCreated(Instant localCreated) {
        this.localCreated = localCreated;
    }

    public Instant getLocaleModified() {
        return localeModified;
    }

    public void setLocaleModified(Instant localeModified) {
        this.localeModified = localeModified;
    }

    public String getGoogleMd5() {
        return googleMd5;
    }

    public void setGoogleMd5(String googleMd5) {
        this.googleMd5 = googleMd5;
    }

    public String getLocalMd5() {
        return localMd5;
    }

    public void setLocalMd5(String localMd5) {
        this.localMd5 = localMd5;
    }

    public boolean isShouldDownloadFromGoogle() {
        return shouldDownloadFromGoogle;
    }

    public void setShouldDownloadFromGoogle(boolean shouldDownloadFromGoogle) {
        this.shouldDownloadFromGoogle = shouldDownloadFromGoogle;
    }

    public FileSync getAction() {
        return action;
    }

    public void setAction(FileSync action) {
        this.action = action;
    }
}
