package com.example.demostorage.DemoStorage;

import java.util.HashMap;

/*
A StorageItem contains the data as well as the associated metadata
 */
public class StorageItem {

    private final long id;
    private final long size; // (DEMO: the number of keys in the stored hashmap)
    private final HashMap<String, Object> content;

    public StorageItem(long id, HashMap<String, Object> content) {
        this.id = id;
        this.content = content;
        this.size = content.size();
    }

    public long getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public HashMap<String, Object> getContent() {
        return content;
    }
}
