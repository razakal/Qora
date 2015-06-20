package qora.web.blog;

import java.text.DateFormat;
import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * This is the representation of an entry in a blog.
 * @author Skerberus
 *
 */
public class BlogEntry {
	
	
	private String titleOpt;
	private final String description;
	private String nameOpt;
	private final long time;
	private final String creator;

	public BlogEntry(String titleOpt, String description, String nameOpt, long timeOpt, String creator) {
		this.titleOpt = titleOpt;
		description	= description.replaceAll("\n", "<br/>");
		this.description = description;
		this.nameOpt = nameOpt;
		this.time = timeOpt;
		this.creator = creator;
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
