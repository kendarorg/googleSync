package org.enel.entities;

import java.util.UUID;

public class ScheduledOperation {
    private UUID id;
    private FileSync operation;
    private String localPath;
    private String remptePath;
    private boolean dir;
    private boolean completed;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FileSync getOperation() {
        return operation;
    }

    public void setOperation(FileSync operation) {
        this.operation = operation;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getRemptePath() {
        return remptePath;
    }

    public void setRemotePath(String remptePath) {
        this.remptePath = remptePath;
    }

    public boolean isDir() {
        return dir;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
