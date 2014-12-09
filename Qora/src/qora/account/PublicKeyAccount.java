package qora.account;

import qora.crypto.Crypto;

public class PublicKeyAccount extends Account {

	protected byte[] publicKey;
	
	public PublicKeyAccount(byte[] publicKey)
	{
		this.publicKey = publicKey;
		this.address = Crypto.getInstance().getAddress(this.publicKey);
	}
	
	protected PublicKeyAccount()
	{

	}
	
	public byte[] getPublicKey() 
	{
		return publicKey;
	}
	
}
