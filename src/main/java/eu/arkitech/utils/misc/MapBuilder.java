package eu.arkitech.utils.misc;

import java.util.HashMap;
import java.util.Map;


/**
 * Utility class to chain-build a HashMap.
 * 
 * Example:
 * Map<String, Integer> m = MapBuilder<String, Integer>("key1", 1).put("key2", 2).asHashMap();
 * 
 *
 * @param <K>
 * @param <V>
 * 
 * @author rcugut
 */
public class MapBuilder<K, V>
{
	private Map<K, V> map = new HashMap<K, V>();
	
	public MapBuilder()
	{
	}
	
	public MapBuilder(K key, V value)
	{
		this.put(key, value);
	}

	public MapBuilder<K, V> put(K key, V value)
	{
		this.map.put(key, value);
		return this;
	}
	
	
	public Map<K, V> asHashMap()
	{
		return this.map;
	}
}
