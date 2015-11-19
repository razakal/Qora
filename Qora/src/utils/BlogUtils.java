package utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.crypto.Base58;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.Transaction;
import qora.web.BlogBlackWhiteList;
import qora.web.BlogProfile;
import qora.web.NameStorageMap;
import qora.web.Profile;
import qora.web.blog.BlogEntry;
import api.BlogPostResource;

import com.twitter.Extractor;

import controller.Controller;
import database.DBSet;

public class BlogUtils {

	/**
	 * 
	 * @return triplet of name, title, description of all enabled blogs.
	 */
	public static List<BlogProfile> getEnabledBlogs(String searchvalueOpt) {

		NameStorageMap nameMap = DBSet.getInstance().getNameStorageMap();
		Set<String> names = nameMap.getKeys();

		Map<String, List<String>> followMap = new HashMap<>();
		List<Profile> resultProfiles = new ArrayList<>();

		for (String name : names) {

			Profile profile = Profile.getProfileOpt(name);

			if (profile != null && profile.isProfileEnabled()) {
				List<String> followedBlogs = profile.getFollowedBlogs();
				if (followedBlogs != null) {
					List<String> alreadyProcessed = new ArrayList<String>();
					for (String followedBlog : followedBlogs) {
						if (!alreadyProcessed.contains(followedBlog)
								&& !name.equals(followedBlog)) {
							alreadyProcessed.add(followedBlog);
							if (followMap.containsKey(followedBlog)) {
								List<String> followerList = followMap
										.get(followedBlog);
								if (!followerList.contains(name)) {
									followerList.add(name);
								}
								followMap.put(followedBlog, followerList);
							} else {
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
								|| (title != null && title.toLowerCase()
										.contains(searchvalueOpt))
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
			if (followMap.containsKey(name)) {
				blogprofiles.add(new BlogProfile(profileWithBlog, followMap
						.get(name)));
			} else {
				blogprofiles.add(new BlogProfile(profileWithBlog,
						new ArrayList<String>()));
			}

		}

		Collections.sort(blogprofiles);

		return blogprofiles;
	}

	public static List<BlogEntry> getBlogPosts(List<String> blogList) {
		List<BlogEntry> blogPosts = new ArrayList<BlogEntry>();
		for (String blogname : blogList) {
			blogPosts.addAll(getBlogPosts(blogname));
		}
		Collections.sort(blogPosts, new BlogEntryTimestampComparator());

		Collections.reverse(blogPosts);

		return blogPosts;
	}

	public static List<BlogEntry> getHashTagPosts(String hashtag) {
		List<BlogEntry> results = new ArrayList<BlogEntry>();
		List<byte[]> list = DBSet.getInstance().getHashtagPostMap()
				.get(hashtag);

		if (list != null) {
			for (byte[] bs : list) {
				BlogEntry blogEntryOpt = getBlogEntryOpt(bs);
				if (blogEntryOpt != null) {
					results.add(blogEntryOpt);
				}
			}
		}
		
		Collections.sort(results, new BlogEntryTimestampComparator());

		Collections.reverse(results);

		return results;
	}

	public static List<String> getHashTags(String text) {
		List<String> extractHashtags = new Extractor().extractHashtags(text);
		List<String> result = new ArrayList<String>();
		for (String hashTag : extractHashtags) {
			result.add("#" + hashTag);
		}
		return result;
	}

	public static List<BlogEntry> getBlogPosts(String blogOpt) {
		List<BlogEntry> results = new ArrayList<>();

		List<byte[]> list = DBSet.getInstance().getBlogPostMap()
				.get(blogOpt == null ? "QORA" : blogOpt);

		List<ArbitraryTransaction> blogPostTX = new ArrayList<>();
		if (list != null) {
			for (byte[] blogArbTx : list) {
				Transaction transaction = Controller.getInstance()
						.getTransaction(blogArbTx);
				if (transaction != null) {
					blogPostTX.add((ArbitraryTransaction) transaction);
				}
			}
		}

		for (ArbitraryTransaction transaction : blogPostTX) {

			byte[] data = ((ArbitraryTransaction) transaction).getData();
			String string = new String(data, StandardCharsets.UTF_8);

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
					String share = (String) jsonObject
							.get(BlogPostResource.SHARE_KEY);
					String post = (String) jsonObject
							.get(BlogPostResource.POST_KEY);
					String nameOpt = (String) jsonObject
							.get(BlogPostResource.AUTHOR);

					String creator = transaction.getCreator().getAddress();
					BlogBlackWhiteList blogBlackWhiteList = BlogBlackWhiteList
							.getBlogBlackWhiteList(blogOpt);

					if (blogBlackWhiteList.isAllowedPost(
							nameOpt != null ? nameOpt : creator, creator)) {
						if (StringUtils.isNotEmpty(share)) {
							BlogEntry blogEntryToShareOpt = BlogUtils
									.getBlogEntryOpt((ArbitraryTransaction) Controller
											.getInstance().getTransaction(
													Base58.decode(share)));
							if (blogEntryToShareOpt != null
									&& StringUtils
											.isNotBlank(blogEntryToShareOpt
													.getDescription())) {
								// share gets time of sharing!
								blogEntryToShareOpt.setTime(transaction
										.getTimestamp());
								blogEntryToShareOpt
										.setShareAuthor(nameOpt != null ? nameOpt
												: creator);
								blogEntryToShareOpt.setShareSignatureOpt(Base58
										.encode(transaction.getSignature()));
								results.add(blogEntryToShareOpt);
							}
						} else {
							// POST NEEDS TO BE FILLED AND POST MUST BE ALLOWED
							if (StringUtil.isNotBlank(post)) {
								results.add(new BlogEntry(title, post, nameOpt,
										transaction.getTimestamp(), creator,
										Base58.encode(transaction
												.getSignature()), blogOpt));

							}
						}
					}

				}
			}

		}

		Collections.reverse(results);

		return results;

	}

	public static BlogEntry getBlogEntryOpt(String signature) {
		return getBlogEntryOpt(Base58.decode(signature));
	}

	public static BlogEntry getBlogEntryOpt(byte[] signature) {
		ArbitraryTransaction transaction = (ArbitraryTransaction) Controller
				.getInstance().getTransaction(signature);

		return transaction == null ? null : BlogUtils
				.getBlogEntryOpt(transaction);
	}

	/**
	 * returns blogentry without any restrictions
	 * 
	 * @param transaction
	 * @return
	 */
	// TODO MAYBE JOIN WITH SHARE SO THAT THIS ALSO CONTAINS SHAREDPOSTS!
	public static BlogEntry getBlogEntryOpt(ArbitraryTransaction transaction) {
		if (transaction.getService() != 777) {
			return null;
		}
		byte[] data = ((ArbitraryTransaction) transaction).getData();
		String string = new String(data, StandardCharsets.UTF_8);

		JSONObject jsonObject = (JSONObject) JSONValue.parse(string);
		if (jsonObject != null) {
			// MAINBLOG OR CUSTOM BLOG?

			String title = (String) jsonObject.get(BlogPostResource.TITLE_KEY);
			String post = (String) jsonObject.get(BlogPostResource.POST_KEY);
			String nameOpt = (String) jsonObject.get(BlogPostResource.AUTHOR);
			String blognameOpt = (String) jsonObject
					.get(BlogPostResource.BLOGNAME_KEY);

			String creator = transaction.getCreator().getAddress();

			// POST NEEDS TO BE FILLED
			if (StringUtil.isNotBlank(post)) {
				return new BlogEntry(title, post, nameOpt,
						transaction.getTimestamp(), creator,
						Base58.encode(transaction.getSignature()), blognameOpt);

			}
		}

		return null;
	}

}
