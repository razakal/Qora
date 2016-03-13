package utils;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun.Tuple2;

import database.DBSet;
import qora.account.Account;
import qora.assets.Asset;
import qora.block.Block;
import qora.block.GenesisBlock;
import qora.blockexplorer.BlockExplorer;
import qora.blockexplorer.BlockExplorer.Stopwatch;
import qora.crypto.Base58;
import qora.transaction.Transaction;
import settings.Settings;


public class BlockExplorerTest {

	public void maxBalance() 
	{
		byte[] amountBytes = new byte[]{127, 127, 127, 127, 127, 127, 127, 127};
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		System.out.println(amount.toPlainString());
		amountBytes = new byte[]{-128, -128, -128, -128, -128, -128, -128, -128};
		amount = new BigDecimal(new BigInteger(amountBytes), 8);
		System.out.println(amount.toPlainString());
	}
	
	public static DBSet createRealEmptyDatabaseSet() {
		//OPEN DB
		File dbFile = new File(Settings.getInstance().getDataDir(), "data2.dat");
		dbFile.getParentFile().mkdirs();
		
		//CREATE DATABASE	
		DB database = DBMaker.newFileDB(dbFile)
				.closeOnJvmShutdown()
				.cacheSize(2048)
				.checksumEnable()
				.mmapFileEnableIfSupported()
				.make();
		
		//CREATE INSTANCE
		return new DBSet(database);		
	}	
	
	public void minBalance() 
	{
		
		Block block = new GenesisBlock();

		DBSet databaseSet = createRealEmptyDatabaseSet();

		List<Pair<Block, BigDecimal>> balancesBlocks =  new ArrayList<>();
		
		Stopwatch stopwatchAll = new Stopwatch();
		
		//ADD QORA ASSET
    	Asset qoraAsset = new Asset(block.getGenerator(), "Qora", "This is the simulated Qora asset.", 10000000000L, true, block.getGeneratorSignature());
    	databaseSet.getIssueAssetMap().set(block.getGeneratorSignature(), 0l);
    	databaseSet.getAssetMap().set(0l, qoraAsset);
    	
		do {
			
			block.process(databaseSet);
			
			if ( block.getHeight()%2000 == 0 )
			{
				System.out.println(block.getHeight());
			}
			
			balancesBlocks.add(new Pair<>(block, block.getGenerator().getBalance(1, databaseSet)));
			
			block = block.getChild();
			
		} while (block != null);
		
		System.out.println(stopwatchAll.elapsedTime()/1000 + " secs");
		
		Collections.sort(balancesBlocks, new BalancesBlocksComparator());
		
		for (int i = 0; i < 400; i++) {
			System.out.print(
					Base58.encode(balancesBlocks.get(i).getA().getSignature())
					);
			System.out.print( " " +
					balancesBlocks.get(i).getA().getGenerator().getAddress());
			System.out.println(" " + balancesBlocks.get(i).getB().toPlainString());
		}
		
		
	}
	
	public class BalancesBlocksComparator implements Comparator<Pair<Block, BigDecimal>> {
		
		@Override
		public int compare(Pair<Block, BigDecimal> one, Pair<Block, BigDecimal> two) 
		{
			return one.getB().compareTo(two.getB());
		}
	}
	
	@Test
	@SuppressWarnings({"rawtypes", "unchecked"})
	public void blockExplorer() throws Exception {
		
		DBSet.getInstance();
		
		ArrayList<String> addrs = new ArrayList();
		addrs.add("QXncuwPehVZ21ymE1jawXg1Uv3sZZ4TvYk");
		addrs.add("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
		addrs.add("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU");
		addrs.add("QQPsGx3khgEboJXWPiDBMVDG5ngu9wDo3k");
		addrs.add("Qd9jQKZSXoYgFypTQySJUSbXcZvjgdiemn");
		addrs.add("QfyocFSGghfpANqUmQFpoG2sk5TVg8LvEm");
		addrs.add("QMu6HXfZCnwaNmyFjjhWTYAUW7k1x7PoVr");
		addrs.add("QdrhixdevE7ZJqSHAfV19yVYrYsys8VLgz");
		addrs.add("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4");
		
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
		boolean allOnOnePage = false;
		String showOnly = "";
		String showWithout = "";
		
		DBSet.getInstance();
		
		for(int i = 0; i < addrs.size(); i++) {
			
			String addr = addrs.get(i);
			List<String> listaddr = new ArrayList<>();
			listaddr.add(addr);
			
			Map<Object, Map> output = BlockExplorer.getInstance().jsonQueryAddress(listaddr, start, txOnPage, filter, allOnOnePage, showOnly, showWithout);
	
			Map<Long, String> totalBalance = (Map<Long, String>) output.get("balance").get("total");
			
			Account account = new Account(addr);
			
			System.out.println(addr);
			for(Map.Entry<Long, String> e : totalBalance.entrySet())
			{
				Long key = e.getKey();
				
				BigDecimal blockExplorerBalance =  new BigDecimal(e.getValue());
				
				System.out.print("(" + key + ") " + " BlockExplorerBalance: " + blockExplorerBalance);
				
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
		
		DBSet.getInstance().close();
	}
	
	public void getTransactionsByAddress() {

		DBSet.getInstance().getTransactionFinalMap().contains(new Tuple2<Integer, Integer>(1, 1));
		
		Stopwatch stopwatchAll = new Stopwatch();

		List<Object> all = new ArrayList<Object>();

		all.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByAddress("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4"));

		System.out.println("getTransactionsByAddress QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4. " + all.size() + " " + stopwatchAll.elapsedTime());
		
		all.clear();
		stopwatchAll = new Stopwatch();
		
		all.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByAddress("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU"));

		System.out.println("getTransactionsByAddress QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU. " + all.size() + " " + stopwatchAll.elapsedTime());

		all.clear();
		
	}

	public void getTransactionsByTypeAndAddress() {
		
		DBSet.getInstance().getTransactionFinalMap().contains(new Tuple2<Integer, Integer>(1, 1));
		
		Stopwatch stopwatchAll = new Stopwatch();

		List<Object> all = new ArrayList<Object>();

		List<Transaction> transactions = new ArrayList<Transaction>();;
		for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress("QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4", type, 0));
		}
		
