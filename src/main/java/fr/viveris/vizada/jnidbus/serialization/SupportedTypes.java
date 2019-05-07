package fr.viveris.vizada.jnidbus.serialization;

public enum SupportedTypes {
    STRING('s'),
    INTEGER('i');

    char value;
    SupportedTypes(char s){
        this.value = s;
    }

    public char getValue(){
        return this.value;
    }
}
