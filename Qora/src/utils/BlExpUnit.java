package utils;

import at.AT_Transaction;
import qora.assets.Trade;
import qora.block.Block;
import qora.transaction.Transaction;

public class BlExpUnit implements Comparable<BlExpUnit>
{
	private Object unit;
	private int height;
	private int seq;
	
	public BlExpUnit(int height, int seq, Object unit)
	{
		this.unit = unit;
		this.height = height;
		this.seq = seq;
	}
	
	public Object getUnit()
	{
		return this.unit;
	}
	
	public int getHeight()
	{
		return this.height;
	}
	
	public int getSeq()
	{
		return this.seq;
	}
	
	@Override 
 	public boolean equals(Object other) {
		if (!(other instanceof BlExpUnit)) {
			return false;
		}
		Object otherUnit = ((BlExpUnit) other).getUnit();
		return otherUnit.equals(this.unit);
	}

	@Override 
	public int hashCode() {
		return Integer.MIN_VALUE + height * 5*700 + seq + getOrder(this.unit)*700;
	}

	private int getOrder(Object unit)
	{
		if(unit instanceof Block)
		{
			return 4; 
		}
		else if(unit instanceof AT_Transaction)
		{
			return 3;
		}
		else if(unit instanceof Trade)
		{
			return 2;
		}
		else if(unit instanceof Transaction)
		{
			return 1;
		}
		
		return 0;
	}
	
	@Override 
	public int compareTo(BlExpUnit other) {
		
		int orderMy = getOrder(this.unit); 
		int orderOther = getOrder(other.getUnit()); 
		
		if (this.height > other.getHeight())
			return 1;
		else if (this.height < other.getHeight())
			return -1;
		else
			if (orderMy > orderOther)
				return 1;
			else if (orderMy < orderOther)
				return -1;
			else
				if (this.seq > other.getSeq())
					return 1;
				else if (this.seq  < other.getSeq())
					return -1;
				else
					return 0;
	}
}