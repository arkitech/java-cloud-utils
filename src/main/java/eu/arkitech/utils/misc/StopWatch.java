package eu.arkitech.utils.misc;

/**
 * Utility class to ease code execution time measurement.
 * 
 * Example usage:
 * StopWatch stopWatch = StopWatch.createAndStart();
 * // some code that you want to measure its execution time
 * System.out.println("execution time: " + stopWatch.stop().getTotalTimeInMillis());
 * 
 * @author rcugut
 */
public class StopWatch
{
	private long startTime = -1;
	private long stopTime = -1;
	
	
	public StopWatch()
	{
	}
	
	
	public StopWatch start()
	{
		this.startTime = System.currentTimeMillis();
		return this;
	}
	

	public StopWatch stop()
	{
		this.stopTime = System.currentTimeMillis();
		return this;
	}
	
	
	public long currentTimeInMillis()
	{
		return System.currentTimeMillis() - this.startTime;
	}
	
	public long currentTimeInSeconds()
	{
		return this.currentTimeInMillis() / 1000;
	}

	
	
	public long getTotalTimeInMillis()
	{
		return this.stopTime - this.startTime;
	}
	public long getTotalTimeInSeconds()
	{
		return this.getTotalTimeInMillis() / 1000;
	}

	
	
	public static StopWatch createAndStart()
	{
		return new StopWatch().start();
	}
}
