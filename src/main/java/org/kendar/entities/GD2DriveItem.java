package org.kendar.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class GD2DriveItem {
    private String id;
    private String name;
    private String parentId;
    private GD2DriveItem parent;
    private ConcurrentHashMap<String,GD2DriveItem> children = new ConcurrentHashMap<>();

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
}
