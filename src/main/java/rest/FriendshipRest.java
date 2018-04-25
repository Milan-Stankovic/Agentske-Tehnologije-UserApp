package rest;

import dbClasses.FriendshipDatabase;
import model.Friendship;
import org.bson.Document;
import rest.dto.ErrorDTO;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@LocalBean
@Stateless
public class FriendshipRest implements FriendshipRestRemote {
    @Inject
    FriendshipDatabase friendDatabase;

    @POST
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFriendship(Friendship newFriendship)
    {
        Document searchBy = new Document();
        searchBy.append("sender", newFriendship.getSender());
        searchBy.append("reciever", newFriendship.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();

        if(found!=null){
            return Response.status(Response.Status.CONFLICT).entity(new ErrorDTO("Friendship entry already exists.")).build();
        }
        else{
            ObjectMapper mapper = new ObjectMapper();
            String json = null;
            try {
                json = mapper.writeValueAsString(newFriendship);
                friendDatabase.getCollection().insertOne(Document.parse(json));

                //TODO notify appropriate node for friendship request

                return Response.status(Response.Status.OK).entity(newFriendship).build();
            } catch (JsonProcessingException e) {
                return Response.status(Response.Status.BAD_REQUEST).entity(new ErrorDTO("Error while parsing JSON.")).build();
            }
        }

    }

    @DELETE
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteFriendship(Friendship toDelete) {
        Document searchBy = new Document();
        searchBy.append("sender", toDelete.getSender());
        searchBy.append("reciever", toDelete.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();

        if(found==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Friendship not found.")).build();
        }else{
            friendDatabase.getCollection().deleteOne(found);

            //TODO notify appropriate node for friendship withdrawal

            return Response.status(Response.Status.OK).entity(toDelete).build();
        }

    }

    @PUT
    @Path("/friendship")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateFriendshipStatus(Friendship toUpdate) {
        Document searchBy = new Document();
        searchBy.append("sender", toUpdate.getSender());
        searchBy.append("reciever", toUpdate.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();

        if(found==null){
            return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("Friendship not found.")).build();
        }else{
            Document updateBSON = new Document();
            updateBSON.put("status", toUpdate.getStatus());

            friendDatabase.getCollection().updateOne(found,new Document("$set", updateBSON));

            //TODO notify appropriate node for friendship acceptance

            return Response.status(Response.Status.OK).entity(toUpdate).build();
        }

    }

}
