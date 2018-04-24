package model;

public class User {

    private String password;

    private String username;

    private String name;

    private String lastName;

   //private transient Host host; // tansient znaci da ga preskace

   public String getPassword() {
        return password;
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

    public User(String username, String password){
        super();
        this.username = username;
        this.password = password;
     //   this.host = null;
    }
}
