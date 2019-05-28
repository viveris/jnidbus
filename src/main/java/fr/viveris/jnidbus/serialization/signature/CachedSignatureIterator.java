package fr.viveris.jnidbus.serialization.signature;

import java.util.Iterator;
import java.util.LinkedList;

public class CachedSignatureIterator implements Iterable<SignatureElement> {
    private LinkedList<SignatureElement> cached = new LinkedList<>();

    public CachedSignatureIterator(String signature){
        SignatureIterator iter = new SignatureIterator(signature);
        while(iter.hasNext()){
            this.cached.add(iter.next());
        }
    }

    @Override
    public Iterator<SignatureElement> iterator() {
        return this.cached.iterator();
    }
}
