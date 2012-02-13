package eu.arkitech.utils.cloud.aws.simpledb;

import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;

public class AwsSdbItem
{
	private final ReplaceableItem item;
	
	
	public AwsSdbItem(String name)
	{
		this.item = new ReplaceableItem(name);
	}
	
	public AwsSdbItem(Enum<?> name)
	{
		this(name.toString());
	}

	public AwsSdbItem attr(String attrName, Object value)
	{
		this.item.getAttributes().add(new ReplaceableAttribute(attrName, String.valueOf(value), true));
		return this;
	}

	public AwsSdbItem attr(Enum<?> attrName, Object value)
	{
		return this.attr(attrName.toString(), String.valueOf(value));
	}


	public ReplaceableItem item()
	{
		return this.item;
	}
}
