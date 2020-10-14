package net.mikej.objectstore.mongodb;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.mongodb.client.FindIterable;
import org.bson.Document;
import org.bson.types.Binary;
import org.mule.runtime.api.store.ObjectAlreadyExistsException;
import org.mule.runtime.api.store.ObjectDoesNotExistException;
import org.mule.runtime.api.store.ObjectStore;
import org.mule.runtime.api.store.ObjectStoreException;

import static org.mule.runtime.api.i18n.I18nMessageFactory.createStaticMessage;
import static org.apache.commons.lang.SerializationUtils.serialize;
import static org.apache.commons.lang.SerializationUtils.deserialize;

public class MongoObjectStore implements ObjectStore<Serializable> {

  private final MongoClientWrapper client;
  public MongoObjectStore(final MongoClientWrapper client) {
    this.client = client;
  }

  @Override
  public boolean contains(String key) throws ObjectStoreException {
    if (key == null) {
      throw new ObjectStoreException(createStaticMessage("The object store key can not be null"));
    }
    Document searchObject = new Document("_id", key);
    FindIterable<Document> findResult = client.getCollection().find(searchObject);
    Document element = findResult.first();
    return element != null;
  }

  @Override
  public void store(String key, Serializable value) throws ObjectStoreException {
    if (key == null) throw new ObjectStoreException(createStaticMessage("The object store key can not be null"));
    if (contains(key)) throw new ObjectAlreadyExistsException(createStaticMessage("Duplicated key %s", key));
    Document document = new Document();
    document.put("timestamp", System.currentTimeMillis());
    document.put("_id", key);
    document.put("value", serialize(value));
    client.getCollection().insertOne(document);
  }

  @Override
  public Serializable retrieve(String key) throws ObjectStoreException {
    if (key == null) throw new ObjectStoreException(createStaticMessage("The object store key can not be null"));
    Document searchObject = new Document("_id", key);
    FindIterable<Document> findResult = client.getCollection().find(searchObject);
    if (!findResult.iterator().hasNext()) throw new ObjectDoesNotExistException(createStaticMessage("Could not find key [%s] in the Object Store; retrieve failed", key));
    return (Serializable) deserialize(((Binary)findResult.first().get("value")).getData());
  }

  @Override
  public Serializable remove(String key) throws ObjectStoreException {
    if (key == null) throw new ObjectStoreException(createStaticMessage("The object store key can not be null"));
    Document searchObject = new Document("_id", key);
    FindIterable<Document> findResult = client.getCollection().find(searchObject);
    Document element = findResult.first();
    if (element == null) throw new ObjectDoesNotExistException(createStaticMessage("Could not find key [%s] in the Object Store; remove failed", key));
    Serializable result = (Serializable) deserialize(((Binary)element.get("value")).getData());
    client.getCollection().deleteOne(searchObject);
    return result;
  }

  @Override
  public boolean isPersistent() {
    return true;
  }

  @Override
  public void clear() throws ObjectStoreException {
    client.getCollection().deleteMany(new Document());
  }

  @Override
  public void open() throws ObjectStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public void close() throws ObjectStoreException {
    // TODO Auto-generated method stub

  }

  @Override
  public List allKeys() throws ObjectStoreException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Map retrieveAll() throws ObjectStoreException {
    // TODO Auto-generated method stub
    return null;
  }
}