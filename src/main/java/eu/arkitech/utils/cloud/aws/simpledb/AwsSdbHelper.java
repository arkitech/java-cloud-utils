package eu.arkitech.utils.cloud.aws.simpledb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.BatchPutAttributesRequest;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesResult;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;

import eu.arkitech.utils.cloud.aws.AwsClientFactory;
import eu.arkitech.utils.runtimeconfig.RuntimeConfig;

public class AwsSdbHelper
{
	private final Logger logger = LoggerFactory.getLogger(AwsSdbHelper.class);

	private final AmazonSimpleDB simpleDbClient;
	
	
	
	public AwsSdbHelper(AwsClientFactory awsClientManager)
	{
		this.simpleDbClient = awsClientManager.getAwsSimpleDbClient(); 
	}
	

	
	

	public String getAttribute(Enum<?> domain, String itemId, String attribute)
	{
		return this.getAttribute(domain.toString(), itemId, attribute);
	}

	public String getAttribute(Enum<?> domain, Enum<?> itemId, Enum<?> attribute)
	{
		return this.getAttribute(domain, itemId.toString(), attribute.toString());
	}

	public String getAttribute(Enum<?> domain, String itemId, Enum<?> attribute)
	{
		return this.getAttribute(domain, itemId, attribute.toString());
	}
	
	public String getAttribute(String domain, String itemId, String attribute)
	{
		logger.debug("getAttribute: domain={},  item={},  attr={}", new Object[] { domain, itemId, attribute });
		
		try
		{
			final GetAttributesResult res_ga = this.simpleDbClient.getAttributes(new GetAttributesRequest(domain + "_" + RuntimeConfig.getEnvironment(), itemId).withConsistentRead(true));
			for(Attribute attr : res_ga.getAttributes())
			{
				if(attribute.equals(attr.getName()))
				{
					return attr.getValue();
				}
			}
		}
		catch(Exception e)
		{
			// ignore -> return null
		}

		return null;
	}
	
	
	
	
	
	public List<Attribute> getAttributes(Enum<?> domain, Enum<?> itemId)
	{
		return this.getAttributes(domain.toString(), itemId.toString());
	}

	
	public List<Attribute> getAttributes(Enum<?> domain, String itemId)
	{
		return this.getAttributes(domain.toString(), itemId);
	}
	
	
	public List<Attribute> getAttributes(String domain, String itemId)
	{
		logger.debug("getAttributes: domain={},  item={}", new Object[] { domain, itemId });

		try
		{
			final GetAttributesResult res_ga = this.simpleDbClient.getAttributes(new GetAttributesRequest(domain + "_" + RuntimeConfig.getEnvironment(), itemId).withConsistentRead(true));
			return res_ga.getAttributes();
		}
		catch(Exception e)
		{
			// ignore -> return null
		}

		return null;
	}


	
	
	
	
	
	public void setAttribute(Enum<?> domain, String itemId, String attribute, Object value)
	{
		this.setAttribute(domain.toString(), itemId, attribute, value);
	}

	public void setAttribute(Enum<?> domain, String itemId, Enum<?> attribute, Object value)
	{
		this.setAttribute(domain.toString(), itemId, attribute.toString(), value);
	}
	
	public void setAttribute(String domain, String itemId, String attribute, Object value)
	{
		logger.debug("setAttribute: domain={},  item={},  attr={},  value={}", new Object[] { domain, itemId, attribute, value });
		
		final List<ReplaceableAttribute> attrs = new ArrayList<ReplaceableAttribute>();
		attrs.add(new ReplaceableAttribute(attribute, String.valueOf(value), true));
		this.simpleDbClient.putAttributes(new PutAttributesRequest(domain + "_" + RuntimeConfig.getEnvironment(), itemId, attrs));
	}
	
	
	
	
	public void setAttributes(Enum<?> domain, AwsSdbItem ... itemsArray)
	{
		this.setAttributes(domain.toString(), itemsArray);
	}
	
	public void setAttributes(String domain, AwsSdbItem ... itemsArray)
	{
		final List<ReplaceableItem> items = new ArrayList<ReplaceableItem>();
		for(AwsSdbItem item : itemsArray)
		{
			items.add(item.item());
		}
		this.simpleDbClient.batchPutAttributes(new BatchPutAttributesRequest(domain + "_" + RuntimeConfig.getEnvironment(), items));
	}
	
	
	
	
	public List<Item> select(Enum<?> domain)
	{
		return this.select(domain, null);
	}
	
	public List<Item> select(Enum<?> domain, String whereExpresion)
	{
		return this.select(domain.toString(), whereExpresion);
	}
	
	public List<Item> select(String domain, String whereExpresion)
	{
		String whereExpr = "";
		if( ! StringUtils.isBlank(whereExpresion))
		{
			whereExpr = " where " + whereExpresion;
		}
		final SelectResult res_s = this.simpleDbClient.select(new SelectRequest("select * from " + (domain + "_" + RuntimeConfig.getEnvironment()) + whereExpr, true));
		return res_s.getItems();
	}

	
	
	
	public void deleteAttribute(String domain, String itemId, String attribute)
	{
		this.simpleDbClient.deleteAttributes(new DeleteAttributesRequest(domain, itemId).withAttributes(new Attribute(attribute, "")));
	}
	
	public void deleteAttribute(Enum<?> domain, String itemId, String attribute)
	{
		this.deleteAttribute(domain.toString(), itemId, attribute);
	}

	public void deleteAttribute(Enum<?> domain, String itemId, Enum<?> attribute)
	{
		this.deleteAttribute(domain, itemId, attribute.toString());
	}

	
	
	// ------------
	
	
	public static Map<String, String> mapItemProperties(Item item)
	{
		if(item == null)
		{
			return null;
		}
			
		final Map<String, String> map = new HashMap<String, String>();
		for(Attribute attr :  item.getAttributes())
		{
			map.put(attr.getName(), attr.getValue());
		}
		
		return map; 
	}
}
