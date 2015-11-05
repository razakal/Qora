package qora.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.whispersystems.curve25519.java.*;

import utils.Pair;

public class Ed25519 {
	
	public static byte[] getSharedSecret(byte[] public_key, byte[] private_key)
	{
		byte[] shared_secret =  new byte[32];
		
	    byte[] e  = new byte[32];
	    int i;
	    
	    int[] x1 = new int[10];
	    int[] x2 = new int[10];
	    int[] z2 = new int[10];
	    int[] x3 = new int[10];
	    int[] z3 = new int[10];
	    int[] tmp0 = new int[10];
	    int[] tmp1 = new int[10];
	
	    int pos;
	    int swap;
	    int b;
	
	    /* copy the private key and make sure it's valid */
	    for (i = 0; i < 32; ++i) {
	        e[i] = private_key[i];
	    }
	
	    e[0] &= 248;
	    e[31] &= 63;
	    e[31] |= 64;
	
	    /* unpack the public key and convert edwards to montgomery */
	    /* due to CodesInChaos: montgomeryX = (edwardsY + 1)*inverse(1 - edwardsY) mod p */
	    fe_frombytes.fe_frombytes(x1, public_key);
	    fe_1.fe_1(tmp1);
	    fe_add.fe_add(tmp0, x1, tmp1);
	    fe_sub.fe_sub(tmp1, tmp1, x1);
	    fe_invert.fe_invert(tmp1, tmp1);
	    fe_mul.fe_mul(x1, tmp0, tmp1);
	
	    fe_1.fe_1(x2);
	    fe_0.fe_0(z2);
	    fe_copy.fe_copy(x3, x1);
	    fe_1.fe_1(z3);
	
	    swap = 0;
	    for (pos = 254; pos >= 0; --pos) {
	        b = e[pos / 8] >> (pos & 7);
	        b &= 1;
	        swap ^= b;
	        fe_cswap.fe_cswap(x2, x3, swap);
	        fe_cswap.fe_cswap(z2, z3, swap);
	        swap = b;
	
	        /* from montgomery.h */
	        fe_sub.fe_sub(tmp0, x3, z3);
	        fe_sub.fe_sub(tmp1, x2, z2);
	        fe_add.fe_add(x2, x2, z2);
	        fe_add.fe_add(z2, x3, z3);
	        fe_mul.fe_mul(z3, tmp0, x2);
	        fe_mul.fe_mul(z2, z2, tmp1);
	        fe_sq.fe_sq(tmp0, tmp1);
	        fe_sq.fe_sq(tmp1, x2);
	        fe_add.fe_add(x3, z3, z2);
	        fe_sub.fe_sub(z2, z3, z2);
	        fe_mul.fe_mul(x2, tmp1, tmp0);
	        fe_sub.fe_sub(tmp1, tmp1, tmp0);
	        fe_sq.fe_sq(z2, z2);
	        fe_mul121666.fe_mul121666(z3, tmp1);
	        fe_sq.fe_sq(x3, x3);
	        fe_add.fe_add(tmp0, tmp0, z3);
	        fe_mul.fe_mul(z3, x1, z2);
	        fe_mul.fe_mul(z2, tmp1, tmp0);
	    }
	
	    fe_cswap.fe_cswap(x2, x3, swap);
	    fe_cswap.fe_cswap(z2, z3, swap);
	
	    fe_invert.fe_invert(z2, z2);
	    fe_mul.fe_mul(x2, x2, z2);
	    fe_tobytes.fe_tobytes(shared_secret, x2);
	    
		return shared_secret;
	}
	
	public static boolean verify(byte[] signature, byte[] message, byte[] publicKey) throws Exception
	{
		byte[] h  = new byte[64];
		byte[] checker  = new byte[32];
	    
		ge_p3 A = new ge_p3();
	    ge_p2 R = new ge_p2();

	    if ((signature[63] & 224) != 0) { 
	    	return false;
	    }
	   
	    if (ge_frombytes.ge_frombytes_negate_vartime(A, publicKey) != 0) {
	        return false;
	    }

	    MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
	    
		sha512.update(signature, 0, 32);
		sha512.update(publicKey, 0, 32);
		sha512.update(message, 0, message.length);
		
		h = sha512.digest();
	    
	    sc_reduce.sc_reduce(h);
	    
	    byte[] sm32 = new byte[32];
        System.arraycopy(signature, 32, sm32, 0, 32);
        
	    ge_double_scalarmult.ge_double_scalarmult_vartime(R, h, A, sm32);
	    ge_tobytes.ge_tobytes(checker, R);

	    Boolean result = CryptoBytes.ConstantTimeEquals(checker, 0, signature, 0, 32);
	    
	    return result;		
	}
	
	public static byte[] sign(Pair<byte[],byte[]> keyPair, byte[] message) throws NoSuchAlgorithmException
	{
		byte[] private_key = keyPair.getA();
		byte[] public_key = keyPair.getB();
		
		byte[] signature = new byte[64];
		
		byte[] hram  = new byte[64];
	    byte[] r  = new byte[64];
	    ge_p3 R = new ge_p3();

	    MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
	    sha512.update(private_key, 32, 32);
	    sha512.update(message, 0, message.length);
	    r = sha512.digest();
		
		sc_reduce.sc_reduce(r);
		ge_scalarmult_base.ge_scalarmult_base(R, r);
		ge_p3_tobytes.ge_p3_tobytes(signature, R);

	    sha512 = MessageDigest.getInstance("SHA-512");
	    sha512.update(signature, 0, 32);
	    sha512.update(public_key, 0, 32);
		sha512.update(message, 0, message.length);
		hram = sha512.digest();
		
		sc_reduce.sc_reduce(hram);
		
		byte[] sm32 = new byte[32];
        sc_muladd.sc_muladd(sm32, hram, private_key, r);
        System.arraycopy(sm32, 0, signature, 32, 32);
        CryptoBytes.Wipe(sm32);
        
        return signature;
	}
	
	public static Pair<byte[], byte[]> createKeyPair(byte[] seed)
	{
		byte[] private_key = new byte[64];
		byte[] public_key = new byte[32];
		
		ge_p3 A = new ge_p3();

	    sha512(seed, 32, private_key);
	    
	    private_key[0] &= 248;
	    private_key[31] &= 63;
	    private_key[31] |= 64;

	    ge_scalarmult_base.ge_scalarmult_base(A, private_key);
	    ge_p3_tobytes.ge_p3_tobytes(public_key, A);	
	    
	    return new Pair<byte[], byte[]>(private_key, public_key);
	}
	
	public static void sha512(byte[] in, long length, byte[] out) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			messageDigest.update(in, 0, (int)length);
			byte[] digest = messageDigest.digest();
			System.arraycopy(digest, 0, out, 0, digest.length);
		} catch (NoSuchAlgorithmException e) {
			throw new AssertionError(e);
		}
	}
}