package model;

import javax.persistence.Id;
import java.util.List;
import java.util.UUID;

public class Group {

    @Id
    private String id;

    private String name;

    private List<User> users;
    
    private String admin;	
    
    

    public Group() {
		super();
	}

    


	public Group(String name){
        this.name=name;
        this.id = UUID.randomUUID().toString();
    }
    
	



	public String getAdmin() {
		return admin;
	}




	public void setAdmin(String admin) {
		this.admin = admin;
	}




	public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
