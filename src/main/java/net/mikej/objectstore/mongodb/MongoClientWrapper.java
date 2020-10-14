package net.mikej.objectstore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mulesoft.connectors.commons.template.connection.ConnectorConnection;
import org.bson.Document;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;
import org.mule.runtime.api.store.ObjectStoreManager;
import org.mule.runtime.api.store.ObjectStoreSettings;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class MongoClientWrapper implements ObjectStoreManager, ConnectorConnection {
    final MongoClient client;
    final MongoDatabase database;
    final MongoCollection collection;
    final Map<String, MongoObjectStore> objectStores = new HashMap<>();
    final Object lock = new Object();

    public MongoClientWrapper(final MongoClient client, final String database, final String collectionName) {
        this.client = client;
        this.database = client.getDatabase(database);
        this.collection = this.database.getCollection(collectionName);
    }

    public void disconnect() {
        client.close();
    }

    @Override
    public void validate() {
        Document result = this.getDatabase().runCommand(new BasicDBObject("ping", "1"));
        if (result.containsKey("ok") && Double.parseDouble(result.get("ok").toString()) == 1.0) {
            long documentCount = this.getCollection().countDocuments();
            return;
        }
    }

    public MongoClient getClient() {
        return client;
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoCollection getCollection() { return collection; }

    @Override
    public ObjectStore<Serializable> getObjectStore(String name) {
        if (!objectStores.containsKey(name)) throw new NoSuchElementException();
        return objectStores.get(name);
    }

    @Override
    public ObjectStore<Serializable> createObjectStore(String name, ObjectStoreSettings settings) {
        MongoObjectStore store = new MongoObjectStore(this);
        objectStores.put(name, store);
        return store;
    }

    @Override
    public ObjectStore<Serializable> getOrCreateObjectStore(String name, ObjectStoreSettings settings) {
        try {
            return getObjectStore(name);
        } catch (NoSuchElementException e) {
            return createObjectStore(name, settings);
        }
    }

    @Override
    public void disposeStore(String name) throws ObjectStoreException {
        if (objectStores.containsKey(name)) objectStores.remove(name);
    }
}
