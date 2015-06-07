package utils;

public enum Qorakeys {

	
	WEBSITE("website");
	private final String keyname;
	
	private Qorakeys(String keyname) {
		this.keyname = keyname;
	}

	public String getKeyname() {
		return keyname;
	}

}
