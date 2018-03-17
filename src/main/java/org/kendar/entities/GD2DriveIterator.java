package org.kendar.entities;

import org.kendar.utils.GD2Consumer;
import org.kendar.utils.GD2Exception;

import java.util.*;

public class GD2DriveIterator {
    private GD2DriveItem root;
    private Stack<Iterator<GD2DriveItem>> indexes = new Stack<>();
    private HashSet<String> uuids = new HashSet<>();
    private GD2DriveItem current;
    boolean first = true;
    boolean last = false;
    private GD2Consumer<GD2DriveItem> callback=null;

    public GD2DriveIterator(GD2DriveItem root){
        this.root = root;
    }


    public List<GD2DriveItem> toList() throws GD2Exception {
        List<GD2DriveItem> items = new ArrayList<>();
        while(moveNext()){
            items.add(getCurrent());
        }
        return items;
    }

    public GD2DriveItem getCurrent(){
        return current;
    }
    public void reset(){
        indexes = new Stack<>();
        first = true;
        last = false;
        uuids.clear();
    }
    public boolean moveNext() throws GD2Exception {
        if(last)return false;
        if(first){
            depthFirstInitialize(root);
            first = false;
        }
        Iterator<GD2DriveItem> index = null;
        if(indexes.size()>0) {
            index = indexes.peek();
        }

        while(index!=null && !index.hasNext()) {
            indexes.pop();
            if(indexes.size()>0) {
                index = indexes.peek();
            }else{
                index = null;
            }
        }
        if(index==null){
            last = true;
            current = root;
            callCallback(current);
            return true;
        }
        current = index.next();
        callCallback(current);
        depthFirstInitialize(current);
        return true;
    }

    private void callCallback(GD2DriveItem current) throws GD2Exception {
        if(callback!=null){
            try {
                callback.run(current);
            }catch(GD2Exception ex){
                throw ex;
            }catch(Exception es){
                throw new GD2Exception("XX1",es);
            }
        }
    }

    private void depthFirstInitialize(GD2DriveItem tmp) {
        if(uuids.contains(tmp.getId())) return;
        uuids.add(tmp.getId());
        if(tmp.children.size()>0){
            indexes.push(tmp.children.values().iterator());
            for(GD2DriveItem item : tmp.children.values()){
                depthFirstInitialize(item);
                break;
            }
        }
    }

    public void setCallback(GD2Consumer<GD2DriveItem> callback) {
        this.callback = callback;
    }
}
