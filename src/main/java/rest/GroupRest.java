package rest;

import dbClasses.GroupDatabase;
import model.Group;
import model.User;
import org.bson.Document;
import rest.dto.ErrorDTO;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@LocalBean
@Stateless
public class GroupRest implements GroupRestRemote {

    @Inject
    GroupDatabase groupDatabase;

    @GET
    @Path("/group/{groupId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group getGroup(@PathParam("groupId") String groupId) {
        return null;
    }

    @POST
    @Path("/group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createGroup(Group toCreate) {
        Document searchBy = new Document();
        searchBy.put("id", toCreate.getId());

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        if(found != null){
            return Response.status(Response.Status.CONFLICT).entity(new ErrorDTO("Group already exists.")).build();
        }else{
            groupDatabase.getCollection().insertOne(toCreate);

            //TODO notify group participants about group creation

            return Response.status(Response.Status.OK).entity(toCreate).build();
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

            //TODO notify group participants about group disbandment

            return Response.status(Response.Status.OK).entity(toDelete).build();
        }

    }

    @POST
    @Path("/group/{groupId}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group addUser(@PathParam("groupId") String groupId,User toAdd) {
        return null;
    }

    @DELETE
    @Path("/group/{groupId}/users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group removeUser(@PathParam("groupId") String groupId, @PathParam("userId") String userId) {
        return null;
    }





}
