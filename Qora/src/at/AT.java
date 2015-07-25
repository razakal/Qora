package at;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Iterator;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.crypto.Base58;
import database.DBSet;

public class AT extends AT_Machine_State {

	private String name;
	private String description;
	private String type;
	private String tags;
	
	public AT(byte[] atId, byte[] creator, String name , String description, String type, String tags , byte[] creationBytes, int height) {
		super(atId, creator, creationBytes, height);
		this.name = name;
		this.description = description;
		this.type = type;
		this.tags = tags;
	}
	
	
	public AT ( byte[] atId , byte[] creator , String name , String description, String type, String tags , short version ,
			byte[] stateBytes, int csize , int dsize , int c_user_stack_bytes , int c_call_stack_bytes ,
			long minActivationAmount , int creationBlockHeight, int sleepBetween , 
			byte[] apCode )
	{
		super( 	atId , creator , version ,
				stateBytes , csize , dsize , c_user_stack_bytes , c_call_stack_bytes ,
				creationBlockHeight , sleepBetween , 
				minActivationAmount ,apCode );
		this.name = name;
		this.description = description;
		this.type = type;
		this.tags = tags;
	}

	public static AT getAT( String id, DBSet dbSet ) 
	{
		return getAT( Base58.decode( id ) , dbSet );
	}
	
	public static AT getAT( byte[] atId, DBSet dbSet)
	{
		AT at = dbSet.getATMap().getAT( atId );
		
		return at; 
	}

	public static AT getAT(byte[] atId) {
		return getAT( atId, DBSet.getInstance() );
	}

	public static Iterator<String> getOrderedATs( DBSet dbSet, Integer height ) 
	{
		return dbSet.getATMap().getOrderedATs(height);
	}

	//public int getDataLength() {
	//	return name.length() + description.length() + this.getStateSize();
	//}

	public byte[] toBytes(boolean b) {
		
		
		byte[] bname = getName().getBytes( Charset.forName( "UTF-8" ) );
		byte[] bdesc = description.getBytes( Charset.forName( "UTF-8" ) );
		byte[] btype = type.getBytes( Charset.forName("UTF-8") );
		byte[] btags = tags.getBytes( Charset.forName("UTF-8") );
		
		ByteBuffer bf = ByteBuffer.allocate( 4 + bname.length + 4 + bdesc.length + getSize() + 4 + btype.length + 4 + btags.length );
		bf.order( ByteOrder.LITTLE_ENDIAN );
		
		bf.putInt( bname.length );
		bf.put( bname );
		
		bf.putInt( bdesc.length );
		bf.put( bdesc );
		
		bf.putInt( btype.length );
		bf.put( btype );
		
		bf.putInt( btags.length );
		bf.put( btags );
		
		bf.put( getBytes() );
		
		return bf.array();
		
	}
	
	public int getCreationLength()
	{
		byte[] bname = getName().getBytes( Charset.forName( "UTF-8" ) );
		byte[] bdesc = description.getBytes( Charset.forName( "UTF-8" ) );
		byte[] btype = type.getBytes( Charset.forName("UTF-8") );
		byte[] btags = tags.getBytes( Charset.forName("UTF-8") );
		
		return 4 + bname.length + 4 + bdesc.length + 4 + btype.length + 4 + btags.length + getSize();
	}
	
	
	public static AT parse(byte[] bytes) 
	{
		ByteBuffer bf = ByteBuffer.allocate( bytes.length );
		bf.order( ByteOrder.LITTLE_ENDIAN );
		bf.put( bytes );
		bf.clear();
		
		
		int nameSize = bf.getInt();
		byte[] bname = new byte[ nameSize ];
		bf.get( bname );
		String name = new String( bname , Charset.forName( "UTF-8" ) );
		
		int descSize = bf.getInt();
		byte[] bdesc = new byte[ descSize ];
		bf.get( bdesc );
		String description = new String( bdesc , Charset.forName( "UTF-8" ) );
		
		int typeSize = bf.getInt();
		byte[] btype = new byte[ typeSize ];
		bf.get( btype );
		String type = new String( btype , Charset.forName( "UTF-8" ) );
		
		int tagsSize = bf.getInt();
		byte[] btags = new byte[ tagsSize ];
		bf.get( btags );
		String tags = new String( btags , Charset.forName( "UTF-8" ) );
		
		byte[] atId = new byte[ AT_Constants.AT_ID_SIZE ];
		bf.get( atId );
		
		byte[] creator = new byte[ AT_Constants.AT_ID_SIZE ];
		bf.get( creator );
		
		short version = bf.getShort();
		int csize = bf.getInt();
		int dsize = bf.getInt();
		int c_call_stack_bytes = bf.getInt();
		int c_user_stack_bytes = bf.getInt();
		long minActivationAmount = bf.getLong();
		int creationBlockHeight = bf.getInt();
		int sleepBetween = bf.getInt();
		
		byte[] ap_code = new byte[ csize ];
		bf.get( ap_code );
		
		byte[] state = new byte[ bf.capacity() - bf.position() ];
		bf.get( state );
		
		AT at = new AT( atId , creator , name , description, type, tags , version ,
				state , csize , dsize , c_user_stack_bytes , c_call_stack_bytes , minActivationAmount , creationBlockHeight , sleepBetween , 
				ap_code );
		
		return at;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJSON()
	{
		JSONObject atJSON = new JSONObject();
		atJSON.put("accountBalance", new Account(Base58.encode(getId())).getConfirmedBalance().toPlainString() );
		atJSON.put("name", this.name);
		atJSON.put("description", description);
		atJSON.put("type", type);
		atJSON.put("tags", tags);
		atJSON.put("version", getVersion());
        atJSON.put("minActivation", BigDecimal.valueOf( minActivationAmount() , 8 ).toPlainString() );
        atJSON.put("creationBlock", getCreationBlockHeight());
		atJSON.put("state", getStateJSON());
		atJSON.put("creator", new Account(Base58.encode(getCreator())).getAddress());
		
		return atJSON;
		
	}


	public String getName() {
		return name;
	}

	public String getDescription() {
		return this.description;
	}

	public String getType() {
		return type;
	}
	
	public String getTags() {
		return tags;
	}



}
