package org.kendar.entities;

import org.kendar.GD2;
import org.kendar.utils.PathUtils;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.kendar.utils.PathUtils.cleanGooglePath;

public class GD2DriveItemUtils {
    public static GD2DriveItem findById(GD2DriveItem item,String id){
        if(item.getId().equals(id)) return item;
        for(GD2DriveItem child : item.children.values() ){
            GD2DriveItem partial = findById(child,id);
            if(partial!=null){
                return partial;
            }
        }
        return null;
    }

    public static GD2Path getPath(GD2DriveItem root,GD2DriveItem children){
        GD2DriveItem tmp = children;
        Path res = Paths.get("/");
        String result = new String();
        while(root!=tmp && tmp!=null){
            if(result.length()==0){
                result = tmp.getName();
            }else {
                result = tmp.getName() + PathUtils.SEPARATOR+ result;
            }
            tmp = tmp.getParent();
        }
        return GD2Path.get(result);
    }

    public static GD2DriveItem getRoot(GD2DriveItem item){
        while(item.getParent()!=null){
            item = item.getParent();
        }
        return item;
    }

    public static GD2DriveItem findByPath(GD2DriveItem item, GD2Path path){
        //path = cleanGooglePath(path);
        GD2DriveItem root = getRoot(item);
        if(path.isEmpty()) return root;
        return findByPathInternal(root, path, 0);
    }

    public static void sortDriveItems(HashMap<String, GD2DriveItem> items, GD2DriveItem root) {
        for(GD2DriveItem item :items.values()){
            if(item.getParentId()==null){
                item.setParent(root);
                root.addChild(item);
            }else if(items.containsKey(item.getParentId())){
                GD2DriveItem parent = items.get(item.getParentId());
                item.setParent(parent);
                parent.addChild(item);
            }
        }
    }

    private static GD2DriveItem findByPathInternal(
            GD2DriveItem root,
            GD2Path explodedPath,
            int index) {
        GD2DriveItem founded = null;
        for(GD2DriveItem child : root.children.values() ){
            if(child.getName().equalsIgnoreCase(explodedPath.getNameLocal(index))){
                if(explodedPath.getNameLocalCount()==(index+1)) {
                    return child;
                }
                founded = child;
                break;
            }
        }
        if(founded==null) return null;
        return findByPathInternal(founded,explodedPath,index+1);
    }
}
