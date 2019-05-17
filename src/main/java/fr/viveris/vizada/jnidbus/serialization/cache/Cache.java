package fr.viveris.vizada.jnidbus.serialization.cache;

import java.util.HashMap;

/**
 * A Cache is a simple container bound to a ClassLoader that stores all the CachedEntities. The CachedEntities are mapped
 * by class name and the Cache is in an append-only mode, as Caches instances are mapped by a weak HashMap
 */
public class Cache {
    public HashMap<String, CachedEntity> cachedEntities = new HashMap<>();

    public void addCachedEntity(String className, CachedEntity cache){
        this.cachedEntities.put(className,cache);
    }

    public CachedEntity getCachedEntity(String className){
        return this.cachedEntities.get(className);
    }
}
