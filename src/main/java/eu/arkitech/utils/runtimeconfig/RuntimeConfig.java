package eu.arkitech.utils.runtimeconfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get config parameters from .properties files based on a defined environment (a la RubyOnRails)
 * 
 * Property files should be set in each module that needs them, in the 'envconfig' folder, under a subfolder named by the environment (i.e. development, staging, production)
 * Their name whould follow the pattern: [module_name].properties
 * Example:
 * somewhere in the CLASSPATH:
 *  +- envconfig/
 *     +- development/
 *        +- module1.properties
 *        \- module2.properties
 *     +- production/
 *        +- module1.properties
 *        \- module2.properties
 *        
 * 
 * The initialization needs to get the environment name in any of 3 different ways, but it tryes them in this order:
 * 1) java parameter -Dcfg.env=staging
 * 2) OS environment variable (in *nix, export CFG_ENV=staging)
 * 3) "env.properties" file - located directly in the classpath; contains a single line, like: env=development
 * 
 * NOTE: 'production' and 'development' have special meaning and activate the use of helper boolean methods 'is[Development|Production]Environment'
 * 
 * @author rcugut
 */
public final class RuntimeConfig
{
	private static final Logger logger = LoggerFactory.getLogger(RuntimeConfig.class);
	
	private static final String SEPARATOR = "/";
	
	private static final String ENV_NAME = "CFG_ENV";

	private static final String ENV_PROPERTIES_FILENAME = "env.properties";

	private static String environment = null;
	private static final Map<String, Properties> moduleProperties = new ConcurrentHashMap<String, Properties>();

	public static boolean isDevelopmentEnvironment;
	public static boolean isProductionEnvironment;
	

	public static void _initialize()
	{
		_initialize(null);
	}

	
	public static void _initialize(String _env)
	{
		if(environment != null)
		{
			logger.warn("!!! WARNING !!!  Environment is already initialized. Won't re-initialize. Your code may have a bug :-)");
			return;
		}

		String envSource = "";

		if(StringUtils.isBlank(_env))
		{
			try
			{
				environment = System.getProperty("cfg.env");
				envSource = "-Dcfg.env";

				if (StringUtils.isBlank(environment))
				{
					environment = System.getenv(ENV_NAME);
					envSource = ENV_NAME;
				}

				if (StringUtils.isBlank(environment))
				{
					final Properties envProperties = new Properties();
					envProperties.load(RuntimeConfig.class.getClassLoader().getResourceAsStream(ENV_PROPERTIES_FILENAME));
					environment = envProperties.getProperty("env");
					envSource = ENV_PROPERTIES_FILENAME;
				}

				if (StringUtils.isBlank(environment))
				{
					throw new RuntimeException();
				}

			}
			catch (Exception e)
			{
				environment = null;
				throw new RuntimeException("ERROR: Environment is not set! (either add cmd line '-Denv=<env>' or set OS var 'export CFG_ENV=<env>' or put a properties file named '" + ENV_PROPERTIES_FILENAME + "' in classpath)");
			}
		}
		else
		{
			StringWriter s = new StringWriter();  
			new Throwable().printStackTrace(new PrintWriter(s));
			
			envSource = "runtime initialize(); stacktrace: " + s.toString();
			environment = _env;
		}

		
		isDevelopmentEnvironment = "development".equalsIgnoreCase(environment);
		isProductionEnvironment = "production".equalsIgnoreCase(environment);

		logger.info(RuntimeConfig.class.getSimpleName() + " created for env [" + environment + "] from '" + envSource + "'");
	}



	/**
	 * 
	 * @return
	 */
	public static String getEnvironment()
	{
		enforceInitializedOk();
		return environment;
	}


	public static boolean isDevelopmentEnvironment()
	{
		return isDevelopmentEnvironment;
	}

	public static boolean isProductionEnvironment()
	{
		return isProductionEnvironment;
	}

	
	

	/**
	 * 
	 * @param modules
	 * @throws IOException
	 */
	public static void loadConfigForModules(String... modules) throws IOException
	{
		enforceInitializedOk();

		for (String module : modules)
		{
			getOrTryLoadModuleProperties(module);
		}
	}



	/**
	 * 
	 * @param <T>
	 * @param paramName
	 * @param module
	 * @param cls
	 * @return
	 * @throws IOException
	 */
	public static <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls)
	{
		enforceInitializedOk();
		logger.debug("getParam('{}', '{}', {})", new Object[]{ paramName, module, cls.getSimpleName() });

		try
		{
			final Properties p = getOrTryLoadModuleProperties(module);

			final String s = p.getProperty(paramName);
			if (s == null)
			{
				logger.info("getParam('{}', '{}', {}): null", new Object[]{ paramName, module, cls.getSimpleName() });
				return null;
			}
			else
			{
				logger.info("getParam('{}', '{}', {}): {}", new Object[]{ paramName, module, cls.getSimpleName(), s });
				
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
			return null;
		}

	}



	/**
	 * 
	 * @param <T>
	 * @param paramName
	 * @param module
	 * @param cls
	 * @return
	 * @throws IOException
	 */
	public static <T extends Object> T getParam(String paramName, String module, Class<? extends T> cls, T defaultValue)
	{
		final T result = getParam(paramName, module, cls);
		if (result == null)
		{
			logger.warn("Null parameter [" + environment + "]>>[" + module + "]>>[" +  paramName+ "]; default value: " + defaultValue);
			return defaultValue;
		}
		return result;
	}



	public static String getParam(String paramName, String module)
	{
		return getParam(paramName, module, String.class);
	}



	public static String getParam(String paramName, String module, String defaultValue)
	{
		return getParam(paramName, module, String.class, defaultValue);
	}



	/**
	 * 
	 * @param module
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	private static Properties getOrTryLoadModuleProperties(String module) throws IOException
	{
		enforceInitializedOk();
		
		Properties p;
		synchronized (moduleProperties)
		{
			p = moduleProperties.get(module);
			if (p == null)
			{
				p = new Properties();

				final String str = SEPARATOR + "envconfig" + SEPARATOR + environment + SEPARATOR + module + ".properties";

				logger.debug("Loading properties for module [" + environment + "]>>[" + module + "]; file: " + str);

				p.load(RuntimeConfig.class.getResourceAsStream(str));

				moduleProperties.put(module, p);

				logger.info("Loaded properties for module [" + environment + "]>>[" + module + "]");
			}
		}
		return p;
	}



	public static Properties getAllProperties(String module) throws IOException
	{
		enforceInitializedOk();

		return getOrTryLoadModuleProperties(module);
	}



	private static void enforceInitializedOk()
	{
		if(environment == null)
		{
			_initialize();
			
			if(environment == null)
			{
				throw new RuntimeException(RuntimeConfig.class.getSimpleName() + " was not successfully initialized; check back in your logs to see why.");
			}
		}
	}



	// don't allow create instance of this class
	private RuntimeConfig()
	{
	}
}
