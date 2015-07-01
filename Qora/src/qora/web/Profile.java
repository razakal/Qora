package qora.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.simple.JSONObject;

import controller.Controller;
import database.DBSet;
import qora.naming.Name;
import utils.GZIP;
import utils.NameUtils;
import utils.Qorakeys;
import api.NamesResource;

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
	
	public static List<Profile> getEnabledProfiles()
	{
		List<Name> namesAsList = Controller.getInstance().getNamesAsList();
		List<Profile> results = new ArrayList<Profile>();
		for (Name name : namesAsList) {
			Profile profile = Profile.getProfile(name);
			if(profile.isProfileEnabled())
			{
				results.add(profile);
			}
		}
		
		return results;
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
	
	public List<String> getFollowedBlogs()
	{
		return Collections.unmodifiableList(getFollowedBlogsInternal());
	}


	private List<String> getFollowedBlogsInternal() {
		String profileFollowString = (String) jsonRepresenation.get(Qorakeys.PROFILEFOLLOW.toString());
		if(profileFollowString != null)
		{
			String[] profileFollowArray = StringUtils.split(profileFollowString, ",");
			return  new ArrayList<String>( Arrays.asList(profileFollowArray));
		}
		
		return new ArrayList<String>();
	}
	
	public void addFollowedBlog(String blogname)
	{
		addRemoveFollowedInternal(blogname, false);
	}
	
	public void removeFollowedBlog(String blogname)
	{
		addRemoveFollowedInternal(blogname, true);
	}


	public void addRemoveFollowedInternal(String blogname, boolean isRemove) {
		Name blogName = DBSet.getInstance().getNameMap().get(blogname);
		if(blogName != null)
		{
			Profile profile = Profile.getProfile(blogName);
			//ADDING ONLY IF ENABLED REMOVE ALWAYS
			if(isRemove ||( profile.isProfileEnabled() && profile.isBlogEnabled()))
			{
				List<String> followedBlogsInternal = getFollowedBlogsInternal();
					if(isRemove)
					{
						followedBlogsInternal.remove(blogname);
					}else
					{
						if(!followedBlogsInternal.contains(blogname))
						{
							followedBlogsInternal.add(blogname);
						}
					}
					String joinResult = StringUtils.join(followedBlogsInternal, ",");
					jsonRepresenation.put(Qorakeys.PROFILEFOLLOW, joinResult);
			}
		}
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

	public Name getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	
}
