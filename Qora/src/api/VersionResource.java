package api;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
		
		jsonObject.put("version", Controller.getInstance().version);

		jsonObject.put("buildtime", BuildTime.getBuildDateTime());
	

		return jsonObject.toJSONString();
	}
}

