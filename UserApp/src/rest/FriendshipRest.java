package rest;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dbClasses.FriendshipDatabase;
import dbClasses.UserDatabase;
import model.Friendship;
import rest.dto.ErrorDTO;

@LocalBean
@Path("/")
@Stateless
public class FriendshipRest implements FriendshipRestRemote {
    @Inject
    FriendshipDatabase friendDatabase;
    
    @Inject
    UserDatabase userDatabase;

    @POST
    @Path("friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFriendship(Friendship newFriendship)
    {
        Document searchBy = new Document();
        searchBy.append("sender", newFriendship.getSender());
        searchBy.append("reciever", newFriendship.getReciever());
        
        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", newFriendship.getSender())).first();
        Document foundReciver = (Document) userDatabase.getCollection().find(new Document("username", newFriendship.getReciever())).first();   
        
        if(found!=null||foundSender==null||foundReciver==null){
            return Response.status(Response.Status.CONFLICT).entity(new ErrorDTO("Friendship entry already exists. Or nonexistant users.")).build();
        }
        else{
            ObjectMapper mapper = new ObjectMapper();
            String json = null;
            try {
                json = mapper.writeValueAsString(newFriendship);
                friendDatabase.getCollection().insertOne(Document.parse(json));

                //notifying
                String hostIp = (String)foundReciver.get("hostIp");
                
                ResteasyClient client = new ResteasyClientBuilder().build();
				ResteasyWebTarget target = client.target(
						"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundReciver.get("username")+"/notifyFriendshipStart");
				Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(newFriendship, MediaType.APPLICATION_JSON));
                //notfying

                return Response.status(Response.Status.OK).entity(newFriendship).build();
            } catch (JsonProcessingException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO("Error while parsing JSON.")).build();
            }
        }

    }

    @POST
    @Path("friendship/delete")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFriendship(Friendship toDelete) {
        Document searchBy = new Document();
        searchBy.append("sender", toDelete.getSender());
        searchBy.append("reciever", toDelete.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", toDelete.getSender())).first();
        Document foundReciver = (Document) userDatabase.getCollection().find(new Document("username", toDelete.getReciever())).first();  

        if(found==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Friendship not found.")).build();
        }else{
            friendDatabase.getCollection().deleteOne(found);

            //notifying
            String hostIp = (String)foundReciver.get("hostIp");
            
            ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundReciver.get("username")+"/notifyFriendshipEnd");
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toDelete, MediaType.APPLICATION_JSON));
			
			hostIp = (String)foundSender.get("hostIp");
            
            ResteasyClient client1 = new ResteasyClientBuilder().build();
			ResteasyWebTarget target1 = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundSender.get("username")+"/notifyFriendshipEnd");
			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toDelete, MediaType.APPLICATION_JSON));
            //notifying

            return Response.status(Response.Status.OK).entity(toDelete).build();
        }

    }

    @PUT
    @Path("friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFriendshipStatus(Friendship toUpdate) {
        Document searchBy = new Document();
        searchBy.append("sender", toUpdate.getSender());
        searchBy.append("reciever", toUpdate.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", toUpdate.getSender())).first();
        
        if(found==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Friendship not found.")).build();
        }else{
            Document updateBSON = new Document();
            updateBSON.put("status", toUpdate.getStatus().toString());
            System.out.println("Updejtujem ga na: "+updateBSON);

            friendDatabase.getCollection().updateOne(found,new Document("$set", updateBSON));

            //notifying
            String hostIp = (String)foundSender.get("hostIp");
            
            ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundSender.get("username")+"/notifyFriendshipStateChange");
			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toUpdate, MediaType.APPLICATION_JSON));
            //notifying

            return Response.status(Response.Status.OK).entity(toUpdate).build();
        }

    }

}
