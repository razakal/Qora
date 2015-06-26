package qora.web;

import java.math.BigDecimal;

import javax.ws.rs.WebApplicationException;

import org.json.simple.JSONObject;

import api.NamesResource;
import qora.naming.Name;
import utils.GZIP;
import utils.NameUtils;
import utils.Qorakeys;

@SuppressWarnings("unchecked")
public class Profile {

	
	
	private final BlogBlackWhiteList blogBlackWhiteList;
	private JSONObject jsonRepresenation;
	private Name name;

	public static Profile getProfile(Name name)
	{
		return new Profile(name);
	}
	
	private Profile(Name name)
	{
		this.name = name;
		blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(name.toString());
		jsonRepresenation = NameUtils.getJsonForNameOpt(name);
	}
	
	
	public String getBlogDescriptionOpt()
	{
		return (String) jsonRepresenation.get(Qorakeys.BLOGDESCRIPTION.toString());
	}
	
	public void saveBlogDescription(String blogDescription)
	{
		jsonRepresenation.put(Qorakeys.BLOGDESCRIPTION.toString(), blogDescription);
	}
	
	public void saveBlogTitle(String blogTitle)
	{
		jsonRepresenation.put(Qorakeys.BLOGTITLE.toString(), blogTitle);
	}
	public void saveAvatarTitle(String profileavatar)
	{
		jsonRepresenation.put(Qorakeys.PROFILEAVATAR.toString(), profileavatar);
	}
	
	public String getBlogTitleOpt()
	{
		return (String) jsonRepresenation.get(Qorakeys.BLOGTITLE.toString());
	}
	
	public String getAvatarOpt()
	{
		return (String) jsonRepresenation.get(Qorakeys.PROFILEAVATAR.toString());
	}
	
	public boolean isProfileEnabled()
	{
		return jsonRepresenation.containsKey(Qorakeys.PROFILEENABLE.toString());
	}
	
	
	public void setProfileEnabled(boolean enabled)
	{
		if(enabled)
		{
			jsonRepresenation.put(Qorakeys.PROFILEENABLE.toString(), "");
		}else
		{
			jsonRepresenation.remove(Qorakeys.PROFILEENABLE.toString());
		}
	}
	
	public void setBlogEnabled(boolean enabled)
	{
		if(enabled)
		{
			jsonRepresenation.put(Qorakeys.BLOGENABLE.toString(), "");
		}else
		{
			jsonRepresenation.remove(Qorakeys.BLOGENABLE.toString());
		}
	}
	
	public boolean isBlogEnabled()
	{
		return jsonRepresenation.containsKey(Qorakeys.BLOGENABLE.toString());
	}


	public BlogBlackWhiteList getBlogBlackWhiteList() {
		return blogBlackWhiteList;
	}
	
	public String saveProfile() throws WebApplicationException
	{
		String jsonString = jsonRepresenation.toJSONString();
		String compressValue = GZIP.compress(jsonString);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("fee", BigDecimal.ONE.setScale(8).toPlainString());
		jsonObject.put("newowner", name.getOwner().getAddress());
		jsonObject.put("newvalue", compressValue);
		
		return new NamesResource().updateName(jsonObject.toJSONString(), name.getName());
	}
	
	
}
