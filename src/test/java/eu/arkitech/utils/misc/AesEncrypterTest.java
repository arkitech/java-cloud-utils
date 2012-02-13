package eu.arkitech.utils.misc;

import org.junit.Assert;
import org.junit.Test;

public class AesEncrypterTest
{
	String SECRET_KEY = "1234567812345678";  // NEED TO BE 16 chars
	
	@Test
	public void test_1()
	{
		String mySecretText = "my secret text";
		
		AesEncrypter o1 = new AesEncrypter(SECRET_KEY);
		String encryptedText = o1.encryptToHexString(mySecretText);
		
		System.out.println("encrypted: " + encryptedText);
		
		AesEncrypter o2 = new AesEncrypter(SECRET_KEY);
		
		Assert.assertEquals(mySecretText, o2.decryptFromHexString(encryptedText));
	}
}
