package fr.viveris.jnidbus.cache;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Cache<Key, Value> {
    public Map<Key, Value> cachedMetadata = Collections.synchronizedMap(new HashMap<Key, Value>());

    public void addCachedEntity(Key key, Value value){
        this.cachedMetadata.put(key,value);
    }

    public Value getCachedEntity(Key key){
        return this.cachedMetadata.get(key);
    }
}
