package utils;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.Test;

import controller.Controller;
import gui.Gui;
import gui.transaction.MessageTransactionDetailsFrame;
import gui.transaction.TransferAssetDetailsFrame;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.crypto.Base58;
import qora.crypto.Base64;
import qora.crypto.Crypto;
import qora.transaction.MessageTransaction;

public class txWindowTest {

	@Test
	public void testLong() throws Exception {
		
		new MessageTransactionDetailsFrame(
				(MessageTransaction) Controller.getInstance().getTransaction(Base58.decode("2kGG3Nmu2VNatZ8MAL1PF5r3VUZyY5FsbPve9G2zJ1UL1x3NHDU96VFWn2cXvqHnvdjvY2jt3kuGTkgabr2JQXAx"))
			);
		
		// L differs more from unity
	}
	
}
