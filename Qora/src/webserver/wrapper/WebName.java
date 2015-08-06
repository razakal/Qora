package webserver.wrapper;

import qora.naming.Name;
import utils.NumberAsString;

/**
 * Used for read only access on names for pebble injection. Here name
 * @author Skerberus
 *
 */
public class WebName {

	
	private final String name;
	private final String owner;
	private final String namebalanceString;

	public WebName(Name name) {
		this.name = name.getName();
		this.owner = name.getOwner().getAddress();
		namebalanceString = NumberAsString.getInstance().numberAsString(name.getOwner().getBalance(0)) + " - " + name.getName();
	}

	public String getName() {
		return name;
	}

	public String getOwner() {
		return owner;
	}

	public String getNameBalanceString() {
		return namebalanceString;
	}
}
