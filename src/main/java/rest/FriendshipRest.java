package rest;

import dbClasses.FriendshipDatabase;
import model.Friendship;
import model.Group;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public class FriendshipRest implements FriendshipRestRemote {
    @Inject
    FriendshipDatabase friendDatabase;

    @POST
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group createFriendship(Friendship newFriendship) {
        return null;
    }

    @DELETE
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group deleteFriendship(Friendship toDelete) {
        return null;
    }

    @PUT
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Group updateFriendshipStatus(Friendship toUpdate) {
        return null;
    }

}
