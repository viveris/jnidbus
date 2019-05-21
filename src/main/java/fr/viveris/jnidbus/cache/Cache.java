package fr.viveris.jnidbus.cache;

import java.util.HashMap;


public class Cache<Key, Value> {
    public HashMap<Key, Value> cachedMetadata = new HashMap<>();

    public void addCachedEntity(Key key, Value value){
        this.cachedMetadata.put(key,value);
    }

    public Value getCachedEntity(Key key){
        return this.cachedMetadata.get(key);
    }
}
