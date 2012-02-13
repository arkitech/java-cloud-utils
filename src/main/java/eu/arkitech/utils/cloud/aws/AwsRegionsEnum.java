package eu.arkitech.utils.cloud.aws;


public enum AwsRegionsEnum
{
	US_EAST_1		("us-east-1"),
	US_WEST_1		("us-west-1"),
	US_WEST_2		("us-west-2"),
	EU_WEST_1		("eu-west-1"),
	AP_SOUTHEAST_1	("ap-southeast-1"),
	AP_NORTHEAST_1	("ap-northeast-1"),
	SA_EAST_1		("sa-east-1");

	
	
	private final String value;
	private AwsRegionsEnum(String n)
	{
		this.value = n;
	}

	public String getAwsName()
	{
		return this.value;
	}

	@Override
	public String toString()
	{
		return this.value;
	}


	public static AwsRegionsEnum fromAwsName(String name)
	{
        if (name == null || "".equals(name)) 
        {
            throw new IllegalArgumentException("Value cannot be null or empty!");
        }
		
		for (AwsRegionsEnum r : AwsRegionsEnum.values())
		{
			if (r.getAwsName().equalsIgnoreCase(name))
			{
				return r;
			}
		}

		throw new IllegalArgumentException("Cannot create AwsRegionsEnum from '" + name + "' value!");
	}

}
