package eu.arkitech.utils.misc;

public class ThreadUtils
{
	public static void sleep(long milis)
	{
		try
		{
			Thread.sleep(milis);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}
	}
}
