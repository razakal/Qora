package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.BlogProfile;
import qora.web.NameStorageMap;
import qora.web.Profile;
import qora.web.blog.BlogEntry;
import api.BlogPostResource;
import controller.Controller;
import database.DBSet;

public class BlogUtils {

	/**
	 * 
	 * @return triplet of name, title, description of all enabled blogs.
	 */
	public static List<BlogProfile> getEnabledBlogs(
			String searchvalueOpt) {

		NameStorageMap nameMap = DBSet.getInstance().getNameStorageMap();
		Set<String> names = nameMap.getKeys();
		
		Map<String, List<String>> followMap = new HashMap<>();
		List<Profile> resultProfiles = new ArrayList<>();

		for (String name : names) {

			Profile profile = Profile.getProfileOpt(name);

			if(profile != null && profile.isProfileEnabled())
			{
				List<String> followedBlogs = profile.getFollowedBlogs();
				if(followedBlogs != null)
				{
					List<String> alreadyProcessed = new ArrayList<String>();
					for (String followedBlog : followedBlogs) {
						if(!alreadyProcessed.contains(followedBlog) && !name.equals(followedBlog))
						{
							alreadyProcessed.add(followedBlog);
							if(followMap.containsKey(followedBlog))
							{
								List<String> followerList = followMap.get(followedBlog);
								if(!followerList.contains(name))
								{
									followerList.add(name);
								}
								followMap.put(followedBlog, followerList);
							}else
							{
								List<String> followerList = new ArrayList<>();
								followerList.add(name);
								followMap.put(followedBlog, followerList);
							}
						}
					}
				}
				
				if (profile.isBlogEnabled()) {
					
					String title = profile.getBlogTitleOpt();
					String description = profile.getBlogDescriptionOpt();
					if (searchvalueOpt != null) {
						searchvalueOpt = searchvalueOpt.toLowerCase();
						if (name.toLowerCase().contains(searchvalueOpt)
								|| (title != null && title.toLowerCase().contains(
										searchvalueOpt))
										|| (description != null)
										&& description.toLowerCase().contains(
												searchvalueOpt)) {
							resultProfiles.add(profile);
						}
						continue;
					}
					resultProfiles.add(profile);
				}
			}

		}
		
		List<BlogProfile> blogprofiles = new ArrayList<>();
		for (Profile profileWithBlog : resultProfiles) {
			
			String name = profileWithBlog.getName().getName();
			if(followMap.containsKey(name))
			{
				blogprofiles.add(new BlogProfile(profileWithBlog, followMap.get(name)));
			}else
			{
				blogprofiles.add(new BlogProfile(profileWithBlog, new ArrayList<String>()));
			}
			
		}
		
		Collections.sort(blogprofiles);
		

		return blogprofiles;
	}

	public static List<BlogEntry> getBlogPosts(String blogOpt) {
		List<BlogEntry> results = new ArrayList<>();
		
		
		List<byte[]> list = DBSet.getInstance().getBlogPostMap().get(blogOpt == null ? "QORA" : blogOpt);
		
		List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
		for (byte[] blogArbTx : list) {
			Transaction transaction = Controller.getInstance().getTransaction(blogArbTx);
			if(transaction != null)
			{
				blogPostTX.add((ArbitraryTransaction) transaction);
			}
		}
		

		for (ArbitraryTransaction transaction : blogPostTX) {

				byte[] data = ((ArbitraryTransaction) transaction).getData();
				String string = new String(data);

				JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
				if (jsonObject != null) {
					// MAINBLOG OR CUSTOM BLOG?
					if ((blogOpt == null && !jsonObject
							.containsKey(BlogPostResource.BLOGNAME_KEY))
							|| (jsonObject
									.containsKey(BlogPostResource.BLOGNAME_KEY) && jsonObject
									.get(BlogPostResource.BLOGNAME_KEY).equals(
											blogOpt))) {

						String title = (String) jsonObject
								.get(BlogPostResource.TITLE_KEY);
						String post = (String) jsonObject
								.get(BlogPostResource.POST_KEY);
						String nameOpt = (String) jsonObject
								.get(BlogPostResource.AUTHOR);

						String creator = transaction
								.getCreator().getAddress();
						BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(blogOpt);
						//POST NEEDS TO BE FILLED AND POST MUST BE ALLOWED
						if (StringUtil.isNotBlank(post) && blogBlackWhiteList.isAllowedPost(nameOpt != null ? nameOpt : creator, creator)) {
							results.add(new BlogEntry(title, post, nameOpt,
									transaction.getTimestamp(), creator));

						}
					}
				}

			}

		Collections.reverse(results);

		return results;

	}
	

}
