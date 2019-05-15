package fr.viveris.vizada.jnidbus.serialization.cache;

import java.util.HashMap;

public class Cache {
    public HashMap<String, CachedEntity> cachedEntities = new HashMap<>();

    public void addCachedEntity(String className, CachedEntity cache){
        this.cachedEntities.put(className,cache);
    }

    public CachedEntity getCachedEntity(String className){
        return this.cachedEntities.get(className);
    }
}
