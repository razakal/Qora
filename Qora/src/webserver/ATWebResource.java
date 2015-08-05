package webserver;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bouncycastle.util.encoders.Hex;

import com.google.common.collect.Lists;

import qora.crypto.Base58;
import qora.transaction.Transaction;
import at.AT;
import at.AT_API_Helper;
import at.AT_Transaction;
import database.DBSet;

public class ATWebResource {
	
	private static ATWebResource instance = new ATWebResource();
	
	public static ATWebResource getInstance()
	{
		if ( instance == null )
		{
			instance = new ATWebResource();
		}
		return instance;
	}
	
	private AT getAT(String id)
	{
		return DBSet.getInstance().getATMap().get(id);
	}
	
	public Collection<String> getIdsByType(String type)
	{
		return DBSet.getInstance().getATMap().getTypeATsList(type);
	}
	
	public List<Transaction> getIncomingTransactions(String atId)
	{
		//TODO
		return null; 
	}
	
	public List<Transaction> getIncomingTransactions(AT at)
	{
		//TODO
		return null;
	}
	
	public List<AT_Transaction> getOutgoingTransactions(String atId)
	{
		return DBSet.getInstance().getATTransactionMap().getATTransactionsBySender( atId );
	}
	
	public List<AT_Transaction> getOutgoingTransactions(AT at)
	{
		return getOutgoingTransactions(Base58.encode(at.getId()));
	}
	
	public Collection<String> getATsByCreator(String creator)
	{
		return Lists.newArrayList(DBSet.getInstance().getATMap().getATsByCreator(creator));
	}
	
	public Collection<String> getOrderedATs(int height)
	{
		return Lists.newArrayList(DBSet.getInstance().getATMap().getOrderedATs(height));
	}
	
	public String getAsHex(String atId, String startPos, String endPos)
	{
		int start = Integer.valueOf(startPos);
		int end = Integer.valueOf(endPos);
		AT at = getAT(atId);
		return Hex.toHexString(Arrays.copyOfRange(at.getAp_data().array(), start, end ));
	}
	
	public String getAsHex(String atId, int startPos, int endPos)
	{
		AT at = getAT(atId);
		return Hex.toHexString(Arrays.copyOfRange(at.getAp_data().array(), startPos, endPos ));
	}
	
	public String getAsBase58(String atId, String startPos, String endPos)
	{
		int start = Integer.valueOf(startPos);
		int end = Integer.valueOf(endPos);
		AT at = getAT(atId);
		
		return Base58.encode(Arrays.copyOfRange(at.getAp_data().array(), start, end ));
	}
	
	public Long getLong(String atId, String startPos)
	{
		int start = Integer.valueOf(startPos);
		AT at = getAT(atId);
		
		return AT_API_Helper.getLong(Arrays.copyOfRange(at.getAp_data().array(), start, start + 8 ));
	}
	
	public int getInt(String atId, String startPos)
	{
		int start = Integer.valueOf(startPos);
		AT at = getAT(atId);
		return at.getAp_data().getInt(start);
	}
	
	public String getDesc(String atId)
	{
		AT at = getAT(atId);
		return at.getDescription();
	}
	
	public String getName(String atId)
	{
		AT at = getAT(atId);
		return at.getName();
	}
	
	public int getCHeight(String atId)
	{
		AT at = getAT(atId);
		return at.getCreationBlockHeight();
	}
	
	public String getCreator(String atId)
	{
		AT at = getAT(atId);
		return Base58.encode(at.getCreator());
	}
	
	public String getTags(String atId)
	{
		AT at = getAT(atId);
		return at.getTags();
	}
	
	public String getType(String atId)
	{
		AT at = getAT(atId);
		return at.getType();
	}
	
	public String getBalance(String atId)
	{
		AT at = getAT(atId);
		return BigDecimal.valueOf(at.getG_balance(),8).toPlainString();
	}
	
	
	
}
