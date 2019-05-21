package fr.viveris.jnidbus.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A cache is limited version of a synchronized map. You can only append data to it and get values.
 * @param <Key>
 * @param <Value>
 */
public class Cache<Key, Value> {
    public Map<Key, Value> cachedMetadata = Collections.synchronizedMap(new HashMap<Key, Value>());

    public void addCachedEntity(Key key, Value value){
        this.cachedMetadata.put(key,value);
    }

    public Value getCachedEntity(Key key){
        return this.cachedMetadata.get(key);
    }
}
