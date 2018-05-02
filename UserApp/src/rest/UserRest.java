package rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import model.User;
import rest.dto.ErrorDTO;
import dbClasses.UserDatabase;
import org.bson.Document;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.ArrayList;

@LocalBean
@Path("/users")
@Stateful
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
    
    
   /* 
    @PUT
    @Path("/addActive/{userName}/ip/{ip}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addActive(@PathParam("userName") String userName,@PathParam("ip")  String ip){ 
    	String returnMessage="";
    
    	 Document found = (Document) userDatabase.getCollection().find(new Document("username", userName));
    	 if(found != null) {
    		  Gson gson = new Gson();
    	      User person = gson.fromJson(found.toJson(), User.class);   
    	      
    	      boolean active= false;
    	      for (User user2 : activeUsers) {
				if(user2.getUsername().equals(person.getUsername())) {
					user2.setHostIp(ip);
					active=true;
				}
					
			}
    	      if(!active) {
    	    	  
    	    	  activeUsers.add(person);
    	      }
    	      returnMessage="ACTIVE";
    	      
    	        
    	 }else {
    		 returnMessage="NOUSER";
    	 }
    	
    	
    	return returnMessage;
    }
    
    */
    
    
    @GET
    @Path("/getActive")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getActive(){
    	
    	
    	 if(activeUsers.isEmpty())
             return Response.status(Response.Status.NOT_FOUND).entity(new ErrorDTO("No users found.")).build();
         
             return Response.status(Response.Status.OK).entity(activeUsers).build();
         
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
				
				Response response = target.request().post(Entity.entity(activeUsers, "application/vnd.com.demo.user-management.user+xml;charset=UTF-8;version=1"));
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
        						"http://" + temp.getHostIp() + ":8096/ChatApp/users/removeActive/"+temp.getUsername() +"/ip/"+user.getHostIp());

        				Response response = target.request(MediaType.APPLICATION_JSON).delete();
            			String alive = response.readEntity(String.class);
            
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
