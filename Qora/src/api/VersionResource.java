package api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import utils.BuildTime;
import controller.Controller;

@Path("version")
@Produces(MediaType.APPLICATION_JSON)
public class VersionResource {

	@SuppressWarnings("unchecked")
	@GET
	public String getVersion()
	{
		JSONObject jsonObject = new JSONObject();
		
		jsonObject.put("version", Controller.getInstance().getVersion());

		jsonObject.put("buildtime", BuildTime.getBuildDateTimeString());
	

		return jsonObject.toJSONString();
	}
}

