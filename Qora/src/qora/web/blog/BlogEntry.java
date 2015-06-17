package qora.web.blog;

import java.text.SimpleDateFormat;
import java.util.Date;

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
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		Date dt = new Date();
		return sdf.format(dt); // formats to 09/23/2009 13:53:28.238
	}
	
	
}
