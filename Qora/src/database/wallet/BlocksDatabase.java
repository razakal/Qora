package database.wallet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.block.Block;
import qora.block.BlockFactory;
import utils.Pair;

public class BlocksDatabase {

	private static final String BLOCKS = "_blocks";
	
	private DB database;
	
	public BlocksDatabase(WalletDatabase walletDatabase, DB database) 
	{
		this.database = database;
	}
	
	public List<Block> getLastBlocks(Account account)
	{
		List<Block> blocks = new ArrayList<Block>();
		
		try
		{
			///OPEN MAP 
			NavigableSet<byte[]> blocksSet = this.database.createTreeSet(account.getAddress() + BLOCKS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
			Iterator<byte[]> iterator = blocksSet.descendingIterator();
			
			for(int i=0; i<50 && iterator.hasNext(); i++)
			{
				//GET SIGNATURE
				byte[] rawBlock = iterator.next();
				
				//GET BLOCK
				Block block = BlockFactory.getInstance().parse(rawBlock);
					
				//ADD TO LIST
				blocks.add(block);
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return blocks;
	}
	
	public List<Pair<Account, Block>> getLastBlocks(List<Account> accounts)
	{
		List<Pair<Account, Block>> blocks = new ArrayList<Pair<Account, Block>>();
		
		try
		{
			synchronized(accounts)
			{
				//FOR EACH ACCOUNTS
				for(Account account: accounts)
				{
					//OPEN MAP 
					NavigableSet<byte[]> blocksSet = this.database.createTreeSet(account.getAddress() + BLOCKS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
					Iterator<byte[]> iterator = blocksSet.descendingIterator();
					
					for(int i=0; i<50 && iterator.hasNext(); i++)
					{
						//GET SIGNATURE
						byte[] rawBlock = iterator.next();
						
						//GET BLOCK
						Block block = BlockFactory.getInstance().parse(rawBlock);
							
						//ADD TO LIST
						blocks.add(new Pair<Account, Block>(account, block));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return blocks;
	}
	
	public void delete(Block block)
	{
		NavigableSet<byte[]> blocksMap = this.database.createTreeSet(block.getGenerator().getAddress() + BLOCKS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		blocksMap.remove(block.toBytes());
	}
	
	public void delete(Account account)
	{
		this.database.delete(account.getAddress() + BLOCKS);
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}

	public void addAll(Map<Account, List<Block>> blocks) 
	{
		//FOR EACH ACCOUNT
	    for(Account account: blocks.keySet())
	    {
	    	NavigableSet<byte[]> blocksMap = this.database.createTreeSet(account.getAddress() + BLOCKS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
	    	//FOR EACH BLOCK
	    	for(Block block: blocks.get(account))
	    	{
	    		blocksMap.add(block.toBytes());
	    	}
	    }
		
	}
	
	public void add(Block block)
	{
		//OPEN BLOCK SET
		NavigableSet<byte[]> blocksSet = this.database.createTreeSet(block.getGenerator().getAddress() + BLOCKS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		
		//ADD TO SET
		blocksSet.add(block.toBytes());
	}
}
