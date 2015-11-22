package api;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import database.DBSet;
import qora.crypto.Base58;
import qora.web.blog.BlogEntry;
import utils.BlogUtils;
import webserver.WebResource;

@Path("blog")
@Produces(MediaType.APPLICATION_JSON)
public class BlogResource {
	
	@GET
	public String getBlogList() {
		return getBlogList("QORA");
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/posts/{blogname}")
	public String getBlogList(@PathParam("blogname") String blogname) {

		if(blogname.equals("QORA"))
		{
			blogname = null;
		}
		
		List<byte[]> txlist = DBSet.getInstance().getBlogPostMap()
				.get(blogname == null ? "QORA" : blogname);

		JSONArray outputJSON = new JSONArray();
		
		for (byte[] sign : txlist) {
			outputJSON.add(Base58.encode(sign));
		}
		
		return outputJSON.toJSONString();
	}
	
	@GET
	@Path("/post/{signature}")
	public String getBlogPost(@PathParam("signature") String signature) {
		
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}
		
		BlogEntry blogEntry = BlogUtils.getBlogEntryOpt(signatureBytes);
		WebResource.addSharingAndLiking(blogEntry, blogEntry.getSignature());
		
		return blogEntry.toJson().toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/entries/{blogname}/limit/{limit}")
	public String getBlogEntry(@PathParam("blogname") String blogname, @PathParam("limit") int limit) {
	
		JSONObject outputJSON = new JSONObject();
		
		if(blogname.equals("QORA"))
		{
			blogname = null;
		}
		
		List<BlogEntry> blogPosts = BlogUtils.getBlogPosts(blogname, limit);
		
		int i = 1;
		
		for (BlogEntry blogEntry : blogPosts) {
			
			WebResource.addSharingAndLiking(blogEntry, blogEntry.getSignature());
			
			outputJSON.put(i, blogEntry.toJson());
			
			i++;
		}
		
		return outputJSON.toJSONString();
	}
	
	
	@GET
	@Path("/entries/{blogname}")
	public String getBlogEntry(@PathParam("blogname") String blogname) {
		return getBlogEntry(blogname, -1);
	}

	@GET
	@Path("/lastEntry/{blogname}")
	public String getLastEntry(@PathParam("blogname") String blogname) {
		if(blogname.equals("QORA"))
		{
			blogname = null;
		}
		
		List<byte[]> txlist = DBSet.getInstance().getBlogPostMap()
				.get(blogname == null ? "QORA" : blogname);
		
		BlogEntry blogEntry = BlogUtils.getBlogEntryOpt(txlist.get(txlist.size()-1));
		//WebResource.addSharingAndLiking(blogEntry, blogEntry.getSignature());
		
		return blogEntry.toJson().toJSONString();
	}
}
