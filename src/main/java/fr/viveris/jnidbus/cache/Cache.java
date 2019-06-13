package fr.viveris.jnidbus.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A cache is limited version of a thread-safe map. You can only append data to it and get values.
 * @param <Key> key of the cache
 * @param <Value> value of the cache
 */
public class Cache<Key, Value> {
    private Map<Key, Value> cachedMetadata = new ConcurrentHashMap<>(16,0.9f,1);


    public void addCachedEntity(Key key, Value value){
        this.cachedMetadata.put(key,value);
    }

    public Value getCachedEntity(Key key){
        return this.cachedMetadata.get(key);
    }

    /**
     * Empty the cache
     */
    public void clear(){
        this.cachedMetadata.clear();
    }
}
