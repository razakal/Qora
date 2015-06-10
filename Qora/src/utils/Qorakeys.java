package utils;

public enum Qorakeys {

	
	DEFAULT("defaultkey"),WEBSITE("website");
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
