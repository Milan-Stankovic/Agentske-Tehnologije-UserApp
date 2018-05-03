package jms;

import java.io.Serializable;

public class jmsDTO implements Serializable{

	private String content;
	
	public jmsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public jmsDTO(String content) {
		super();
		this.content = content;
	}



	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	

	
}
