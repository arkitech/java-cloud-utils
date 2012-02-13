package eu.arkitech.utils.cloud.aws.ec2;

import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import eu.arkitech.utils.runtimeconfig.RuntimeConfig;

public class AwsEc2InstanceMetaDataClient
{
	private static final String API_URL = "http://169.254.169.254/latest";



	public static String getMetaData(AwsEc2InstanceMetaDataEnum metaData)
	{
		if (RuntimeConfig.isDevelopmentEnvironment)
		{
			return getMetaDataMock(metaData);
		}

		URLConnection connection = null;
		try
		{
			connection = new URL(API_URL + metaData.getApiCall()).openConnection();
			connection.setRequestProperty("method", "GET");
			connection.setUseCaches(false);
			connection.connect();
			return IOUtils.toString(connection.getInputStream());
		}
		catch (Exception e)
		{
		}

		return null;
	}



	public static String getMetaDataMock(AwsEc2InstanceMetaDataEnum metaData)
	{
		switch (metaData)
		{
			case INSTANCE_ID:
				return "i-12345678";

			case PRIVATE_IP4:
			case PUBLIC_IP4:
				return "127.0.0.1";
			
			case INSTANCE_TYPE:
				return "m1.small";
		}
		return null;
	}

	public static enum AwsEc2InstanceMetaDataEnum
	{
		USER_DATA("/user-data"),
		INSTANCE_ID("/meta-data/instance-id"),
		PRIVATE_IP4("/meta-data/local-ipv4"),
		PUBLIC_IP4("/meta-data/public-ipv4"),
		INSTANCE_TYPE("/meta-data/instance-type");

		private final String call;



		private AwsEc2InstanceMetaDataEnum(String p)
		{
			this.call = p;
		}



		public String getApiCall()
		{
			return this.call;
		}
	}

}
