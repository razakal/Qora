package qora.web;

public enum NavbarElements {
	BlogNavbar("web/blogleftnavbar.html"), Searchnavbar("web/searchnavbar.html"), NoNavbar("");
	
	private String url;

	private NavbarElements(String url)
	{
		this.url = url;
		
	}

	public String getUrl() {
		return url;
	}

}
