package fr.viveris.vizada.jnidbus.serialization.signature;

import java.util.Iterator;

/**
 * The Signature object allows the developer to parse and explore DBus signature in a proper iterator-oriented way.
 */
public class Signature implements Iterable<SignatureElement>{
    private String signature;

    public Signature(String signature) {
        this.signature = signature;
    }

    @Override
    public Iterator<SignatureElement> iterator() {
        return new SignatureIterator(this.signature);
    }

    public String getSignature() {
        return signature;
    }

    /**
     * Method creating an iterator and returning the first element , this is useful when dealing with arrays as they only contain
     * one signature element (the array type)
     * @return
     */
    public SignatureElement getFirst(){
        return new SignatureIterator(this.signature).next();
    }
}
