package model;

import javax.persistence.Id;

public class Host {
    @Id
    private String id;
    private String address;
    private String alias;
    
    

    public Host() {
		super();
	}

	public Host(String address) {
        this.address = address;
    }

    public Host(String address, String alias) {
        this.address = address;
        this.alias = alias;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
