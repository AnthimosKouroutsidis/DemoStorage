package com.example.demostorage.DemoStorage;

import com.google.gson.Gson;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Storage {

    private final long maxCapacity;

    private static SessionFactory sessionFactory;

    public Storage(int capacity) {
        this.maxCapacity = capacity;

        StandardServiceRegistry ssr = new StandardServiceRegistryBuilder().configure().build();
        sessionFactory = new MetadataSources(ssr).buildMetadata().buildSessionFactory();
    }
    public Storage() {
        this(4);
    }

    public StorageItem getItem(String key) throws Exception {

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("FROM StorageItem WHERE item_key = :key");
        query.setParameter("key", key);
        List results = query.list();

        transaction.commit();
        session.close();

        if (results.size() > 0) {
            return (StorageItem) results.get(0);
        } else {
            throw new Exception("Storage with key " + key + " doesn't exist.");
        }
    }

    public boolean hasItem(String key) {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("FROM StorageItem WHERE item_key = :key");
        query.setParameter("key", key);
        List results = query.list();

        transaction.commit();
        session.close();

        return results.size() > 0;
    }

    public void setItem(String key, HashMap<String, Object> content) throws Exception {
        Gson gson = new Gson();
        String json = gson.toJson(content);
        StorageItem item = new StorageItem(key, json);
        long itemCapacityIncrement = item.getSize();

        boolean itemExists = false;
        if (hasItem(key)) {
            itemExists = true;
            // item exists already, so subtract its current size
            itemCapacityIncrement -= getItem(key).getSize();
        }

        long neededCapacity = getUsedCapacity() + itemCapacityIncrement;
        if (neededCapacity > this.maxCapacity) {
            throw new Exception("Not enough capacity to store the item (" + neededCapacity + " needed, " +
                    this.maxCapacity + " available).");
        }

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        if (itemExists) {
            Query query = session.createQuery("DELETE FROM StorageItem WHERE item_key = :key");
            query.setParameter("key", key);
            query.executeUpdate();
        }

        session.save(item);

        transaction.commit();
        session.close();
    }

    public long getVersion() {
        return 0; // in order to track changes persistently, an option would be to use database triggers
    }

    public List<String> getItemList() {
        List<String> items = new ArrayList<String>();

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("FROM StorageItem");
        List result = query.list();
        for ( StorageItem item : (List<StorageItem>) result ) {
            items.add(item.getKey());
        }

        transaction.commit();
        session.close();

        return items;
    }

    public void deleteItem(String key) {
        // free up the capacity of the item, if it exists
        StorageItem item;
        try {
            item = getItem(key);

        } catch (Exception e) {
            // item does not exist
            return;
        }

        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        session.delete(item);

        session.close();
        transaction.commit();
    }

    public long getAvailableCapacity() {
        return this.maxCapacity - getUsedCapacity();
    }

    private long getUsedCapacity() {
        Session session = sessionFactory.openSession();
        Transaction transaction = session.beginTransaction();

        Query query = session.createQuery("SELECT sum(size) FROM StorageItem");
        List result = query.list();
        transaction.commit();
        session.close();
        return (long) result.get(0);
    }
}
