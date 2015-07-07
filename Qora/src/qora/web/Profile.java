package qora.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.simple.JSONObject;

import qora.naming.Name;
import utils.GZIP;
import utils.NameUtils;
import utils.Pair;
import utils.Qorakeys;
import api.NamesResource;
import controller.Controller;
import database.DBSet;

@SuppressWarnings("unchecked")
public class Profile {

	public static boolean isAllowedProfileName(String name) {
		// RULES FOR PROFILES
		if (name == null || name.length() < 3 || name.contains(";")
				|| name.endsWith(" ") || name.startsWith(" ")) {
			return false;
		}

		return true;
	}

	private final BlogBlackWhiteList blogBlackWhiteList;
	private JSONObject jsonRepresenation;
	private Name name;
	private List<Name> followerCache = null;
	private List<Name> likeCache = null;

	public static Profile getProfileOpt(String name) {
		Profile result = null;
		if (name != null) {
			Name nameObj = DBSet.getInstance().getNameMap().get(name);
			result = Profile.getProfileOpt(nameObj);
		}

		return result;
	}

	public static Profile getProfileOpt(Name name) {
		if (!isAllowedProfileName(name.getName())) {
			return null;
		}
		return new Profile(name);
	}

	private Profile(Name name) {
		this.name = name;
		blogBlackWhiteList = BlogBlackWhiteList.getBlogBlackWhiteList(name
				.toString());
		jsonRepresenation = NameUtils.getJsonForNameOpt(name);
	}

	public List<Name> getFollower() {

		if (followerCache != null) {
			return followerCache;
		}

		List<Name> results = new ArrayList<>();
		List<Name> resultsLike = new ArrayList<>();
		Collection<Name> values = DBSet.getInstance().getNameMap().getValues();

		for (Name name : values) {
			Profile profileOpt = Profile.getProfileOpt(name);
			// FOLLOWING ONLY WITH ENABLED PROFILE
			if (profileOpt != null && profileOpt.isProfileEnabled()) {
				if (profileOpt.getFollowedBlogs().contains(this.name.getName())) {
					results.add(profileOpt.getName());
				}
				if (profileOpt.getLikedProfiles().contains(this.name.getName())) {
					resultsLike.add(profileOpt.getName());
				}
				
				
				
			}
		}
		followerCache = results;
		likeCache = resultsLike;
		return results;
	}

	public List<Name> getLikes() {
	
		if (likeCache != null) {
			return likeCache;
		}
		
	
		List<Name> results = new ArrayList<>();
		List<Name> resultsFollower = new ArrayList<>();
		Collection<Name> values = DBSet.getInstance().getNameMap().getValues();

		for (Name name : values) {
			Profile profileOpt = Profile.getProfileOpt(name);
			// FOLLOWING ONLY WITH ENABLED PROFILE
			if (profileOpt != null && profileOpt.isProfileEnabled()) {
				if (profileOpt.getLikedProfiles().contains(this.name.getName())) {
					results.add(profileOpt.getName());
				}
				if (profileOpt.getFollowedBlogs().contains(this.name.getName())) {
					resultsFollower.add(profileOpt.getName());
				}
			}
		}
		likeCache = results;
		followerCache = resultsFollower;
		return results;
	}

	public static List<Profile> getEnabledProfiles() {
		List<Name> namesAsList = Controller.getInstance().getNamesAsList();
		List<Profile> results = new ArrayList<Profile>();
		for (Name name : namesAsList) {
			Profile profile = Profile.getProfileOpt(name);
			if (profile != null && profile.isProfileEnabled()) {
				results.add(profile);
			}
		}

		return results;
	}

	public String getBlogDescriptionOpt() {
		return (String) jsonRepresenation.get(Qorakeys.BLOGDESCRIPTION
				.toString());
	}

	public void saveBlogDescription(String blogDescription) {
		storeKeyValueIfNotBlank(Qorakeys.BLOGDESCRIPTION, blogDescription);
	}

	public void storeKeyValueIfNotBlank(Qorakeys key, String value) {
		if (!StringUtils.isBlank(value)) {
			jsonRepresenation.put(key.toString(), value);
		} else {
			jsonRepresenation.remove(key);
		}
	}

	public void saveBlogTitle(String blogTitle) {
		storeKeyValueIfNotBlank(Qorakeys.BLOGTITLE, blogTitle);
	}

	public void saveAvatarTitle(String profileavatar) {
		storeKeyValueIfNotBlank(Qorakeys.PROFILEAVATAR, profileavatar);
	}

	public void saveProfileMainGraphicOpt(String maingraphicurl) {
		storeKeyValueIfNotBlank(Qorakeys.PROFILEMAINGRAPHIC, maingraphicurl);
	}

	public String getBlogTitleOpt() {
		return (String) jsonRepresenation.get(Qorakeys.BLOGTITLE.toString());
	}

	public String getAvatarOpt() {
		return (String) jsonRepresenation
				.get(Qorakeys.PROFILEAVATAR.toString());
	}

	public String getProfileGraphicOpt() {
		return (String) jsonRepresenation.get(Qorakeys.PROFILEMAINGRAPHIC
				.toString());
	}

