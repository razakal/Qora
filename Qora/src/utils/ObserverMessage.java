package utils;

public class ObserverMessage {

	public static final int ADD_BLOCK_TYPE = 1;
	public static final int REMOVE_BLOCK_TYPE = 2;
	public static final int LIST_BLOCK_TYPE = 3;
	
	public static final int ADD_TRANSACTION_TYPE = 4;
	public static final int REMOVE_TRANSACTION_TYPE = 5;
	public static final int LIST_TRANSACTION_TYPE = 6;
	
	public static final int ADD_PEER_TYPE = 7;
	public static final int REMOVE_PEER_TYPE = 8;
	public static final int LIST_PEER_TYPE = 9;
	
	public static final int ADD_ACCOUNT_TYPE = 10;
	public static final int REMOVE_ACCOUNT_TYPE = 11;
	
	public static final int WALLET_STATUS = 12;
	public static final int NETWORK_STATUS = 13;
	
	public static final int ADD_NAME_TYPE = 14;
	public static final int REMOVE_NAME_TYPE = 15;
	public static final int LIST_NAME_TYPE = 16;
	
	public static final int ADD_NAME_SALE_TYPE = 17;
	public static final int REMOVE_NAME_SALE_TYPE = 18;
	public static final int LIST_NAME_SALE_TYPE = 19;

	public static final int ADD_POLL_TYPE = 20;
	public static final int REMOVE_POLL_TYPE = 21;
	public static final int LIST_POLL_TYPE = 22;
	
	public static final int ADD_ASSET_TYPE = 23;
	public static final int REMOVE_ASSET_TYPE = 24;
	public static final int LIST_ASSET_TYPE = 25;
	
	private int type;
	private Object value;
	
	public ObserverMessage(int type, Object value)
	{
		this.type = type;
		this.value = value;
	}
	
	public int getType()
	{
		return this.type;
	}
	
	public Object getValue()
	{
		return this.value;
	}
	
}
