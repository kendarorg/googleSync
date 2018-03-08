package org.old;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DriveItem {
    private String fullPath=null;
    private String id;
    private DriveItem parent;
    private String parentId;
    private String name;
    private boolean isDir;
    private List<DriveItem> item = new ArrayList<>();
    private boolean ignore;

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
        fullPath = "/"+result;
        return fullPath;
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
}
