package rest;

import dbClasses.GroupDatabase;
import model.Group;
import model.User;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@LocalBean
@Path("/users")
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
    public Group createGroup(Group toCreate) {
        return null;
    }

    @DELETE
    @Path("/group")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group deleteGroup(Group toDelete) {
        return null;
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
