package model;

import javax.persistence.Id;

import com.google.gson.Gson;
import com.sun.mail.imap.protocol.UID;

import java.util.Date;
import java.util.UUID;

public class Message {

    @Id
    private String id;
    private String sender;
    private String reciver;
    private Date creationDate;
    private String content;
    private String groupId="";
    
    
    
    

    public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public Message() {

    }
    
    public Message(String text) {
    	Gson test = new Gson();
    	Message m = test.fromJson(text, Message.class);
    	this.id= UUID.randomUUID().toString();
    	this.sender=m.sender;
    	this.reciver=m.reciver;
    	this.creationDate= new Date();
    	this.content=m.content;
    	this.groupId=m.groupId;
    }

    public Message(String id, String sender, String reciver, Date creationDate, String content) {
        this.id = id;
        this.sender = sender;
        this.reciver = reciver;
        this.creationDate = creationDate;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReciver() {
        return reciver;
    }

    public void setReciver(String reciver) {
        this.reciver = reciver;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
