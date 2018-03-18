package org.kendar.entities;

public class GD2DriveDelta {
    private GD2DriveActionEnum action;
    private GD2DriveItem local;
    private GD2DriveItem remote;

    public GD2DriveDelta(){

    }

    public GD2DriveDelta(GD2DriveActionEnum action, GD2DriveItem local, GD2DriveItem remote) {
        this.action = action;
        this.local = local;
        this.remote = remote;
    }

    public GD2DriveActionEnum getAction() {
        return action;
    }

    public void setAction(GD2DriveActionEnum action) {
        this.action = action;
    }

    public GD2DriveItem getLocal() {
        return local;
    }

    public void setLocal(GD2DriveItem local) {
        this.local = local;
    }

    public GD2DriveItem getRemote() {
        return remote;
    }

    public void setRemote(GD2DriveItem remote) {
        this.remote = remote;
    }
}
