package jms;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateful;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@LocalBean
@Path("/")
@Stateful
public class JMSTest {


    private static final int MSG_COUNT = 5;

   /* @Inject
    private JMSContext context;

    @Resource(mappedName = "java:jboss/exported/jms/queue/mojQueue")
    private Queue queue;*/
    
	@GET
	@Path("JMStest")
	@Produces(MediaType.TEXT_PLAIN)
	public String testjms() {
		new JMSQueue(new jmsDTO("BAD MOTHAFUCKER"));
		return "JMS!!!";
	}



}
