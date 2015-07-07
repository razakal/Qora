package qora.web.blog;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import qora.naming.Name;
import qora.web.Profile;
import utils.LinkUtils;
import utils.Pair;
import webserver.WebResource;
import database.DBSet;

/**
 * This is the representation of an entry in a blog.
 * 
 * @author Skerberus
 *
 */
public class BlogEntry {

	private String titleOpt;
	private String description;
	private String nameOpt;
	private final long time;
	private final String creator;
	private String 	avatar = "img/qora-user.png";
	private final Profile profileOpt;

	private List<String> imagelinks = new ArrayList<String>();

	public BlogEntry(String titleOpt, String description, String nameOpt,
			long timeOpt, String creator) {
		this.titleOpt = titleOpt;
		this.description = description.replaceAll("\n", "<br/>");
		this.description = Jsoup.clean(this.description, Whitelist.basic());
		handleImages();
		handleLinks();
		this.nameOpt = nameOpt;
		profileOpt = Profile.getProfileOpt(nameOpt);
		addAvatar();
		this.time = timeOpt;
		this.creator = creator;
	}

	private void addAvatar() {
		if (nameOpt != null) {
			Name name = DBSet.getInstance().getNameMap().get(nameOpt);
			Profile profile = Profile.getProfileOpt(name);
			if(profile != null)
			{
				String avatarOpt = profile.getAvatarOpt();
				if (avatarOpt != null && StringUtils.isNotBlank(avatarOpt)) {
					avatar = avatarOpt;
				}
			}
		}
	}

	private void handleImages() {
		Pattern pattern = Pattern.compile(Pattern.quote("[img]") + "(.+)" + Pattern.quote("[/img]"));
		Matcher matcher = pattern.matcher(description);
		while (matcher.find()) {
			String url = matcher.group(1);
			imagelinks.add(url);
			description = description.replace(matcher.group(), getImgHtml(url));
		}

	}
	
	
	private String getImgHtml(String url)
	{
		try {
			String template = WebResource.readFile("web/imgtemplate",
					StandardCharsets.UTF_8);
			return template.replace("{{url}}", url);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}

	private void handleLinks() {
		List<Pair<String, String>> linkList = LinkUtils
				.createHtmlLinks(LinkUtils.getAllLinks(description));

		for (Pair<String, String> link : linkList) {
			String originalLink = link.getA();
			String newLink = link.getB();
			if(!imagelinks.contains(originalLink))
			{
				description = description.replace(originalLink, newLink);
			}
		}

	}

	public String getTitleOpt() {
		return titleOpt;
	}

	public String getDescription() {
		return description;
	}

	public String getNameOpt() {
		return nameOpt;
	}

	/**
	 * Returns name if name not null creator else
	 * 
	 * @return
	 */
	public String getNameOrCreator() {
		return nameOpt == null ? creator : nameOpt;
	}

	public long getTime() {
		return time;
	}

	public String getCreator() {
		return creator;
	}

	public String getCreationTime() {
		Date date = new Date(time);
		DateFormat format = DateFormat.getDateTimeInstance();
		return format.format(date);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public String getAvatar() {
		return avatar;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson() {
		
		JSONObject json = new JSONObject();
		
		json.put("nameOrCreator", this.getNameOrCreator());
		json.put("creationTime", this.getCreationTime());
		json.put("titleOpt", this.getTitleOpt());
		json.put("description", this.getDescription());
		json.put("avatar", this.getAvatar());
		
		return json;	
	}

	public Profile getProfileOpt() {
		return profileOpt;
	}
}