	public List<String> getFollowedBlogs() {
		return Collections.unmodifiableList(getFollowedBlogsInternal());
	}

	public List<String> getLikedProfiles() {
		return Collections.unmodifiableList(getLikedProfilesInternal());
	}

	private List<String> getFollowedBlogsInternal() {
		String profileFollowString = (String) jsonRepresenation
				.get(Qorakeys.PROFILEFOLLOW.toString());
		if (profileFollowString != null) {
			String[] profileFollowArray = StringUtils.split(
					profileFollowString, ";");
			return new ArrayList<String>(Arrays.asList(profileFollowArray));
		}

		return new ArrayList<String>();
	}

	private List<String> getLikedProfilesInternal() {
		String profileLikeString = (String) jsonRepresenation
				.get(Qorakeys.PROFILELIKE.toString());
		if (profileLikeString != null) {
			String[] profileLikeArray = StringUtils.split(profileLikeString,
					";");
			return new ArrayList<String>(Arrays.asList(profileLikeArray));
		}

		return new ArrayList<String>();
	}

	public void addFollowedBlog(String blogname) {
		addRemoveFollowedInternal(blogname, false);
	}

	public void removeFollowedBlog(String blogname) {
		addRemoveFollowedInternal(blogname, true);
	}

	public void addRemoveFollowedInternal(String blogname, boolean isRemove) {
		Name blogName = DBSet.getInstance().getNameMap().get(blogname);
		if (blogName != null) {
			Profile profile = Profile.getProfileOpt(blogName);
			// ADDING ONLY IF ENABLED REMOVE ALWAYS
			if (isRemove
					|| (profile != null && profile.isProfileEnabled() && profile
							.isBlogEnabled())) {
				List<String> followedBlogsInternal = getFollowedBlogsInternal();
				if (isRemove) {
					followedBlogsInternal.remove(blogname);
				} else {
					if (!followedBlogsInternal.contains(blogname)) {
						followedBlogsInternal.add(blogname);
					}
				}
				String joinResult = StringUtils
						.join(followedBlogsInternal, ";");
				jsonRepresenation.put(Qorakeys.PROFILEFOLLOW.toString(),
						joinResult);
			}
		}
	}

	public void addLikeProfile(String profilename) {
		addRemoveLikeInternal(profilename, false);
	}

	public void removeLikeProfile(String profilename) {
		addRemoveLikeInternal(profilename, true);
	}

	public void addRemoveLikeInternal(String profilename, boolean isRemove) {
		Profile profileOpt = Profile.getProfileOpt(profilename);
		// ADDING ONLY IF ENABLED REMOVE ALWAYS
		if (isRemove || (profileOpt != null && profileOpt.isProfileEnabled())) {
			List<String> likedProfilesInternal = getLikedProfilesInternal();
			if (isRemove) {
				likedProfilesInternal.remove(profilename);
			} else {
				if (!likedProfilesInternal.contains(profilename)) {
					likedProfilesInternal.add(profilename);
				}
			}
			String joinResult = StringUtils.join(likedProfilesInternal, ";");
			jsonRepresenation.put(Qorakeys.PROFILELIKE.toString(), joinResult);
		}
	}

	public boolean isProfileEnabled() {
		return jsonRepresenation.containsKey(Qorakeys.PROFILEENABLE.toString());
	}

	public void setProfileEnabled(boolean enabled) {
		if (enabled) {
			jsonRepresenation.put(Qorakeys.PROFILEENABLE.toString(), "");
		} else {
			jsonRepresenation.remove(Qorakeys.PROFILEENABLE.toString());
		}
	}

	public void setBlogEnabled(boolean enabled) {
		if (enabled) {
			jsonRepresenation.put(Qorakeys.BLOGENABLE.toString(), "");
		} else {
			jsonRepresenation.remove(Qorakeys.BLOGENABLE.toString());
		}
	}

	public boolean isBlogEnabled() {
		return jsonRepresenation.containsKey(Qorakeys.BLOGENABLE.toString());
	}

	public BlogBlackWhiteList getBlogBlackWhiteList() {
		return blogBlackWhiteList;
	}

	public String saveProfile() throws WebApplicationException {
		Pair<String, String> jsonKeyPairRepresentation = blogBlackWhiteList
				.getJsonKeyPairRepresentation();
		jsonRepresenation.put(jsonKeyPairRepresentation.getA(),
				jsonKeyPairRepresentation.getB());
		if (blogBlackWhiteList.isWhitelist()) {
			jsonRepresenation.remove(Qorakeys.BLOGBLACKLIST.toString());
		} else {
			jsonRepresenation.remove(Qorakeys.BLOGWHITELIST.toString());
		}

		String jsonString = jsonRepresenation.toJSONString();
		String compressValue = GZIP.compress(jsonString);
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("fee", Controller.getInstance()
				.calcRecommendedFeeForNameUpdate(name.getName(), compressValue)
				.getA().toPlainString());
		jsonObject.put("newowner", name.getOwner().getAddress());
		jsonObject.put("newvalue", compressValue);

		return new NamesResource().updateName(jsonObject.toJSONString(),
				name.getName());
	}

	public Name getName() {
		return name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
