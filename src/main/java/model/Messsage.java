package model;

import java.util.Date;

public class Messsage {

    private String id;
    private String sender;
    private String reciver;
    private Date creationDate;
    private String content;

    public Messsage() {

    }

    public Messsage(String id, String sender, String reciver, Date creationDate, String content) {
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
