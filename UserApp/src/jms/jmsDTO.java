package jms;

import java.io.Serializable;

public class jmsDTO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String info;
	private JMSStatus status;
	private Object content;
	public jmsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	

	public jmsDTO(String info, JMSStatus status, Object content) {
		super();
		this.info = info;
		this.status = status;
		this.content = content;
	}



	public String getInfo() {
		return info;
	}



	public void setInfo(String info) {
		this.info = info;
	}



	public JMSStatus getStatus() {
		return status;
	}



	public void setStatus(JMSStatus status) {
		this.status = status;
	}



	public void setContent(Object content) {
		this.content = content;
	}



	public jmsDTO(String content) {
		super();
		this.content = content;
	}



	public Object getContent() {
		return content;
	}


	
	
	

	
}
