package eu.arkitech.utils.misc;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class ToStringUtils
{
	public static String toString(Map<?, ?> m)
	{
		StringBuilder sb = new StringBuilder("[");
        String sep = "";
        for (Entry<?, ?> entry : m.entrySet()) {
            sb.append(sep)
              .append(entry.getKey().toString())
              .append("=")
              .append(entry.getValue().toString());
            sep = ", ";
        }
        return sb.append("]").toString();
	}
	
	
	
	
	
	public static String toString(Collection<?> collection)
	{
		if(collection == null)
		{
			return "null";
		}
		
		return Arrays.toString(collection.toArray());
	}
}
