package fr.viveris.vizada.jnidbus.serialization;

public class DBusObject {

    private String signature;
    private Object[] values;

    public DBusObject(String signature, Object[] values) {
        this.signature = signature;
        this.values = values;
    }

    /**
     * Get the DBus-like type string.
     *
     * ex: ssi => String, String, Integer
     *
     * @return
     */
    public String getSignature(){
        return this.signature;
    }

    /**
     * Object array containing the values in the same order as given by the getSignature() method
     *
     * ex: if the type is "ssi", the output object will be [String,String,Integer]
     *
     * @return
     */
    public Object[] getValues(){
        return this.values;
    }
}
