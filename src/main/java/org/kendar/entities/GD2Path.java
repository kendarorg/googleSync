package org.kendar.entities;

import org.apache.commons.lang3.SystemUtils;
import org.kendar.utils.PathUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GD2Path {
    private List<String> root = new ArrayList<>();
    private List<String> local = new ArrayList<>();
    private String parent;
    private String fileName;
    private boolean absolute =false;


    public static GD2Path get(String root,String child){
        return get(Paths.get(root),Paths.get(child));
    }

    public static GD2Path get(String root){
        return get(Paths.get(root));
    }


    public static GD2Path get(Path root,Path child){
        GD2Path path = new GD2Path();
        if(root.toString().length()>0) {
            fillRoot(root, path);
        }
        fillLocal(child, path);
        path.absolute = root.isAbsolute();
        return path;
    }

    public static GD2Path get(Path root){
        GD2Path path = new GD2Path();
        if(root.toString().length()>0) {
            fillLocal(root, path);
        }
        path.absolute = root.isAbsolute();
        return path;
    }


    private static void fillLocal(Path child, GD2Path path) {
        for(int i=0;i<child.getNameCount();i++){
            path.local.add(child.getName(i).toString());
        }
    }

    private static void fillRoot(Path root, GD2Path path) {
        if(!SystemUtils.IS_OS_UNIX){
            if(root.isAbsolute()){
                path.root.add(root.getRoot().toString());
            }
        }
        for(int i=0;i<root.getNameCount();i++){
            path.root.add(root.getName(i).toString());
        }
    }

    public String getParent() {
        GD2Path path = new GD2Path();

        cloneStringArray(path.root,root,9999);
        cloneStringArray(path.local,local,local.size()-1);
        return parent;
    }

    private void cloneStringArray(List<String> dst, List<String> src,int upTo) {
        for(int i=0;i<upTo && i<src.size();i++){
            dst.add(src.get(i)+"");
        }
    }

    public String getFileName() {
        if(local.size()>0) return local.get(local.size()-1);
        if(root.size()>0) return root.get(root.size()-1);
        return "";
    }

    public Path toPath(){
        return Paths.get(this.toString());
    }

    @Override
    public String toString(){
        String roots = String.join(PathUtils.SEPARATOR,root);
        String locals = String.join(PathUtils.SEPARATOR,local);
        if(locals.length()>0 && roots.length()>0){
            return roots+PathUtils.SEPARATOR+locals;
        }else if(locals.length()>0){
            return locals;
        }else{
            return roots;
        }
    }


    public boolean isEmpty() {
        return root.isEmpty() && local.isEmpty();
    }

    public String getNameLocal(int index){
        return local.get(index);
    }
    public int getNameLocalCount(){
        return local.size();
    }
}
