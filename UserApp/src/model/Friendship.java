package model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Id;

public class Friendship implements Serializable{

    @Id
    private String id;

    private String sender;

    private String reciever;

    private FriendshipStatus status;

    public Friendship(){

    }
    
    

    public String getId() {
		return id;
	}



	public void setId(String id) {
		this.id = UUID.randomUUID().toString();
	}



	public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciever() {
        return reciever;
    }

    public void setReciever(String reciever) {
        this.reciever = reciever;
    }

    public FriendshipStatus getStatus() {
        return status;
    }

    public void setStatus(FriendshipStatus status) {
        this.status = status;
    }
}
