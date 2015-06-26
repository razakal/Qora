package qora.web;

import org.json.simple.JSONObject;

import qora.naming.Name;
import utils.NameUtils;
import utils.Qorakeys;

public class Profile {

	
	
	private final BlogBlackWhiteList blogBlackWhiteList;
	private JSONObject jsonRepresenation;

	public static Profile getProfile(Name name)
	{
		return new Profile(name);
	}
	
	private Profile(Name name)
	{
		blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(name.toString());
		jsonRepresenation = NameUtils.getJsonForNameOpt(name);
	}
	
	
	public String getBlogDescriptionOpt()
	{
		return (String) jsonRepresenation.get(Qorakeys.BLOGDESCRIPTION.toString());
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
	
	public boolean isBlogEnabled()
	{
		return jsonRepresenation.containsKey(Qorakeys.BLOGENABLE.toString());
	}


	public BlogBlackWhiteList getBlogBlackWhiteList() {
		return blogBlackWhiteList;
	}
	
	
}
