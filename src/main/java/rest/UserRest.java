package rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;
import model.User;
import model.UserDatabase;
import org.bson.Document;
import org.jboss.resteasy.annotations.Form;

import javax.ejb.LocalBean;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ejb.Stateless;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;

@LocalBean
@Path("/users")
@Stateless
public class UserRest implements UserRestRemote {

    @Inject
    public UserDatabase userDatabase;


    private ArrayList<User> activeUsers = new ArrayList<User>();

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String Test(){


        return "TEST!";
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String login(User user){
        String s ="";
        Document found = (Document) userDatabase.getCollection().find(new Document("username", user.getUsername())).first();
        if(found !=null){
            for (User temp: activeUsers) {
                if(temp.getUsername().equals(user.getUsername())){
                    s="ALLREADYIN";
                }
            }
            if(s.equals("")){
                activeUsers.add(user);
                s="LOGGEDIN";

                //PROSLEDI SVIM CHATAPPOVIMA
            }

        }
        return s;

    }


    @POST
    @Path("/logout")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String logout(User user){
        String s ="";
        Document found = (Document) userDatabase.getCollection().find(new Document("username", user.getUsername())).first();
        if(found !=null){
            for (User temp: activeUsers) {
                if(temp.getUsername().equals(user.getUsername())){
                    activeUsers.remove(user);
                    s="LOGGEDOUT";
                    //PROSLEDIM SVIM CHATAPPOVIMA
                    break;
                }
            }
            if(s.equals("")){
                s="NOTINLIST";
            }

        }
        return s;

    }



    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String register(User user) {
        String s = "";
        Document found = (Document) userDatabase.getCollection().find(new Document("username", user.getUsername())).first();
        if(found == null){
            ObjectMapper mapper = new ObjectMapper();
            try {
                String json = mapper.writeValueAsString(user);

                userDatabase.getCollection().insertOne(Document.parse(json));
                s="ADDED";
            } catch (JsonProcessingException e) {
                e.printStackTrace();
                s="ERROR";
            }
            // userDatabase.getCollection().insertOne();


        }else{
           s="EXISTING";
        }
        return s;

    }


}
