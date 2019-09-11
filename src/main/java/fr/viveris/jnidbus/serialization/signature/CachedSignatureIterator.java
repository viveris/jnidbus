/* Copyright 2019, Viveris Technologies <opensource@toulouse.viveris.fr>
 * Distributed under the terms of the Academic Free License.
 */
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
