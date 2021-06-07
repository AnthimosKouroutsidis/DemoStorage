package com.example.demostorage.DemoStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Storage {

    private final long maxCapacity;
    private final AtomicLong capacity;
    private final HashMap<String, StorageItem> storage;
    private final AtomicLong counter;

    public Storage(int capacity) {
        this.maxCapacity = capacity;
        this.storage = new HashMap<String, StorageItem>();

        this.counter = new AtomicLong();
        this.capacity = new AtomicLong();
    }
    public Storage() {
        this(4);
    }

    public StorageItem getItem(String key) throws Exception {
        if (storage.containsKey(key)) {
            return storage.get(key);
        } else {
            throw new Exception("Storage with key " + key + " doesn't exist.");
        }
    }

    public void setItem(String key, HashMap<String, Object> content) throws Exception {
        StorageItem item = new StorageItem(counter.incrementAndGet(), content);
        long itemCapacityIncrement = item.getSize();

        // item exists already, so subtract its current size
        if (storage.containsKey(key)) {
            itemCapacityIncrement -= storage.get(key).getSize();
        }

        long neededCapacity = this.capacity.get() + itemCapacityIncrement;
        if (neededCapacity > this.maxCapacity) {
            // hack: since the version counter gets incremented at the beginning of this function, we have to undo
            // that change
            counter.decrementAndGet();
            throw new Exception("Not enough capacity to store the item (" + neededCapacity + " needed, " +
                    this.maxCapacity + " available).");
        }

        this.capacity.addAndGet(itemCapacityIncrement);
        storage.put(key, item);
    }

    public long getVersion() {
        return counter.get();
    }

    public List<String> getItemList() {
        return new ArrayList<String>(storage.keySet());
    }

    public void deleteItem(String key) {
        // free up the capacity of the item, if it exists
        if (storage.containsKey(key)) {
            long itemCapacityIncrement = -storage.get(key).getSize();
            this.capacity.getAndAdd(itemCapacityIncrement);

            storage.remove(key);

            // the data has changed, so increment the version counter
            counter.incrementAndGet();
        }
    }

    public long getAvailableCapacity() {
        return this.maxCapacity - this.capacity.get();
    }
}
