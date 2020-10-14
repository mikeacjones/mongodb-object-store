package net.mikej.objectstore.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import org.bson.Document;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import com.mulesoft.connectors.commons.template.connection.provider.ConnectorConnectionProvider;
import org.mule.runtime.extension.api.annotation.param.stereotype.Stereotype;

@DisplayName("MongoObjectStore")
@Stereotype(ObjectStoreConnectionStereotype.class)
public class MongoObjectStoreConnectionProvider implements CachedConnectionProvider<MongoClientWrapper>, ConnectorConnectionProvider<MongoClientWrapper> {

    @Parameter
    @DisplayName("Connection URI")
    public String connectionString;

    @Parameter
    @DisplayName("Database")
    public String database;

    @Parameter
    @DisplayName("Collection Name")
    public String collectionName;

    @Override
    public MongoClientWrapper connect() throws ConnectionException {
        try {
            final MongoClientURI mongoClientURI = new MongoClientURI(connectionString);
            if (database != null) {
                return new MongoClientWrapper(MongoClients.create(connectionString), database, collectionName);
            } else {
                throw new MongoException("Could not retrieve database name");
            }
        } catch (IllegalArgumentException e) {
            throw new ConnectionException(e);
        }
    }

    @Override
    public void disconnect(MongoClientWrapper connection) {
        connection.disconnect();
    }

    @Override
    public ConnectionValidationResult validate(MongoClientWrapper connection) {
        try {
            Document result = connection.getDatabase().runCommand(new BasicDBObject("ping", "1"));
            if (result.containsKey("ok") && Double.parseDouble(result.get("ok").toString()) == 1.0) {
                connection.getDatabase().getCollection(collectionName).countDocuments();
                return ConnectionValidationResult.success();
            }
        } catch (Exception e) {
            return ConnectionValidationResult.failure("Failed to validate connection", e);
        }
        return ConnectionValidationResult.failure("Failed to validate connection", null);
    }

}