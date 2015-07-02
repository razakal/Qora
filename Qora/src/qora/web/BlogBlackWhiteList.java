package qora.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

import qora.account.Account;
import qora.crypto.Crypto;
import qora.naming.Name;
import utils.NameUtils;
import utils.NameUtils.NameResult;
import utils.Pair;
import utils.Qorakeys;
import controller.Controller;
import database.DBSet;

/**
 * Used to determine which names or accounts are allowed to post in a blog
 * @author Skerberus
 *
 */
public class BlogBlackWhiteList {

	
	/**
	 * 
	 * @param blogname the blog to check for white/blacklist
	 * @return An object containing all names or accounts that are allowed/forbidden the blogowner is always part of the whitelist
	 */
	public static BlogBlackWhiteList getBlogBlackWhiteList(String blogname)
	{
		
		if(blogname == null)
		{
			return new BlogBlackWhiteList(false, new ArrayList<String>(), null);
		}
		
			Name blognameName = DBSet.getInstance().getNameMap().get(blogname);
			
			//Name not registered, --> Default = Whitelist
			if(blognameName == null)
			{
				return new BlogBlackWhiteList(true, new ArrayList<String>(), blogname);
			}
			
			JSONObject jsonForNameOpt = NameUtils.getJsonForNameOpt(blognameName);
			
			//No Json or no keys -> --> Default = Whitelist
			if(jsonForNameOpt != null)
			{
				if(jsonForNameOpt.containsKey(Qorakeys.BLOGWHITELIST.toString()))
				{
					String whitelist = (String) jsonForNameOpt.get(Qorakeys.BLOGWHITELIST.toString());
					
					String[] whiteListEntries = StringUtils.split(whitelist, ", ");
					List<String> result = new ArrayList<String>(Arrays.asList(whiteListEntries));
					return new BlogBlackWhiteList(true, result , blogname);
					
				}
				
				if(jsonForNameOpt.containsKey(Qorakeys.BLOGBLACKLIST.toString()))
				{
					String blackList = (String) jsonForNameOpt.get(Qorakeys.BLOGBLACKLIST.toString());
					
					String[] blackListEntries = StringUtils.split(blackList, ", ");
					return new BlogBlackWhiteList(false, Arrays.asList(blackListEntries), blogname);
				}
				
			}
			
			
			return new BlogBlackWhiteList(true, new ArrayList<String>(), blogname);
		
	}
	
	
	private final boolean whitelist;
	private final List<String> blackwhiteList;
	private final String blogname;
	
	private BlogBlackWhiteList(boolean isWhiteList, List<String> blackwhiteList, String blogname) {
		whitelist = isWhiteList;
		this.blackwhiteList = blackwhiteList;
		this.blogname = blogname;
	}

	public List<String> getBlackwhiteList() {
		return Collections.unmodifiableList(blackwhiteList);
	}

	public boolean isWhitelist() {
		return whitelist;
	}
	
	public boolean isBlacklist() {
		return !whitelist;
	}
	
	public String getBlogname() {
		return blogname;
	}
	
	/**
	 * Checks if post is allowed in blog.
	 * @param accountOrName name if post by name, creator else
	 * @param creator the creator of that post
	 * @return true if post is allowed, false else
	 */
	public boolean isAllowedPost(String accountOrName, String creator)
	{
		
		
		Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(accountOrName);
		if(nameToAdress.getB() == NameResult.OK)
		{
			String address = nameToAdress.getA().getAddress();
			//Name is not matching creator, maybe name sold or someone tried to fake a post.
			if(!address.equals(creator))
			{
				return false;
			}
			
			//blogowner can always post
			if(accountOrName.equals(blogname))
			{
				return true;
			}
			
		}
		
		
		if(isWhitelist())
		{
			return (blackwhiteList.contains(accountOrName));
		}else
		{
			return !blackwhiteList.contains(accountOrName);
		}
	}
	
	
	/**
	 * 
	 * @return a pair containing every account and every name that current user owns and that is allowed to post in the blog.
	 * In case of a whitelist the blogowner is always part of that
	 */
	public Pair<List<Account>, List<Name>> getOwnAllowedElements(boolean removeZeroBalance)
	{
		
		List<Name> resultingNames = new CopyOnWriteArrayList<Name>();
		List<Account> resultingAccounts = new CopyOnWriteArrayList<Account>();
		
		List<Account> myaccounts = new ArrayList<Account>(Controller
				.getInstance().getAccounts());
		List<Name> myNames = new ArrayList<Name>(Controller
				.getInstance().getNamesAsList());
		
		
		
		if(isWhitelist())
		{
			for (String accountOrName : blackwhiteList) {
				Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(accountOrName);
				
				if(nameToAdress.getB() == NameResult.OK)
				{
					//DO I OWN THAT NAME?
					Name name = Controller.getInstance().getName(accountOrName);
					if(myNames.contains(name))
					{
						if(!resultingNames.contains(name))
						{
							//YOU CAN ONLY POST BY NAME IF PROFILE IS ENABLED
							if(Profile.getProfile(name).isProfileEnabled())
							{
								resultingNames.add(name);
							}
						}
					}
					
					
				}else if(!Crypto.getInstance().isValidAddress(accountOrName))
				{
					Account accountByAddress = Controller.getInstance().getAccountByAddress(accountOrName);
					
					//DO I OWN THAT ADDRESS?
					if(accountByAddress != null)
					{
						if(!resultingAccounts.contains(accountByAddress))
						{
							resultingAccounts.add(accountByAddress);
						}
					}
					
				}
				
				
			}
			//IF IT IS MY OWN BLOG, MY NAME WILL BE OF COURSE PART OF THE WHITELIST 
			Name blogName = Controller.getInstance().getName(blogname);
			if(myNames.contains(blogName))
			{
				if(!resultingNames.contains(blogName))
				{
					resultingNames.add(blogName);
				}
			}
		}else
		{
			List<Profile> activeProfiles = Profile.getEnabledProfiles();
			for (Profile profile : activeProfiles) {
				if(profile.isProfileEnabled())
				{
					resultingNames.add(profile.getName());
				}
			}
			resultingAccounts.addAll(myaccounts);
			
			for (String accountOrName : blackwhiteList) {
				Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(accountOrName);
				
				if(nameToAdress.getB() == NameResult.OK)
				{
					//DO I OWN THAT NAME?
					Name name = Controller.getInstance().getName(accountOrName);
					if(myNames.contains(name))
					{
						resultingNames.remove(name);
					}
					
					
				}else if(!Crypto.getInstance().isValidAddress(accountOrName))
				{
					Account accountByAddress = Controller.getInstance().getAccountByAddress(accountOrName);
					
					//DO I OWN THAT ADDRESS?
					if(accountByAddress != null)
					{
						resultingAccounts.remove(accountByAddress);
					}
					
				}
				
			}
		}
		
		
		if(removeZeroBalance)
		{
			for (Name name : resultingNames) {
				//No balance account not shown
				if(name.getOwner().getBalance(0).compareTo(BigDecimal.ZERO) <= 0)
				{
					resultingNames.remove(name);
				}
			}
			
			
			for (Account account : resultingAccounts) {
				if(account.getBalance(0).compareTo(BigDecimal.ZERO) <= 0)
				{
					resultingAccounts.remove(account);
				}
			}
		}
		
		return new Pair<List<Account>, List<Name>>(resultingAccounts, resultingNames);
		
		
		
		
	}

	
	
}
