package org.enel.entities;

import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class DriveItem {
    private String md5;
    private String fullPath=null;
    private String id;
    private DriveItem parent;
    private String parentId;
    private String name;
    private boolean isDir;
    private List<DriveItem> item = new ArrayList<>();
    private boolean ignore;
    private Instant modifiedTime;
    private Instant createdTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DriveItem getParent() {
        return parent;
    }

    public void setParent(DriveItem parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public List<DriveItem> getItem() {
        return item;
    }

    public void setItem(List<DriveItem> item) {
        this.item = item;
    }

    public String getFullPath(){
        if(fullPath!=null) return fullPath;
        String result = getName();
        DriveItem current = this.parent;
        while(current!=null){
            result= current.getName()+ File.separator+result;
            current = current.parent;
        }
        fullPath = File.separator+result;
        return fullPath;
    }

    public String getFullPath(DriveItem upTo){
        if(fullPath!=null) return fullPath;
        String result = getName();
        DriveItem current = this.parent;
        while(current!=null && current!=upTo){
            result= current.getName()+ File.separator+result;
            current = current.parent;
        }
        fullPath = File.separator+result;
        return fullPath;
    }

    public RootDriveItem getRoot(){
        String result = getName();
        DriveItem current = this.parent;

        DriveItem root = null;
        while(current!=null){
            root = current;
            current = current.parent;
        }
        return (RootDriveItem)root;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    public boolean isIgnore() {
        return ignore;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public String getMd5() {
        return md5==null?"":md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
