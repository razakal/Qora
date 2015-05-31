package namewebserver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.GZIP;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Context;
import javax.servlet.http.HttpServletRequest;

import qora.naming.Name;
import controller.Controller;


@Path("/")
public class NamesWebResource 
{
    @Context HttpServletRequest request;

	@GET
	public Response Default()
	{
		try {
			String content = readFile("web/index.html", StandardCharsets.UTF_8);
			
			content = content.replace( "<warning></warning>", getWarning(request));
			
			return Response.ok(content, "text/html; charset=utf-8").build();
		} catch (IOException e) {
			e.printStackTrace();
			return error404();
		}
	}

	@Path("favicon.ico")
	@GET
	public Response favicon()
	{
		File file = new File("web/favicon.ico");
		
		if(file.exists()){
			return Response.ok(file, "image/vnd.microsoft.icon").build();
		}
		else
		{
			return error404();
		}
	}
	
	@Path("libs/qora.png")
	@GET
	public Response qorapng()
	{
		File file = new File("web/qora.png");
		
		if(file.exists()){
			return Response.ok(file, "image/png").build();
		}
		else
		{
			return error404();
		}
	}
	
	@Path("libs/jquery.{version}.js")
	@GET
	public Response jquery(@PathParam("version") String version)
	{
		File file;
		if(version.equals("1"))
		{
			file = new File("web/jquery-1.11.3.min.js");
		}
		else if(version.equals("2"))
		{
			file = new File("web/jquery-2.1.4.min.js");
		}
		else
		{
			file = new File("web/jquery-2.1.4.min.js");
		}
		
		if(file.exists()){
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		}
		else
		{
			return error404();
		}
	}

	@Path("libs/angular.min.{version}.js")
	@GET
	public Response angular(@PathParam("version") String version)
	{
		File file;
		if(version.equals("1.3"))
		{
			file = new File("web/angular.min.1.3.15.js");
		}
		else if(version.equals("1.4"))
		{
			file = new File("web/angular.min.1.4.0.js");
		}
		else
		{
			file = new File("web/angular.min.1.3.15.js");
		}
		
		if(file.exists()){
			return Response.ok(file, "text/javascript; charset=utf-8").build();
		}
		else
		{
			return error404();
		}
	}
	
	@Path("libs/bootstrap/{version}/{folder}/{filename}")
	@GET
	public Response bootstrap(@PathParam("version") String version, @PathParam("folder") String folder, @PathParam("filename") String filename)
	{
		String fullname = "web/bootstrap-3.3.4-dist/";
		String type = "text/html; charset=utf-8";
		
		switch(folder)
		{
			case "css":
			{	
				fullname += "css/";
				type = "text/css";
				switch(filename)
				{
					case "bootstrap.css":
						
						fullname += "bootstrap.css";
						break;
						
					case "bootstrap.css.map":
						
						fullname += "bootstrap.css.map";
						break;
						
					case "bootstrap.min.css":
						
						fullname += "bootstrap.min.css";
						break;
						
					case "bootstrap-theme.css":
						
						fullname += "bootstrap-theme.css";
						break;
						
					case "bootstrap-theme.css.map":
						
						fullname += "bootstrap-theme.css.mapp";
						break;
						
					case "bootstrap-theme.min.css":
						
						fullname += "bootstrap-theme.min.css";
						break;
				}
				break;
			}
			case "fonts":
			{
				fullname += "fonts/";
				switch(filename)
				{
					case "glyphicons-halflings-regular.eot":
						
						fullname += "glyphicons-halflings-regular.eot";
						type = "application/vnd.ms-fontobject";
						break;
						
					case "glyphicons-halflings-regular.svg":
						
						fullname += "glyphicons-halflings-regular.svg";
						type = "image/svg+xml";
						break;
						
					case "glyphicons-halflings-regular.ttf":
						
						fullname += "glyphicons-halflings-regular.ttf";
						type = "application/x-font-ttf";
						break;
						
					case "glyphicons-halflings-regular.woff":
						
						fullname += "glyphicons-halflings-regular.woff";
						type = "application/font-woff";
						break;
						
					case "glyphicons-halflings-regular.woff2":
						
						fullname += "glyphicons-halflings-regular.woff2";
						type = "application/font-woff";
						break;
				}
				break;
			}
			case "js":
			{
				fullname += "js/";
				type = "text/javascript";
				switch(filename)
				{
					case "bootstrap.js":
					
						fullname += "bootstrap.js"; 
						break;
						
					case "bootstrap.min.js":
					
						fullname += "bootstrap.js"; 
						break;
											
					case "npm.js":
					
						fullname += "npm.js"; 
						break;
					
				}
				break;
			}
		}
		
		File file = new File(fullname);
		
		if(file.exists()){
			return Response.ok(file, type).build();
		}
		else
		{
			return error404();
		}
	}
	
	@Path("{name}")
	@GET
	public Response getNames(@PathParam("name") String nameName)
	{
		Name name = Controller.getInstance().getName(nameName);
		
		//CHECK IF NAME EXISTS
		if(name == null)
		{
			return error404();
		}
		
		String Value = name.getValue().toString();
	
		//REDIRECT
		if(Value.startsWith("http://") || Value.startsWith("https://"))
		{
			return Response.status(302)
					.header("Location", Value)
					.build();
		}
		
		//WEBPAGE GZIP DECOMPRESSOR
		Value = GZIP.webDecompress(Value);
        
        //PROCESSING TAG INJ
        Pattern pattern = Pattern.compile("(?i)<inj>(.*?)</inj>");
        Matcher matcher = pattern.matcher(Value);
        while (matcher.find()) {
        	Name nameinj = Controller.getInstance().getName(matcher.group(1));
        	Value = Value.replace( matcher.group(), GZIP.webDecompress(nameinj.getValue().toString()));
        }
     
		//SHOW WEB-PAGE
		return Response.status(200)
				.header("Content-Type", "text/html; charset=utf-8")
				.entity(getWarning(request)+Value)
				.build();
	}
	
	public Response error404()
	{
		return Response.status(404)
		.header("Content-Type", "text/html; charset=utf-8")
		.entity("<html><title>404 Not Found</title><body><h1>name does not exist</h1><hr>© <a href=http://www.qora.org>Qora</a></body></html>")
		.build();
	}
	
	private String getWarning(HttpServletRequest request)
	{
		if(Controller.getInstance().isWalletUnlocked())
		{	
			String ipAddress  = request.getHeader("X-FORWARDED-FOR");  
		    if(ipAddress == null)  
		    {  
		    	ipAddress = request.getRemoteAddr();  
		    }  
			
			if(ipAddress.equals("127.0.0.1"))
			{
				return "<style type=\"text/css\">" //
				+ "#message_box {" // 
				+ "position:absolute;" // 
				+ "left:0; right:0; top:0;" // 
				+ "z-index: 10;" // 
				+ "background:#ffc;" //
				+ "padding:5px;" //
				+ "border:1px solid #CCCCCC;" //
				+ "text-align:center;" // 
				+ "font-weight:bold;" // 
				+ "width:auto;" //
				+ "}" //
				+ "</STYLE>" //
				+ "<div id=message_box>Wallet is unlocked!</div>" //
				+ "<div style='margin-top:50px'>";
			}
		}
		return "";	
	}
	
	static String readFile(String path, Charset encoding) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}
}