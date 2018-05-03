package jms;

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.ws.rs.PathParam;
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
import dbClasses.GroupDatabase;
import dbClasses.UserDatabase;
import model.Friendship;
import model.Group;
import model.User;
import rest.UserRest;
import rest.dto.ErrorDTO;


@MessageDriven(activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:jboss/exported/jms/queue/mojQueue") })

public class PrimalacQueueMDB implements MessageListener {

	Logger log = Logger.getLogger("Primalac MDB");
	
	@Inject
	private FriendshipDatabase friendDatabase;
	
	@Inject
	private UserDatabase userDatabase;
	
	@Inject
	private GroupDatabase groupDatabase;
	
	@Inject
	private UserRest userRest;


	public void onMessage(Message msg) {
		log.info("UserApp - Primio poruku!!!");
		ObjectMessage omsg = (ObjectMessage) msg;
		try {
			jmsDTO aclMessage = (jmsDTO) omsg.getObject();
			
			switch (aclMessage.getStatus()) {
			case NEW_FRIENDSHIP:
				
				newFriendships((Friendship)aclMessage.getContent());
				break;
			case DELETE_FRIENDSHIP:
				System.out.println("Usao sam u DELETEFRIENDSHIP!!!");
				deleteFriendship((Friendship)aclMessage.getContent());	
				break;
			case PUT_FRIENDSHIP:
				updateFriendshipStatus((Friendship)aclMessage.getContent());
				break;
			case GET_GROUP:
				getGroup((String)aclMessage.getContent());
				break;
			case NEW_GROUP:
				createGroup((Group)aclMessage.getContent());
				break;
			case DELETE_GROUP:
				deleteGroup((Group)aclMessage.getContent());
				break;
			case ADD_USER:
				addUser(aclMessage.getInfo(), (User)aclMessage.getContent());
				break;
			case REMOVE_USER_GROUP:
				String[] niz = ((String)aclMessage.getContent()).split("----");
				removeUser(aclMessage.getInfo(), niz[0], niz[1]);
				break;
			case LOGIN:
				
				break;

			default:
				break;
			}
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void newFriendships(Friendship newFriendship) {
		Document searchBy = new Document();
        searchBy.append("sender", newFriendship.getSender());
        searchBy.append("reciever", newFriendship.getReciever());
        
        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", newFriendship.getSender())).first();
        Document foundReciver = (Document) userDatabase.getCollection().find(new Document("username", newFriendship.getReciever())).first();   
        
        if(found!=null||foundSender==null||foundReciver==null){
            new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Friendship already exists."));
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
				
				new JMSQueue(new jmsDTO("ADDED", JMSStatus.NEW_FRIENDSHIP, newFriendship));
            } catch (JsonProcessingException e) {
            	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "JSON Parse error."));
            }
        }
	}
	
	public void deleteFriendship(Friendship toDelete) {
        Document searchBy = new Document();
        searchBy.append("sender", toDelete.getSender());
        searchBy.append("reciever", toDelete.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", toDelete.getSender())).first();
        Document foundReciver = (Document) userDatabase.getCollection().find(new Document("username", toDelete.getReciever())).first();  

        if(found==null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Friendship not found."));
        }else{
            friendDatabase.getCollection().deleteOne(found);

            //notifying
            String hostIp = (String)foundReciver.get("hostIp");
            
            ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundReciver.get("username")+"/notifyFriendshipEnd");
			
			System.out.println("Prva IP adresa: "+
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundReciver.get("username")+"/notifyFriendshipEnd");
			
			Response response = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toDelete, MediaType.APPLICATION_JSON));
			
			hostIp = (String)foundSender.get("hostIp");
			
            ResteasyClient client1 = new ResteasyClientBuilder().build();
			ResteasyWebTarget target1 = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundSender.get("username")+"/notifyFriendshipEnd");
			
			System.out.println("Prva IP adresa: http://" + hostIp + ":8096/ChatApp/notify/"+(String)foundSender.get("username")+"/notifyFriendshipEnd");
			
			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toDelete, MediaType.APPLICATION_JSON));
			

			
            //notifying

			new JMSQueue(new jmsDTO("DELETED", JMSStatus.DELETE_FRIENDSHIP, toDelete));
        }

    }
	
	public void updateFriendshipStatus(Friendship toUpdate) {
        Document searchBy = new Document();
        searchBy.append("sender", toUpdate.getSender());
        searchBy.append("reciever", toUpdate.getReciever());

        Document found = (Document) friendDatabase.getCollection().find(searchBy).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", toUpdate.getSender())).first();
        
        if(found==null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Friendship not found."));
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

			new JMSQueue(new jmsDTO("UPDATED", JMSStatus.DELETE_FRIENDSHIP, toUpdate));
        }

    }
	
	public void getGroup(String groupId) {
        Document searchBy = new Document();
        searchBy.put("id", groupId);

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        if(found == null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Group not found."));
        }else{
        	new JMSQueue(new jmsDTO("TAKEN", JMSStatus.GET_GROUP, found));
        }
    }
	
	public void createGroup(Group toCreate) {
        Document searchBy = new Document();
        searchBy.put("id", toCreate.getId());

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        Document foundAdmin = (Document) userDatabase.getCollection().find(new Document("username", toCreate.getAdmin())).first();
        if(found != null||foundAdmin==null){
            new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Group already exists. Or admin doesnt exist."));
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
        			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toCreate, MediaType.APPLICATION_JSON));
                }
                //notifying
                new JMSQueue(new jmsDTO("ADDED", JMSStatus.NEW_GROUP, toCreate));
            } catch (JsonProcessingException e) {
                new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Error while parsing JSON."));
            }
        }
	}
	
	public void deleteGroup(Group toDelete) {
        Document searchBy = new Document();
        searchBy.put("id", toDelete.getId());

        Document found = (Document)groupDatabase.getCollection().find(searchBy).first();
        if(found == null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Group not found."));
        }else{
            groupDatabase.getCollection().deleteOne(found);

            for(User u:toDelete.getUsers()) {
            	String hostIp = u.getHostIp();
                
                ResteasyClient client = new ResteasyClientBuilder().build();
    			ResteasyWebTarget target = client.target(
    					"http://" + hostIp + ":8096/ChatApp/notify/"+u.getUsername()+"/notifyEndGroup");
    			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(toDelete, MediaType.APPLICATION_JSON));
            }
            new JMSQueue(new jmsDTO("DELETED", JMSStatus.DELETE_GROUP, toDelete));
        }

    }
	
	public void addUser(String groupId,User toAdd) {
        Document searchBy = new Document();
        searchBy.put("id", groupId);

        Document found = (Document) groupDatabase.getCollection().find(searchBy).first();
        
        Document foundUser = (Document) userDatabase.getCollection().find(new Document("username", toAdd.getUsername())).first();

        if(found==null||foundUser==null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Group or user not found."));
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
    			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(found, MediaType.APPLICATION_JSON));
            }
            
            String hostIp = toAdd.getHostIp();
            
            ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(
					"http://" + hostIp + ":8096/ChatApp/notify/"+toAdd.getUsername()+"/notifyNewGroupMember");
			Response response1 = target.request(MediaType.APPLICATION_JSON).get();

            //TODO notify other users about user that left

			new JMSQueue(new jmsDTO(toAdd.getUsername(), JMSStatus.ADD_USER, found));//TODO CHECK
        }
    }
	
	public void removeUser(String groupId, String userId, String sendingId) {
		Document searchBy = new Document();
        searchBy.put("id", groupId);

        Document found = (Document) groupDatabase.getCollection().find(searchBy).first();
        Document foundUser = (Document) userDatabase.getCollection().find(new Document("username", userId)).first();
        Document foundSender = (Document) userDatabase.getCollection().find(new Document("username", sendingId)).first();
        
        if(found==null||foundSender==null||foundUser==null){
        	new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "Group, user or admin not found."));
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
    			Response response1 = target.request(MediaType.APPLICATION_JSON).post(Entity.entity(found, MediaType.APPLICATION_JSON));
            }
            //notifying

            new JMSQueue(new jmsDTO(userId, JMSStatus.REMOVE_USER_GROUP, found));
        }
    	else {
    		new JMSQueue(new jmsDTO("ERROR", JMSStatus.ERROR, "No authoriy"));
    	}
}
	
	

	
	

	
}