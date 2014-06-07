package utils;

import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import database.DatabaseSet;
import qora.naming.NameSale;

public class NameSalesList extends AbstractList<NameSale> 
{
	private List<String> nameSaleKeys;
	private Map<String, NameSale> nameSales;
	
	public NameSalesList(List<String> nameSaleKeys)
	{
		this.nameSaleKeys = nameSaleKeys;
		this.nameSales= new HashMap<String, NameSale>();
	}
	
	
	@Override
	public NameSale get(int index) 
	{
		if(!this.nameSales.containsKey(this.nameSaleKeys.get(index)))
		{
			this.nameSales.put(this.nameSaleKeys.get(index), DatabaseSet.getInstance().getNameExchangeDatabase().getNameSale(this.nameSaleKeys.get(index)));
		}
		
		return this.nameSales.get(this.nameSaleKeys.get(index));
	}

	@Override
	public int size() 
	{
		return this.nameSaleKeys.size();
	}

}
