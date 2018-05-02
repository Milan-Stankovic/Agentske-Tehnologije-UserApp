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

            //TODO notify group participants about group creation           
                        
            ObjectMapper mapper = new ObjectMapper();
            String json = null;
            try {
                json = mapper.writeValueAsString(toCreate);
                groupDatabase.getCollection().insertOne(Document.parse(json));

                //TODO notify appropriate node for friendship request

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
            groupDatabase.getCollection().deleteOne(found);//check delete method on mongodb

            //TODO notify group participants about group disbandment

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

        if(found==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group not found.")).build();
        }else{
        	/*ObjectMapper mapper = new ObjectMapper();
            try {
            	ArrayList<User> temp = (ArrayList<User>)found.get("users");
            	temp.add(toAdd);
            	//found.put("users", temp);
                String json = mapper.writeValueAsString(temp);
                
                Document updateBSON = new Document();
                updateBSON.put("users", json);
                groupDatabase.getCollection().updateOne(found,new Document("$set", updateBSON));
            

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }*/
           
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
	
	        
	        if(found==null){
	            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Group not found.")).build();
	        }
        	else if(sendingId==userId||found.get("admin").equals(sendingId)){
	
	            Document updateBSON = new Document();
	            updateBSON.put("username", userId);

	            groupDatabase.getCollection().updateOne(//check whatever this is
	            		found,
	                    new Document("$pull", new Document("users", updateBSON))
	            );
	
	            return Response.status(Response.Status.OK).entity(found).build();//check if u need to return found or something else
	        }
        	else {
        		return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("No authoriy")).build();
        	}
    }





}
