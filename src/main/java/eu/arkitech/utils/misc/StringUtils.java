package eu.arkitech.utils.misc;


/**
 * Extension of  org.apache.commons.lang.StringUtils  to add [to|from]Hex methods
 * 
 * @author rcugut
 */
public class StringUtils extends org.apache.commons.lang.StringUtils
{
	private static final String HEXES = "0123456789ABCDEF";


	public static String hexToString(String hex)
	{
		return new String(hexToByteArray(hex));
	}

	
	public static byte[] hexToByteArray(String hex)
	{
		if (hex == null)
			return null;

		byte[] result = new byte[hex.length() / 2];

		int i = 0;
		for (int pos = 0; pos < hex.length(); pos += 2, i++)
		{
			byte b = 0;
			try
			{
				b = (byte) Integer.parseInt(hex.substring(pos, pos + 2), 16);

			}
			catch (NumberFormatException ex)
			{
				ex.printStackTrace();

			}
			result[i] = b;
		}

		return result;
	}
	
	
	public static String stringToHex(String str)
	{
		return byteArrayToHex(str.getBytes());
	}

	public static String byteArrayToHex(byte[] bytes)
	{
		if (bytes == null)
		{
			return null;
		}
		final StringBuilder hex = new StringBuilder(2 * bytes.length);
		for (final byte b : bytes)
		{
			hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
		}
		return hex.toString();
	}
}
