package rest;

import java.util.ArrayList;
import java.util.Arrays;

import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import com.google.gson.Gson;
import com.mongodb.client.FindIterable;

import dbClasses.FriendshipDatabase;
import dbClasses.UserDatabase;
import model.Friendship;
import model.FriendshipStatus;
import model.User;
import rest.dto.ErrorDTO;

@LocalBean
@Path("/users")
@Singleton
public class UserRest implements UserRestRemote {

    @Inject
    public UserDatabase userDatabase;


    private ArrayList<User> activeUsers = new ArrayList<User>();
    
    @Inject 
    private FriendshipDatabase friendDb;
    
    

    public ArrayList<User> getActiveUsers() {
		return activeUsers;
	}


	@GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String Test(){


        return "TEST!";
    }
      
    
    @GET
    @Path("/getActive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getActive(){
    	
    	
    	 if(activeUsers.isEmpty())
             return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("No users found.")).build();
         
             return Response.status(Response.Status.OK).entity(activeUsers).build();
         
    }
    
    
    private void alertFriends(String userName, boolean remove) {
		System.out.println("ALERTUJEM!!!!");
	
		
		Document or1 = new Document(
		  		  "$and", Arrays.asList(
		  		    new Document("sender", userName),
		  		    new Document("status", FriendshipStatus.ACCEPTED.toString())
		  		  )
		  		);
		  	
		  	
			Document or2 = new Document(
			  		  "$and", Arrays.asList(
			  		    new Document("reciever", userName),
			  		    new Document("status", FriendshipStatus.ACCEPTED.toString())
			  		  )
			  		);
			
			Document query = new Document(
			  		  "$or", Arrays.asList(
			  		  or1,
			  		  or2
			  		  )
			  		);
			
			System.out.println(query);
			
			Gson gson= new Gson();
			 @SuppressWarnings("unchecked")
	  	     FindIterable<Document> docs = friendDb.getCollection().find(query);//valjda sortira .sort(new Document("created_at",1))
  	     for (Document doc : docs) {
  	    	 System.out.println("Pronasao prijatelja!!!");
	      Friendship friend = gson.fromJson(doc.toJson(), Friendship.class);
	      for (User u : activeUsers) {
	    	  System.out.println("Ima aktivnih!!");
	    	  System.out.println("RECI : " +friend.getReciever());
	    	  System.out.println("SEND : " +friend.getSender());
	    	  System.out.println("KORISNIK : "+u.getUsername());
	    	  
	    	  if(!u.getUsername().equals(userName)) {
	    	  if( u.getUsername().equals(friend.getReciever()) ||u.getUsername().equals(friend.getSender()) ) {
	    		  System.out.println("I CAK IH UPISUJEM, ZAMISLI");
	    		  String tip = "LOGIN";
	    		  
	    		  if(remove)
	    			  tip="LOGOUT";
	    		  
	    		  ResteasyClient client = new ResteasyClientBuilder().build();
					System.out.println(
							"http://" + u.getHostIp() + ":8096/ChatApp/rest/users/notifyFriend/"+userName +"/firend/"+u.getUsername()+"/"+tip);
					ResteasyWebTarget target = client.target(
							"http://" + u.getHostIp() + ":8096/ChatApp/rest/users/notifyFriend/"+userName +"/firend/"+u.getUsername()+"/"+tip);
					
					
					Response response = target.request().get();
	    		  
	    		  
	    		  
	    	  	}
	    	  }
			
	      }
  	    }

		
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
                    temp.setHostIp(user.getHostIp());
                }
            }
            System.out.println(s+"SRAMOTA JEBENA");
            if(s.equals("")){
            	
            	if(!activeUsers.isEmpty()) {
            		for (User temp : activeUsers) {
						
            			ResteasyClient client = new ResteasyClientBuilder().build();
        				
        				ResteasyWebTarget target = client.target(
        						"http://" + temp.getHostIp() + ":8096/ChatApp/users/addActive/"+temp.getUsername() +"/ip/"+user.getHostIp());
            			
        				Response response = target.request(MediaType.APPLICATION_JSON).get();
            			String alive = response.readEntity(String.class);
            		/*	if(alive!=null & alive != "") {
            				System.out.println("Ziv je nod : " + temp.getHost().getAddress());
            			}else {
            				for (User brisi : activeUsers) {
								if(brisi.getHost().getAddress().equals(temp.getHost().getAddress()))
									activeUsers.remove(brisi);
							}
            			} Znam da treba iterator :D 
            			*/	
					}
            	}
            	
                activeUsers.add(user);
                
                
                ResteasyClient client = new ResteasyClientBuilder().build();
				
				ResteasyWebTarget target = client.target(
						"http://" + user.getHostIp() + ":8096/ChatApp/users/addAllUsers/");
				
				Response response = target.request().post(Entity.entity(activeUsers, MediaType.APPLICATION_JSON));
				
				System.out.println("Pokrecem alert na prijatelje!!!");
				alertFriends(user.getUsername(), false);
			// Nemam pojma da li ce ono raditi xD
                
                s="LOGGEDIN";
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
                    for (User temp2 : activeUsers) {
						
            			ResteasyClient client = new ResteasyClientBuilder().build();
        				
        				ResteasyWebTarget target = client.target(
        						"http://" + temp.getHostIp() + ":8096/ChatApp/users/removeActive/"+temp.getUsername());

        				Response response = target.request(MediaType.APPLICATION_JSON).delete();
            			String alive = response.readEntity(String.class);
            			
            			alertFriends(user.getUsername(), true);
            
					}
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
    	
    	System.out.println("PA JA SAM ZAPRAVO U USERAPPU");
    	
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
