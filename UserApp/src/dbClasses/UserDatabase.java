package dbClasses;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Singleton
@LocalBean
@Startup
public class UserDatabase {

    private String dbUri = "mongodb://Admin:admin@agenti1-shard-00-00-bkght.mongodb.net:27017,agenti1-shard-00-01-bkght.mongodb.net:27017,agenti1-shard-00-02-bkght.mongodb.net:27017/test?ssl=true&replicaSet=Agenti1-shard-0&authSource=admin";
    private MongoCollection collection;


    @PostConstruct
    private void setUp(){
        MongoClientURI uri = new MongoClientURI(dbUri);
        MongoClient mongoClient = new MongoClient(uri);
        MongoDatabase database = mongoClient.getDatabase("Agenti1");
        collection = database.getCollection("Users");
    }

    public MongoCollection getCollection(){
        return collection;
    }

}
