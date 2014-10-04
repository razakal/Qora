package qora.account;

import qora.crypto.Crypto;
import utils.Pair;

public class PrivateKeyAccount extends PublicKeyAccount {

	private byte[] seed;
	private Pair<byte[], byte[]> keyPair;
	
	public PrivateKeyAccount(byte[] seed)
	{
		this.seed = seed;
		this.keyPair = Crypto.getInstance().createKeyPair(seed);
		this.publicKey = keyPair.getB();
		this.address = Crypto.getInstance().getAddress(this.publicKey);
	}
	
	public byte[] getSeed()
	{
		return this.seed;
	}
	
	public byte[] getPrivateKey() 
	{
		return this.keyPair.getA();
	}
	
	public Pair<byte[], byte[]> getKeyPair()
	{
		return this.keyPair;
	}
	
}
