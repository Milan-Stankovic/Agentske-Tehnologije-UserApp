package rest;

import java.util.ArrayList;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dbClasses.GroupDatabase;
import dbClasses.UserDatabase;
import model.Group;
import model.User;
import rest.dto.ErrorDTO;

@LocalBean
@Path("/groups")
@Stateless
public class GroupRest implements GroupRestRemote {

    @Inject
    GroupDatabase groupDatabase;
    
    @Inject
    UserDatabase userDatabase;

    @GET
    @Path("/group/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getGroup(@PathParam("groupId") String groupId) {
        Document searchBy = new Document();
        searchBy.put("id", groupId);

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        if(found == null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group not found.")).build();
        }else{
            return Response.status(Response.Status.OK).entity(found).build();
        }
    }

    @POST
    @Path("/group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(Group toCreate) {
        Document searchBy = new Document();
        searchBy.put("id", toCreate.getId());

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        Document foundAdmin = (Document) userDatabase.getCollection().find(new Document("username", toCreate.getAdmin())).first();
        if(found != null||foundAdmin==null){
            return Response.status(Response.Status.CONFLICT).entity(new ErrorDTO("Group already exists. Or admin doesnt exist.")).build();
        }else{                             
            ObjectMapper mapper = new ObjectMapper();
            String json = null;
            try {
                json = mapper.writeValueAsString(toCreate);
                groupDatabase.getCollection().insertOne(Document.parse(json));
                //notifying
                for(User u:toCreate.getUsers()) {
                	String hostIp = u.getHostIp();
                    
                    ResteasyClient client = new ResteasyClientBuilder().build();
        			ResteasyWebTarget target = client.target(
        					"http://" + hostIp + ":8096/ChatApp/notify/"+u.getUsername()+"/notifyNewGroup");
        			Response response1 = target.request(MediaType.APPLICATION_JSON).get();
                }
                //notifying
                return Response.status(Response.Status.OK).entity(toCreate).build();
            } catch (JsonProcessingException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO("Error while parsing JSON.")).build();
            }
        }
    }

    @DELETE
    @Path("/group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteGroup(Group toDelete) {
        Document searchBy = new Document();
        searchBy.put("id", toDelete.getId());

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        if(found == null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group not found.")).build();
        }else{
            groupDatabase.getCollection().deleteOne(found);

            for(User u:toDelete.getUsers()) {
            	String hostIp = u.getHostIp();
                
                ResteasyClient client = new ResteasyClientBuilder().build();
    			ResteasyWebTarget target = client.target(
    					"http://" + hostIp + ":8096/ChatApp/notify/"+u.getUsername()+"/notifyEndGroup");
    			Response response1 = target.request(MediaType.APPLICATION_JSON).get();
            }

            return Response.status(Response.Status.OK).entity(toDelete).build();
        }

    }

    @POST
    @Path("/group/{groupId}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(@PathParam("groupId") String groupId,User toAdd) {
        Document searchBy = new Document();
        searchBy.put("id", groupId);

        Document found = (Document) groupDatabase.getCollection().find(searchBy).first();
        
        Document foundUser = (Document) userDatabase.getCollection().find(new Document("username", toAdd.getUsername())).first();

        if(found==null||foundUser==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group or user not found.")).build();
        }else{          
        	Document updateBSON = new Document();
            updateBSON.put("password", toAdd.getPassword());
            updateBSON.put("username", toAdd.getUsername());
            updateBSON.put("name", toAdd.getName());
            updateBSON.put("lastName", toAdd.getLastName());
            updateBSON.put("hostIp", toAdd.getHostIp());
            groupDatabase.getCollection().updateOne(//check whatever this is
            		found,
                    new Document("$push", new Document("users", updateBSON))
            );
            
            for(User u:(ArrayList<User>)found.get("users")) {
            	String hostIp = u.getHostIp();
                
                ResteasyClient client = new ResteasyClientBuilder().build();
    			ResteasyWebTarget target = client.target(
    					"http://" + hostIp + ":8096/ChatApp/notify/"+u.getUsername()+"/notifyNewGroupMember");
    			Response response1 = target.request(MediaType.APPLICATION_JSON).get();
            }
            
            String hostIp = toAdd.getHostIp();
            
            ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+toAdd.getUsername()+"/notifyNewGroupMember");
			Response response1 = target.request(MediaType.APPLICATION_JSON).get();

            //TODO notify other users about user that left

            return Response.status(Response.Status.OK).entity(found).build();//check if u need to return found or something else
        }
    }

    @DELETE
    @Path("/group/{groupId}/users/{userId}/sender/{sendingId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeUser(@PathParam("groupId") String groupId, @PathParam("userId") String userId, @PathParam("sendingId") String sendingId) {
    		Document searchBy = new Document();
	        searchBy.put("id", groupId);
	
	        Document found = (Document) groupDatabase.getCollection().find(searchBy).first();
	        Document foundUser = (Document) userDatabase.getCollection().find(new Document("username", userId)).first();
	        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", sendingId)).first();
	        
	        if(found==null||foundSender==null||foundUser==null){
	            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group, user or admin not found.")).build();
	        }
        	else if(sendingId==userId||found.get("admin").equals(sendingId)){
	
	            Document updateBSON = new Document();
	            updateBSON.put("username", userId);

	            groupDatabase.getCollection().updateOne(
	            		found,
	                    new Document("$pull", new Document("users", updateBSON))
	            );
	            
	            //notifying
                for(User u:(ArrayList<User>)found.get("users")) {
                	String hostIp = u.getHostIp();
                    
                    ResteasyClient client = new ResteasyClientBuilder().build();
        			ResteasyWebTarget target = client.target(
        					"http://" + hostIp + ":8096/ChatApp/notify/"+u.getUsername()+"/notifyRemovedUser");
        			Response response1 = target.request(MediaType.APPLICATION_JSON).get();
                }
                //notifying
	
	            return Response.status(Response.Status.OK).entity(found).build();
	        }
        	else {
        		return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("No authoriy")).build();
        	}
    }





}
