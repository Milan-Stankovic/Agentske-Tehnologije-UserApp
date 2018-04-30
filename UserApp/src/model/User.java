package model;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.List;

public class User {

    private String password;

    @Id
    private String username;

    private String name;

    private String lastName;

    private List<Friendship> friends;
    
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

    public List<Friendship> getFriends() {
        return friends;
    }

    public void setFriends(List<Friendship> friends) {
        this.friends = friends;
    }

    public User(String username, String password){
        super();
        this.username = username;
        this.password = password;
        this.hostIp = null;
        friends= new ArrayList<Friendship>();
    }

    public User(String username, String password, String host){
        super();
        this.username = username;
        this.password = password;
        this.hostIp = host;

    }

    public Friendship sendFriendRequest(String reciverUsername){
        Friendship f = new Friendship();
        f.setReciever(reciverUsername);
        f.setSender(this.username);
        f.setStatus(FriendshipStatus.PENDING);
        friends.add(f);
        return f;
    }

    public void acceptFriendship(String sender){
        for (Friendship f:friends) {
            if(f.getReciever().equals(this.username))
                if(f.getSender().equals(sender))
                    if(f.getStatus()==FriendshipStatus.PENDING)
                        f.setStatus(FriendshipStatus.ACCEPTED);
        }

    }

    public void declineFriendship(String sender) {
        for (Friendship f : friends) {
            if (f.getReciever().equals(this.username))
                if (f.getSender().equals(sender))
                    if (f.getStatus() == FriendshipStatus.PENDING)
                        f.setStatus(FriendshipStatus.DECLINED);

        }
    }

    public void addFriendship(Friendship f){
        friends.add(f);

    }



}
