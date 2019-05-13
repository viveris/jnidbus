package fr.viveris.vizada.jnidbus.serialization.signature;

public class SignatureElement {
    private String signature;
    private SupportedTypes containerType;
    private SupportedTypes primitiveType;

    public SignatureElement(String signature, SupportedTypes primitiveType, SupportedTypes containerType) {
        this.signature = signature;
        this.primitiveType = primitiveType;
        this.containerType = containerType;
    }

    public boolean isArray(){
        return this.containerType == SupportedTypes.ARRAY;
    }

    public boolean isPrimitive(){
        return this.primitiveType != null && !this.isArray();
    }

    public SupportedTypes getPrimitive(){
        return this.primitiveType;
    }

    public boolean isObject(){
        return this.containerType == SupportedTypes.OBJECT_BEGIN;
    }

    public Signature getSignature(){
        return new Signature(this.signature);
    }

    public String getSignatureString(){
        return this.signature;
    }

    public SupportedTypes getContainerType() { return containerType; }

    @Override
    public String toString() {
        return "SignatureElement{" +
                "signature='" + signature + '\'' +
                ", containerType=" + containerType +
                ", primitiveType=" + primitiveType +
                '}';
    }
}