		Map<String, Boolean> signatures = new LinkedHashMap<String, Boolean>();
		
		for (Transaction transaction : transactions){
			byte[] signature = transaction.getSignature();
			if(!signatures.containsKey( new String(signature) ))
			{	
				signatures.put(new String(signature), true);
				all.add(transaction);
			}
		}
		
		System.out.println("getTransactionsByTypeAndAddress QPVknSmwDryB98Hh8xB7E6U75dGFYwNkJ4. " + all.size() + " " + stopwatchAll.elapsedTime());
		
		all.clear();
		stopwatchAll = new Stopwatch();
		
		transactions = new ArrayList<Transaction>();;
		for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress("QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU", type, 0));
		}
		
		signatures = new LinkedHashMap<String, Boolean>();
		
		for (Transaction transaction : transactions){
			byte[] signature = transaction.getSignature();
			if(!signatures.containsKey( new String(signature) ))
			{	
				signatures.put(new String(signature), true);
				all.add(transaction);
			}
		}
		
		System.out.println("getTransactionsByTypeAndAddress QYsLsfwMRBPnunmuWmFkM4hvGsfooY8ssU. " + all.size() + " " + stopwatchAll.elapsedTime());
		
		all.clear();
		
		
		stopwatchAll = new Stopwatch();
		
		transactions = new ArrayList<Transaction>();;
		for (int type = 1; type <= 23; type++) {  // 17 - The number of transaction types. 23 - for the future
			transactions.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByTypeAndAddress("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ", type, 0));
		}
		
		for (Transaction transaction : transactions) {
			System.out.println(Base58.encode(transaction.getSignature()));
		}
		
		System.out.println("getTransactionsByTypeAndAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + transactions.size() + " " + stopwatchAll.elapsedTime());
		
		all.clear();
		
		stopwatchAll = new Stopwatch();

		all.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByAddress("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ"));

		System.out.println("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());
		
		stopwatchAll = new Stopwatch();
		all.clear();
		all.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsBySender("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ"));

		for (Object transaction : all) {
			System.out.println(Base58.encode(((Transaction)transaction).getSignature()));
		}

		System.out.println("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());
		
		all.clear();
		
		all.addAll(DBSet.getInstance().getTransactionFinalMap().getTransactionsByRecipient("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ"));

		for (Object transaction : all) {
			System.out.println(Base58.encode(((Transaction)transaction).getSignature()));
		}

		System.out.println("getTransactionsByAddress QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ. " + all.size() + " " + stopwatchAll.elapsedTime());
		
	}
		
	public void txItSelf() throws Exception {

		Transaction transaction = getTransaction(Base58.decode("4JXPXqdP7GT743AoX2m8vHBeWNrKvBcf71TcDLfLeMn6rmV5uyVRDcV5gLspNquZyatY4tHB9RXDWKahEM85oTJv"));
		Account account = new Account("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
		System.out.println(transaction.getAmount(account));
		
		transaction = getTransaction(Base58.decode("4JXPXqdP7GT743AoX2m8vHBeWNrKvBcf71TcDLfLeMn6rmV5uyVRDcV5gLspNquZyatY4tHB9RXDWKahEM85oTJv"));
		account = new Account("QRZ5Ggk6o5wwEgzL4Wo3xmueXuDEgwLeyQ");
		System.out.println(transaction.getAmount(account));
	}
	
	public Transaction getTransaction(byte[] signature) {

		return getTransaction(signature, DBSet.getInstance());
	}
	
	public Transaction getTransaction(byte[] signature, DBSet database) {
		
		// CHECK IF IN BLOCK
		Block block = database.getTransactionParentMap()
				.getParent(signature);
		if (block != null) {
			return block.getTransaction(signature);
		}
		
		// CHECK IF IN TRANSACTION DATABASE
		return database.getTransactionMap().get(signature);
	}
}
