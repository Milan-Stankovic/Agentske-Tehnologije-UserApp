package model;

import javax.persistence.Id;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable{

    private String password;

    @Id
    private String username;

    private String name;

    private String lastName;
    
    private String hostIp; 

/*    private transient Host host; // tansient znaci da ga preskace

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
    
    */

    public String getPassword() {
        return password;
    }

    public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public User() {
    	
    }

    public User(String username, String password){
        super();
        this.username = username;
        this.password = password;
        this.hostIp = null;
    }

    public User(String username, String password, String host){
        super();
        this.username = username;
        this.password = password;
        this.hostIp = host;

    }
    




}
