/*
 * Copyright (c) 2014 CIYAM Developers

 Distributed under the MIT/X11 software license, please refer to the file license.txt
 in the root project directory or http://www.opensource.org/licenses/mit-license.php.

 */

package at;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.json.simple.JSONObject;

import qora.crypto.Base58;
import utils.Converter;


public class AT_Transaction{
	
	private byte[] senderId = new byte[ AT_Constants.AT_ID_SIZE ];
	private byte[] recipientId = new byte[ AT_Constants.AT_ID_SIZE ];
	private long amount;
	private byte[] message;
	private int blockHeight;
	private int seq;
	private final int BASE_SIZE = 2 * AT_Constants.AT_ID_SIZE + 8 + 4 + 4 + 4;
	
	AT_Transaction( byte[] senderId , byte[] recipientId , long amount , byte[] message ){
		this.senderId = senderId.clone();
		this.recipientId = recipientId.clone();
		this.amount = amount;
		this.message = (message != null) ? message.clone() : null;
	}
	
	public AT_Transaction(int blockHeight, int seq, byte[] senderId,
			byte[] recipientId, long amount, byte[] message) {
		this.blockHeight = blockHeight;
		this.seq = seq;
		this.senderId = senderId;
		this.recipientId = recipientId;
		this.amount = amount;
		this.message = message;
	}

	public Long getAmount(){
		return amount;
	}
	
	public byte[] getSenderId(){
		return senderId;
	}
	
	public byte[] getRecipientId(){
		return recipientId;
	}
	
	public byte[] getMessage() {
		return message;
	}
	
	public int getBlockHeight()
	{
		return this.blockHeight;
	}
	
	public int getSeq()
	{
		return this.seq;
	}
	
	public int getSize()
	{
		return ( message != null ) ? BASE_SIZE + message.length : BASE_SIZE;
	}
	
	public byte[] toBytes()
	{
		int size = BASE_SIZE;
		if ( message!= null )
		{
			size += message.length;
		}
		
		ByteBuffer bf = ByteBuffer.allocate( size );
		bf.order( ByteOrder.LITTLE_ENDIAN );
		bf.clear();
		
		bf.putInt( blockHeight );
		bf.putInt( seq );
		bf.put( senderId );
		bf.put( (recipientId != null) ? recipientId : new byte[ AT_Constants.AT_ID_SIZE ] );
		bf.putLong( amount );
		bf.putInt( (message != null) ? message.length : 0 );
		if ( message != null )
		{
			bf.put( message );
		}
		
		
		return bf.array().clone();
		
	}
	
	public static AT_Transaction fromBytes( byte[] data )
	{
		ByteBuffer bf = ByteBuffer.wrap( data );
		bf.order( ByteOrder.LITTLE_ENDIAN );
		bf.clear();
		
		int blockHeight = bf.getInt();
		int seq = bf.getInt();
		byte[] senderId = new byte[AT_Constants.AT_ID_SIZE];
		byte[] recipientId = new byte[AT_Constants.AT_ID_SIZE];
		bf.get( senderId, 0, senderId.length );
		bf.get( recipientId, 0, recipientId.length );
		long amount = bf.getLong();
		int messageLength = bf.getInt();
		byte[] message = null;
		if ( messageLength > 0 )
		{
			message = new byte[messageLength];
			bf.get(message, 0, messageLength);
		}
		
		return new AT_Transaction(blockHeight, seq, senderId, recipientId, amount, message);
		
	}
	
	public void setBlockHeight(Integer blockHeight) {
		this.blockHeight = blockHeight;
	}
	
	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public String getRecipient() {
		return Base58.encode(recipientId);
	}

	public String getSender() {
		return Base58.encode(senderId);
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJSON() {
		JSONObject ob = new JSONObject();
		ob.put("blockHeight", blockHeight);
		ob.put("seq", seq);
		ob.put("sender", getSender());
		ob.put("recipient", getRecipient());
		ob.put("amount", BigDecimal.valueOf( amount , 8).toPlainString());
		ob.put("message", ( message != null ) ? Converter.toHex(message) : "");
		return ob;
	}
	
}
