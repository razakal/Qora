package utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import controller.Controller;
import database.DBSet;
import lang.Lang;
import network.Peer;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.block.Block;
import qora.crypto.AEScrypto;
import qora.crypto.Base58;
import qora.crypto.Base64;
import qora.crypto.Crypto;
import qora.naming.Name;
import qora.transaction.ArbitraryTransaction;
import qora.transaction.ArbitraryTransactionV3;
import qora.transaction.PaymentTransaction;
import qora.transaction.RegisterNameTransaction;
import qora.transaction.Transaction;

public class AgransTests {

	
	@Test
	public void commpress() throws Exception 
	{
		DBSet.getInstance();
		
		ArbitraryTransaction tx = (ArbitraryTransaction) Controller.getInstance().getTransaction(Base58.decode("3ijkofxsRQMy5BbeJ7pLKQUGpAA4BKH65YpnJf7xLddTtCaQiR8XSgUo6L53d19QAi4ApqDcgKrcAYU19CaDfX4N"));
		
		String string = new String(tx.getData(), Charsets.UTF_8);
		
		String stringgzip = GZIP.compress(string);
		//byte[] stringgzipbytes = GZIP.GZIPcompress(string);
		System.out.println(string.getBytes(Charsets.UTF_8).length);
		System.out.println(stringgzip.getBytes(Charsets.UTF_8).length);
		//System.out.println(stringgzipbytes.length);

		string += "";
		
		DBSet.getInstance().close();

	}		
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void openLangFile() 
	{
		JSONObject langObj = Lang.openLangFile("ru.json");
		JSONObject langObj2 = new JSONObject(); 

	    Iterator iterator = langObj.entrySet().iterator();
	    while(iterator.hasNext()) {
	    	Map.Entry pair = (Entry) iterator.next();
	    	langObj2.put(pair.getKey(), "");
	    }
	    
	    TreeMap a = new TreeMap<String, String>(langObj2);

	    System.out.println(StrJSonFine.convert(JSONValue.toJSONString(a)));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	public void openLangFile2() 
	{
		JSONObject langObj = Lang.openLangFile("ru.json");
		JSONObject langObj2 = new JSONObject(); 
		JSONObject langObj3 = Lang.openLangFile("zh.json");
		JSONObject langObj4 = new JSONObject(); 
		
		
	    Iterator iterator = langObj.entrySet().iterator();
	    while(iterator.hasNext()) {
	    	Map.Entry pair = (Entry) iterator.next();
	    	
	    	if(!langObj3.containsKey(pair.getKey()))
	    	{
	    		langObj2.put(pair.getKey(), "");
	    	}
	    }
	    
	    TreeMap a = new TreeMap<String, String>(langObj2);
	    
	    System.out.println("=========ADDED==========");
	    System.out.println(StrJSonFine.convert(JSONValue.toJSONString(a)));
	    
	    
	    Iterator iterator2 = langObj3.entrySet().iterator();
	    while(iterator2.hasNext()) {
	    	Map.Entry pair = (Entry) iterator2.next();
	    	
	    	if(!langObj.containsKey(pair.getKey()))
	    	{
	    		langObj4.put(pair.getKey(), "");
	    	}
	    }
	    
	    TreeMap b = new TreeMap<String, String>(langObj4);

	    System.out.println("=========REMOVED==========");
	    System.out.println(StrJSonFine.convert(JSONValue.toJSONString(b)));
	}

	
	@Test
	public void replaceTest() 
	{
		String res = "123 123 123";
		res = res.replace("123", "12");
		System.out.println(res);
	}		
	
	@Test
	public void iterablesTest() 
	{
		final List<Integer> first  = Lists.newArrayList(1, 2, 3);
		final List<Integer> second = Lists.newArrayList(4, 5, 9);
		final List<Integer> third  = Lists.newArrayList(7, 8, 9);
		final Iterable<Integer> all =
		    Iterables.unmodifiableIterable(
		        Iterables.concat(first, second, third));
		System.out.println(all);
		third.add(9999999);
		System.out.println(all);
	}
	
	@Test
	public void getRecipient()
	{
		//Block block = DBSet.getInstance().getBlockMap().get(Base58.decode("Fae4sXUUHP1BTvucgU6VWejvyMgSXSZLeT1SYnVY3UNgd6Qao5mQvCd6bFUQTmEas449s8RHk9KdcEp26Yxj1JqE1j1k11PbSWDBjefAsUitprh3Tqk3uCaRYRvW4rxfoevcQknzSmiu9GvHkeu7UttMokT5Mf5R29cBsHK3Pk688QH"));
		
		//Block block = DBSet.getInstance().getBlockMap().get(Base58.decode("BwR3MRV4H4bkE2i5pNGzoUwUeipWvezTByrnwKvbRKwDHDhG7XYT3XWPy1WtA7mUJ2YbDpZ3kcTjiiYtTu6iGUmYzMZQVWDgyVn38Dqv1hmKsGXxCAwTEtQnEXT2mY45gyy3tmra7xa6dfv8PSVRe3Z3s4TbGwA2SBvxgtiDBD8f796"));

		Block block = DBSet.getInstance().getBlockMap().get(Base58.decode("HD5rjfvFrBzpGz2ZuXs9wwX8EnmxnM4yPc5sbW1fpKogYPx9tDXVFos3Bc3FABQgZ9P9MYBRDarcWi4NZkhpnGevSptrNEAE5vaLDmM7XzZpB81sJjo4FwWscPpdpU2TL6ofydXrQqpntKUbzmKiaaPLS7Exq1Y3Uk1Fj59q3RM69BZ"));
		
		//
		do {
			System.out.println("block: " + block.getHeight());

			for ( Transaction transaction : block.getTransactions())
			{
				System.out.println("tx: " + Base58.encode(transaction.getSignature()));
				
				List<String> recps = new ArrayList<String>();

				int creatorInvolvedCount = 0;
				System.out.print("getInvolvedAccounts: ");

				for ( Account acc : transaction.getInvolvedAccounts())
				{
					System.out.print( acc.getAddress().toString() + " ");
				}
				System.out.println();
				
				for ( Account acc : transaction.getInvolvedAccounts())
				{
					System.out.println("getAmount: " + acc.getAddress() + " " + transaction.getAmount(acc).toPlainString());
					
					if ( acc.getAddress().equals(transaction.getCreator().getAddress()))
					{
						creatorInvolvedCount ++;
						if(creatorInvolvedCount > 1 && transaction.getType() != Transaction.UPDATE_NAME_TRANSACTION && transaction.getType() != Transaction.REGISTER_NAME_TRANSACTION) {
							System.out.println("itself! ");
						}
						
					}
					
					if ( creatorInvolvedCount < 2 && transaction.getAmount(acc).compareTo( BigDecimal.ZERO) < 0 )
					{
						continue;
					}
					recps.add(acc.getAddress());
				}
				System.out.println("sender: " + transaction.getCreator().getAddress());

				System.out.println("recps: " + recps.toString());
				
			}
			
			block = block.getChild();
		//} while (!Base58.encode(block.getSignature()).equals("ENh5ScVnifkqam3rVtiSaVx8w1wejAKQMHqEeRgZFGGWLXshHHbV85XehnKpNsa6kKYZipg9b3FvHS7TBN8hXcXDpcmzvifq9tZUtrzmEi56PY3hY78kQa2cY4DhbMwzPgP9bBz2iwdWh5EPcXepKYWngrDAWhBkwaAp2VFCf2YSvPa"));
			 
		} while (block != null);
	}
	
	@Test
	public void paymentTransaction() 
	{
		// http://qora.co.in/light/payment.html
		// AccountSeed 3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD
		// Reference   YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F
		// recipient   QTz6fSV2VNc2wjwwsw57kwQzgQhmGw5idQ
		// timestamp   1455849866776
		// amount      100.00000000
		// fee         1.00000000 
		 
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE KNOWN ACCOUNT
		byte[] AccountSeed = Base58.decode("3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD");
		PrivateKeyAccount sender = new PrivateKeyAccount(AccountSeed);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		//Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet);

		databaseSet.getReferenceMap().set(sender, Base58.decode("YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F"));
		
		//CREATE SIGNATURE
		Account recipient = new Account("QTz6fSV2VNc2wjwwsw57kwQzgQhmGw5idQ");
		
		long timestamp = 1455849866776L;
		//long timestamp = NTP.getTime();
		byte[] signature = PaymentTransaction.generateSignature(databaseSet, sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp);
		
		System.out.println("signature: " + Base58.encode(signature));
		
		//CREATE PAYMENT
		Transaction payment = new PaymentTransaction(sender, recipient, BigDecimal.valueOf(100).setScale(8), BigDecimal.valueOf(1).setScale(8), timestamp, databaseSet.getReferenceMap().get(sender), signature);

		System.out.println("toBytes: " + Base58.encode(payment.toBytes()));
		
		assertEquals(Base58.encode(payment.toBytes()), "111C87H8Ud2M9kriW66fcZ6KVDx88n5eHQW4tMEaUmzoLibTm8QVB2fRJx88xctEhHg9g5vfJX396HBjc5TNG64Ewz8MRcyjnRvg5RpSnomHpcX2n8HgRUTNqq18MtEkceSMqWbATtH9uu5pBnMzdu4MjFf87he2smq39RVtwMJnuw4qgaVwNYCwSxZrRgsT6RGpGkBHZPzoYYh8qXffmLzHvawYJqp4xN41uMPpe7J4xxbPdN2av4HKjCNtB82sygUbbt6QDBW5wMFcJyGYMn3mJVmhsFonM");
		
	}

	@Test
	public void arbitraryTransactionV3() 
	{
		// http://qora.co.in/light/payment.html
		// AccountSeed 3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD
		// Reference   YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F
		// timestamp   1455849866776
		// service     555
		// fee         1.00000000 
		//
		 
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE KNOWN ACCOUNT
		byte[] AccountSeed = Base58.decode("3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD");
		PrivateKeyAccount creator = new PrivateKeyAccount(AccountSeed);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		//Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet);

		databaseSet.getReferenceMap().set(creator, Base58.decode("YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F"));
		
		//CREATE SIGNATURE
		//Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		
		long timestamp = 1455849866776L;
		//long timestamp = NTP.getTime();
		
		byte[] arbitraryData = "{'postaфыва':'test'}".getBytes(StandardCharsets.UTF_8);

		BigDecimal fee = BigDecimal.valueOf(1).setScale(8);
		
		int service = 555;
		
		byte[] signature = ArbitraryTransactionV3.generateSignature(databaseSet, creator, null, service, arbitraryData, fee, timestamp);
		
		
		 
		System.out.println("signaturebl: " + Base58.decode("3YrKpErpfZhr5MPeEoHEEFJJXHdf9nqFUMPn3acgssufkGvgwejTsVecxw7FTKYfatjBYqxJ6xY3SsHN3xsAew5mR1X68ryeJE9dRxvUtLkMt9XpFUUgRD76N5KhbQknxy2PL755hsF71Dsw36rRxWzXiSnBF4SqjCA3A2bHLvLRKMn").length);
		System.out.println("signaturetx: " + signature.length);
		System.out.println("signature: " + Base58.encode(signature));
		
		//CREATE PAYMENT
		Transaction arbitraryTransactionV3 = new ArbitraryTransactionV3(creator, null, service, arbitraryData, fee, timestamp, databaseSet.getReferenceMap().get(creator), signature);

		System.out.println("toBytes: " + Base58.encode(arbitraryTransactionV3.toBytes()));
		
		assertEquals(Base58.encode(arbitraryTransactionV3.toBytes()), "1112RT1bWVqwdNEDfvWoyyhpQeyvBfVZGY9gZCzDdNoR9j2MXjfxnW9U8xrif9QWySYAL5nJwZA1M3d4afD677sC8c21ZpaVHzDzE4NrX4vJ9DVDSWhYsRsEGXjWLrLLBbAWko24hsUFo6Mpj6KWFJpbQNaw6K2sXCthwJvu5wfGZYY5CKxzawJFuWGLrcZ9rUfAAJwSgvHoZCTfA6HsatNwfKfbkUZa173WyizEiba3edq82nwKripWRszN7YCzLhCKmjU1XQCGCHkcjA1f9F4wVzYc8ohoQc7DZV");
	}

	@Test
	public void registerNameTransaction() 
	{
		// http://qora.co.in/light/payment.html
		// AccountSeed 3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD
		// Reference   YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F
		// timestamp   1455849866776
		// fee         1.00000000 
		// Name(new Account("QTz6fSV2VNc2wjwwsw57kwQzgQhmGw5idQ"), "проверкаимени", "Проверка значения")
		 
		//CREATE EMPTY MEMORY DATABASE
		DBSet databaseSet = DBSet.createEmptyDatabaseSet();
		
		//CREATE KNOWN ACCOUNT
		byte[] AccountSeed = Base58.decode("3dnubGUuxK5oLhURK4WLtFL7rWgSKCj5MJc48AKD6FdD");
		PrivateKeyAccount creator = new PrivateKeyAccount(AccountSeed);
		
		//PROCESS GENESIS TRANSACTION TO MAKE SURE SENDER HAS FUNDS
		//Transaction transaction = new GenesisTransaction(sender, BigDecimal.valueOf(1000).setScale(8), NTP.getTime());
		//transaction.process(databaseSet);

		databaseSet.getReferenceMap().set(creator, Base58.decode("YWv9Gyi2xxEyEe6ztrGGuAPhmUD86s7h8CANQAcmsxdeS3pU5BvQKnbeyXjnXXd8HgLaDvYBBz6im3dDYTR817F"));
		
		//CREATE SIGNATURE
		//Account recipient = new Account("XUi2oga2pnGNcZ9es6pBqxydtRZKWdkL2g");
		
		long timestamp = 1455849866776L;
		//long timestamp = NTP.getTime();
		
		Name name = new Name(new Account(creator.getAddress()), "проверкаимени", "Проверка значения");  
		
		BigDecimal fee = BigDecimal.valueOf(1).setScale(8);
		
		byte[] signature = RegisterNameTransaction.generateSignature(databaseSet, creator, name, fee, timestamp);
		
		System.out.println("signature: " + Base58.encode(signature));
		
		//CREATE PAYMENT
		Transaction registerNameTransaction = new RegisterNameTransaction(creator, name, fee, timestamp, databaseSet.getReferenceMap().get(creator), signature);

		System.out.println("toBytes: " + Base58.encode(registerNameTransaction.toBytes()));
		
		assertEquals(Base58.encode(registerNameTransaction.toBytes()), "1113xUrqp9fbHjfR7GTP92AsiFVjaGFRL86rsCKzjDdfEEu5bDcvo6zwcUyBXDmHnkbJDVWz59cDvuBPiTGf9sxdEWMdUooaqT887VnmZoNKQb2AMr5G6YNkP2oiph76kaVRFucrsz1xg1DLbaj6azacCTQqdbwzPZK23rpMR6ETeyD7UFRJo7SyhKSE8Rwmzdx6M1E8BYd1Dy8A2GwVF9AyATrtD7EbWR814qhyiCBs8LFK6UXb2D9nKhYixqNMm5r4rRzUtMMhnH2LhcKrWFjYmJgCmgzqLCKGhY7odRdaAeG23qr6ekpaRZRHAPryb1dbYfFy4a7xJYAvLAkCtRUmydK4QMcFn1b5LNvaenC3Y2skD8");
	}

	@Test
	public void address1() 
	{	
		System.out.println( Base58.clean("QSBhY5GnYnVFzRqEYLhPGKQ9JnngZVmLxJ"));
		System.out.println( Base58.clean("QSBhY5GnYnVFzRqEYLhPGKQ9JnngZVmLxJ0"));
		System.out.println( Base58.isExtraSymbols("QSBhY5GnYnVFzRqEYLhPGKQ9JnngZVmLxJ0"));
		System.out.println( Base58.isExtraSymbols("QSBhY5GnYnVFzRqEYLhPGKQ9JnngZVmLxJ"));
		System.out.println( Base58.clean("QSBhY5GnYnVFzRqEYsdgfывапLhPGKQ9JnngZVmLxJ0"));

		System.out.println("C:\\Users\\baby\\AppData\\Roaming\\Qora\\settings.json".replace("\\", "/"));
	}
	
	@Test
	public void address() 
	{
		
		BigDecimal amount = new BigDecimal("0.00000001"); 
		byte[] amountBytes = amount.unscaledValue().toByteArray();
		byte[] fill = new byte[8 - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		
		amount = new BigDecimal("10.00000000"); 
		amountBytes = amount.unscaledValue().toByteArray();
		fill = new byte[8 - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		
		amount = new BigDecimal("100012.00000000"); 
		amountBytes = amount.unscaledValue().toByteArray();
		fill = new byte[8 - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		
		amount = new BigDecimal("100012.00000123"); 
		amountBytes = amount.unscaledValue().toByteArray();
		fill = new byte[8 - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		
		
		byte[] a = new byte[]{-43, 68, 59, 54};
		
		int b = Ints.fromByteArray(a);
		System.out.println(b);
		System.out.println(Crypto.getInstance().getAddress(Base58.decode("6Voasmk9ZzYc4tMZnh5SEsiPdnQKPHMiDZSpethNUpNj")));

	}
	
	@Test
	public void testLinkedHashMap() throws UnknownHostException 
	{
		Map<Peer, Integer> peerHeight;
		peerHeight = new LinkedHashMap<Peer, Integer>();
		
		Peer peer1 = new Peer(InetAddress.getByName("127.7.6.5"));
		Peer peer2 = new Peer(InetAddress.getByName("127.3.4.5"));
		
		peerHeight.put(peer2, 1);
		
		System.out.println(peerHeight.toString());

		peerHeight.remove(peer2);
		peerHeight.remove(peer1);
		
		System.out.println("peer1 "+peerHeight.get(peer1));
		
		System.out.println(peerHeight.toString());
	}
	
	@Test
	public void testBigDecimal() {
		
		System.out.println( BigDecimal.ONE.compareTo(BigDecimal.ZERO));
		System.out.println( BigDecimal.ZERO.compareTo(BigDecimal.ONE));
		System.out.println( BigDecimal.ONE.compareTo(BigDecimal.ONE));
		
	}

	@Test
	public void testLong() {
		assertEquals(0l, 0L);
		assertEquals(1243124l, 1243124L);
		// L differs more from unity
	}

	
	@Test
	public void stripTrailingZerosTest() {

        // create 4 BigDecimal objects
        BigDecimal bg1, bg2, bg3, bg4, bg5, bg6;

        bg1 = new BigDecimal("235.000");
        bg2 = new BigDecimal("23500");
        bg3 = new BigDecimal("235.010");
        
        // assign the result of stripTrailingZeros method to bg3, bg4
        bg4 = bg1.stripTrailingZeros();
        bg5 = bg2.stripTrailingZeros();
        bg6 = bg3.stripTrailingZeros();

        
        String str1 = bg1 + " after removing trailing zeros " +bg4.toPlainString();
        String str2 = bg2 + " after removing trailing zeros " +bg5.toPlainString();
        String str3 = bg3 + " after removing trailing zeros " +bg6.toPlainString();

        // print bg3, bg4 values
        System.out.println( str1 );
        System.out.println( str2 );
        System.out.println( str3 );
    }
	 
	@Test
	public void testSign() {
		//address: QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8
		//wallet seed: AsF8sY23poJZro7to4ifXQyMzJQsVGFdDgkQd1uihnrg
		//address seed: ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j
		
		String text = "Test message. Rus:Тестовое сообщение.";
		
		String signerSeed = "ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j";
		byte[] signerSeedByte = Base58.decode(signerSeed);
	
		Pair<byte[], byte[]> signerKeyPair = Crypto.getInstance().createKeyPair(signerSeedByte);
		
		byte[] signerPublicKey = signerKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(signerPublicKey), "QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8");
		
		PrivateKeyAccount account = new PrivateKeyAccount(signerSeedByte); 
		
		byte[] textByte = text.getBytes(StandardCharsets.UTF_8); 
		byte[] signatureByte = Crypto.getInstance().sign(account, textByte);
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, textByte), true);
		
		String wrongText = text + " wrong"; 
		byte[] wrongTextByte = wrongText.getBytes(StandardCharsets.UTF_8);
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, wrongTextByte), false);
		
		byte[] wrongSignatureByte = new byte[signatureByte.length];
		System.arraycopy(signatureByte, 0, wrongSignatureByte, 0, signatureByte.length);
		wrongSignatureByte[0] = (byte) (wrongSignatureByte[0] + 1); 
		assertEquals(Crypto.getInstance().verify(signerPublicKey, wrongSignatureByte, textByte), false);

		byte[] wrongSignerPublicKey = new byte[signerPublicKey.length];
		System.arraycopy(signerPublicKey, 0, wrongSignerPublicKey, 0, signerPublicKey.length);
		wrongSignerPublicKey[0] = (byte) (wrongSignerPublicKey[0] + 1); 
		assertEquals(Crypto.getInstance().verify(wrongSignerPublicKey, signatureByte, textByte), false);
		
		assertEquals(Crypto.getInstance().verify(signerPublicKey, signatureByte, textByte), true);
	}
	
