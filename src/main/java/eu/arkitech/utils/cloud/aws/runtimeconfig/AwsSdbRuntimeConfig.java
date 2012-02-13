package eu.arkitech.utils.cloud.aws.runtimeconfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpledb.model.Attribute;

import eu.arkitech.utils.cloud.aws.AwsClientFactory;
import eu.arkitech.utils.cloud.aws.simpledb.AwsSdbHelper;
import eu.arkitech.utils.runtimeconfig.RuntimeConfig;

public final class AwsSdbRuntimeConfig
{
	private static final Logger logger = LoggerFactory.getLogger(AwsSdbRuntimeConfig.class);

	private static boolean initialized = false;
	
	private static AwsSdbHelper awsSimpleDbHelper;

	private static final Map<String, Properties> moduleProperties = new ConcurrentHashMap<String, Properties>();
	private static String environment = RuntimeConfig.getEnvironment();
	private static String awsSdbDomain = null;

	
	public static void _initialize()
	{
		_initialize(null, null);
	}
	
	public static void _initialize(String p_awsSdbDomain)
	{
		_initialize(null, p_awsSdbDomain);
	}
	
	public static void _initialize(String p_environment, String p_awsSdbDomain)
	{
		if( ! eu.arkitech.utils.misc.StringUtils.isBlank(p_environment))
		{
			environment = p_environment;
		}
		
		try
		{
			awsSdbDomain = p_awsSdbDomain;
			if (StringUtils.isBlank(awsSdbDomain))
			{
				awsSdbDomain = System.getProperty("cfg.sdbdomain");
			}
			
			if (StringUtils.isBlank(awsSdbDomain))
			{
				awsSdbDomain = RuntimeConfig.getParam("cfg.sdbdomain", "aws");
			}

			if (StringUtils.isBlank(awsSdbDomain))
			{
				throw new RuntimeException();
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("ERROR: sdbdomain is not set! (either add cmd line '-Dcfg.sdbdomain=<domain>' or put the property 'cfg.sdbdomain' in the aws.properties file in classpath)");
		}
		
		
		try
		{
			awsSimpleDbHelper = new AwsSdbHelper(AwsClientFactory.getInstance());
		}
		catch(Exception e)
		{
			throw new RuntimeException("ERROR: creating AwsSimpleDbHelper: " + e.getMessage(), e);
		}

		initialized = true;
		logger.info("{} created for env [{}] with sdbdomain [{}]", new Object[] { AwsSdbRuntimeConfig.class.getSimpleName(), environment, awsSdbDomain});
	}
	
	
	
	public static String getParam(String paramName, Enum<?> module, String defaultValue)
	{
		return getParam(paramName, module.toString(), defaultValue);
	}

	public static String getParam(Enum<?> attr, Enum<?> module, boolean throwEx)
	{
		return getParam(attr.name(), module.name(), String.class, throwEx);
	}
	
	public static String getParam(Enum<?> attr, Enum<?> module)
	{
		return getParam(attr.name(), module);
	}

	public static String getParam(String attr, Enum<?> module, boolean throwEx)
	{
		return getParam(attr, module.name(), String.class, throwEx);
	}

	public static String getParam(String attr, Enum<?> module)
	{
		return getParam(attr, module.name());
	}

	public static String getParam(String paramName, String module, boolean throwEx)
	{
		return getParam(paramName, module, String.class, throwEx);
	}

	public static String getParam(String paramName, String module)
	{
		return getParam(paramName, module, String.class, false);
	}

	public static String getParam(String paramName, String module, String defaultValue)
	{
		return getParam(paramName, module, String.class, defaultValue);
	}

	public static <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls, T defaultValue)
	{
		final T result = getParam(paramName, module, cls, false);
		if (result == null)
		{
			logger.warn("Null parameter [{}]>>[{}]>>[{}]; default value: {}", new Object[] { environment, module, paramName, defaultValue });
			return defaultValue;
		}
		return result;
	}
	
	

	public static <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls, boolean throwEx)
	{
		enforceInitialized();

		try
		{
			final Properties p = getOrTryLoadModuleProperties(module);

			final String s = p.getProperty(paramName);

			logger.info("getParam('{}', '{}', '{}'): {}", new Object[] { paramName, module, cls, s });

			if (s == null)
			{
				if(throwEx)
				{
					throw new NullPointerException("property ["+paramName+"]:["+module+"] not found");
				}
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
			logger.warn("Exception for parameter [" + environment + "]>>[" + module + "]>>[" + paramName + "]", e);
			if(throwEx)
			{
				throw new RuntimeException("Exception for parameter [" + environment + "]>>[" + module + "]>>[" + paramName + "]", e);
			}
			else
			{
				return null;
			}
		}
	}



	private static Properties getOrTryLoadModuleProperties(String module) throws IOException
	{
		enforceInitialized();
		
		Properties p;
		synchronized (moduleProperties)
		{
			p = moduleProperties.get(module);
			if (p == null)
			{
				p = new Properties();

				logger.debug("Loading properties for module [{}]>>[{}] from SimpleDB: " + awsSdbDomain + "_" + environment, new Object[] { environment, module });

				final List<Attribute> attrs = awsSimpleDbHelper.getAttributes(awsSdbDomain, module);

				for (Attribute attr : attrs)
				{
					p.put(attr.getName(), attr.getValue());
				}

				moduleProperties.put(module, p);

				logger.info("Loaded properties for module [{}]>>[{}]", new Object[]{ environment, module });
			}
		}
		return p;
	}
	
	
	private static void enforceInitialized()
	{
		if( ! initialized)
		{
			_initialize();
		}
	}
	
	
	// don't allow create instance of this class
	private AwsSdbRuntimeConfig()
	{
	}
}
