package eu.arkitech.utils.cloud.aws;

import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;

import eu.arkitech.utils.cloud.aws.ec2.AwsEc2Helper;
import eu.arkitech.utils.cloud.aws.simpledb.AwsSdbHelper;
import eu.arkitech.utils.misc.StringUtils;
import eu.arkitech.utils.runtimeconfig.RuntimeConfig;

public class AwsClientFactory
{
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	private static AwsClientFactory instance = null;
	
	
	private final AWSCredentials credentials;
	private final Map<String, AmazonEC2> ec2ClientsPerRegionMap = new HashMap<String, AmazonEC2>();
	private AmazonSimpleDB sdbClient;
	private AmazonS3 s3Client;
	private AmazonSNS snsClient;
	private AmazonSimpleEmailService sesClient;
	
	
	private AwsEc2Helper awsEc2Helper = null;
	private AwsSdbHelper awsSdbHelper = null;
	

	
	protected AwsClientFactory()
	{
		String source = "JVM_ARGS";
		String accessKey = System.getProperty("AWS_ACCESS_KEY_ID");
		String secretKey = System.getProperty("AWS_SECRET_ACCESS_KEY");
		if(StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey))
		{
			source = "ENV";
			accessKey = System.getenv("AWS_ACCESS_KEY_ID");
			secretKey = System.getenv("AWS_SECRET_ACCESS_KEY");
		}
		if(StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey))
		{
			source = "EXTERNAL_PROPERTIES_FILE";
			try
			{
				final String awsPropsFilePath = System.getProperty("AwsCredentialsFile");
				if(StringUtils.isNotBlank(awsPropsFilePath))
				{
					final Properties awsProps = new Properties();
					awsProps.load(new FileReader(awsPropsFilePath));
					accessKey = awsProps.getProperty("AWS_ACCESS_KEY_ID", awsProps.getProperty("accessKey"));
					secretKey = awsProps.getProperty("AWS_SECRET_ACCESS_KEY", awsProps.getProperty("secretKey"));
				}
			}
			catch(Exception e)
			{
				logger.warn("Failed to load AWS credentials from EXTERNAL_PROPERTIES_FILE", e);
				accessKey = null;
			}
		}
		if(StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey))
		{
			source = "CONFIG_PARAMS";
			accessKey = RuntimeConfig.getParam("accessKey", "aws"); 
			secretKey = RuntimeConfig.getParam("secretKey", "aws"); 
		}
		final boolean blank_accessKey = StringUtils.isBlank(accessKey);
		final boolean blank_secretKey = StringUtils.isBlank(secretKey);
		if(blank_accessKey || blank_secretKey)
		{
			throw new RuntimeException("Can't find AWS credentials(accessKey="+ ! blank_accessKey +"; secretKey="+ ! blank_secretKey +")");
		}
		logger.info("AWS credentials loaded from {}", source);
		
		this.credentials = new BasicAWSCredentials(accessKey, secretKey);
	}
	
	
	
	
	public void initAllAvailableClients()
	{
		this.getAwsEc2Client(AwsRegionsEnum.US_EAST_1);
		this.getAwsEc2Client(AwsRegionsEnum.US_WEST_1);
		this.getAwsEc2Client(AwsRegionsEnum.EU_WEST_1);
		this.getAwsEc2Client(AwsRegionsEnum.AP_SOUTHEAST_1);
		this.getAwsS3Client();
		this.getAwsSimpleDbClient();
		this.getAwsSnsClient();
		this.getAwsSimpleEmailServiceClient();
	}
	
	
	
	public static final AwsClientFactory getInstance()
	{
		if(instance == null)
		{
			instance = new AwsClientFactory();
		}
		return instance;
	}
	
	
	
	
	
	public AmazonEC2 getAwsEc2Client(AwsRegionsEnum region)
	{
		AmazonEC2 client = this.ec2ClientsPerRegionMap.get(region.getAwsName());
		if(client == null)
		{
			client = new AmazonEC2Client(credentials);
			client.setEndpoint("ec2." + region.getAwsName() + ".amazonaws.com");
			ec2ClientsPerRegionMap.put(region.getAwsName(), client);
			logger.info("Created AmazonEC2 Client for region '{}'", new String[] { region.getAwsName() });
		}
		return client;
	}


	public AmazonSimpleDB getAwsSimpleDbClient()
	{
		if(this.sdbClient == null)
		{
			this.sdbClient = new AmazonSimpleDBClient(credentials);
			logger.info("Created AmazonSimpleDB Client");
		}
		return this.sdbClient;
	}


	public AmazonS3 getAwsS3Client()
	{
		if(this.s3Client == null)
		{
			this.s3Client = new AmazonS3Client(credentials);
			logger.info("Created AmazonS3 Client");
		}
		return this.s3Client;
	}

	public AmazonSNS getAwsSnsClient()
	{
		if(this.snsClient == null)
		{
			this.snsClient = new AmazonSNSClient(credentials);
			logger.info("Created AmazonSNS Client");
		}
		return this.snsClient;
	}

	public AmazonSimpleEmailService getAwsSimpleEmailServiceClient()
	{
		if(this.sesClient == null)
		{
			this.sesClient = new AmazonSimpleEmailServiceClient(credentials);
			logger.info("Created AmazonSimpleEmailService Client");
		}
		return this.sesClient;
	}

	
	public AwsEc2Helper getAwsEc2Helper()
	{
		if(this.awsEc2Helper == null)
		{
			this.awsEc2Helper = new AwsEc2Helper(this);
			logger.info("Created AwsEc2Helper");
		}
		return this.awsEc2Helper;
	}
	
	
	public AwsSdbHelper getAwsSdbHelper()
	{
		if(this.awsSdbHelper == null)
		{
			this.awsSdbHelper = new AwsSdbHelper(this);
			logger.info("Created AwsSimpleDbHelper");
		}
		return this.awsSdbHelper;
	}
}
