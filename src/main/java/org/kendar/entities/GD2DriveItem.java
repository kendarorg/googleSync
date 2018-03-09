package org.kendar.entities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GD2DriveItem {
    private String id;
    private String name;
    private String parentId;
    private GD2DriveItem parent;
    ConcurrentHashMap<String,GD2DriveItem> children = new ConcurrentHashMap<>();
    private Instant createdTime;
    private Instant modifiedTime;
    private boolean dir;
    private boolean local = false;
    private long size =0;
    private String md5 = "";

    public List<GD2DriveItem> getChildren(){
        return new ArrayList<GD2DriveItem>(children.values());
    }

    public void setChildren(List<GD2DriveItem> children){
        for(GD2DriveItem item:children){
            item.setParent(this);
            item.setParentId(this.getId());
            this.children.put(item.id,item);
        }
    }

    public void addChild(GD2DriveItem child){
        this.children.put(child.id,child);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public GD2DriveItem getParent() {
        return parent;
    }

    public void setParent(GD2DriveItem parent) {
        this.parent = parent;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setModifiedTime(Instant modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Instant getModifiedTime() {
        return modifiedTime;
    }

    public void setDir(boolean dir) {
        this.dir = dir;
    }

    public boolean isDir() {
        return dir;
    }

    public boolean isLocal() {
        return local;
    }

    public void setLocal(boolean local) {
        this.local = local;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getSize() {
        return size;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }
}
