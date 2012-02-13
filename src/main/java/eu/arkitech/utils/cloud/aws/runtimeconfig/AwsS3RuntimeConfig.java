package eu.arkitech.utils.cloud.aws.runtimeconfig;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import eu.arkitech.utils.cloud.aws.AwsClientFactory;
import eu.arkitech.utils.runtimeconfig.RuntimeConfig;


/**
 * DO NOT USE YET!
 * still in development
 * 
 * 
 * @author rcugut
 */
public class AwsS3RuntimeConfig
{
	private static final String S3_BUCKET = null;

	private final Logger logger = LoggerFactory.getLogger(AwsS3RuntimeConfig.class);
	
	private final AmazonS3 awsS3Client;
	private final Map<String, Properties> moduleProperties = new ConcurrentHashMap<String, Properties>();
	private final String environment;

	
	public AwsS3RuntimeConfig(AwsClientFactory awsClientManager)
	{
		this.awsS3Client = awsClientManager.getAwsS3Client();
		this.environment = RuntimeConfig.getEnvironment();
		logger.info(this.getClass().getSimpleName() + " created for env ["+ this.environment +"]");
	}
	
	public String getEnvironment()
	{
		return this.environment;
	}
	
	
	public String getParam(String paramName, String module)
	{
		return this.getParam(paramName, module, String.class);
	}



	public String getParam(String paramName, String module, String defaultValue)
	{
		return this.getParam(paramName, module, String.class, defaultValue);
	}

	
	public <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls, T defaultValue)
	{
		final T result = this.getParam(paramName, module, cls);
		if (result == null)
		{
			logger.warn("Null parameter [" + paramName + "]:[" + module + "]:[" + environment + "]; falling to default value: " + defaultValue);
			return defaultValue;
		}
		return result;
	}
	
	
	public <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls)
	{
		try
		{
			final Properties p = this.getOrTryLoadModuleProperties(module);

			final String s = p.getProperty(paramName);
			if (s == null)
			{
				return null;
			}
			else
			{
				if (String.class.isAssignableFrom(cls))
				{
					return cls.cast(s);
				}
				else if (Integer.class.isAssignableFrom(cls))
				{
					return cls.cast(Integer.parseInt(s));
				}
				else if (Long.class.isAssignableFrom(cls))
				{
					return cls.cast(Long.parseLong(s));
				}
				else if (Float.class.isAssignableFrom(cls))
				{
					return cls.cast(Float.parseFloat(s));
				}
				else if (Double.class.isAssignableFrom(cls))
				{
					return cls.cast(Double.parseDouble(s));
				}
				else
				{
					throw new ClassCastException("Can't cast property to class of type " + cls.getCanonicalName());
				}
			}
		}
		catch (Exception e)
		{
			logger.warn("Exception for parameter [" + paramName + "]:[" + module + "]:[" + environment + "] ('" + e.getMessage() + "')", e);
			return null;
		}

	}
	
	
	

	private Properties getOrTryLoadModuleProperties(String module) throws IOException
	{
		Properties p;
		synchronized (moduleProperties)
		{
			p = moduleProperties.get(module);
			if (p == null)
			{
				p = new Properties();

				final String s3filepath = "envconfig/" + this.environment + "/" + module + ".properties";
				
				
				logger.debug("Loading properties for module [" + module + "]:[" + environment + "] from s3://" + S3_BUCKET + "/" + s3filepath);
				final S3Object s3obj = this.awsS3Client.getObject(S3_BUCKET, s3filepath);

				p.load(s3obj.getObjectContent());
				
				s3obj.getObjectContent().close();//IMPORTANT

				moduleProperties.put(module, p);

				logger.info("Loaded properties for module [" + module + "]:[" + environment + "]");
			}
		}
		return p;
	}

	
}
