package eu.arkitech.utils.misc;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * Used to obfuscate short strings (that can be used to pass semi-sensitive data in HTTP requests)
 * 
 * @author rcugut
 */
public class AesEncrypter
{
	private final Key key;
	private final Cipher cipher;



	public AesEncrypter(String _16charsKey)
	{
		this.key = new SecretKeySpec(_16charsKey.getBytes(), "AES");

		try
		{
			this.cipher = Cipher.getInstance("AES");
		}
		catch (Exception e)
		{
			throw new RuntimeException("Can't create cipher encrypt", e);
		}
	}



	public String encryptToHexString(Object obj)
	{
		try
		{
			final byte[] input = String.valueOf(obj).getBytes("UTF8");

			byte[] encryptedData;

			synchronized (cipher)
			{
				cipher.init(Cipher.ENCRYPT_MODE, key);
				encryptedData = cipher.doFinal(input);
			}

			return StringUtils.byteArrayToHex(encryptedData);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error encrypting", e);
		}
	}



	public String decryptFromHexString(String hexStr)
	{
		try
		{
			byte[] encryptedData = StringUtils.hexToByteArray(hexStr);

			byte[] data;
			synchronized (cipher)
			{
				cipher.init(Cipher.DECRYPT_MODE, key);
				data = cipher.doFinal(encryptedData);
			}

			return new String(data);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Error decrypting", e);

		}
	}
}
