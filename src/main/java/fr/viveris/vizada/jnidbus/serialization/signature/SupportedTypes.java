package fr.viveris.vizada.jnidbus.serialization.signature;

/**
 * List of the types supported by the library, primitive or container
 */
public enum SupportedTypes {
    STRING('s'),
    INTEGER('i'),
    ARRAY('a'),
    OBJECT_BEGIN('('),
    OBJECT_END(')');

    char value;
    SupportedTypes(char s){
        this.value = s;
    }

    public char getValue(){
        return this.value;
    }

    /**
     * Get the enum entry for the given char
     *
     * @param c char to match
     * @return the enum entry for the given char
     */
    public static SupportedTypes forChar(char c){
        switch(c){
            case 's': return STRING;
            case 'i': return INTEGER;
            case 'a': return ARRAY;
            case '(': return OBJECT_BEGIN;
            case ')': return OBJECT_END;
            default: throw new IllegalArgumentException("No supported type for the given char");
        }
    }
}
