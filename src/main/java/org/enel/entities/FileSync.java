package org.enel.entities;

public enum FileSync {
    DOWNLOAD_FROM_GOOGLE(2),
    UPLOAD_TO_GOOGLE(3),
    REMOVE_FROM_LOCAL(1),
    DO_NOTHING (0);

    private int val;

    FileSync(int val) {
        this.val = val;
    }

    public int toInt() {
        return val;
    }
}
