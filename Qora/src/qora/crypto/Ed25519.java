package qora.crypto;

import java.io.File;

import utils.Pair;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;

public class Ed25519 {
		
	public interface ed25519 extends Library {
	   public void ed25519_key_exchange(byte[] shared_secret, byte[] public_key, byte[] private_key);
	   public void ed25519_sign(byte[] signature, byte[] message, int messageLength, byte[] publicKey, byte[] privateKey);
	   public int ed25519_verify(byte[] signature, byte[] message, int message_len, byte[] public_key);
	   public void ed25519_create_keypair(byte[] public_key, byte[] private_key, byte[] seed);
	}
	   
	private static ed25519 lib;
	   
	public static boolean load() 
	{
		try
		{
			String arch = System.getProperty("os.arch").toLowerCase();
			String os = System.getProperty("os.name").toLowerCase();
			
			String libname = null; 
					
			//WINDOWS
			if(os.contains("windows"))
			{
				//32BIT
				if(arch.contains("x86") || arch.contains("i386"))
				{
					libname = "ed25519.windows.32.dll";
				}
				//64BIT
				else
				{
					libname = "ed25519.windows.64.dll";
				}
			}
				
			//LINUX
			if(os.contains("linux"))
			{
				//ARM
				if(arch.startsWith("arm"))
				{
					libname = "ed25519.linux.arm7.so";
				}
				//32BIT
				else if(arch.contains("x86") || arch.contains("i386"))
				{
					libname = "ed25519.linux.32.so";
				}
				//64BIT
				else
				{
					libname = "ed25519.linux.64.so";
				}
			}
				
			//OSX
			if(os.contains("mac"))
			{
				libname = "ed25519.mac";
			}
			
			NativeLibrary.addSearchPath(libname, new File("").getAbsolutePath() + "\\libs\\native\\");
			NativeLibrary.addSearchPath(libname, new File("").getAbsolutePath() + "/libs/native/");
			NativeLibrary.addSearchPath(libname, "\\libs\\native\\");
			NativeLibrary.addSearchPath(libname, "/libs/native/");
			
			lib = (ed25519) Native.loadLibrary(libname, ed25519.class);
		}
		catch(Exception e)
		{
				return false;
		}
			
		return true;
	}

	public static byte[] getSharedSecret(byte[] public_key, byte[] private_key)
	{
		byte[] shared_secret =  new byte[32];
		
		lib.ed25519_key_exchange(shared_secret, public_key, private_key);
		
		return shared_secret;
	}
	
	public static boolean verify(byte[] signature, byte[] message, byte[] publicKey)
	{
		int valid = lib.ed25519_verify(signature, message, message.length, publicKey);
		return valid == 1;
	}
	
	public static Pair<byte[], byte[]> createKeyPair(byte[] seed)
	{
		byte[] privateKey = new byte[64];
		byte[] publicKey = new byte[32];
		
		lib.ed25519_create_keypair(publicKey, privateKey, seed);
		
		return new Pair<byte[], byte[]>(privateKey, publicKey);
	}
	
	public static byte[] sign(Pair<byte[],byte[]> keyPair, byte[] message)
	{
		byte[] signature = new byte[64];
		lib.ed25519_sign(signature, message, message.length, keyPair.getB(), keyPair.getA());
		return signature;
	}
}