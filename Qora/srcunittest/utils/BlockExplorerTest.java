package utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;
import org.junit.Test;
import controller.Controller;
import qora.account.Account;
import qora.blockexplorer.BlockExplorer;


public class BlockExplorerTest {

	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void blockExplorer() throws Exception {
		
		Controller.getInstance().start();
		//DBSet.getInstance();
		
		ArrayList<String> addrs = new ArrayList();
		addrs.add("QXncuwPehVZ21ymE1jawXg1Uv3sZZ4TvYk");
		addrs.add("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU");
		addrs.add("QQPsGx3khgEboJXWPiDBMVDG5ngu9wDo3k");
		addrs.add("Qd9jQKZSXoYgFypTQySJUSbXcZvjgdiemn");
		addrs.add("QfyocFSGghfpANqUmQFpoG2sk5TVg8LvEm");
		addrs.add("QMu6HXfZCnwaNmyFjjhWTYAUW7k1x7PoVr");
		addrs.add("QdrhixdevE7ZJqSHAfV19yVYrYsys8VLgz");
		
		/*
		Cancel Order:
		Payment: 
		Name Registration: 
		Name Update: 
		Name Sale: 
		Cancel Name	Sale: 
		Name Purchase: 
		Poll Creation:
		Arbitrary Transaction:
		Asset Transfer:
		Poll Vote: 
		Asset Issue: 
		Order Creation:
		Multi Payment:
		Message:
		Deploy AT:
		Genesis:
		//+Trades: 
		//Generated blocks:
		//AT Transactions:
		
		17 tx type
		*/
		
		int start = -1;
		int txOnPage = 10;
		String filter = "standart";
		boolean withoutBlocks = false;
		boolean allOnOnePage = false;
		String showOnly = "";
		String showWithout = "";
		
		for(int i = 0; i < addrs.size(); i++) {
			
			String addr = addrs.get(i);
		
			Map<Object, Map> output = BlockExplorer.getInstance().jsonQueryAddress(addr, start, txOnPage, filter, allOnOnePage, withoutBlocks, showOnly, showWithout);
	
			Map<Object, Map> totalBalance = output.get("totalBalance");
			
			Account account = new Account(addr);
			
			System.out.println(addr);
			for(Map.Entry<Object, Map> e : totalBalance.entrySet())
			{
				if(e.getKey() instanceof Long)
				{
					Long key = (Long) e.getKey();
					
					BigDecimal blockExplorerBalance =  new BigDecimal((String) e.getValue().get("amount"));
					
					System.out.print("(" + key + ") " + e.getValue().get("assetName") + " BlockExplorerBalance: " + blockExplorerBalance);
					
					BigDecimal nativeBalance = account.getConfirmedBalance(key);
					
					System.out.print("; NantiveBalance: " + nativeBalance);
					
					if(blockExplorerBalance.equals(nativeBalance))
					{
						System.out.println(" OK.");
					}
					else
					{
						System.out.println(" Fail!!!");
					}
					
					assertEquals(blockExplorerBalance, nativeBalance);
				}
			}
		}
		
		Controller.getInstance().stopAll();	
	}
}
