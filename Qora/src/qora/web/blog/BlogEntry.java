package qora.web.blog;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import utils.LinkUtils;
import utils.Pair;

/**
 * This is the representation of an entry in a blog.
 * @author Skerberus
 *
 */
public class BlogEntry {
	
	
	private String titleOpt;
	private String description;
	private String nameOpt;
	private final long time;
	private final String creator;

	public BlogEntry(String titleOpt, String description, String nameOpt, long timeOpt, String creator) {
		this.titleOpt = titleOpt;
		description	= description.replaceAll("\n", "<br/>");
		this.description = Jsoup.clean(description, Whitelist.basic());
		handleLinks();
		this.nameOpt = nameOpt;
		this.time = timeOpt;
		this.creator = creator;
	}

	private void handleLinks() {
		List<Pair<String, String>> linkList = LinkUtils.createHtmlLinks(LinkUtils.getAllLinks(description));

		for (Pair<String, String> link : linkList) {
			String originalLink = link.getA();
			String newLink = link.getB();

			description = description.replace(originalLink, newLink);
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
	 * @return
	 */
	public String getNameOrCreator()
	{
		return nameOpt == null? creator : nameOpt;
	}


	public long getTime() {
		return time;
	}


	public String getCreator() {
		return creator;
	}
	public String getCreationTime()
	{
		Date date = new Date( time );
		DateFormat format = DateFormat.getDateTimeInstance();
		return format.format(date);
	}
	
	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
	
	
}
