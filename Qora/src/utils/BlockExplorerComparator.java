package utils;

import java.util.Comparator;

import controller.Controller;
import at.AT_Transaction;
import qora.assets.Trade;
import qora.block.Block;
import qora.transaction.Transaction;

public class BlockExplorerComparator implements Comparator<Object> {
	
	@Override
	public int compare(Object one, Object two) 
	{
		long oneTime = 0;
		long twoTime = 0;
		
		if(one instanceof AT_Transaction)
		{
			if(two instanceof AT_Transaction)
			{
				if(((AT_Transaction)one).getBlockHeight() == ((AT_Transaction)two).getBlockHeight())
				{
					return ((AT_Transaction)two).getSeq() - ((AT_Transaction)one).getSeq();
				}
				
				return ((AT_Transaction)two).getBlockHeight() - ((AT_Transaction)one).getBlockHeight();
			}
			if(two instanceof Transaction)
			{
				return ((Transaction)two).getParent().getHeight() - ((AT_Transaction)one).getBlockHeight();			
			}
			if(two instanceof Block)
			{
				return ((Block)two).getHeight() - ((AT_Transaction)one).getBlockHeight();			
			}
			if(two instanceof Trade)
			{
				return Controller.getInstance().getTransaction((((Trade)two).getTarget().toByteArray())).getParent().getHeight() - ((AT_Transaction)one).getBlockHeight();			
			}
		}
		
		if(two instanceof AT_Transaction)
		{
			if(one instanceof AT_Transaction)
			{
				if(((AT_Transaction)one).getBlockHeight() == ((AT_Transaction)two).getBlockHeight())
				{
					return ((AT_Transaction)two).getSeq() - ((AT_Transaction)one).getSeq();
				}
				
				return ((AT_Transaction)two).getBlockHeight() - ((AT_Transaction)one).getBlockHeight();
			}
			if(one instanceof Transaction)
			{
				return ((AT_Transaction)two).getBlockHeight() - ((Transaction)one).getParent().getHeight();			
			}
			if(one instanceof Block)
			{
				return ((AT_Transaction)two).getBlockHeight() - ((Block)one).getHeight();			
			}
			if(one instanceof Trade)
			{
				return ((AT_Transaction)two).getBlockHeight() - Controller.getInstance().getTransaction((((Trade)one).getTarget().toByteArray())).getParent().getHeight();			
			}
		}
		
		if (one instanceof Transaction)
		{
			oneTime = ((Transaction)one).getTimestamp();
		}
		
		if (one instanceof Block)
		{
			oneTime = ((Block)one).getTimestamp();
		}
		
		if (one instanceof Trade)
		{
			oneTime = ((Trade)one).getTimestamp();
		}
		
		
		if (two instanceof Transaction)
		{
			twoTime = ((Transaction)two).getTimestamp();
		}
		
		if (two instanceof Block)
		{
			twoTime = ((Block)two).getTimestamp();
		}
		
		if (two instanceof Trade)
		{
			twoTime = ((Trade)two).getTimestamp();
		}
		
		if(oneTime < twoTime)
			return 1;
		else if(oneTime > twoTime) 
			return -1;
		else
			if(one instanceof Trade && two instanceof Transaction)
		    	return -1;
		    else if(one instanceof Transaction && two instanceof Trade)
		    	return 1;
		    else
		    	return 0;
	}
}

