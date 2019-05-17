package fr.viveris.vizada.jnidbus.serialization.signature;

import java.util.Iterator;

/**
 * This class does the actual parsing of the signature string and transform it in proper SignatureElement.
 * The inner workings are simple, we have a position integer that tells us at which position we are, and each call to next()
 * will increment this position according to the type read (1 for primitive types, undetermined for container types).
 *
 * This iterator is immutable and the remove() method will throw if called
 */
public class SignatureIterator  implements Iterator<SignatureElement> {
    /**
     * Dbus signature string
     */
    private String signature;

    /**
     * Current position in the signature
     */
    private int position = 0;

    public SignatureIterator(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean hasNext() {
        return this.position < this.signature.length();
    }

    @Override
    public SignatureElement next() {
        //get current char and increment
        char current = this.signature.charAt(this.position++);

        if(current == SupportedTypes.ARRAY.getValue()){
            //if the current element is an array, get its first signature element, if this element is primitive, it means we have
            //a non-recursive primitive array, which is easy to parse. We don't increment the position as we need the first element to build
            //the signature of recursive arrays
            char firstSignatureElement = this.signature.charAt(this.position);
            if(firstSignatureElement == SupportedTypes.OBJECT_BEGIN.getValue() || firstSignatureElement == SupportedTypes.ARRAY.getValue()){
                return new SignatureElement(this.generateArraySignature(),null,SupportedTypes.ARRAY);
            }else{
                //go to the next element and return the primitive array signature element
                this.position++;
                return new SignatureElement(String.valueOf(firstSignatureElement),SupportedTypes.forChar(firstSignatureElement),SupportedTypes.ARRAY);
            }
        }else if(current == SupportedTypes.OBJECT_BEGIN.getValue()){
            return new SignatureElement(this.generateStructSignature(),null,SupportedTypes.OBJECT_BEGIN);
        }else{
            return new SignatureElement(String.valueOf(current),SupportedTypes.forChar(current),null);
        }
    }

    public void reset(){
        this.position = 0;
    }

    /**
     * Generate an array signature containing the signature of its content. This method supports nested arrays
     * (ex: aai)
     *
     * @return String of the array signature
     */
    private String generateArraySignature(){
        StringBuilder builder = new StringBuilder();
        //do...while in order to support nested arrays
        do{
            builder.append(this.signature.charAt(this.position));
        }while(this.signature.charAt(this.position++) == SupportedTypes.ARRAY.getValue());

        //check at position -1 as the do-while already increased the counter
        if(this.signature.charAt(this.position-1) == SupportedTypes.OBJECT_BEGIN.getValue()){
            //add struct content
            builder.append(this.generateStructSignature());
            //generateStructSignature skip ")", we add it manually
            builder.append(this.signature.charAt(this.position-1));
        }

        return builder.toString();
    }

    /**
     * Generate an object signature. This method supports nested object and will ignore the object delimiter signature
     * ex: (si) will become si
     *
     * @return
     */
    private String generateStructSignature(){
        StringBuilder builder = new StringBuilder();
        int depth = 1;
        while(depth > 0){
            char current = this.signature.charAt(this.position++);
            if(current == SupportedTypes.OBJECT_END.getValue()) depth--;
            //allow us to ignore OBJECT_END token if it's the end of the parsing
            if(depth > 0){
                builder.append(current);
            }
        }
        return builder.toString();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("A signature iterator is immutable");
    }
}
