package qora.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

public class AEScrypto {
	
	private static byte[] ivconst = new byte[]{6,4,3,8,1,2,1,2,7,2,3,8,5,7,1,1};

	public static byte[] dataEncrypt(byte[] data, byte[] myPrivateKey, byte[] theirPublicKey)
	{
		byte[] encryptdata = null;
		
		byte[] SharedSecret = Ed25519.getSharedSecret(theirPublicKey, myPrivateKey);
		byte[] password = Crypto.getInstance().digest(SharedSecret);  // SHA-256
		
		try {
			encryptdata = aesEncrypt(data, password, ivconst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] encryptdataandver = new byte[encryptdata.length + 1];
		System.arraycopy(encryptdata, 0, encryptdataandver, 1, encryptdata.length);
		encryptdataandver[0] = 1; //version crypto algo
		
		return encryptdataandver;
	}
	
	public static byte[] dataDecrypt(byte[] encryptdata, byte[] myPrivateKey, byte[] theirPublicKey) throws InvalidCipherTextException
	{
		byte[] decryptdata = null;
		
		byte[] SharedSecret = Ed25519.getSharedSecret(theirPublicKey, myPrivateKey);
		byte[] password = Crypto.getInstance().digest(SharedSecret);  // SHA-256
		
		//byte version = encryptdata[0];
		byte[] encryptdata2 = new byte[encryptdata.length - 1];
		System.arraycopy(encryptdata, 1, encryptdata2, 0, encryptdata.length-1);
		
		try {
			decryptdata = aesDecrypt(encryptdata2, password, ivconst);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		
		return decryptdata;
	}

	public static byte[] cipherData(PaddedBufferedBlockCipher cipher, byte[] data)
		throws Exception 
	{
		int minSize = cipher.getOutputSize(data.length);
		
		byte[] outBuf = new byte[minSize];
		
		int length1 = cipher.processBytes(data, 0, data.length, outBuf, 0);
		
		int length2 = cipher.doFinal(outBuf, length1);
		
		int actualLength = length1 + length2;
		
		byte[] result = new byte[actualLength];
		
		System.arraycopy(outBuf, 0, result, 0, result.length);
		
		return result;
	}

	public static byte[] aesDecrypt(byte[] cipher, byte[] key, byte[] iv)
			throws Exception 
	{

		PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
        new CBCBlockCipher(new AESEngine()));

		CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);

		aes.init(false, ivAndKey);

		return cipherData(aes, cipher);
	}

	public static byte[] aesEncrypt(byte[] plain, byte[] key, byte[] iv) throws Exception 
	{
		PaddedBufferedBlockCipher aes = new PaddedBufferedBlockCipher(
				new CBCBlockCipher(
				new AESEngine()));
		CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(key), iv);

		aes.init(true, ivAndKey);

		return cipherData(aes, plain);
	}
}