package qora.crypto;

import java.util.Arrays;

import utils.Pair;

public class Ed25519 {

	private native static byte[] createKeyPairN(byte[] seed);
	private native static byte[] signN(byte[] privateKey, byte[] publicKey, byte[] message, int messageLength);
	private native static int verifyN(byte[] signature, byte[] message, int messageLength, byte[] publicKey);
	
	
	public static boolean load() 
	{
		try
		{
			String arch = System.getProperty("os.arch").toLowerCase();
			String os = System.getProperty("os.name").toLowerCase();
				
			//WINDOWS
			if(os.contains("windows"))
			{
				//32BIT
				if(arch.contains("x86") || arch.contains("i386"))
				{
					System.loadLibrary("Qora25519.windows.x86");
				}
				//64BIT
				else
				{
					System.loadLibrary("Qora25519.windows.x64");
				}
			}
				
			//LINUX
			if(os.contains("linux"))
			{
				//32BIT
				if(arch.contains("x86") || arch.contains("i386"))
				{
					System.loadLibrary("Qora25519.linux.x86");
				}
				//64BIT
				else
				{
					System.loadLibrary("Qora25519.linux.x64");
				}
			}
				
			//OSX
			if(os.contains("mac"))
			{
				System.loadLibrary("Qora25519.mac");
			}
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	public static Pair<byte[], byte[]> createKeyPair(byte[] seed)
	{
		byte[] rawPair = createKeyPairN(seed);
		
		byte[] privateKey = Arrays.copyOfRange(rawPair, 0, 64);
		byte[] publicKey = Arrays.copyOfRange(rawPair, 64, 96);
		
		return new Pair<byte[], byte[]>(privateKey, publicKey);
	}
	
	public static byte[] sign(Pair<byte[],byte[]> keyPair, byte[] message)
	{
		return signN(keyPair.getA(), keyPair.getB(), message, message.length);
	}
	
	public static boolean verify(byte[] signature, byte[] message, byte[] publicKey)
	{
		int valid = verifyN(signature, message, message.length, publicKey);
		return valid == 1;
	}
}
