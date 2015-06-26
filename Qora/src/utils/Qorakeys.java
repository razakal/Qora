package utils;

public enum Qorakeys {

	DEFAULT("defaultkey"), WEBSITE("website"), BLOGWHITELIST("blogwhitelist"), BLOGBLACKLIST(
			"blogblacklist"), BLOGDESCRIPTION("blogdescription"), BLOGTITLE(
			"blogtitle"), BLOGENABLE("blogenable"), PROFILEENABLE("profileenable"), PROFILEAVATAR("profileavatar");
	private final String keyname;

	private Qorakeys(String keyname) {
		this.keyname = keyname;
	}

	public String getKeyname() {
		return keyname;
	}

	public String toString() {
		return keyname;
	}

}
