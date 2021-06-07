package com.example.demostorage.DemoStorage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

/*
A StorageItem contains the data as well as the associated metadata
 */
public class StorageItem {

    private long id;
    private String key;
    private long size = 1; // (DEMO: the number of keys in the stored hashmap)
    private String content;

    public StorageItem(String key, String content) {
        this.key = key;
        this.content = content;

        // the "size" is the amount of attributes the data has (if it is a json)
        try {
            HashMap<String, Object> result = null;
            result = new ObjectMapper().readValue(content, HashMap.class);
            this.size = result.size();
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public StorageItem() {}

    public String getKey() {
        return key;
    }
    private void setKey(String key) {
        this.key = key;
    }

    public long getId() {
        return id;
    }
    private void setId(long id) {
        this.id = id;
    }

    public long getSize() {
        return size;
    }
    private void setSize(long size) {
        this.size = size;
    }

    public String getContent() {
        return content;
    }
    private void setContent(String content) {
        this.content = content;
    }
}
