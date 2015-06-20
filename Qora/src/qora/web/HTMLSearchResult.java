package qora.web;

public class HTMLSearchResult {

	
	private String title;
	private String description;
	private String titlelink;
	private String namelink;
	private String keyslink;
	private String name;

	public HTMLSearchResult(String title, String description,String name, String titlelink, String namelink, String keyslink) {
		this.title = title;
		this.description = description;
		this.name = name;
		this.titlelink = titlelink;
		this.namelink = namelink;
		this.keyslink = keyslink;
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




	
}