	@Test
	public void testMessages() {
		
		//address: QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8
		//wallet seed: AsF8sY23poJZro7to4ifXQyMzJQsVGFdDgkQd1uihnrg
		//address seed: ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j
		String senderSeed = "ETWEM8bdV2DQxjaS8p9qn9Q5556htaLXoZPc6Hz4Qo3j";
		
		byte[] senderSeedByte = Base58.decode(senderSeed);
		
		assertEquals(Base58.encode(senderSeedByte), senderSeed);
		
		Pair<byte[], byte[]> senderKeyPair = Crypto.getInstance().createKeyPair(senderSeedByte);
		
		byte[] senderPrivateKey = senderKeyPair.getA();
		byte[] senderPublicKey = senderKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(senderPublicKey), "QQQQD8UkkJPnW3yRZqJAUH9Pi9BzVKJCv8");
		
		//address: QQQdEJ9xYHkBru1tCg2V7m2jPiHHcrJT4r
		//wallet seed: 7Zn2MtvqF8kYNwTwzgyWz9C7rSM5CUVm2MfcoTiT67ii
		//address seed: BExrh6dUDNSG1dLyVChTxBY4pfA3NPAqLkEMtbMdgX6V
		String recipientSeed = "BExrh6dUDNSG1dLyVChTxBY4pfA3NPAqLkEMtbMdgX6V";
		
