package qora.web;

import java.util.List;

public class HTMLSearchResult {

	
	private String title;
	private String description;
	private String titlelink;
	private String namelink;
	private String keyslink;
	private String name;
	private List<String> followerOpt;

	public HTMLSearchResult(String title, String description,String name, String titlelink, String namelink, String keyslink, List<String> followerOpt) {
		this.title = title;
		this.description = description;
		this.name = name;
		this.titlelink = titlelink;
		this.namelink = namelink;
		this.keyslink = keyslink;
		this.followerOpt = followerOpt;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getTitlelink() {
		return titlelink;
	}

	public String getNamelink() {
		return namelink;
	}

	public String getKeyslink() {
		return keyslink;
	}

	public String getName() {
		return name;
	}

	public List<String> getFollowerOpt() {
		return followerOpt;
	}





	
}
