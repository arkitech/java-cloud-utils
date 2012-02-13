package eu.arkitech.utils.cloud.aws.ec2;

import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;

import eu.arkitech.utils.cloud.aws.AwsClientFactory;
import eu.arkitech.utils.cloud.aws.AwsRegionsEnum;

public class AwsEc2Helper
{
	private AwsClientFactory awsClientFactory;



	public AwsEc2Helper(AwsClientFactory awsClientFactory)
	{
		this.awsClientFactory = awsClientFactory;
	}



	public String getPublicIpAddress(AwsRegionsEnum region, String instanceId)
	{
		final DescribeInstancesResult res_di = this.awsClientFactory.getAwsEc2Client(region).describeInstances(new DescribeInstancesRequest().withInstanceIds(instanceId));

		try
		{
			return res_di.getReservations().get(0).getInstances().get(0).getPublicIpAddress();
		}
		catch (Exception e)
		{
			return null;
		}
	}



	public void createTags(AwsRegionsEnum region, String instanceId, Tag... tags)
	{
		this.awsClientFactory.getAwsEc2Client(region).createTags(new CreateTagsRequest().withResources(instanceId).withTags(tags));
	}



	public List<Instance> getEc2InstancesByTags(AwsRegionsEnum region, Tag... tags)
	{
		final Filter filters[] = new Filter[tags.length];

		int c = 0;
		for (Tag tag : tags)
		{
			filters[c++] = new Filter("tag:" + tag.getKey()).withValues(tag.getValue());
		}

		return this.getEc2InstancesByFilters(region, filters);
	}



	public List<Instance> getEc2InstancesByFilters(AwsRegionsEnum region, Filter... filters)
	{
		final DescribeInstancesRequest req_di = new DescribeInstancesRequest();
		req_di.withFilters(filters);

		final DescribeInstancesResult res_di = this.awsClientFactory.getAwsEc2Client(region).describeInstances(req_di);

		final List<Instance> result = new ArrayList<Instance>();

		for (Reservation r : res_di.getReservations())
		{
			for (Instance i : r.getInstances())
			{
				result.add(i);
			}
		}

		return result;
	}

}
