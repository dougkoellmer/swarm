package swarm.server.account;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.codec.binary.Base64;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * A utility class for creating secure hashes and salts.
 */
public class smU_Hashing
{
	private static final Logger s_logger = Logger.getLogger(smU_Hashing.class.getName());
	
	public static final int RADIX = 32;
	
	private static final String CHARSET					= "UTF-8";
	
	private static final SecureRandom s_random = new SecureRandom(); // thread safe

	private smU_Hashing()
	{
	}
	
	/*public static String calcRandomAlphaNumbericString(int bitCount)
	{
		BigInteger bigInt = new BigInteger(bitCount, s_random);
		return bigInt.toString(RADIX);
	}*/
	
	public static String calcRandomSaltString(int sizeInBytes)
	{
		byte[] bytes = calcRandomSaltBytes(sizeInBytes);
		return Base64.encodeBase64String(bytes);
	}

	public static byte[] calcRandomSaltBytes(int sizeInBytes)
	{
		byte[] bytes = new byte[sizeInBytes];
		s_random.nextBytes(bytes);
		return bytes;
	}
	
	public static String convertBytesToUrlSafeString(byte[] bytes)
	{
		return Base64.encodeBase64URLSafeString(bytes);
	}
	
	public static String convertBytesToHashString(byte[] bytes)
	{
		String string = Base64.encodeBase64String(bytes);
		return string;
	}
	
	public static byte[] convertHashStringToBytes(String base64HashString)
	{
		byte[] bytes = Base64.decodeBase64(base64HashString);
		return bytes;
	}
	
	public static String hashWithSalt(String text, String base64Salt)
	{
		byte[] saltBytes = convertHashStringToBytes(base64Salt);
		byte[] hashBytes = hashWithSalt(text, saltBytes);
		return convertBytesToHashString(hashBytes);
	}
	
	public static byte[] hashWithSalt(String text, byte[] salt)
	{
		byte[] textBytes;
		try
		{
			textBytes = text.getBytes(CHARSET);
		}
		catch (UnsupportedEncodingException e)
		{
			s_logger.severe("Unsupported encoding.");
			
			return null;
		}
		
		byte[] textPlusSalt = new byte[textBytes.length + salt.length];
		
		for (int i = 0; i < textPlusSalt.length; ++i)
		{
			textPlusSalt[i] = i < textBytes.length ? textBytes[i] : salt[i - textBytes.length];
		}
		
		byte[] hash = DigestUtils.sha256(textPlusSalt);
		
		return hash;
	}
}