		byte[] recipientSeedByte = Base58.decode(recipientSeed);
		
		Pair<byte[], byte[]> recipientKeyPair = Crypto.getInstance().createKeyPair(recipientSeedByte);
		
		byte[] recipientPrivateKey = recipientKeyPair.getA();
		byte[] recipientPublicKey = recipientKeyPair.getB();
		
		assertEquals(Crypto.getInstance().getAddress(recipientPublicKey), "QQQdEJ9xYHkBru1tCg2V7m2jPiHHcrJT4r");
		
		String StartMessage = "Test message. Rus:Тестовое сообщение.";
		
		byte[] messageBytes;
		
		messageBytes = StartMessage.getBytes( Charset.forName("UTF-8") );
		
		messageBytes = AEScrypto.dataEncrypt(messageBytes, senderPrivateKey, recipientPublicKey);
		
		try {
			messageBytes = AEScrypto.dataDecrypt(messageBytes, recipientPrivateKey, senderPublicKey);
		} catch (InvalidCipherTextException e) {
			e.printStackTrace();
		}
		
		String EndMessage = new String( messageBytes, Charset.forName("UTF-8") );
		
		assertEquals(EndMessage, StartMessage);
		
	}

	@Test
	public void testBase_58_64() {
		String source = "skerberus\nvbcs\n" + "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8";
		
		String base58 = Base58.encode(source.getBytes(StandardCharsets.UTF_8));
		String result = new String(Base58.decode(base58), StandardCharsets.UTF_8);
		assertEquals(source, result);
		
		String base64 = Base64.encode(source.getBytes(StandardCharsets.UTF_8));
		result = new String(Base64.decode(base64), StandardCharsets.UTF_8);
		assertEquals(source, result);
	}
}
