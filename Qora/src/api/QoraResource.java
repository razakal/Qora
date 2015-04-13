package api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import controller.Controller;

@Path("qora")
@Produces(MediaType.APPLICATION_JSON)
public class QoraResource 
{
	@GET
	@Path("/stop")
	public String stop()
	{
		//STOP
		Controller.getInstance().stopAll();		
		System.exit(0);
		
		//RETURN
		return String.valueOf(true);
	}
	    
	@GET 
	@Path("/status")
	public String getStatus() 
	{ 
		return String.valueOf(Controller.getInstance().getStatus());
	}
	
	@GET 
	@Path("/status/forging")
	public String getForgingStatus() 
	{ 
		return String.valueOf(Controller.getInstance().getForgingStatus().getStatuscode());
	}
	
	@GET 
	@Path("/isuptodate")
	public String isUpToDate() 
	{ 
		return String.valueOf(Controller.getInstance().isUpToDate());
	}
}
