package net.mikej.objectstore.mongodb;

import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;

@Xml(prefix = "mongodb-object-store")
@Extension(name = "MongoDB Object Store")
@ConnectionProviders({ MongoObjectStoreConnectionProvider.class })
public class MongoObjectStoreExtension {
}