package qora.blockexplorer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple6;

import at.AT;
import at.AT_Transaction;
import controller.Controller;
import database.BalanceMap;
import database.DBSet;
import database.SortableList;
import qora.account.Account;
import qora.assets.Asset;
import qora.assets.Order;
import qora.assets.Trade;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.payment.Payment;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.BuyNameTransaction;
import qora.transaction.CancelOrderTransaction;
import qora.transaction.CancelSellNameTransaction;
import qora.transaction.CreateOrderTransaction;
import qora.transaction.CreatePollTransaction;
import qora.transaction.DeployATTransaction;
import qora.transaction.IssueAssetTransaction;
import qora.transaction.MessageTransaction;
import qora.transaction.MultiPaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.SellNameTransaction;
import qora.transaction.Transaction;
import qora.transaction.TransferAssetTransaction;
import qora.transaction.UpdateNameTransaction;
import qora.transaction.VoteOnPollTransaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import settings.Settings;
import utils.BlExpUnit;
import utils.DateTimeFormat;
import utils.GZIP;
import utils.Pair;
import utils.ReverseComparator;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BlockExplorer
{
	
	private static final Logger LOGGER = Logger.getLogger(BlockExplorer.class);
	private static BlockExplorer blockExplorer;

	public static BlockExplorer getInstance()
	{
		if(blockExplorer == null)
		{
			blockExplorer = new BlockExplorer();
		}

		return blockExplorer;
	}

	public static String timestampToStr(long timestamp)
	{
		return DateTimeFormat.timestamptoString(timestamp);
	}


	public Map jsonQueryMain(UriInfo info)
	{		
		Stopwatch stopwatchAll = new Stopwatch();

		Map output = new LinkedHashMap();

		try {

			if(info.getQueryParameters().containsKey("balance"))
			{
				output.put("lastBlock", jsonQueryLastBlock());
				for (String address : info.getQueryParameters().get("balance")) {
					output.put(address, jsonQueryBalance(address));
				}
				return output;
			}
				
			if(info.getQueryParameters().containsKey("q"))
			{
				output.put("lastBlock", jsonQueryLastBlock());
				output.putAll(jsonQuerySearch(URLDecoder.decode(info.getQueryParameters().getFirst("q"), "UTF-8")));
				return output;
			}

			if(info.getQueryParameters().containsKey("names"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryNames());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("top"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				if(info.getQueryParameters().containsKey("asset"))
				{
					output.putAll(jsonQueryTopRichest(
							Integer.valueOf((info.getQueryParameters().getFirst("top"))),
							Long.valueOf((info.getQueryParameters().getFirst("asset")))
							));
				}
				else
				{
					output.putAll(jsonQueryTopRichest(Integer.valueOf((info.getQueryParameters().getFirst("top"))), 0l ));
				}

				output.put("assets", jsonQueryAssetsLite());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("assetsLite"))
			{
				output.put("assetsLite", jsonQueryAssetsLite());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("assets"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.put("assets", jsonQueryAssets());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("aTs"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.put("aTs", jsonQueryATs());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("polls"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryPools());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("asset"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				if(info.getQueryParameters().get("asset").size() == 1)
				{
					try 
					{
						output.put("asset", jsonQueryAsset(Long.valueOf((info.getQueryParameters().getFirst("asset")))));
					} catch (Exception e) {
						output.put("error", "Asset with given key is missing!");
						return output;
					}
				}

				if(info.getQueryParameters().get("asset").size() == 2)
				{
					long have = Integer.valueOf(info.getQueryParameters().get("asset").get(0));
					long want = Integer.valueOf(info.getQueryParameters().get("asset").get(1));

					output.putAll(jsonQueryTrades(have, want));
				}

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("blocks"))
			{
				int start = -1;

				if(info.getQueryParameters().containsKey("start"))
				{
					start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
				}

				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryBlocks(start));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("lastBlock"))
			{
				output = jsonQueryLastBlock();

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("addr"))
			{
				int start = -1;
				int txOnPage = 100;
				String filter = "standart";
				boolean allOnOnePage = false;
				String showOnly = "";
				String showWithout = "";

				if(info.getQueryParameters().containsKey("start"))
				{
					start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
				}

				if(info.getQueryParameters().containsKey("txOnPage"))
				{
					txOnPage = Integer.valueOf((info.getQueryParameters().getFirst("txOnPage")));
				}

				if(info.getQueryParameters().containsKey("filter"))
				{
					filter = info.getQueryParameters().getFirst("filter");
				}

				if(info.getQueryParameters().containsKey("allOnOnePage"))
				{
					allOnOnePage = true;
				}

				if(info.getQueryParameters().containsKey("showOnly"))
				{
					showOnly = info.getQueryParameters().getFirst("showOnly");
				}
				
				if(info.getQueryParameters().containsKey("showWithout"))
				{
					showWithout = info.getQueryParameters().getFirst("showWithout");
				}
				
				output.put("lastBlock", jsonQueryLastBlock());
				output.putAll(jsonQueryAddress(info.getQueryParameters().get("addr"), start, txOnPage, filter, allOnOnePage, showOnly, showWithout));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("name"))
			{
				int start = -1;
				int txOnPage = 100;
				String filter = "standart";
				boolean allOnOnePage = false;

				if(info.getQueryParameters().containsKey("start"))
				{
					start = Integer.valueOf((info.getQueryParameters().getFirst("start")));
				}

				if(info.getQueryParameters().containsKey("txOnPage"))
				{
					txOnPage = Integer.valueOf((info.getQueryParameters().getFirst("txOnPage")));
				}

				if(info.getQueryParameters().containsKey("filter"))
				{
					filter = info.getQueryParameters().getFirst("filter");
				}

				if(info.getQueryParameters().containsKey("allOnOnePage"))
				{
					allOnOnePage = true;
				}

				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryName(info.getQueryParameters().getFirst("name"), start, txOnPage, filter, allOnOnePage));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("block"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryBlock(info.getQueryParameters().getFirst("block")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("tx"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryTX(info.getQueryParameters().getFirst("tx")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("trade"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryTrade(info.getQueryParameters().getFirst("trade")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("atTx"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryATtx(info.getQueryParameters().getFirst("atTx")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("poll"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryPool(info.getQueryParameters().getFirst("poll")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("unconfirmed"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryUnconfirmedTXs());

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			if(info.getQueryParameters().containsKey("blogposts"))
			{
				output.put("lastBlock", jsonQueryLastBlock());

				output.putAll(jsonQueryBlogPostsTx(info.getQueryParameters().getFirst("blogposts")));

				output.put("queryTimeMs", stopwatchAll.elapsedTime());
				return output;
			}

			output.put("queryTimeMs", stopwatchAll.elapsedTime());

		} catch (Exception e1) {
			output = new LinkedHashMap();
			output.put("error", e1.getLocalizedMessage());
			output.put("help", jsonQueryHelp());
			return output;
		}

		output.put("error", "Not enough parameters.");
		output.put("help", jsonQueryHelp());

		return output;
	}


	public Map jsonQueryHelp()
	{
		Map help = new LinkedHashMap();

		help.put("Unconfirmed Transactions", "blockexplorer.json?unconfirmed");
		help.put("Block", "blockexplorer.json?block={block}");
		help.put("Blocks List", "blockexplorer.json?blocks[&start={height}]");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("Assets List Lite", "blockexplorer.json?assetsLite");
		help.put("Asset", "blockexplorer.json?asset={asset}");
		help.put("Asset Trade", "blockexplorer.json?asset={assetHave}&asset={assetWant}");
		help.put("Polls List", "blockexplorer.json?polls");
		help.put("Poll", "blockexplorer.json?poll={poll}");
		help.put("AT TX", "blockexplorer.json?atTx={atTx}");
		help.put("Trade", "blockexplorer.json?trade={initiatorSignature}/{targetSignature}");
		help.put("Transaction", "blockexplorer.json?tx={txSignature}");
		help.put("Name", "blockexplorer.json?name={name}");
		help.put("Name (additional)", "blockexplorer.json?name={name}&start={offset}&allOnOnePage");
		help.put("Address", "blockexplorer.json?addr={address}");
		help.put("Address (additional)", "blockexplorer.json?addr={address}&start={offset}&allOnOnePage&withoutBlocks&showWithout={1,2,blocks}&showOnly={type}");
		help.put("Top Richest", "blockexplorer.json?top");
		help.put("Top Richest", "blockexplorer.json?top={limit}&asset={asset}");
		help.put("Address All Not Zero", "blockexplorer.json?top=allnotzero");
		help.put("Address All Addresses", "blockexplorer.json?top=all");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("Assets List", "blockexplorer.json?assets");
		help.put("AT List", "blockexplorer.json?aTs");
		help.put("Names List", "blockexplorer.json?names");
		help.put("BlogPosts of Address", "blockexplorer.json?blogposts={addr}");
		help.put("Search", "blockexplorer.json?q={text}");
		help.put("Balance", "blockexplorer.json?balance={address}[&balance=address2...]");
		

		return help;
	}

	public Map jsonQuerySearch(String query)
	{
		Map output=new LinkedHashMap();
		Map foundList=new LinkedHashMap();

		output.put("query", query);

		int i = 0;

		byte[] signatureBytes = null;

		try
		{
			signatureBytes = Base58.decode(query);
		}
		catch (Exception e)
		{
			
		}

		if (Crypto.getInstance().isValidAddress(query))
		{
			if(query.startsWith("Q"))
			{
				i++;
				foundList.put(i, "standardAccount");
			}

			if(query.startsWith("A"))
			{
				i++;
				foundList.put(i, "atAccount");
			}

			output.put("foundCount", i);
			output.put("foundList", foundList);

			return output;
		}

		if (query.indexOf(',') != -1 )
		{
			String[] strings = query.split(",");
			
			boolean isAddresses = strings.length > 0;
			
			for (String string : strings) 
			{
				if (!string.startsWith("Q")) 
				{
					isAddresses = false;
					break;
				}
				
				if (!Crypto.getInstance().isValidAddress(string)) 
				{
					isAddresses = false;
					break;
				}
			}

			if (isAddresses) 
			{
				i++;
				foundList.put(i, "multiAccount");

				output.put("foundCount", i);
				output.put("foundList", foundList);

				return output;
		
			}
		}
		
		if (signatureBytes != null && DBSet.getInstance().getBlockMap().contains(signatureBytes))
		{
			i++;
			foundList.put(i, "blockSignature");
		}
		else if(query.matches("\\d+") && Integer.valueOf(query) > 0 && Integer.valueOf(query) <= getHeight())
		{
			i++;
			foundList.put(i, "blockHeight");
		}
		else if (query.equals("last"))
		{
			i++;
			foundList.put(i, "blockLast");
		}
		else
		{
			if(!(signatureBytes == null) && (DBSet.getInstance().getTransactionParentMap().contains(signatureBytes)))
			{
				i++;
				foundList.put(i, "transactionSignature");
			}
		}

		if (DBSet.getInstance().getNameMap().contains(query))
		{
			i++;
			foundList.put(i, "name");
		}	

		if (query.matches("\\d+") && DBSet.getInstance().getAssetMap().contains(Long.valueOf(query)))
		{
			i++;
			foundList.put(i, "asset");
		}	

		if (DBSet.getInstance().getPollMap().contains(query))
		{
			i++;
			foundList.put(i, "pool");
		}	

		if (query.indexOf('/') != -1 )
		{
			String[] signatures = query.split("/");

			try
			{
				if(DBSet.getInstance().getTransactionParentMap().contains(Base58.decode(signatures[0])) || 
						DBSet.getInstance().getTransactionParentMap().contains(Base58.decode(signatures[1])))
				{
					i++;
					foundList.put(i, "trade");
				}
			}
			catch (Exception e) 
			{
				LOGGER.error(e.getMessage(),e);
			}
		}


		if (query.indexOf(':') != -1 )
		{

			int blockHeight = Integer.valueOf(query.split(":")[0]);
			int seq = Integer.valueOf(query.split(":")[1]);

			LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = DBSet.getInstance().getATTransactionMap().getATTransactions(blockHeight);

			if(atTxs.size()>seq)
			{
				i++;
				foundList.put(i, "atTx");
			}
		}

		output.put("foundCount", i);
		output.put("foundList", foundList);

		return output;
	}

	public Map jsonQueryBlogPostsTx(String addr) {

		Map output=new LinkedHashMap();
		try {

			AssetNames assetNames = new AssetNames();
			
			List<Transaction> transactions = new ArrayList<Transaction>();

			if (Crypto.getInstance().isValidAddress(addr)) {
				Account account = new Account(addr);

				byte[] signatureBytes = DBSet.getInstance().getReferenceMap().get(account);

				do{
					Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
					if(transaction == null)
					{
						break;
					}
					if(!transaction.getCreator().getAddress().equals(account.getAddress()))
					{
						break;
					}

					if(transaction.getType() == Transaction.ARBITRARY_TRANSACTION
							&& ((ArbitraryTransaction)transaction).getService() == 777
							)
					{
						transactions.add(transaction);
					}
					signatureBytes = transaction.getReference();

				}while(true);

				int count = transactions.size();

				output.put("count", count);

				int i = 0; 
				for (Transaction transaction : transactions) {
					output.put(count - i, jsonUnitPrint(transaction, assetNames));
					i++;
				}
			}

			output.put("assetNames", assetNames.getMap());
			
		} catch (Exception e1) {
			output=new LinkedHashMap();
			output.put("error", e1.getLocalizedMessage());
		}
		return output;
	}

	public Map jsonQueryAssetsLite()
	{
		Map output=new LinkedHashMap();

		Collection<Asset> assets = Controller.getInstance().getAllAssets();

		for (Asset asset : assets) {
			output.put(asset.getKey(), asset.getName());
		}

		return output;
	}

	public Map jsonQueryAssets()
	{
		Map output=new LinkedHashMap();

		Collection<Asset> assets = Controller.getInstance().getAllAssets();

		for (Asset asset : assets) {
			Map assetJSON=new LinkedHashMap();

			assetJSON.put("key", asset.getKey());
			assetJSON.put("name", asset.getName());
			assetJSON.put("description", asset.getDescription());
			assetJSON.put("owner", asset.getOwner().getAddress());
			assetJSON.put("quantity", asset.getQuantity());
			assetJSON.put("isDivisible", asset.isDivisible());

			List<Order> orders = DBSet.getInstance().getOrderMap().getOrders(asset.getKey());
			List<Trade> trades = DBSet.getInstance().getTradeMap().getTrades(asset.getKey());

			assetJSON.put("operations", orders.size() + trades.size());

			output.put(asset.getKey(), assetJSON);
		}

		return output;
	}


	public Map jsonQueryATs()
	{
		Map output=new LinkedHashMap();

		Iterable<String> ids = DBSet.getInstance().getATMap().getATsLimited(100);

		Iterator<String> iter = ids.iterator();
		while (iter.hasNext())
		{
			String atAddr = iter.next();

			AT at = DBSet.getInstance().getATMap().getAT(atAddr);

			output.put(atAddr, at.toJSON());
		}

		return output;
	}

	public Map jsonQueryPools()
	{
		Map lastPools = new LinkedHashMap();
		Map output=new LinkedHashMap();

		List<Poll> pools = new ArrayList< Poll > (DBSet.getInstance().getPollMap().getValues());

		if(pools.size() == 0)
		{
			output.put("error", "There is no Polls.");
			return output;
		}
			
		//SCAN
		int back = 815; // 3*24*60*60/318 = 815 // 3 days
		//back = 40815;
		Pair<Block, List<Transaction>> result = Controller.getInstance().scanTransactions(Controller.getInstance().getBlockByHeight(getHeight()-back), back, 100, Transaction.CREATE_POLL_TRANSACTION, -1, null);

		for(Transaction transaction: result.getB())
		{
			lastPools.put(((CreatePollTransaction)transaction).getPoll().getName(), true);
		}

		Comparator<Poll> comparator = new Comparator<Poll>() {
			public int compare(Poll c1, Poll c2) {

				BigDecimal c1votes = c1.getTotalVotes();
				BigDecimal c2votes = c2.getTotalVotes();

				return c2votes.compareTo(c1votes);
			}
		};

		Collections.sort(pools, comparator); 

		Map poolsJSON=new LinkedHashMap();

		for (Poll pool : pools) {
			Map poolJSON=new LinkedHashMap();

			poolJSON.put( "totalVotes",  pool.getTotalVotes().toPlainString() ); 

			poolJSON.put( "new",  lastPools.containsKey(pool.getName()) );

			poolsJSON.put(pool.getName(), poolJSON);
		}

		output.put("pools", poolsJSON);

		return output;
	}

	public Map jsonQueryPool(String query)
	{
		Map output = new LinkedHashMap();

		Poll poll = Controller.getInstance().getPoll(query);

		Map pollJSON = new LinkedHashMap();

		pollJSON.put("creator", poll.getCreator().getAddress());
		pollJSON.put("name", poll.getName());
		pollJSON.put("description", poll.getDescription());
		pollJSON.put("totalVotes", poll.getTotalVotes().toPlainString());

		
		List<Transaction> transactions = DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(poll.getCreator().getAddress(), Transaction.CREATE_POLL_TRANSACTION, 0);
		for (Transaction transaction : transactions) {
			CreatePollTransaction createPollTransaction = ((CreatePollTransaction)transaction);
			if(createPollTransaction.getPoll().getName().equals(poll.getName()))
			{
				pollJSON.put("timestamp", createPollTransaction.getTimestamp());
				pollJSON.put("dateTime", BlockExplorer.timestampToStr(createPollTransaction.getTimestamp()));
				break;
			}
		}

		Map optionsJSON = new LinkedHashMap();
		for(PollOption option: poll.getOptions())
		{
			optionsJSON.put(option.getName(), option.getVotes().toPlainString());
		}
		pollJSON.put("options", optionsJSON);

		Comparator<Pair<Account, PollOption>> comparator = new Comparator<Pair<Account, PollOption>>() {
			public int compare(Pair<Account, PollOption> c1, Pair<Account, PollOption> c2) {

				BigDecimal c1votes = c1.getA().getConfirmedBalance();
				BigDecimal c2votes = c2.getA().getConfirmedBalance();

				return c2votes.compareTo(c1votes);
			}
		};

		
		Map votesJSON = new LinkedHashMap();

		List<Pair<Account, PollOption>> votes = poll.getVotes(); 

		Collections.sort(votes, comparator);

		for(Pair<Account, PollOption> vote: votes)
		{
			Map voteJSON = new LinkedHashMap();
			voteJSON.put("option", vote.getB().getName());
			voteJSON.put("votes", vote.getA().getConfirmedBalance().toPlainString());

			votesJSON.put(vote.getA().getAddress(), voteJSON);
		}
		pollJSON.put("votes", votesJSON);

		output.put("pool", pollJSON);

		return output;
	}

	public Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> calcForAsset(List<Order> orders, List<Trade> trades)
	{
		Map<Long, Integer> pairsOpenOrders = new TreeMap<Long, Integer>();
		Map<Long, BigDecimal> volumePriceOrders = new TreeMap<Long, BigDecimal>();
		Map<Long, BigDecimal> volumeAmountOrders = new TreeMap<Long, BigDecimal>();

		int count;
		BigDecimal volumePrice =  BigDecimal.ZERO.setScale(8);		
		BigDecimal volumeAmount =  BigDecimal.ZERO.setScale(8);	

		for (Order order : orders) 
		{
			if(!pairsOpenOrders.containsKey(order.getWant()))
			{
				count = 0;
			}
			else
			{
				count = pairsOpenOrders.get(order.getWant());
			}	

			if(!volumeAmountOrders.containsKey(order.getWant()))
			{
				volumeAmount =  BigDecimal.ZERO.setScale(8);				
			}
			else
			{
				volumeAmount =  volumeAmountOrders.get(order.getWant());
			}	

			if(!volumePriceOrders.containsKey(order.getWant()))
			{
				volumePrice =  BigDecimal.ZERO.setScale(8);				
			}
			else
			{
				volumePrice =  volumePriceOrders.get(order.getWant());
			}	

			count ++;
			pairsOpenOrders.put(order.getWant(), count);

			volumeAmount = volumeAmount.add(order.getAmountLeft());

			volumeAmountOrders.put(order.getWant(), volumeAmount);

			volumePriceOrders.put(order.getWant(), volumePrice);

			if(!pairsOpenOrders.containsKey(order.getHave()))
			{
				count = 0;
			}
			else
			{
				count = pairsOpenOrders.get(order.getHave());
			}	

			if(!volumePriceOrders.containsKey(order.getHave()))
			{
				volumePrice =  BigDecimal.ZERO.setScale(8);				
			}
			else
			{
				volumePrice =  volumePriceOrders.get(order.getHave());
			}	

			if(!volumeAmountOrders.containsKey(order.getHave()))
			{
				volumeAmount =  BigDecimal.ZERO.setScale(8);				
			}
			else
			{
				volumeAmount =  volumeAmountOrders.get(order.getHave());
			}	

			count ++;
			pairsOpenOrders.put(order.getHave(), count);

			volumePrice = volumePrice.add(order.getAmountLeft());

			volumePriceOrders.put(order.getHave(), volumePrice);

			volumeAmountOrders.put(order.getHave(), volumeAmount);
		}

		Map<Long, Integer> pairsTrades = new TreeMap<Long, Integer>();
		Map<Long, BigDecimal> volumePriceTrades = new TreeMap<Long, BigDecimal>();
		Map<Long, BigDecimal> volumeAmountTrades = new TreeMap<Long, BigDecimal>();

		for (Trade trade : trades) 
		{
			if(!pairsTrades.containsKey(trade.getInitiatorOrder(DBSet.getInstance()).getWant()) )
			{
				count = 0;
				volumePrice =  BigDecimal.ZERO.setScale(8);
				volumeAmount =  BigDecimal.ZERO.setScale(8);
			}
			else
			{
				count = pairsTrades.get(trade.getInitiatorOrder(DBSet.getInstance()).getWant());
				volumePrice =  volumePriceTrades.get(trade.getInitiatorOrder(DBSet.getInstance()).getWant());
				volumeAmount =  volumeAmountTrades.get(trade.getInitiatorOrder(DBSet.getInstance()).getWant());
			}	

			count ++;
			pairsTrades.put(trade.getInitiatorOrder(DBSet.getInstance()).getWant(), count);

			volumePrice = volumePrice.add(trade.getPrice());
			volumeAmount = volumeAmount.add(trade.getAmount());

			volumePriceTrades.put(trade.getInitiatorOrder(DBSet.getInstance()).getWant(), volumePrice);
			volumeAmountTrades.put(trade.getInitiatorOrder(DBSet.getInstance()).getWant(), volumeAmount);

			if(!pairsTrades.containsKey(trade.getTargetOrder(DBSet.getInstance()).getWant()))
			{
				count = 0;
				volumePrice =  BigDecimal.ZERO.setScale(8);
				volumeAmount =  BigDecimal.ZERO.setScale(8);
			}
			else
			{
				count = pairsTrades.get(trade.getTargetOrder(DBSet.getInstance()).getWant());
				volumePrice =  volumePriceTrades.get(trade.getTargetOrder(DBSet.getInstance()).getWant());
				volumeAmount =  volumeAmountTrades.get(trade.getTargetOrder(DBSet.getInstance()).getWant());
			}	

			count ++;
			pairsTrades.put(trade.getTargetOrder(DBSet.getInstance()).getWant(), count);

			volumePrice = volumePrice.add(trade.getAmount());
			volumeAmount = volumeAmount.add(trade.getPrice());

			volumePriceTrades.put(trade.getTargetOrder(DBSet.getInstance()).getWant(), volumePrice);
			volumeAmountTrades.put(trade.getTargetOrder(DBSet.getInstance()).getWant(), volumeAmount);
		}

		Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all = 
				new TreeMap<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>>();

		for(Map.Entry<Long, Integer> pair : pairsOpenOrders.entrySet())
		{
			all.put(pair.getKey(), 
					Fun.t6(
							pair.getValue(), 
							0, 
							volumePriceOrders.get(pair.getKey()), volumeAmountOrders.get(pair.getKey()),
							BigDecimal.ZERO.setScale(8), BigDecimal.ZERO.setScale(8)
							)
					);
		}

		for(Map.Entry<Long, Integer> pair : pairsTrades.entrySet())
		{

			if(all.containsKey(pair.getKey()))
			{
				all.put(
						pair.getKey(), Fun.t6(
								all.get(pair.getKey()).a, 
								pair.getValue(), 
								all.get(pair.getKey()).c, 
								all.get(pair.getKey()).d,
								volumePriceTrades.get(pair.getKey()), 
								volumeAmountTrades.get(pair.getKey()) 
								)
						);
			}
			else
			{
				all.put(
						pair.getKey(), Fun.t6(
								0, 
								pair.getValue(), 
								BigDecimal.ZERO.setScale(8), 
								BigDecimal.ZERO.setScale(8),
								volumePriceTrades.get(pair.getKey()), 
								volumeAmountTrades.get(pair.getKey()) 
								)
						);
			}
		}

		return all;
	}
	
	public Map jsonQueryAsset(long key)
	{
		Map output=new LinkedHashMap();

		List<Order> orders = DBSet.getInstance().getOrderMap().getOrders(key);

		List<Trade> trades = DBSet.getInstance().getTradeMap().getTrades(key);

		Asset asset = Controller.getInstance().getAsset(key);

		Map assetJSON=new LinkedHashMap();

		assetJSON.put("key", asset.getKey());
		assetJSON.put("name", asset.getName());
		assetJSON.put("description", asset.getDescription());
		assetJSON.put("owner", asset.getOwner().getAddress());
		assetJSON.put("quantity", asset.getQuantity());
		assetJSON.put("isDivisible", asset.isDivisible());

		
		List<Transaction> transactions = DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(asset.getOwner().getAddress(), Transaction.ISSUE_ASSET_TRANSACTION, 0);
		for (Transaction transaction : transactions) {
			IssueAssetTransaction issueAssetTransaction = ((IssueAssetTransaction)transaction);
			if(issueAssetTransaction.getAsset().getName().equals(asset.getName()))
			{
				assetJSON.put("timestamp", issueAssetTransaction.getTimestamp());
				assetJSON.put("dateTime", BlockExplorer.timestampToStr(issueAssetTransaction.getTimestamp()));
				break;
			}
		}

		
		output.put("this", assetJSON);

		output.put("totalOpenOrdersCount", orders.size());
		output.put("totalTradesCount", trades.size());

		Map<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> all = calcForAsset(orders, trades);

		if(all.containsKey(key))
		{
			output.put("totalOrdersVolume", all.get(key).c.toPlainString());
		}
		else
		{
			output.put("totalOrdersVolume", BigDecimal.ZERO.setScale(8).toPlainString());
		}

		if(all.containsKey(key))
		{
			output.put("totalTradesVolume", all.get(key).f.toPlainString());
		}
		else
		{
			output.put("totalTradesVolume", BigDecimal.ZERO.setScale(8).toPlainString());
		}

		Map pairsJSON=new LinkedHashMap();

		pairsJSON=new LinkedHashMap();
		for(Map.Entry<Long, Tuple6<Integer, Integer, BigDecimal, BigDecimal, BigDecimal, BigDecimal>> pair : all.entrySet())
		{
			if(pair.getKey() == key)
			{
				continue;				
			}
			Asset assetWant = Controller.getInstance().getAsset(pair.getKey());

			Map pairJSON = new LinkedHashMap();
			pairJSON.put("openOrdersCount", pair.getValue().a);
			pairJSON.put("tradesCount", pair.getValue().b);
			pairJSON.put("sum", pair.getValue().a + pair.getValue().b);
			pairJSON.put("ordersPriceVolume", pair.getValue().c.toPlainString());
			pairJSON.put("ordersAmountVolume", pair.getValue().d.toPlainString());
			pairJSON.put("tradesPriceVolume", pair.getValue().e.toPlainString());
			pairJSON.put("tradeAmountVolume", pair.getValue().f.toPlainString());
			pairJSON.put("asset", pair.getKey());
			pairJSON.put("assetName", assetWant.getName());
			pairJSON.put("description", assetWant.getDescription());
			pairsJSON.put(pair.getKey(), pairJSON);
		}

		output.put("pairs", pairsJSON);

		return output;
	}

	public Map jsonQueryTrades(long have, long want)
	{
		Map output=new LinkedHashMap();

		List<Order> ordersHave = DBSet.getInstance().getOrderMap().getOrders(have, want);
		List<Order> ordersWant = DBSet.getInstance().getOrderMap().getOrders(want, have);

		//Collections.reverse(ordersWant); 

		List<Trade> trades = DBSet.getInstance().getTradeMap().getTrades(have, want);

		Asset assetHave = Controller.getInstance().getAsset(have);
		Asset assetWant = Controller.getInstance().getAsset(want);

		output.put("assetHaveOwner", assetHave.getOwner().getAddress());
		output.put("assetWantOwner", assetWant.getOwner().getAddress());

		output.put("assetHave", assetHave.getKey());
		output.put("assetHaveName", assetHave.getName());
		output.put("assetWant", assetWant.getKey());
		output.put("assetWantName", assetWant.getName());

		Map sellsJSON = new LinkedHashMap();
		Map buysJSON = new LinkedHashMap();


		BigDecimal sumAmount = BigDecimal.ZERO.setScale(8);
		BigDecimal sumAmountGood = BigDecimal.ZERO.setScale(8);

		BigDecimal sumSellingAmount = BigDecimal.ZERO.setScale(8);
		BigDecimal sumSellingAmountGood = BigDecimal.ZERO.setScale(8);
		
		for (Order order : ordersHave) 		
		{
			Map sellJSON = new LinkedHashMap();

			sellJSON.put("price", order.getPrice().toPlainString());
			sellJSON.put("amount", order.getAmountLeft().toPlainString());
			sumAmount = sumAmount.add(order.getAmountLeft());

			sellJSON.put("sellingPrice", BigDecimal.ONE.setScale(8).divide(order.getPrice(), 8, RoundingMode.DOWN).toPlainString());

			BigDecimal sellingAmount = order.getPrice().multiply(order.getAmountLeft()).setScale(8, RoundingMode.DOWN);

			sellJSON.put("sellingAmount", sellingAmount.toPlainString());

			BigDecimal increment = order.calculateBuyIncrement(order, DBSet.getInstance());
			BigDecimal amount = order.getAmountLeft();
			amount = amount.subtract(amount.remainder(increment));
			
			boolean good = (amount.compareTo(BigDecimal.ZERO) > 0);
			
			sellJSON.put("good", good);
			
			if(good)
			{
				sumAmountGood = sumAmountGood.add(order.getAmountLeft());
				
				sumSellingAmountGood = sumSellingAmountGood.add(sellingAmount);
			}
			
			sumSellingAmount = sumSellingAmount.add(sellingAmount);
			
			sellsJSON.put(Base58.encode(order.getId()), sellJSON);
		}

		output.put("sells", sellsJSON);

		output.put("sellsSumAmount", sumAmount.toPlainString());
		output.put("sellsSumAmountGood", sumAmountGood.toPlainString());
		output.put("sellsSumTotal", sumSellingAmount.toPlainString());
		output.put("sellsSumTotalGood", sumSellingAmountGood.toPlainString());

		sumAmount = BigDecimal.ZERO.setScale(8);
		sumAmountGood = BigDecimal.ZERO.setScale(8);
		
		BigDecimal sumBuyingAmount = BigDecimal.ZERO.setScale(8);
		BigDecimal sumBuyingAmountGood = BigDecimal.ZERO.setScale(8);

		for (Order order : ordersWant) 	
		{	
			Map buyJSON = new LinkedHashMap();

			buyJSON.put("price", order.getPrice().toPlainString());
			buyJSON.put("amount", order.getAmountLeft().toPlainString());

			sumAmount = sumAmount.add(order.getAmountLeft());

			buyJSON.put("buyingPrice", BigDecimal.ONE.setScale(8).divide(order.getPrice(), 8, RoundingMode.DOWN).toPlainString());

			BigDecimal buyingAmount = order.getPrice().multiply(order.getAmountLeft()).setScale(8, RoundingMode.DOWN);

			buyJSON.put("buyingAmount", buyingAmount.toPlainString());

			BigDecimal increment = order.calculateBuyIncrement(order, DBSet.getInstance());
			BigDecimal amount = order.getAmountLeft();
			amount = amount.subtract(amount.remainder(increment));
			
			boolean good = (amount.compareTo(BigDecimal.ZERO) > 0);
			
			buyJSON.put("good", good);
			
			if(good)
			{
				sumBuyingAmountGood = sumBuyingAmountGood.add(buyingAmount);
				sumAmountGood = sumAmountGood.add(order.getAmountLeft());
			}
			
			sumBuyingAmount = sumBuyingAmount.add(buyingAmount);

			buysJSON.put(Base58.encode(order.getId()), buyJSON);
		}
		output.put("buys", buysJSON);

		output.put("buysSumAmount", sumBuyingAmount.toPlainString());
		output.put("buysSumAmountGood", sumBuyingAmountGood.toPlainString());
		output.put("buysSumTotal", sumAmount.toPlainString());
		output.put("buysSumTotalGood", sumAmountGood.toPlainString());

		Map tradesJSON = new LinkedHashMap();

		output.put("tradesCount", trades.size());

		BigDecimal tradeWantAmount = BigDecimal.ZERO.setScale(8);
		BigDecimal tradeHaveAmount = BigDecimal.ZERO.setScale(8);

		int i = 0;
		for (Trade trade : trades) 	
		{	
			i++;
			Map tradeJSON = new LinkedHashMap();

			Order orderInitiator = trade.getInitiatorOrder(DBSet.getInstance());

			Order orderTarget = trade.getTargetOrder(DBSet.getInstance());

			tradeJSON.put("amount", trade.getAmount().toPlainString());
			tradeJSON.put("price", trade.getPrice().toPlainString());

			tradeJSON.put("realPrice", trade.getPrice().divide(trade.getAmount(), 8, RoundingMode.FLOOR).toPlainString());
			tradeJSON.put("realReversePrice", trade.getAmount().divide(trade.getPrice(), 8, RoundingMode.FLOOR).toPlainString());

			tradeJSON.put("initiatorTxSignature", Base58.encode(orderInitiator.getId()));

			tradeJSON.put("initiatorCreator", orderInitiator.getCreator().getAddress());
			tradeJSON.put("initiatorAmount", orderInitiator.getAmount().toPlainString());
			if(orderInitiator.getHave() == have)
			{
				tradeJSON.put("type", "sell");
				tradeWantAmount = tradeWantAmount.add(trade.getAmount());
				tradeHaveAmount = tradeHaveAmount.add(trade.getPrice());

			}
			else
			{
				tradeJSON.put("type", "buy");

				tradeWantAmount = tradeWantAmount.add(trade.getPrice());
				tradeHaveAmount = tradeHaveAmount.add(trade.getAmount());
			}	
			tradeJSON.put("targetTxSignature", Base58.encode(orderTarget.getId()));
			tradeJSON.put("targetCreator", orderTarget.getCreator().getAddress());
			tradeJSON.put("targetAmount", orderTarget.getAmount().toPlainString());

			tradeJSON.put("timestamp", trade.getTimestamp());
			tradeJSON.put("dateTime", BlockExplorer.timestampToStr(trade.getTimestamp()));

			tradesJSON.put(i, tradeJSON);
		}
		output.put("trades", tradesJSON);

		output.put("tradeWantAmount", tradeWantAmount.toPlainString());
		output.put("tradeHaveAmount", tradeHaveAmount.toPlainString());

		return output;
	}

	public Map jsonQueryNames()
	{
		Map output=new LinkedHashMap();
		Map namesJSON=new LinkedHashMap();

		Collection<Name> names = DBSet.getInstance().getNameMap().getValues();

		for (Name name : names) {
			namesJSON.put(name.toString(), name.getOwner().getAddress());
		}

		output.put("names", namesJSON);
		output.put("count", names.size());

		return output;
	}

	public Map jsonQueryBlocks(int start)
	{
		Block block;
		if(start > 0)
		{
			block = Controller.getInstance().getBlockByHeight(start);
		}
		else
		{
			block = getLastBlock();	
			start = block.getHeight(); 
		}

		Map output=new LinkedHashMap();

		output.put("maxHeight", block.getHeight());

		output.put("unconfirmedTxs", Controller.getInstance().getUnconfirmedTransactions().size());

		int counter = start; 

		do{
			Map blockJSON=new LinkedHashMap();
			blockJSON.put("height", counter);
			blockJSON.put("signature", Base58.encode(block.getSignature()));
			blockJSON.put("generator", block.getGenerator().getAddress());
			blockJSON.put("generatingBalance", block.getGeneratingBalance());
			blockJSON.put("transactionCount", block.getTransactionCount());
			blockJSON.put("timestamp", block.getTimestamp());
			blockJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));
			blockJSON.put("totalFee", block.getTotalFee().toPlainString());

			BigDecimal totalAmount = BigDecimal.ZERO.setScale(8);
			for (Transaction transaction : block.getTransactions()) {
				for (Account account : transaction.getInvolvedAccounts()) {
					BigDecimal amount = transaction.getAmount(account); 
					if(amount.compareTo(BigDecimal.ZERO) > 0)
					{
						totalAmount = totalAmount.add(amount);
					}
				}
			}

			blockJSON.put("totalAmount", totalAmount.toPlainString());

			LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> aTtxs = DBSet.getInstance().getATTransactionMap().getATTransactions(counter);

			BigDecimal totalATAmount = BigDecimal.ZERO.setScale(8);

			for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : aTtxs.entrySet())
			{	
				totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , 8));
			}

			blockJSON.put("totalATAmount", totalATAmount.toPlainString());
			blockJSON.put("aTfee", block.getATfee().toPlainString());

			output.put(counter, blockJSON);

			counter --;
			block = block.getParent();
		}
		while(block != null && counter >= start - 20);


		return output;
	}

	public Map jsonQueryLastBlock()
	{
		Map output=new LinkedHashMap();

		Block lastBlock = getLastBlock();

		output.put("height", lastBlock.getHeight());
		output.put("timestamp", lastBlock.getTimestamp());
		output.put("dateTime", BlockExplorer.timestampToStr(lastBlock.getTimestamp()));

		output.put("timezone", Settings.getInstance().getTimeZone());
		output.put("timeformat", Settings.getInstance().getTimeFormat());

		return output;
	}

	public Map jsonQueryTopRichest(int limit, long key)
	{
		Map output=new LinkedHashMap();
		Map balances=new LinkedHashMap();
		BigDecimal all = BigDecimal.ZERO.setScale(8);
		BigDecimal alloreders = BigDecimal.ZERO.setScale(8);

		List<Tuple2<String, BigDecimal>> top100s = new ArrayList<Tuple2<String, BigDecimal>>();


		Collection<Tuple2<String, Long>> addrs = DBSet.getInstance().getBalanceMap().getKeys();
		for (Tuple2<String, Long> addr : addrs) {
			if(addr.b == key)
			{
				BigDecimal ball =  DBSet.getInstance().getBalanceMap().get(addr);
				all = all.add(ball);

				top100s.add(Fun.t2(addr.a, ball));
			}
		}

		Collection<Order> orders = DBSet.getInstance().getOrderMap().getValues();

		for (Order order : orders) {
			if(order.getHave() == key)
			{
				alloreders = alloreders.add(order.getAmountLeft());
			}
		}
		Collections.sort(top100s, new ReverseComparator(new BigDecimalComparator())); 

		int couter = 0;
		for (Tuple2<String, BigDecimal> top100 : top100s) {
			if(limit == -1) // allnotzero
			{
				if(top100.b.compareTo(BigDecimal.ZERO) <= 0)
				{
					break;
				}
			}
			couter ++;

			Map balance=new LinkedHashMap();
			balance.put("address", top100.a);
			balance.put("balance", top100.b.toPlainString());
			balances.put(couter, balance);

			if(couter >= limit && limit != -2 && limit != -1) // -2 = all
			{
				break;
			}
		}

		output.put("all", all.toPlainString());
		output.put("allinOrders", alloreders.toPlainString());
		output.put("allTotal", (all.add(alloreders)).toPlainString());
		output.put("assetKey", key);
		output.put("assetName", Controller.getInstance().getAsset(key).getName());
		output.put("limit", limit);
		output.put("count", couter);

		output.put("top", balances);

		return output;
	}	

	public Map jsonUnitPrint(Object unit, AssetNames assetNames)
	{
		Map transactionDataJSON = new LinkedHashMap();
		Map transactionJSON = new LinkedHashMap();

		if (unit instanceof Trade)
		{
			Trade trade = (Trade)unit;

			Order orderInitiator = trade.getInitiatorOrder(DBSet.getInstance());

			/*
			if(DBSet.getInstance().getOrderMap().contains(trade.getInitiator()))
			{
				orderInitiator =  DBSet.getInstance().getOrderMap().get(trade.getInitiator());
			}
			else
			{
				orderInitiator =  DBSet.getInstance().getCompletedOrderMap().get(trade.getInitiator());
			}
			 */

			Order orderTarget = trade.getTargetOrder(DBSet.getInstance());

			/*
			if(DBSet.getInstance().getOrderMap().contains(trade.getTarget()))
			{
				orderTarget =  DBSet.getInstance().getOrderMap().get(trade.getTarget());
			}
			else
			{
				orderTarget =  DBSet.getInstance().getCompletedOrderMap().get(trade.getTarget());
			}
			 */

			transactionDataJSON.put("amount", trade.getAmount().toPlainString());
			transactionDataJSON.put("price", trade.getPrice().toPlainString());

			transactionDataJSON.put("realPrice", trade.getPrice().divide(trade.getAmount(), 8, RoundingMode.FLOOR).toPlainString());

			transactionDataJSON.put("initiatorTxSignature", Base58.encode(orderInitiator.getId()));

			transactionDataJSON.put("initiatorCreator", orderInitiator.getCreator().getAddress());
			transactionDataJSON.put("initiatorAmount", orderInitiator.getAmount().toPlainString());
			transactionDataJSON.put("initiatorHave", orderInitiator.getHave());
			transactionDataJSON.put("initiatorWant", orderInitiator.getWant());

			if(assetNames != null) 
			{
				assetNames.setKey(orderInitiator.getHave());
				assetNames.setKey(orderInitiator.getWant());
			}

			transactionDataJSON.put("targetTxSignature", Base58.encode(orderTarget.getId()));
			transactionDataJSON.put("targetCreator", orderTarget.getCreator().getAddress());
			transactionDataJSON.put("targetAmount", orderTarget.getAmount().toPlainString());

			Block parentBlock = Controller.getInstance().getTransaction(orderInitiator.getId().toByteArray()).getParent(); 
			transactionDataJSON.put("height", parentBlock.getHeight());
			transactionDataJSON.put("confirmations", getHeight() - parentBlock.getHeight() + 1 );

			transactionDataJSON.put("timestamp", trade.getTimestamp());
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(trade.getTimestamp()));

			transactionJSON.put("type", "trade");
			transactionJSON.put("trade", transactionDataJSON);
		}

		if (unit instanceof Transaction)
		{
			Transaction transaction = (Transaction)unit;

			transactionDataJSON = transaction.toJson();

			if(transaction.getType() == Transaction.REGISTER_NAME_TRANSACTION)
			{
				if(transactionDataJSON.get("value").toString().startsWith("?gz!"))
				{
					transactionDataJSON.put("value", GZIP.webDecompress(transactionDataJSON.get("value").toString()));	
					transactionDataJSON.put("compressed", true);	
				}
				else
				{
					transactionDataJSON.put("compressed", false);
				}
			}

			if(transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION)
			{
				if(transactionDataJSON.get("newValue").toString().startsWith("?gz!"))
				{
					transactionDataJSON.put("newValue", GZIP.webDecompress(transactionDataJSON.get("newValue").toString()));	
					transactionDataJSON.put("compressed", true);	
				}
				else
				{
					transactionDataJSON.put("compressed", false);
				}
			}

			if(transaction.getType() == Transaction.CANCEL_ORDER_TRANSACTION) 
			{
				BigInteger key = ((CancelOrderTransaction)unit).getOrder();
				Order order;
				if(DBSet.getInstance().getCompletedOrderMap().contains(key))
				{
					order =  DBSet.getInstance().getCompletedOrderMap().get(key);
				}
				else
				{
					order =  DBSet.getInstance().getOrderMap().get(key);
				}	

				Map orderJSON = new LinkedHashMap();
				
				if (assetNames != null) {
					assetNames.setKey(order.getHave());
					assetNames.setKey(order.getWant());
				}
				
				orderJSON.put("have", order.getHave());
				orderJSON.put("want", order.getWant());
				
				orderJSON.put("amount", order.getAmount().toPlainString());
				orderJSON.put("amountLeft", order.getAmountLeft().toPlainString());
				orderJSON.put("price", order.getPrice().toPlainString());

				transactionDataJSON.put("orderSource", orderJSON);
			}

			if(transaction.getType() == Transaction.ISSUE_ASSET_TRANSACTION) 
			{
				long assetkey = ((IssueAssetTransaction) transaction).getAsset().getKey();
				
				transactionDataJSON.put("asset", assetkey);
				
				transactionDataJSON.put("assetName", ((IssueAssetTransaction) transaction).getAsset().getName());
			}

			if(transaction.getType() == Transaction.TRANSFER_ASSET_TRANSACTION) 
			{
				if (assetNames != null) 
				{
					assetNames.setKey(((TransferAssetTransaction)unit).getKey());
				}
			}

			if(transaction.getType() == Transaction.MESSAGE_TRANSACTION) 
			{
				if (assetNames != null) 
				{
					assetNames.setKey(((MessageTransaction)unit).getKey());
				}
				
				if(((MessageTransaction)unit).isEncrypted()){
					transactionDataJSON.put("data", "encrypted");
				}
			}
			
			if(transaction.getType() == Transaction.MULTI_PAYMENT_TRANSACTION) 
			{
				Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

				for (Payment payment : ((MultiPaymentTransaction)transaction).getPayments()) {
					BigDecimal amount = BigDecimal.ZERO.setScale(8); 
					if(totalAmountOfAssets.containsKey(payment.getAsset())) {
						amount = totalAmountOfAssets.get(payment.getAsset());
					}
					amount = amount.add(payment.getAmount());
					
					if (assetNames != null) {
						assetNames.setKey(payment.getAsset());	
					}

					totalAmountOfAssets.put( payment.getAsset(), amount );
				}
				
				Map amountOfAssetsJSON = new LinkedHashMap();

				for(Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet())
				{	
					amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
				}

				transactionDataJSON.put("amounts", amountOfAssetsJSON);
			}
			
			if(transaction.getType() == Transaction.ARBITRARY_TRANSACTION) 
			{
				Map<Long, BigDecimal> totalAmountOfAssets = new TreeMap<Long, BigDecimal>();

				for (Payment payment : ((ArbitraryTransaction)transaction).getPayments()) {
					BigDecimal amount = BigDecimal.ZERO.setScale(8); 
					if(totalAmountOfAssets.containsKey(payment.getAsset())) {
						amount = totalAmountOfAssets.get(payment.getAsset());
					}
					amount = amount.add(payment.getAmount());
					
					if (assetNames != null) {
						assetNames.setKey(payment.getAsset());	
					}

					totalAmountOfAssets.put( payment.getAsset(), amount );
				}
				
				Map amountOfAssetsJSON = new LinkedHashMap();

				for(Map.Entry<Long, BigDecimal> assetInfo : totalAmountOfAssets.entrySet())
				{	
					amountOfAssetsJSON.put(assetInfo.getKey(), assetInfo.getValue().toPlainString());
				}

				transactionDataJSON.put("amounts", amountOfAssetsJSON);
			}

			if(transaction.getType() == Transaction.VOTE_ON_POLL_TRANSACTION) 
			{
				transactionDataJSON.put("optionString", 
						Controller.getInstance().getPoll(((VoteOnPollTransaction)transaction).getPoll()).getOptions().get(((VoteOnPollTransaction)transaction).getOption()).getName()
						);
			}

			if(transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) 
			{
				if (assetNames != null) {
					assetNames.setKey(((CreateOrderTransaction)transaction).getOrder().getHave());
					assetNames.setKey(((CreateOrderTransaction)transaction).getOrder().getWant());
				}
			}

			if(transaction.getType() == Transaction.DEPLOY_AT_TRANSACTION) 
			{
				transactionDataJSON.put("atAddress", ((DeployATTransaction)transaction).getATaccount().getAddress());
			}

			if(transaction.isConfirmed())
			{
				Block parent = transaction.getParent();
				transactionDataJSON.put("block", Base58.encode(parent.getSignature()));
				transactionDataJSON.put("blockHeight", parent.getHeight());
			}

			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(transaction.getTimestamp()));

			transactionJSON.put("type", "transaction");
			transactionJSON.put("transaction", transactionDataJSON);
		}

		if (unit instanceof Block)
		{
			Block block = (Block)unit;

			transactionDataJSON = new LinkedHashMap();
			transactionDataJSON.put("timestamp", block.getTimestamp());
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));

			int height = block.getHeight();
			transactionDataJSON.put("confirmations", getHeight() - height + 1 );
			transactionDataJSON.put("height", height);

			transactionDataJSON.put("generator", block.getGenerator().getAddress());
			transactionDataJSON.put("signature", Base58.encode(block.getSignature()));

			/*
			transactionDataJSON.put("generatingBalance", block.getGeneratingBalance());
			transactionDataJSON.put("atFees", block.getATfee());
			transactionDataJSON.put("reference", Base58.encode(block.getReference()));
			transactionDataJSON.put("generatorSignature", Base58.encode(block.getGeneratorSignature()));
			transactionDataJSON.put("transactionsSignature", block.getTransactionsSignature());
			transactionDataJSON.put("version", block.getVersion());
			 */

			//transactionDataJSON.put("fee", balances[size - counter].getTransactionBalance().get(0l).toPlainString());
			transactionDataJSON.put("fee", block.getTotalFee().toPlainString());

			transactionJSON.put("type", "block");
			transactionJSON.put("block", transactionDataJSON);

		}

		if (unit instanceof AT_Transaction)
		{
			AT_Transaction aTtransaction = (AT_Transaction)unit; 
			transactionDataJSON = aTtransaction.toJSON();

			Block block = Controller.getInstance().getBlockByHeight(aTtransaction.getBlockHeight());
			long timestamp = block.getTimestamp();
			transactionDataJSON.put("timestamp", timestamp);
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(timestamp));

			transactionDataJSON.put("confirmations", getHeight() - ((AT_Transaction)unit).getBlockHeight() + 1 );

			if(((AT_Transaction)unit).getRecipient().equals("11111111111111111111111111"))
			{
				transactionDataJSON.put("generatorAddress", block.getGenerator().getAddress());
			}

			transactionJSON.put("type", "atTransaction");
			transactionJSON.put("atTransaction", transactionDataJSON);
		}

		return transactionJSON;
	}

	public Map jsonQueryName(String query, int start, int txOnPage, String filter, boolean allOnOnePage)
	{
		TreeSet<BlExpUnit> all = new TreeSet<>();
		String name = query;

		int[] txsTypeCount = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};

		Map output=new LinkedHashMap();

		int txsCount = 0;
		int height = 1;
		
		Block block = new GenesisBlock();
		do
		{
			int seq = 1;
			for(Transaction transaction: block.getTransactions())
			{
				if	(
						(transaction.getType() == Transaction.REGISTER_NAME_TRANSACTION && ((RegisterNameTransaction)transaction).getName().toString().equals(name))
						||(transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION && ((UpdateNameTransaction)transaction).getName().toString().equals(name)) 
						||(transaction.getType() == Transaction.SELL_NAME_TRANSACTION && ((SellNameTransaction)transaction).getNameSale().toString().equals(name))
						||(transaction.getType() == Transaction.CANCEL_SELL_NAME_TRANSACTION && ((CancelSellNameTransaction)transaction).getName().equals(name))
						||(transaction.getType() == Transaction.BUY_NAME_TRANSACTION && ((BuyNameTransaction)transaction).getNameSale().toString().equals(name))
						) 
				{
					all.add( new BlExpUnit( height, seq, transaction));
					txsTypeCount[transaction.getType()-1] ++;
				}
				seq ++;
			}
			block = block.getChild();
			height ++;
		}
		while(block != null);

		int size = all.size();
		txsCount = size; 

		if(start == -1 )
		{
			start = size;
		}

		output.put("type", "name");	

		output.put("name", name);

		Map txCountJSON = new LinkedHashMap();

		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}

		txCountJSON.put("allCount", txsCount);

		output.put("countTx", txCountJSON);

		output.put("txOnPage", txOnPage);

		output.put("filter", filter);

		output.put("allOnOnePage", allOnOnePage);

		output.put("start", start);

		int end;

		if(start > txOnPage)
		{
			if(allOnOnePage)
			{
				end = 1;
			}
			else
			{
				end = start - txOnPage;	
			}
		}
		else
		{
			end = 1;
		}

		output.put("end", end);

		int counter = 0;

		AssetNames assetNames = new AssetNames();
		
		for (BlExpUnit unit : all) {
			if(counter >= size - start)
			{
				output.put(size - counter, jsonUnitPrint(unit.getUnit(), assetNames));
			}

			if(counter > size - end)
			{
				break;
			}

			counter++;
		}

		return output;
	}

	public Map jsonQueryBalance(String address)
	{
		Map output = new LinkedHashMap();

		if(!Crypto.getInstance().isValidAddress(address))
		{
			output.put("error", "Address is not valid!");
			return output; 
		}

		SortableList<Tuple2<String, Long>, BigDecimal> assetsBalances = DBSet.getInstance().getBalanceMap().getBalancesSortableList(new Account(address));

		for (Pair<Tuple2<String, Long>, BigDecimal> assetsBalance : assetsBalances) 	
		{
			Map assetBalance = new LinkedHashMap();

			assetBalance.put("assetName", Controller.getInstance().getAsset(assetsBalance.getA().b).getName());
			assetBalance.put("amount", assetsBalance.getB().toPlainString());
			
			output.put(assetsBalance.getA().b, assetBalance);
		}
		
		return output; 
	}
	
	public Map<Long, BigDecimal> assetBalance(String address)
	{
		Map<Long, BigDecimal> output = new LinkedHashMap();

		SortableList<Tuple2<String, Long>, BigDecimal> assetsBalances = DBSet.getInstance().getBalanceMap().getBalancesSortableList(new Account(address));

		for (Pair<Tuple2<String, Long>, BigDecimal> assetsBalance : assetsBalances) 	
		{
			output.put(assetsBalance.getA().b, assetsBalance.getB());
		}
		
		return output; 
	}
	
	
	
	@SuppressWarnings("serial")
	public Map jsonQueryAddress(List<String> addresses, int start, int txOnPage, String filter, boolean allOnOnePage, String showOnly, String showWithout)
	{
		TreeSet<BlExpUnit> all = new TreeSet<>();
	
		addresses = new ArrayList<>(new LinkedHashSet<String>(addresses));

		Map error = new LinkedHashMap();
		
		Map output = new LinkedHashMap();

		Map<String, Boolean> showOnlyMap = new LinkedHashMap<String, Boolean>();
		for (String string : showOnly.split(",")) {
			showOnlyMap.put(string, true);
		}
		
		Map<String, Boolean> showWithoutMap = new LinkedHashMap<String, Boolean>();
		for (String string : showWithout.split(",")) {
			showWithoutMap.put(string, true);
		}
		

		for (String address : addresses)
		{
			if(!Crypto.getInstance().isValidAddress(address))
			{
				error.put(address, "Address is not valid!");
			}
			
			if (addresses.size() > 1 && address.startsWith("A"))
			{
				error.put(address, "Multiple addresses with the AT is not supported!");
			}
		}
		
		if (!error.isEmpty()) {
			output.put("error", error);
			return output;
		}
		
		if (addresses.size() > 10)
		{
			output.put("error", "Too many addresses!");
		}
		
		Map<String, Integer> txsCountOfAddr = new LinkedHashMap<>();  

		output.put( "addresses", addresses );

		if(addresses.get(0).startsWith("A"))
		{
			String address = addresses.get(0);
			
			AT at = DBSet.getInstance().getATMap().getAT(address);
			Block block = Controller.getInstance().getBlockByHeight(at.getCreationBlockHeight());
			long aTtimestamp = block.getTimestamp(); 
			BigDecimal aTbalanceCreation = BigDecimal.ZERO.setScale(8); 
			for (Transaction transaction : block.getTransactions()) {
				if (transaction.getType() == Transaction.DEPLOY_AT_TRANSACTION )
				{
					Account atAccount = ((DeployATTransaction)transaction).getATaccount();

					if(atAccount.getAddress().equals(address))
					{
						all.add( new BlExpUnit(at.getCreationBlockHeight(), 0, transaction) );
						aTbalanceCreation = ((DeployATTransaction)transaction).getAmount();
					}
				}
			}

			Set<BlExpUnit> atTransactions = DBSet.getInstance().getATTransactionMap().getBlExpATTransactionsBySender(address);
			
			all.addAll( atTransactions );

			output.put("type", "at");

			Map atJSON = new LinkedHashMap();
			atJSON = at.toJSON();
			atJSON.put("balanceCreation", aTbalanceCreation.toPlainString());
			atJSON.put("timestamp", aTtimestamp);
			atJSON.put("dateTime", BlockExplorer.timestampToStr(aTtimestamp));

			output.put("at", atJSON);
		}
		else
		{
			output.put("type", "standardAccount");	
		}

		for (String address : addresses) {
			if (!address.startsWith("A")) {
				Collection<byte[]> blocks = DBSet.getInstance().getBlockMap().getGeneratorBlocks(address);
				
				for (byte[] b : blocks)
				{
					Block block = DBSet.getInstance().getBlockMap().get(b);
					all.add( new BlExpUnit( block.getHeight(), 0, block ) );
				}
			}
		
			Set<BlExpUnit> transactions = DBSet.getInstance().getTransactionFinalMap().getBlExpTransactionsByAddress(address);
			txsCountOfAddr.put(address, transactions.size());
			all.addAll(transactions);
		}
		
		for (String address : addresses) {
			Map<Tuple2<BigInteger, BigInteger>, Trade> trades = new TreeMap<Tuple2<BigInteger, BigInteger>, Trade>();
			List<Transaction> orders = DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress(address, Transaction.CREATE_ORDER_TRANSACTION, 0);
			for (Transaction transaction : orders)
			{
				Order order =  ((CreateOrderTransaction)transaction).getOrder();
	
				SortableList<Tuple2<BigInteger, BigInteger>, Trade> tradesBuf = Controller.getInstance().getTrades(order);
				for (Pair<Tuple2<BigInteger, BigInteger>, Trade> pair : tradesBuf) {
					trades.put(pair.getA(), pair.getB());
				}
			}
	
			for(Map.Entry<Tuple2<BigInteger, BigInteger>, Trade> trade : trades.entrySet())
			{
				Transaction txInitiator = Controller.getInstance().getTransaction(trade.getValue().getInitiator().toByteArray());
				
				Transaction txTarget = Controller.getInstance().getTransaction(trade.getValue().getTarget().toByteArray());
				
				all.add( new BlExpUnit(txInitiator.getParent().getHeight(), txTarget.getParent().getHeight(), txInitiator.getSeq(), txTarget.getSeq(), trade.getValue() ) );
			}
			
			Set<BlExpUnit> atTransactions = DBSet.getInstance().getATTransactionMap().getBlExpATTransactionsByRecipient(address);
			all.addAll( atTransactions );
		}
		
		int size = all.size();
		
		if(size == 0)
		{
			output.put("error", "No transactions found for this address.<br>It has probably not been used on the network yet.");
			return output;
		}
		
		int tradesCount = 0;
		int aTTxsCount = 0;
		int txsCount = 0;
		int totalBlocksGeneratedCount = 0;
		BigDecimal totalBlocksGeneratedFee = BigDecimal.ZERO.setScale(8);
		int[] txsTypeCount = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		List<Map<String, Map<Long, BigDecimal>>> tXincomes = new ArrayList<>();
		List<Map<Long, BigDecimal>> totalBalances = new ArrayList<>();
		BigDecimal spentFee = BigDecimal.ZERO.setScale(8);
		Map<Long, BigDecimal> receivedCoins = new LinkedHashMap<>();  
		Map<Long, BigDecimal> sentCoins = new LinkedHashMap<>();  
		Map<String, BigDecimal> generatedFee = new LinkedHashMap<>();  
		Map<String, Integer> blocksGeneratedCount = new LinkedHashMap<>();  
		
		Map<Long, BigDecimal> zeroAmount = new LinkedHashMap<Long, BigDecimal>(){{put(BalanceMap.QORA_KEY, BigDecimal.ZERO);}};
		
		int i = 1;
		for ( BlExpUnit unit : all ) {
			
			Map<String, Map<Long, BigDecimal>> tXincome = new LinkedHashMap<>();
			
			if (unit.getUnit() instanceof Transaction) {
				
				Transaction tx = (Transaction)unit.getUnit();
				tXincome = tx.getAssetAmount();
				
				if (tx.getCreator() != null && addresses.contains(tx.getCreator().getAddress()))
				{
					spentFee = spentFee.add(tx.getFee());
				}
				
				txsCount ++;
				txsTypeCount[((Transaction)unit.getUnit()).getType()-1] ++;
				
			} else if (unit.getUnit() instanceof Block) {
				
				BigDecimal fee = ((Block)unit.getUnit()).getTotalFee();
				String generator = ((Block)unit.getUnit()).getGenerator().getAddress();
				
				tXincome = Transaction.addAssetAmount(tXincome, generator, BalanceMap.QORA_KEY, fee);

				generatedFee.put(
						generator, 
						generatedFee.getOrDefault(generator, BigDecimal.ZERO.setScale(8)).add(fee)
						);
				
				totalBlocksGeneratedFee = totalBlocksGeneratedFee.add(fee);
				
				blocksGeneratedCount.put(
						generator, 
						blocksGeneratedCount.getOrDefault(generator, 0) + 1
						);
				
				totalBlocksGeneratedCount ++;
				
			} 
			else if (unit.getUnit() instanceof Trade) 
			{
				Trade trade = (Trade)unit.getUnit();

				Order orderInitiator;
				if (DBSet.getInstance().getCompletedOrderMap().contains(trade.getInitiator())) 
				{
					orderInitiator =  DBSet.getInstance().getCompletedOrderMap().get(trade.getInitiator());
				} 
				else 
				{
					orderInitiator =  DBSet.getInstance().getOrderMap().get(trade.getInitiator());
				}

				Order orderTarget;
				if (DBSet.getInstance().getCompletedOrderMap().contains(trade.getTarget())) 
				{
					orderTarget =  DBSet.getInstance().getCompletedOrderMap().get(trade.getTarget());
				} 
				else 
				{
					orderTarget =  DBSet.getInstance().getOrderMap().get(trade.getTarget());
				}

				if(addresses.contains(orderInitiator.getCreator().getAddress())) 
				{
					tXincome = Transaction.addAssetAmount(tXincome, orderInitiator.getCreator().getAddress(), orderInitiator.getWant(), trade.getAmount());
				}

				if(addresses.contains(orderTarget.getCreator().getAddress())) {
					
					tXincome = Transaction.addAssetAmount(tXincome, orderTarget.getCreator().getAddress(), orderInitiator.getHave(), trade.getPrice());
					
				}

				tradesCount ++;
				
			} 
			else if (unit.getUnit() instanceof AT_Transaction) 
			{
				AT_Transaction atTransaction = (AT_Transaction)unit.getUnit();

				if (addresses.contains(atTransaction.getSender())) 
				{
					tXincome = Transaction.subAssetAmount(tXincome, atTransaction.getSender(), BalanceMap.QORA_KEY, BigDecimal.valueOf( atTransaction.getAmount() , 8));
				}
				
				if(addresses.contains(atTransaction.getRecipient())) 
				{
					tXincome = Transaction.addAssetAmount(tXincome, atTransaction.getRecipient(), BalanceMap.QORA_KEY, BigDecimal.valueOf( atTransaction.getAmount() , 8));
				}

				aTTxsCount++;
			}

			tXincomes.add(tXincome);

			Map<Long, BigDecimal> newTotalBalance;
			if (totalBalances.size() > 0) 
			{
				newTotalBalance = new LinkedHashMap<>(totalBalances.get(totalBalances.size()-1));
			} 
			else 
			{
				newTotalBalance = new LinkedHashMap<>(zeroAmount);
			}

			for (String address : addresses) 
			{
				for (Map.Entry<Long, BigDecimal> assetAmount : tXincome.getOrDefault(address, zeroAmount).entrySet()) 
				{
					if (assetAmount.getValue().compareTo(BigDecimal.ZERO) < 0)
					{
						sentCoins.put(
								assetAmount.getKey(), 
								sentCoins.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(8))
								.subtract(assetAmount.getValue())
								);
					}
					
					if (assetAmount.getValue().compareTo(BigDecimal.ZERO) > 0)
					{
						receivedCoins.put(
								assetAmount.getKey(), 
								receivedCoins.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(8))
								.add(assetAmount.getValue())
								);
					}
					
					newTotalBalance.put(assetAmount.getKey(), 
							newTotalBalance.getOrDefault(assetAmount.getKey(), BigDecimal.ZERO.setScale(8))
							.add(assetAmount.getValue())
							);
				}
					
				if ((newTotalBalance.containsKey(BalanceMap.QORA_KEY)) && newTotalBalance.get(BalanceMap.QORA_KEY).compareTo(BigDecimal.ZERO) < 0) 
				{
					System.out.println(i);
				}
			}
			
			totalBalances.add(newTotalBalance);
			
			i++;
		}

		Map blockExplorerBalance=new LinkedHashMap();
		Map total=new LinkedHashMap();

		Map<Long, String> receivedCoinsPrint = new LinkedHashMap();
		for (Map.Entry<Long, BigDecimal> e : receivedCoins.entrySet())	
		{
			receivedCoinsPrint.put(e.getKey(), e.getValue().toPlainString());
		}
		blockExplorerBalance.put("received", receivedCoinsPrint);

		Map<Long, String> sentCoinsPrint = new LinkedHashMap();
		for (Map.Entry<Long, BigDecimal> e : sentCoins.entrySet())	
		{
			if (e.getKey() == BalanceMap.QORA_KEY) 
			{
				sentCoinsPrint.put(e.getKey(), e.getValue().subtract(spentFee).toPlainString());
			}
			else
			{
				sentCoinsPrint.put(e.getKey(), e.getValue().toPlainString());
			}	
		}
		blockExplorerBalance.put("sent", sentCoinsPrint);

		blockExplorerBalance.put("spentFee", spentFee.toPlainString());
		
		for (Map.Entry<Long, BigDecimal> assetAmounts : totalBalances.get(size - 1).entrySet())
		{	
			total.put(assetAmounts.getKey(), assetAmounts.getValue().toPlainString());
		}
		blockExplorerBalance.put("total", total);
		
		output.put("balance", blockExplorerBalance);

		Map generatedBlocks = new LinkedHashMap();
		for (Map.Entry<String, Integer> e : blocksGeneratedCount.entrySet())	
		{
			Map generatedInfo = new LinkedHashMap();
			generatedInfo.put("count", e.getValue());
			generatedInfo.put("fees", generatedFee.get(e.getKey()).toPlainString());
			generatedBlocks.put(e.getKey(), generatedInfo);
		}

		Map generatedInfo = new LinkedHashMap();
		generatedInfo.put("count", totalBlocksGeneratedCount);
		generatedInfo.put("fees", totalBlocksGeneratedFee.toPlainString());
		generatedBlocks.put("total", generatedInfo);
		
		output.put("generatedBlocks", generatedBlocks);

		output.put("txsCountOfAddr", txsCountOfAddr);
			
		Map nativeBalance = new LinkedHashMap();
		
		Map<Long, BigDecimal> assetAmountTotal = new LinkedHashMap<>();

		for (String address : addresses) {

			Map<Long, BigDecimal> assetAmountOfAddr = assetBalance(address);

			Map<Long, String> assetAmountOfAddrPrint = new LinkedHashMap<>();
			
			for (Map.Entry<Long, BigDecimal> assetAmounts : assetAmountOfAddr.entrySet()) 
			{
				if (assetAmountTotal.containsKey(assetAmounts.getKey())) 
				{
					assetAmountTotal.put(assetAmounts.getKey(), assetAmountTotal.get(assetAmounts.getKey()).add(assetAmounts.getValue()));
				} 
				else
				{
					assetAmountTotal.put(assetAmounts.getKey(), assetAmounts.getValue());
				}
				
				assetAmountOfAddrPrint.put(assetAmounts.getKey(), assetAmounts.getValue().toPlainString());
			}
			
			nativeBalance.put(address, assetAmountOfAddrPrint);
		}
		
		Map<Long, String> assetAmountTotalPrint = new LinkedHashMap<>();
		for (Map.Entry<Long, BigDecimal> assetAmounts : assetAmountTotal.entrySet()) 
		{
			assetAmountTotalPrint.put(assetAmounts.getKey(), assetAmounts.getValue().toPlainString());
		}
		
		nativeBalance.put("total", assetAmountTotalPrint);
		
		output.put("nativeBalance", nativeBalance);

		Map assetNames=new LinkedHashMap();

		for (Map.Entry<Long, BigDecimal> assetAmounts : totalBalances.get(size - 1).entrySet())
		{	
			assetNames.put(assetAmounts.getKey(), Controller.getInstance().getAsset(assetAmounts.getKey()).getName());
		}
		
		output.put("assetNames", assetNames);
		
		Map txCountJSON = new LinkedHashMap();
		
		if (!showOnly.equals(""))
		{
			showWithoutMap.clear();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					if(!showOnlyMap.containsKey(String.valueOf(n)))
					{
						showWithoutMap.put(String.valueOf(n), true);
					}
				}
				
				n ++;
			}
			
			if(totalBlocksGeneratedCount > 0)
			{
				if(!showOnlyMap.containsKey("blocks"))
				{
					showWithoutMap.put("blocks", true);
				}	
			}
			
			if(aTTxsCount > 0)
			{
				if(!showOnlyMap.containsKey("aTTxs"))
				{
					showWithoutMap.put("aTTxs", true);
				}	
			}
			
			if(tradesCount > 0)
			{
				if(!showOnlyMap.containsKey("trades"))
				{
					showWithoutMap.put("trades", true);
				}	
			}
		}
		
		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);					
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}
		if(totalBlocksGeneratedCount > 0)
		{
			txCountJSON.put("blocksCount", totalBlocksGeneratedCount);
		}
		if(aTTxsCount > 0)
		{
			txCountJSON.put("aTTxsCount", aTTxsCount);
		}
		if(tradesCount > 0)
		{
			txCountJSON.put("tradesCount", tradesCount);
		}

		txCountJSON.put("allCount",  tradesCount + aTTxsCount + totalBlocksGeneratedCount + txsCount);

		output.put("countTx", txCountJSON);

		output.put("txOnPage", txOnPage);

		output.put("filter", filter);

		output.put("allOnOnePage", allOnOnePage);

		output.put("showOnly", showOnly);

		output.put("showWithout", showWithout);
		
		int end = -1;

		int counter = size;

		Map<Integer, Map<String, Integer>> pagesStartEnd = new LinkedHashMap();
		Map<String, Integer> pageStartEnd = new LinkedHashMap();

		int onThisPage = 0;
		int pagesCounter = 0;

		int onThisPageCurent = 0;
		boolean firstPage = false;

		Iterator iterator;
		iterator = all.descendingIterator();
		
		while (iterator.hasNext()){
			
			BlExpUnit unit = (BlExpUnit) iterator.next();
			
			onThisPage ++;
			
			if(((unit.getUnit() instanceof Block) && (showWithoutMap.containsKey("blocks"))))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof Trade) && showWithoutMap.containsKey("trades")))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof AT_Transaction) && showWithoutMap.containsKey("aTTxs")))
			{
				onThisPage --;
			}

			if(((unit.getUnit() instanceof Transaction) && showWithoutMap.containsKey(String.valueOf(((Transaction)unit.getUnit()).getType()))))
			{
				onThisPage --;
			}
			
			if(!firstPage && onThisPage == 1)
			{
				pageStartEnd.put("start", counter);
				firstPage = true;
				
				if(start == -1)
				{
					start = counter;
				}
			}

			if(onThisPage >= txOnPage)
			{
				pageStartEnd.put("end", counter);

				onThisPage = 0;
				firstPage = false;
			}

			if(pageStartEnd.size() == 2)
			{
				pagesCounter ++;
				
				pagesStartEnd.put(pagesCounter, new LinkedHashMap(pageStartEnd));
				
				pageStartEnd.clear();
			}
			
			if(start != -1 && counter <= start && ((onThisPageCurent < txOnPage) || allOnOnePage))
			{
				if((unit.getUnit() instanceof Block) && (showWithoutMap.containsKey("blocks")))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof Trade) && showWithoutMap.containsKey("trades"))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof AT_Transaction) && showWithoutMap.containsKey("aTTxs"))
				{
					counter--;
					continue;
				}

				if((unit.getUnit() instanceof Transaction) && showWithoutMap.containsKey(String.valueOf(((Transaction)unit.getUnit()).getType())))
				{
					counter--;
					continue;
				}

				onThisPageCurent++;
				
				Map transactionJSON = new LinkedHashMap();
				
				transactionJSON.putAll(jsonUnitPrint(unit.getUnit(), null));

				Map tXbalanceChange = new LinkedHashMap();
				Map<Long, Boolean> assetIsChange = new LinkedHashMap(){{ put(BalanceMap.QORA_KEY, true); }};

				for(Map.Entry<String, Map<Long, BigDecimal>> addrsMap : tXincomes.get(counter - 1).entrySet()) 
				{
					if (addresses.contains(addrsMap.getKey())) 
					{
						Map<Long, String> tXaddrBalanceChange = new LinkedHashMap();

						for(Map.Entry<Long, BigDecimal> assetAmount : addrsMap.getValue().entrySet()) 
						{
							tXaddrBalanceChange.put(assetAmount.getKey(), assetAmount.getValue().toPlainString());
							assetIsChange.put(assetAmount.getKey(), true);
						}
						
						tXbalanceChange.put(addrsMap.getKey(), tXaddrBalanceChange);
					}
				}
				
				transactionJSON.put("tXbalanceChange", tXbalanceChange);

				Map<Long, String> tXbalance = new LinkedHashMap();

				for (Long assetKey : assetIsChange.keySet() )	
				{
					tXbalance.put(assetKey, totalBalances.get( counter - 1 ).get(assetKey).toPlainString());
				}

				transactionJSON.put("tXbalance", tXbalance);

				output.put(counter, transactionJSON);

				end = counter;
			}

			counter--;
		}
		
		if(pageStartEnd.size() == 1)
		{
			pageStartEnd.put("end", 1);
			
			pagesCounter ++;

			pagesStartEnd.put(pagesCounter, new LinkedHashMap(pageStartEnd));
		}
		
		output.put("start", start);
		output.put("end", end);
		
		output.put("pages", pagesStartEnd);

		return output;
	}	

	public Map jsonQueryATtx(String query)
	{
		Map output=new LinkedHashMap();

		int blockHeight = Integer.valueOf(query.split(":")[0]);
		int seq = Integer.valueOf(query.split(":")[1]);

		output.put("type", "atTransaction");

		output.put("atTransaction", query);

		LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = DBSet.getInstance().getATTransactionMap().getATTransactions(blockHeight);

		AssetNames assetNames = new AssetNames();
		
		for(Entry<Tuple2<Integer, Integer>, AT_Transaction> e : atTxs.entrySet())
		{	
			if(e.getValue().getSeq() == seq)
			{
				output.put(1, jsonUnitPrint(e.getValue(), assetNames));
			}
		}

		output.put("assetNames", assetNames.getMap());
		
		output.put("start", 1);
		output.put("end", 1);

		return output;
	}

	public Map jsonQueryTrade(String query)
	{
		Map output=new LinkedHashMap();
		AssetNames assetNames = new AssetNames();

		List<Object> all = new ArrayList<Object>();

		String[] signatures = query.split("/");

		Trade trade = DBSet.getInstance().getTradeMap().get(Fun.t2(Base58.decodeBI(signatures[0]), Base58.decodeBI(signatures[1])));
		output.put("type", "trade");
		output.put("trade", query);

		all.add(trade);

		all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[0])));
		all.add(Controller.getInstance().getTransaction(Base58.decode(signatures[1])));

		int size = all.size();

		output.put("start", size);
		output.put("end", 1);

		int counter = 0;
		for (Object unit : all) {
			output.put(size - counter, jsonUnitPrint(unit, assetNames));
			counter ++;
		}

		output.put("assetNames", assetNames.getMap());

		return output;
	}

	public Map jsonQueryTX(String query)
	{
		Map output=new LinkedHashMap();
		AssetNames assetNames = new AssetNames();

		TreeSet<BlExpUnit> all = new TreeSet<>();
		Map<Tuple2<BigInteger, BigInteger>, Trade> trades = new TreeMap<Tuple2<BigInteger, BigInteger>, Trade>();

		String[] signatures = query.split(",");

		byte[] signatureBytes = null;

		output.put("type", "transaction");

		for (int i = 0; i < signatures.length; i++) {
			signatureBytes = Base58.decode(signatures[i]);
			Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
			all.add( new BlExpUnit( transaction.getParent().getHeight(), transaction.getSeq(), transaction));

			if(transaction instanceof CreateOrderTransaction)
			{
				Order order =  ((CreateOrderTransaction)transaction).getOrder();

				SortableList<Tuple2<BigInteger, BigInteger>, Trade> tradesBuf = Controller.getInstance().getTrades(order);
				for (Pair<Tuple2<BigInteger, BigInteger>, Trade> pair : tradesBuf) {
					trades.put(pair.getA(), pair.getB());
				}
			}
		}
		
		for(Map.Entry<Tuple2<BigInteger, BigInteger>, Trade> trade : trades.entrySet())
		{
			Transaction txInitiator = Controller.getInstance().getTransaction(trade.getValue().getInitiator().toByteArray());
			
			Transaction txTarget = Controller.getInstance().getTransaction(trade.getValue().getTarget().toByteArray());
			
			all.add( new BlExpUnit(txInitiator.getParent().getHeight(), txTarget.getParent().getHeight(), txInitiator.getSeq(), txTarget.getSeq(), trade.getValue() ) );
		}

		int size = all.size();

		output.put("start", size);
		output.put("end", 1);

		int counter = 0;
		for (BlExpUnit unit : all) {
			output.put(size - counter, jsonUnitPrint(unit.getUnit(), assetNames));
			counter ++;
		}
		
		output.put("assetNames", assetNames.getMap());

		return output;
	}

	public Map jsonQueryBlock(String query)
	{
		Map output=new LinkedHashMap();
		List<Object> all = new ArrayList<Object>();
		int[] txsTypeCount = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		int aTTxsCount = 0;
		Block block; 
		AssetNames assetNames = new AssetNames();

		if(query.matches("\\d+"))
		{
			block = Controller.getInstance().getBlockByHeight(Integer.valueOf(query));
		}
		else if (query.equals("last"))
		{
			block = getLastBlock();
		}
		else
		{
			block = Controller.getInstance().getBlock(Base58.decode(query));
		}

		for(Transaction transaction: block.getTransactions())
		{
			all.add(transaction);
			txsTypeCount[transaction.getType()-1] ++;
		}

		int txsCount = all.size();

		LinkedHashMap<Tuple2<Integer, Integer>, AT_Transaction> atTxs = DBSet.getInstance().getATTransactionMap().getATTransactions(block.getHeight());

		for(Entry<Tuple2<Integer, Integer>, AT_Transaction> e : atTxs.entrySet())
		{	
			all.add(e.getValue());
			aTTxsCount ++;
		}

		output.put("type", "block");

		output.put("blockSignature", Base58.encode(block.getSignature()));
		output.put("blockHeight", block.getHeight());

		if(block.getParent() != null)
		{
			output.put("parentBlockSignature", Base58.encode(block.getParent().getSignature()));
		}

		if(block.getChild() != null)
		{
			output.put("childBlockSignature", Base58.encode(block.getChild().getSignature()));
		}

		int size = all.size();

		Map txCountJSON = new LinkedHashMap();

		if(txsCount > 0)
		{
			txCountJSON.put("txsCount", txsCount);
			Map txTypeCountJSON = new LinkedHashMap();
			int n = 1;
			for (int txCount : txsTypeCount) {
				if(txCount > 0)
				{
					txTypeCountJSON.put(n, txCount);
				}
				n ++;
			}
			txCountJSON.put("txsTypesCount", txTypeCountJSON);
		}

		if(aTTxsCount > 0)
		{
			txCountJSON.put("aTTxsCount", aTTxsCount);
		}

		txCountJSON.put("allCount", txsCount);

		output.put("countTx", txCountJSON);

		BigDecimal totalAmount = BigDecimal.ZERO.setScale(8);
		for (Transaction transaction : block.getTransactions()) {
			for (Account account : transaction.getInvolvedAccounts()) {
				BigDecimal amount = transaction.getAmount(account); 
				if(amount.compareTo(BigDecimal.ZERO) > 0)
				{
					totalAmount = totalAmount.add(amount);
				}
			}
		}

		output.put("totalAmount", totalAmount.toPlainString());

		BigDecimal totalATAmount = BigDecimal.ZERO.setScale(8);

		for(Map.Entry<Tuple2<Integer, Integer> , AT_Transaction> e : atTxs.entrySet())
		{	
			totalATAmount = totalATAmount.add(BigDecimal.valueOf( e.getValue().getAmount() , 8));
		}

		output.put("totalATAmount", totalATAmount.toPlainString());
		output.put("aTfee", block.getATfee().toPlainString());
		output.put("totalFee", block.getTotalFee().toPlainString());
		output.put("version", block.getVersion());


		output.put("start", size+1);
		output.put("end", 1);

		Map assetsJSON=new LinkedHashMap();

		int counter = 0;
		for(Object unit: all)
		{
			counter ++;

			output.put(counter, jsonUnitPrint(unit, assetNames));
		}


		{
			Map transactionJSON = new LinkedHashMap();
			Map transactionDataJSON = new LinkedHashMap();

			transactionDataJSON = new LinkedHashMap();
			transactionDataJSON.put("timestamp", block.getTimestamp());
			transactionDataJSON.put("dateTime", BlockExplorer.timestampToStr(block.getTimestamp()));

			int height = block.getHeight();
			transactionDataJSON.put("confirmations", getHeight() - height + 1 );
			transactionDataJSON.put("height", height);

			transactionDataJSON.put("generator", block.getGenerator().getAddress());
			transactionDataJSON.put("signature", Base58.encode(block.getSignature()));

			transactionDataJSON.put("generatingBalance", block.getGeneratingBalance());
			transactionDataJSON.put("atFees", block.getATfee().toPlainString());
			transactionDataJSON.put("reference", Base58.encode(block.getReference()));
			transactionDataJSON.put("generatorSignature", Base58.encode(block.getGeneratorSignature()));
			//transactionDataJSON.put("transactionsSignature", Base58.encode(block.getTransactionsSignature()));
			transactionDataJSON.put("version", block.getVersion());

			transactionDataJSON.put("fee", block.getTotalFee().toPlainString());

			transactionJSON.put("type", "block");
			transactionJSON.put("block", transactionDataJSON);

			output.put(counter + 1, transactionJSON);
		}

		output.put("assetNames", assetNames.getMap());
		
		output.put("totalBalance", assetsJSON);

		return output;
	}


	public Map jsonQueryUnconfirmedTXs()
	{
		Map output=new LinkedHashMap();
		List<Transaction> all = new ArrayList<Transaction>();

		AssetNames assetNames = new AssetNames();

		all.addAll(Controller.getInstance().getUnconfirmedTransactions());

		output.put("type", "unconfirmed");

		int size = all.size();

		output.put("start", size);

		if(size>0)
		{
			output.put("end", 1);	
		}
		else
		{
			output.put("end", 0);	
		}	

		int counter = 0;
		for(Object unit: all)
		{
			counter ++;

			output.put(counter, jsonUnitPrint(unit, assetNames));
		}

		return output;
	}

	class AssetNames
	{
		private Map<Long, String> assetNames;

		public AssetNames()
		{
			assetNames = new TreeMap<Long, String>();
		}

		public void setKey(long key)
		{
			if(!assetNames.containsKey(key))
			{
				assetNames.put(key, Controller.getInstance().getAsset(key).getName());			
			}
		}
		
		public Map<Long, String> getMap()
		{
			return assetNames;
		}
	}

	class Balance
	{
		private Map<Long, BigDecimal> totalBalance;
		private Map<Long, BigDecimal> transactionBalance;
		public Balance()
		{
			totalBalance = new TreeMap<Long, BigDecimal>();
			transactionBalance = new TreeMap<Long, BigDecimal>();
		}
		public void setTotalBalance(long key, BigDecimal amount)
		{
			totalBalance.put(key, amount);
		}

		public void addTotalBalance(long key, BigDecimal amount)
		{
			if(totalBalance.containsKey(key))
			{
				totalBalance.put(key, totalBalance.get(key).add(amount));
			}
			else
			{
				totalBalance.put(key, amount);
			}
		}

		public void setTransactionBalance(long key, BigDecimal amount)
		{
			transactionBalance.put(key, amount);
		}

		public void addTransactionBalance(long key, BigDecimal amount)
		{
			if(transactionBalance.containsKey(key))
			{
				transactionBalance.put(key, transactionBalance.get(key).add(amount));
			}
			else
			{
				transactionBalance.put(key, amount);
			}
		}

		public BigDecimal getTransactionBalance(long key)
		{
			if(transactionBalance.containsKey(key))
			{
				return transactionBalance.get(key);
			}
			else
			{
				return BigDecimal.ZERO.setScale(8);
			}			
		}

		public BigDecimal getTotalBalance(long key)
		{
			if(totalBalance.containsKey(key))
			{
				return totalBalance.get(key);
			}
			else
			{
				return BigDecimal.ZERO.setScale(8);
			}		
		}

		public Map<Long, BigDecimal> getTotalBalance()
		{
			return totalBalance;
		}

		public Map<Long, BigDecimal> getTransactionBalance()
		{
			return transactionBalance;
		}

		public void setFromTransactionToTotalBalance()
		{
			for(Map.Entry<Long, BigDecimal> e : transactionBalance.entrySet()){
				if(totalBalance.containsKey(e.getKey()))
				{
					totalBalance.put(e.getKey(), totalBalance.get(e.getKey()).add(e.getValue()));
				}
				else
				{
					totalBalance.put(e.getKey(), e.getValue());
				}
			}
		}

		public void copyTotalBalanceFrom(Map<Long, BigDecimal> fromTotalBalance)
		{
			for(Map.Entry<Long, BigDecimal> e : fromTotalBalance.entrySet())
			{	
				totalBalance.put(e.getKey(), e.getValue());
			}
		}
	}

	public class BigDecimalComparator implements Comparator<Tuple2<String, BigDecimal>> {

		@Override
		public int compare(Tuple2<String, BigDecimal> a, Tuple2<String, BigDecimal> b) 
		{	
			try
			{
				return a.b.compareTo(b.b);
			}
			catch(Exception e)
			{
				return 0;
			}
		}

	}
	

	
	/*
	public int getHeight() {
		return  Controller.getInstance().getHeight();
	}
	
	public Block getLastBlock() 
	{
		return  Controller.getInstance().getLastBlock();
	}
	*/
	
	public int getHeight() {
		
		//GET LAST BLOCK
		byte[] lastBlockSignature = DBSet.getInstance().getBlockMap().getLastBlockSignature();
		
		//RETURN HEIGHT
		return DBSet.getInstance().getHeightMap().get(lastBlockSignature);
	}
	
	public Block getLastBlock() 
	{	
		return DBSet.getInstance().getBlockMap().getLastBlock();
	}
	
	public static class Stopwatch { 

		private long start;

		/**
		 * Create a stopwatch object.
		 */
		public Stopwatch() {
			start = System.currentTimeMillis();
		} 


		/**
		 * Return elapsed time (in seconds) since this object was created.
		 */
		public double elapsedTime() {
			long now = System.currentTimeMillis();
			return (now - start);
		}
		public double elapsedTime0() {
			long now = System.currentTimeMillis();
			long start0 = start;
			start = System.currentTimeMillis();
			return (now - start0);
		}

	} 


}
